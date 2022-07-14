package com.ebicep.warlords.party;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.poll.polls.PartyPoll;
import com.ebicep.warlords.util.chat.ChatUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;
import java.util.stream.Collectors;

public class Party {

    private final List<PartyPlayer> partyPlayers = new ArrayList<>();
    private final List<PartyPoll> polls = new ArrayList<>();
    private final HashMap<UUID, Integer> invites = new HashMap<>();
    private final BukkitTask partyTask;
    private final RegularGamesMenu regularGamesMenu = new RegularGamesMenu(this);
    private boolean open = false;
    private boolean allInvite = false;

    public Party(UUID leader, boolean open) {
        partyPlayers.add(new PartyPlayer(leader, PartyPlayerType.LEADER));
        this.open = open;
        partyTask = new BukkitRunnable() {

            @Override
            public void run() {
                invites.forEach((uuid, integer) -> invites.put(uuid, integer - 1));
                invites.entrySet().removeIf(invite -> {
                    if (invite.getValue() <= 0) {
                        sendMessageToAllPartyPlayers(
                                ChatColor.RED + "队伍邀请" + ChatColor.AQUA + Bukkit.getOfflinePlayer(invite.getKey()).getName() + ChatColor.RED + "已超时!",
                                ChatColor.BLUE, true);
                    }
                    return invite.getValue() <= 0;
                });
                for (int i = 0; i < partyPlayers.size(); i++) {
                    PartyPlayer partyPlayer = partyPlayers.get(i);
                    if (partyPlayer != null && partyPlayer.getOfflineTimeLeft() != -1) {
                        int offlineTimeLeft = partyPlayer.getOfflineTimeLeft();
                        partyPlayer.setOfflineTimeLeft(offlineTimeLeft - 1);
                        if (offlineTimeLeft == 0) {
                            leave(partyPlayer.getUuid());
                            i--;
                        } else {
                            if (offlineTimeLeft % 60 == 0) {
                                sendMessageToAllPartyPlayers(
                                        ChatColor.AQUA + Bukkit.getOfflinePlayer(partyPlayer.getUuid()).getName() + ChatColor.YELLOW + "有" + ChatColor.RED + (offlineTimeLeft / 60) + ChatColor.YELLOW + "分钟的时间重新加入，否则将被移除!",
                                        ChatColor.BLUE, true);
                            }
                        }
                    }
                }
            }
        }.runTaskTimer(Warlords.getInstance(), 0, 20);
    }

    public void invite(String name) {
        Player player = Bukkit.getPlayer(name);
        invites.put(player.getUniqueId(), 60);
    }

    public void join(UUID uuid) {
        invites.remove(uuid);
        partyPlayers.add(new PartyPlayer(uuid, PartyPlayerType.MEMBER));
        Player player = Bukkit.getPlayer(uuid);
        sendMessageToAllPartyPlayers(ChatColor.AQUA + player.getName() + ChatColor.GREEN + "已加入队伍", ChatColor.BLUE, true);
        if (player.hasPermission("warlords.party.automoderator")) {
            promote(Bukkit.getOfflinePlayer(uuid).getName());
        }
        Bukkit.getPlayer(uuid).sendMessage(getPartyList());
    }

    public void leave(UUID uuid) {
        OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
        PartyPlayer partyPlayer = getPartyPlayerByUUID(uuid);
        if (partyPlayer == null) return;

        partyPlayers.remove(partyPlayer);
        //if leader leaves
        if (partyPlayer.getPartyPlayerType() == PartyPlayerType.LEADER) {
            //disband party if no other members
            if (partyPlayers.isEmpty()) {
                if (partyPlayer.isOnline()) {
                    ChatUtils.sendMessageToPlayer(player.getPlayer(), ChatColor.RED + "队伍已解散", ChatColor.BLUE, true);
                }
                disband();
            } else {
                //promote if moderators or else promote first person that joined
                PartyPlayer playerToPromote = partyPlayers.stream()
                        .filter(p -> p.getPartyPlayerType() == PartyPlayerType.MODERATOR)
                        .findFirst()
                        .orElse(partyPlayers.get(0));
                playerToPromote.setPartyPlayerType(PartyPlayerType.LEADER);

                sendMessageToAllPartyPlayers(ChatColor.AQUA + player.getName() + ChatColor.RED + "退出了组队", ChatColor.BLUE, true);
                sendMessageToAllPartyPlayers(ChatColor.AQUA + Bukkit.getOfflinePlayer(playerToPromote.getUuid()).getName() + ChatColor.GREEN + "现在是新的队长", ChatColor.BLUE, true);
            }
        } else {
            sendMessageToAllPartyPlayers(ChatColor.AQUA + player.getName() + ChatColor.RED + "退出了组队", ChatColor.BLUE, true);
        }
    }

