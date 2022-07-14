package com.ebicep.warlords.database.repositories.games.pojos;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.leaderboards.LeaderboardManager;
import com.ebicep.warlords.database.repositories.games.GamesCollections;
import com.ebicep.warlords.database.repositories.player.PlayersCollections;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.events.WarlordsGameTriggerWinEvent;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.GameAddon;
import com.ebicep.warlords.game.GameMap;
import com.ebicep.warlords.game.GameMode;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import me.filoghost.holographicdisplays.api.beta.HolographicDisplaysAPI;
import me.filoghost.holographicdisplays.api.beta.hologram.Hologram;
import me.filoghost.holographicdisplays.api.beta.hologram.VisibilitySettings;
import me.filoghost.holographicdisplays.api.beta.hologram.line.ClickableHologramLine;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Field;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

public abstract class DatabaseGameBase {

    @Transient
    public static final Location lastGameStatsLocation = new Location(LeaderboardManager.world, -2532.5, 56, 766.5);
    @Transient
    public static final Location topDamageLocation = new Location(LeaderboardManager.world, -2540.5, 58, 785.5);
    @Transient
    public static final Location topHealingLocation = new Location(LeaderboardManager.world, -2546.5, 58, 785.5);
    @Transient
    public static final Location topAbsorbedLocation = new Location(LeaderboardManager.world, -2552.5, 58, 785.5);
    @Transient
    public static final Location topDHPPerMinuteLocation = new Location(LeaderboardManager.world, -2530.5, 59.5, 781.5);
    @Transient
    public static final Location topDamageOnCarrierLocation = new Location(LeaderboardManager.world, -2572.5, 58, 778.5);
    @Transient
    public static final Location topHealingOnCarrierLocation = new Location(LeaderboardManager.world, -2579.5, 58, 774.5);
    @Transient
    public static final Location gameSwitchLocation = new Location(LeaderboardManager.world, -2543.5, 53.5, 769.5);
    @Transient
    protected static final DateFormat DATE_FORMAT = new SimpleDateFormat("MM/dd/yyyy HH:mm");
    @Transient
    public static List<DatabaseGameBase> previousGames = new ArrayList<>();
    @Id
    protected String id;
    @Field("exact_date")
    protected Date exactDate = new Date();
    protected String date;
    protected GameMap map;
    @Field("game_mode")
    protected GameMode gameMode = GameMode.CAPTURE_THE_FLAG;
    @Field("game_addons")
    protected List<GameAddon> gameAddons = new ArrayList<>();
    protected boolean counted = false;
    @Transient
    protected List<Hologram> holograms = new ArrayList<>();

    public DatabaseGameBase() {
    }

    public DatabaseGameBase(@Nonnull Game game, boolean counted) {
        this.exactDate = new Date();
        this.date = DATE_FORMAT.format(new Date());
        this.map = game.getMap();
        this.gameMode = game.getGameMode();
        this.gameAddons = Arrays.asList(game.getAddons().toArray(new GameAddon[0]));
        this.counted = counted;
    }

    public static void addGame(@Nonnull Game game, @Nullable WarlordsGameTriggerWinEvent gameWinEvent, boolean updatePlayerStats) {
        try {
            float highestDamage = game.warlordsPlayers().max(Comparator.comparing((WarlordsPlayer wp) -> wp.getMinuteStats().total().getDamage())).get().getMinuteStats().total().getDamage();
            float highestHealing = game.warlordsPlayers().max(Comparator.comparing((WarlordsPlayer wp) -> wp.getMinuteStats().total().getHealing())).get().getMinuteStats().total().getHealing();
            //checking for inflated stats
            if (highestDamage > 750000 || highestHealing > 750000) {
                updatePlayerStats = false;
                System.out.println(ChatColor.GREEN + "[Warlords] NOT UPDATING PLAYER STATS - Game exceeds 750k damage / healing");
            }
            //check for private + untracked gamemodes
            if (game.getAddons().contains(GameAddon.PRIVATE_GAME)) {
                switch (game.getGameMode()) {
                    case DUEL:
                    case DEBUG:
                    case SIMULATION_TRIAL:
                        updatePlayerStats = false;
                        break;
                }
            }

            //Any game with these game addons will not record player stats
            for (GameAddon addon : game.getAddons()) {
                if (!updatePlayerStats) {
                    break;
                }
                switch (addon) {
                    case CUSTOM_GAME:
                    case IMPOSTER_MODE:
                    case COOLDOWN_MODE:
                    case TRIPLE_HEALTH:
                    case INTERCHANGE_MODE:
                        System.out.println(ChatColor.GREEN + "[Warlords] NOT UPDATING PLAYER STATS - Some addon detected");
                        updatePlayerStats = false;
                        break;
                }
            }

            if (updatePlayerStats) {
                System.out.println(ChatColor.GREEN + "[Warlords] UPDATING PLAYER STATS " + game.getGameId());

                //if(!game.getAddons().contains(GameAddon.PRIVATE_GAME)) {
                //CHALLENGE ACHIEVEMENTS
//                game.warlordsPlayers().forEachOrdered(warlordsPlayer -> {
//                    DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(warlordsPlayer.getUuid());
//                    databasePlayer.addAchievements(warlordsPlayer.getAchievementsUnlocked());
//                });
                //}
            }

            DatabaseGameBase databaseGame = game.getGameMode().createDatabaseGame.apply(game, gameWinEvent, updatePlayerStats);
            if (databaseGame == null) {
                System.out.println(ChatColor.GREEN + "[Warlords] Cannot add game to database - the collection has not been configured");
                return;
            }

            if (previousGames.size() > 0) {
                previousGames.get(0).deleteHolograms();
                previousGames.remove(0);
            }
            previousGames.add(databaseGame);
            databaseGame.createHolograms();

            if (!game.getAddons().contains(GameAddon.CUSTOM_GAME)) {
                addGameToDatabase(databaseGame);
            }

            //sending message if player information remained the same
            for (WarlordsPlayer value : PlayerFilter.playingGame(game)) {
                if (value.getEntity().hasPermission("warlords.database.messagefeed")) {
                    if (updatePlayerStats) {
                        value.sendMessage(ChatColor.GREEN + "This game was added to the database and player information was updated");
                    } else {
                        value.sendMessage(ChatColor.GREEN + "This game was added to the database but player information remained the same");
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("ERROR TRYING TO ADD GAME");
        }

    }

    public static void addGameToDatabase(DatabaseGameBase databaseGame) {
        if (DatabaseManager.gameService == null) return;
        GamesCollections collection = databaseGame.getGameMode().gamesCollections;
        //game in the database
        if (DatabaseManager.gameService.exists(databaseGame, collection)) {
            //if not counted then update player stats then set counted to true, else do nothing
            if (!databaseGame.isCounted()) {
                databaseGame.updatePlayerStatsFromGame(databaseGame, true);
                databaseGame.setCounted(true);
                DatabaseManager.updateGameAsync(databaseGame, collection);
            }
        } else {
            //game not in database then add game and update player stats if counted
            if (databaseGame.isCounted()) {
                databaseGame.updatePlayerStatsFromGame(databaseGame, true);
            }
            //only add game if comps
            //if (databaseGame.isPrivate) {
            Warlords.newChain()
                    .delay(4, TimeUnit.SECONDS)
                    .async(() -> DatabaseManager.gameService.create(databaseGame, collection))
                    .async(() -> LeaderboardManager.addHologramLeaderboards(UUID.randomUUID().toString(), false))
                    .execute();
            //}
        }
    }

    public static void removeGameFromDatabase(DatabaseGameBase databaseGame) {
        if (DatabaseManager.gameService == null) return;
        GamesCollections collection = databaseGame.getGameMode().gamesCollections;
        //game in the database
        if (DatabaseManager.gameService.exists(databaseGame, collection)) {
            //if counted then remove player stats then set counted to false, else do nothing
            if (databaseGame.isCounted()) {
                databaseGame.updatePlayerStatsFromGame(databaseGame, false);
                databaseGame.setCounted(false);
                DatabaseManager.updateGameAsync(databaseGame, collection);
            }
        }
        //else game not in database then do nothing
    }

    protected static void updatePlayerStatsFromTeam(DatabaseGameBase databaseGame, DatabaseGamePlayerBase gamePlayer, boolean add) {
        if (DatabaseManager.playerService == null) {
            System.out.println("playerService is null - cannot update player stats");
            return;
        }

        DatabasePlayer databasePlayerAllTime = DatabaseManager.playerService.findByUUID(UUID.fromString(gamePlayer.getUuid()));
        DatabasePlayer databasePlayerSeason = DatabaseManager.playerService.findByUUID(UUID.fromString(gamePlayer.getUuid()), PlayersCollections.SEASON_6);
        DatabasePlayer databasePlayerWeekly = DatabaseManager.playerService.findByUUID(UUID.fromString(gamePlayer.getUuid()), PlayersCollections.WEEKLY);
        DatabasePlayer databasePlayerDaily = DatabaseManager.playerService.findByUUID(UUID.fromString(gamePlayer.getUuid()), PlayersCollections.DAILY);

        if (databasePlayerAllTime != null) {
            databasePlayerAllTime.updateStats(databaseGame, gamePlayer, add);
//            databasePlayerAllTime.addAchievements(
//                    Arrays.stream(TieredAchievements.values())
//                            .filter(tieredAchievements -> tieredAchievements.gameMode == null || tieredAchievements.gameMode == databaseGame.getGameMode())
//                            .filter(tieredAchievements -> tieredAchievements.databasePlayerPredicate.test(databasePlayerAllTime))
//                            .map(TieredAchievements.TieredAchievementRecord::new)
//                            .collect(Collectors.toList())
//            );
            DatabaseManager.updatePlayerAsync(databasePlayerAllTime);
        } else System.out.println("WARNING - " + gamePlayer.getName() + " was not found in ALL_TIME");
        if (databasePlayerSeason != null) {
            databasePlayerSeason.updateStats(databaseGame, gamePlayer, add);
            DatabaseManager.updatePlayerAsync(databasePlayerSeason, PlayersCollections.SEASON_6);
        } else System.out.println("WARNING - " + gamePlayer.getName() + " was not found in SEASON");
        if (databasePlayerWeekly != null) {
            databasePlayerWeekly.updateStats(databaseGame, gamePlayer, add);
            DatabaseManager.updatePlayerAsync(databasePlayerWeekly, PlayersCollections.WEEKLY);
        } else System.out.println("WARNING - " + gamePlayer.getName() + " was not found in WEEKLY");
        if (databasePlayerDaily != null) {
            databasePlayerDaily.updateStats(databaseGame, gamePlayer, add);
            DatabaseManager.updatePlayerAsync(databasePlayerDaily, PlayersCollections.DAILY);
        } else System.out.println("WARNING - " + gamePlayer.getName() + " was not found in DAILY");
    }

    private static int getGameBefore(int currentGame) {
        if (currentGame <= 0) {
            return previousGames.size() - 1;
        }
        return currentGame - 1;
    }

    private static int getGameAfter(int currentGame) {
        if (currentGame >= previousGames.size() - 1) {
            return 0;
        }
        return currentGame + 1;
    }

    public static void setGameHologramVisibility(Player player) {
        if (!LeaderboardManager.playerGameHolograms.containsKey(player.getUniqueId()) ||
                LeaderboardManager.playerGameHolograms.get(player.getUniqueId()) == null ||
                LeaderboardManager.playerGameHolograms.get(player.getUniqueId()) < 0
        ) {
            LeaderboardManager.playerGameHolograms.put(player.getUniqueId(), previousGames.size() - 1);
        }
        int selectedGame = LeaderboardManager.playerGameHolograms.get(player.getUniqueId());
        for (int i = 0; i < previousGames.size(); i++) {
            List<Hologram> gameHolograms = previousGames.get(i).getHolograms();
            if (i == selectedGame) {
                gameHolograms.forEach(hologram -> hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE));
            } else {
                gameHolograms.forEach(hologram -> hologram.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.HIDDEN));
            }
        }

        createGameSwitcherHologram(player);
    }

    private static void createGameSwitcherHologram(Player player) {
        HolographicDisplaysAPI.get(Warlords.getInstance()).getHolograms().stream()
                .filter(h -> h.getVisibilitySettings().isVisibleTo(player) && h.getPosition().toLocation().equals(DatabaseGameBase.gameSwitchLocation))
                .forEach(Hologram::delete);

        Hologram gameSwitcher = HolographicDisplaysAPI.get(Warlords.getInstance()).createHologram(DatabaseGameBase.gameSwitchLocation);
        gameSwitcher.getLines().appendText(ChatColor.AQUA.toString() + ChatColor.UNDERLINE + "最近" + ChatColor.GOLD + previousGames.size() + ChatColor.AQUA + "局游戏");
        gameSwitcher.getLines().appendText("");

        int selectedGame = LeaderboardManager.playerGameHolograms.get(player.getUniqueId());
        int gameBefore = getGameBefore(selectedGame);
        int gameAfter = getGameAfter(selectedGame);

        if (previousGames.isEmpty()) {
            return;
        }

        ClickableHologramLine beforeLine;
        ClickableHologramLine afterLine;
        if (gameBefore == previousGames.size() - 1) {
            beforeLine = gameSwitcher.getLines().appendText(ChatColor.GRAY + "上局游戏");
        } else {
            beforeLine = gameSwitcher.getLines().appendText(ChatColor.GRAY.toString() + (gameBefore + 1) + ". " + previousGames.get(gameBefore).getDate());
        }

        if (selectedGame == previousGames.size() - 1) {
            gameSwitcher.getLines().appendText(ChatColor.GREEN + "上局游戏");
        } else {
            gameSwitcher.getLines().appendText(ChatColor.GREEN.toString() + (selectedGame + 1) + ". " + previousGames.get(selectedGame).getDate());
        }

        if (gameAfter == previousGames.size() - 1) {
            afterLine = gameSwitcher.getLines().appendText(ChatColor.GRAY + "上局游戏");
        } else {
            afterLine = gameSwitcher.getLines().appendText(ChatColor.GRAY.toString() + (gameAfter + 1) + ". " + previousGames.get(gameAfter).getDate());
        }

        beforeLine.setClickListener((clicker) -> {
            LeaderboardManager.playerGameHolograms.put(player.getUniqueId(), gameBefore);
            setGameHologramVisibility(player);
        });

        afterLine.setClickListener((clicker) -> {
            LeaderboardManager.playerGameHolograms.put(player.getUniqueId(), gameAfter);
            setGameHologramVisibility(player);
        });

        gameSwitcher.getVisibilitySettings().setGlobalVisibility(VisibilitySettings.Visibility.HIDDEN);
        gameSwitcher.getVisibilitySettings().setIndividualVisibility(player, VisibilitySettings.Visibility.VISIBLE);
    }

    public static List<DatabaseGameBase> getPreviousGames() {
        return previousGames;
    }

    public static Date convertToDateFrom(String objectId) {
        return new Date(convertToTimestampFrom(objectId));
    }

    public static long convertToTimestampFrom(String objectId) {
        return Long.parseLong(objectId.substring(0, 8), 16) * 1000;
    }

    public abstract void updatePlayerStatsFromGame(DatabaseGameBase databaseGame, boolean add);

    public abstract DatabaseGamePlayerResult getPlayerGameResult(DatabaseGamePlayerBase player);

    public abstract void createHolograms();

    public abstract String getGameLabel();

    public boolean isPrivate() {
        return gameAddons.contains(GameAddon.PRIVATE_GAME);
    }

    public List<Hologram> getHolograms() {
        if (holograms.isEmpty()) {
            createHolograms();
        }
        return holograms;
    }

    public void deleteHolograms() {
        holograms.forEach(Hologram::delete);
    }

    public String getId() {
        return id;
    }

    public Date getExactDate() {
        return exactDate;
    }

    public void setExactDate(Date exactDate) {
        this.exactDate = exactDate;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public GameMap getMap() {
        return map;
    }

    public void setMap(GameMap map) {
        this.map = map;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public List<GameAddon> getGameAddons() {
        return gameAddons;
    }

    public void setGameAddons(List<GameAddon> gameAddons) {
        this.gameAddons = gameAddons;
    }

    public boolean isCounted() {
        return counted;
    }

    public void setCounted(boolean counted) {
        this.counted = counted;
    }
}