    public void transfer(String name) {
        partyPlayers.stream()
                .filter(partyPlayer -> Bukkit.getOfflinePlayer(partyPlayer.getUuid()).getName().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(partyPlayer -> {
                    getPartyLeader().setPartyPlayerType(PartyPlayerType.MODERATOR);
                    partyPlayer.setPartyPlayerType(PartyPlayerType.LEADER);
                    String newLeaderName = Bukkit.getOfflinePlayer(partyPlayer.getUuid()).getName();
                    if (newLeaderName.equalsIgnoreCase("MC_tianci") || newLeaderName.equalsIgnoreCase("GuDong_z") || newLeaderName.equalsIgnoreCase("_SilkSong")) {
                        sendMessageToAllPartyPlayers(ChatColor.AQUA + newLeaderName + ChatColor.GREEN + "已劫持该队伍!", ChatColor.BLUE, true);
                    } else {
                        sendMessageToAllPartyPlayers(ChatColor.GREEN + "改队伍已转移至" + ChatColor.AQUA + newLeaderName, ChatColor.BLUE, true);
                    }
                });
    }

    public void remove(String name) {
        partyPlayers.stream()
                .filter(partyPlayer -> Bukkit.getOfflinePlayer(partyPlayer.getUuid()).getName().equalsIgnoreCase(name))
                .findFirst()
                .ifPresent(partyPlayer -> {
                    partyPlayers.remove(partyPlayer);
                    OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(partyPlayer.getUuid());
                    sendMessageToAllPartyPlayers(ChatColor.AQUA + offlinePlayer.getName() + ChatColor.RED + "已被移出队伍", ChatColor.BLUE, true);
                    if (offlinePlayer.isOnline()) {
                        ChatUtils.sendMessageToPlayer(offlinePlayer.getPlayer(), ChatColor.RED + "你已被移出队伍", ChatColor.BLUE, true);
                    }
                });
    }

    public void disband() {
        Warlords.partyManager.disbandParty(this);
        sendMessageToAllPartyPlayers(ChatColor.DARK_RED + "该队伍已解散", ChatColor.BLUE, true);
        partyTask.cancel();
    }

    public String getPartyList() {
        PartyPlayer leader = getPartyLeader();
        StringBuilder stringBuilder = new StringBuilder(ChatColor.BLUE + "-----------------------------\n").append(ChatColor.GOLD).append("队伍 成员 (").append(partyPlayers.size())
                .append(")\n \n").append(ChatColor.YELLOW).append("队长: ")
                .append(ChatColor.AQUA).append(Bukkit.getOfflinePlayer(leader.getUuid()).getName()).append(leader.getPartyListDot()).append("\n");

        List<PartyPlayer> moderators = getPartyModerators();
        if (!moderators.isEmpty()) {
            stringBuilder.append(ChatColor.YELLOW).append("组队 管理员: ").append(ChatColor.AQUA);
            moderators.forEach(partyPlayer -> stringBuilder
                    .append(ChatColor.AQUA)
                    .append(Bukkit.getOfflinePlayer(partyPlayer.getUuid()).getName())
                    .append(partyPlayer.getPartyListDot())
            );
            stringBuilder.append("\n");
        }

        List<PartyPlayer> members = getPartyMembers();
        if (!members.isEmpty()) {
            stringBuilder.append(ChatColor.YELLOW).append("组队 成员: ").append(ChatColor.AQUA);
            members.forEach(partyPlayer -> stringBuilder
                    .append(ChatColor.AQUA)
                    .append(Bukkit.getOfflinePlayer(partyPlayer.getUuid()).getName())
                    .append(partyPlayer.getPartyListDot())
            );
        }
        stringBuilder.append(ChatColor.BLUE).append("\n-----------------------------");
        return stringBuilder.toString();
    }

    public void afk(UUID uuid) {
        partyPlayers.stream()
                .filter(partyPlayer -> partyPlayer.getUuid().equals(uuid))
                .findFirst()
                .ifPresent(partyPlayer -> {
                    partyPlayer.setAFK(!partyPlayer.isAFK());
                    if (partyPlayer.isAFK()) {
                        sendMessageToAllPartyPlayers(ChatColor.AQUA + Bukkit.getOfflinePlayer(uuid).getName() + ChatColor.RED + " is now AFK", ChatColor.BLUE, true);
                    } else {
                        sendMessageToAllPartyPlayers(ChatColor.AQUA + Bukkit.getOfflinePlayer(uuid).getName() + ChatColor.GREEN + " is no longer AFK", ChatColor.BLUE, true);
                    }
                });
    }

    public void promote(String name) {
        UUID uuid = Bukkit.getOfflinePlayer(name).getUniqueId();
        if (getPartyModerators().stream().anyMatch(partyPlayer -> partyPlayer.getUuid().equals(uuid))) {
            transfer(name);
        } else {
            partyPlayers.stream()
                    .filter(partyPlayer -> partyPlayer.getUuid().equals(uuid))
                    .findFirst()
                    .ifPresent(partyPlayer -> partyPlayer.setPartyPlayerType(PartyPlayerType.MODERATOR));
            sendMessageToAllPartyPlayers(ChatColor.AQUA + Bukkit.getOfflinePlayer(uuid).getName() + ChatColor.YELLOW + " was promoted to Party Moderator", ChatColor.BLUE, true);
        }
    }

    public void demote(String name) {
        partyPlayers.stream()
                .filter(partyPlayer -> Bukkit.getOfflinePlayer(name).getUniqueId().equals(partyPlayer.getUuid()))
                .findFirst()
                .ifPresent(partyPlayer -> {
                    if (partyPlayer.getPartyPlayerType() == PartyPlayerType.MODERATOR) {
                        partyPlayer.setPartyPlayerType(PartyPlayerType.MEMBER);
                        sendMessageToAllPartyPlayers(ChatColor.AQUA + Bukkit.getOfflinePlayer(name).getName() + ChatColor.YELLOW + " was demoted to Party Member", ChatColor.BLUE, true);
                    }
                });
    }

    public boolean isOpen() {
        return open;
    }

    public void setOpen(boolean open) {
        this.open = open;
        if (open) {
            sendMessageToAllPartyPlayers(ChatColor.GREEN + "The party is now open", ChatColor.BLUE, true);
        } else {
            sendMessageToAllPartyPlayers(ChatColor.RED + "The party is now closed", ChatColor.BLUE, true);
        }
    }

    public boolean isAllInvite() {
        return allInvite;
    }

    public void setAllInvite(boolean allInvite) {
        this.allInvite = allInvite;
    }

    public PartyPlayer getPartyLeader() {
        return partyPlayers.stream().filter(partyPlayer -> partyPlayer.getPartyPlayerType() == PartyPlayerType.LEADER).findFirst().get();
    }

    public String getLeaderName() {
        return Bukkit.getOfflinePlayer(getPartyLeader().getUuid()).getName();
    }

    public List<PartyPlayer> getPartyModerators() {
        return partyPlayers.stream()
                .filter(partyPlayer -> partyPlayer.getPartyPlayerType() == PartyPlayerType.MODERATOR)
                .sorted(Comparator.comparing(PartyPlayer::isOffline)
                        .thenComparing(PartyPlayer::isAFK))
                .collect(Collectors.toList());
    }

    public List<PartyPlayer> getPartyMembers() {
        return partyPlayers.stream()
                .filter(partyPlayer -> partyPlayer.getPartyPlayerType() == PartyPlayerType.MEMBER)
                .sorted(Comparator.comparing(PartyPlayer::isOffline)
                        .thenComparing(PartyPlayer::isAFK))
                .collect(Collectors.toList());
    }

    public PartyPlayer getPartyPlayerByUUID(UUID uuid) {
        return partyPlayers.stream().filter(partyPlayer -> partyPlayer.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public void sendMessageToAllPartyPlayers(String message, ChatColor borderColor, boolean centered) {
        getAllPartyPeoplePlayerOnline().forEach(partyMember -> {
            ChatUtils.sendMessageToPlayer(partyMember, message, borderColor, centered);
        });
    }

    public List<Player> getAllPartyPeoplePlayerOnline() {
        return Bukkit.getOnlinePlayers().stream()
                .filter(player -> getPartyPlayers().stream().anyMatch(partyPlayer -> partyPlayer.getUuid().equals(player.getUniqueId())))
                .collect(Collectors.toList());
    }

    public List<PartyPlayer> getPartyPlayers() {
        return partyPlayers;
    }

    public boolean allOnlineAndNoAFKs() {
        return partyPlayers.stream().noneMatch(partyPlayer -> !partyPlayer.isOnline() || partyPlayer.isAFK());
    }

    public boolean hasUUID(UUID uuid) {
        return partyPlayers.stream().anyMatch(partyPlayer -> partyPlayer.getUuid().equals(uuid));
    }

    public void addPoll(PartyPoll poll) {
        polls.add(poll);
    }

    public List<PartyPoll> getPolls() {
        return polls;
    }

    public HashMap<UUID, Integer> getInvites() {
        return invites;
    }

    public RegularGamesMenu getRegularGamesMenu() {
        return regularGamesMenu;
    }
}
