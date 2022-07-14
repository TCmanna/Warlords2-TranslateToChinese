package com.ebicep.warlords.menu;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.classes.AbstractPlayerClass;
import com.ebicep.warlords.database.DatabaseManager;
import com.ebicep.warlords.database.repositories.player.pojos.general.DatabasePlayer;
import com.ebicep.warlords.game.Game;
import com.ebicep.warlords.game.Team;
import com.ebicep.warlords.game.option.marker.LobbyLocationMarker;
import com.ebicep.warlords.game.option.marker.MapSymmetryMarker;
import com.ebicep.warlords.player.*;
import com.ebicep.warlords.util.bukkit.ItemBuilder;
import com.ebicep.warlords.util.java.NumberFormat;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ebicep.warlords.menu.Menu.ACTION_CLOSE_MENU;
import static com.ebicep.warlords.menu.Menu.ACTION_DO_NOTHING;
import static com.ebicep.warlords.player.ArmorManager.*;
import static com.ebicep.warlords.player.Settings.*;
import static com.ebicep.warlords.player.Specializations.APOTHECARY;
import static java.lang.Math.round;

public class GameMenu {
    private static final ItemStack MENU_BACK_PREGAME = new ItemBuilder(Material.ARROW)
            .name(ChatColor.GREEN + "返回")
            .lore(ChatColor.GRAY + "至游戏主菜单")
            .get();
    private static final ItemStack MENU_SKINS = new ItemBuilder(Material.PAINTING)
            .name(ChatColor.GREEN + "武器皮肤选择器")
            .lore("§7通过更改你的武器\n§7的特效显示来更好\n§7的迎合你的喜好，", "", "§e点击更换武器皮肤!")
            .get();
    private static final ItemStack MENU_ARMOR_SETS = new ItemBuilder(Material.DIAMOND_HELMET)
            .name(ChatColor.AQUA + "护甲套装" + ChatColor.GRAY + "&" + ChatColor.AQUA + "头盔" + ChatColor.GOLD + "(仅供装饰)")
            .lore("§7解锁或装备你最\n§7喜欢的套装或头盔。")
            .get();
    private static final ItemStack MENU_BOOSTS = new ItemBuilder(Material.BOOKSHELF)
            .name(ChatColor.AQUA + "技能加成切换")
            .lore("§7选择你所装备的武器\n§7想要提升哪些技能。", "", "§e点击以更改武器技能加成!")
            .get();
    private static final ItemStack MENU_SETTINGS = new ItemBuilder(Material.NETHER_STAR)
            .name(ChatColor.AQUA + "设置")
            .lore("§7允许你切换不同的设置选项。", "", "§e点击更改你的设置!")
            .get();
    private static final ItemStack MENU_SETTINGS_PARTICLE_QUALITY = new ItemBuilder(Material.NETHER_STAR)
            .name(ChatColor.GREEN + "粒子质量")
            .lore("§7允许你开启/关闭粒子效果\n§7和控制他们的数量。")
            .get();
    private static final ItemStack MENU_ABILITY_DESCRIPTION = new ItemBuilder(Material.BOOK)
            .name(ChatColor.GREEN + "职业信息")
            .lore("§7预览你的能力信息\n和职业统计资料")
            .get();
    private static final ItemStack MENU_ARCADE = new ItemBuilder(Material.GOLD_BLOCK)
            .name(ChatColor.GREEN + "小游戏")
            .lore("§7在这里试试你的运气以及打开武器碎片!\n")
            .get();

    private static final String[] legendaryNames = new String[]{"Warlord", "Vanquisher", "Champion"};
    private static final String[] mythicNames = new String[]{"Mythical", "Ascendant", "Brilliant"};
    private static final Map<WeaponsRarity, List<Weapons>> weaponByRarity = Stream.of(Weapons.values()).collect(Collectors.groupingBy(Weapons::getRarity));
    private static final Random random = new Random();
    private static final Map<UUID, Long> openWeaponCooldown = new HashMap<>();

    public static void openMainMenu(Player player) {
        Specializations selectedSpec = Warlords.getPlayerSettings(player.getUniqueId()).getSelectedSpec();

        Menu menu = new Menu("战争领主商店", 9 * 6);
        Classes[] values = Classes.values();
        for (int i = 0; i < values.length; i++) {
            Classes group = values[i];
            List<String> lore = new ArrayList<>();
            lore.add(group.description);
            lore.add("");
            lore.add(ChatColor.GOLD + "Specializations:");
            for (Specializations subClass : group.subclasses) {
                lore.add((subClass == selectedSpec ? ChatColor.GREEN : ChatColor.GRAY) + subClass.name);
            }
            lore.add("");
            long experience = ExperienceManager.getExperienceForClass(player.getUniqueId(), group);
            int level = (int) ExperienceManager.calculateLevelFromExp(experience);
            lore.add(ExperienceManager.getProgressString(experience, level + 1));
            lore.add("");
            lore.add(ChatColor.YELLOW + "Click here to select a " + group.name + "\n" + ChatColor.YELLOW + "specialization");
            ItemStack item = new ItemBuilder(group.item)
                    .name(ChatColor.GOLD + group.name + ChatColor.DARK_GRAY + " [" + ChatColor.GRAY + "Lv" + ExperienceManager.getLevelString(level) + ChatColor.DARK_GRAY + "]")
                    .lore(lore)
                    .get();
            menu.setItem(
                    9 / 2 - values.length / 2 + i * 2 - 2,
                    1,
                    item,
                    (m, e) -> {
                        openClassMenu(player, group);
                    }
            );
        }
        menu.setItem(1, 3, MENU_SKINS, (m, e) -> openWeaponMenu(player, 1));
        menu.setItem(3, 3, MENU_ARMOR_SETS, (m, e) -> openArmorMenu(player, 1));
        menu.setItem(5, 3, MENU_BOOSTS, (m, e) -> openSkillBoostMenu(player, selectedSpec));
        menu.setItem(7, 3, MENU_SETTINGS, (m, e) -> openSettingsMenu(player));
        menu.setItem(4, 5, Menu.MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.setItem(4, 2, MENU_ABILITY_DESCRIPTION, (m, e) -> openLobbyAbilityMenu(player));
        menu.setItem(4, 4, MENU_ARCADE, (m, e) -> openArcadeMenu(player));
        menu.openForPlayer(player);
    }

    public static void openClassMenu(Player player, Classes selectedGroup) {
        Specializations selectedSpec = Warlords.getPlayerSettings(player.getUniqueId()).getSelectedSpec();
        Menu menu = new Menu(selectedGroup.name, 9 * 4);
        List<Specializations> values = selectedGroup.subclasses;
        for (int i = 0; i < values.size(); i++) {
            Specializations subClass = values.get(i);
            ItemBuilder builder = new ItemBuilder(subClass.specType.itemStack)
                    .name(ChatColor.GREEN + "专精: " + subClass.name + " " + ChatColor.DARK_GRAY + "[" + ChatColor.GRAY + "Lv" + ExperienceManager.getLevelString(ExperienceManager.getLevelForSpec(player.getUniqueId(), subClass)) + ChatColor.DARK_GRAY + "]")
                    .flags(ItemFlag.HIDE_ENCHANTS);
            List<String> lore = new ArrayList<>();
            lore.add(subClass.description);
            lore.add("");
            long experience = ExperienceManager.getExperienceForSpec(player.getUniqueId(), subClass);
            int level = (int) ExperienceManager.calculateLevelFromExp(experience);
            lore.add(ExperienceManager.getProgressString(experience, level + 1));
            lore.add("");
            if (subClass == selectedSpec) {
                lore.add(ChatColor.GREEN + ">>> 激活 <<<");
                builder.enchant(Enchantment.OXYGEN, 1);
            } else {
                lore.add(ChatColor.YELLOW + "> 点击激活 <");
            }
            builder.lore(lore);
            menu.setItem(
                    9 / 2 - values.size() / 2 + i * 2 - 1,
                    1,
                    builder.get(),
                    (m, e) -> {
                        player.sendMessage(ChatColor.WHITE + "职业: §6" + subClass);
                        player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 2);
                        ArmorManager.resetArmor(player, subClass, Warlords.getPlayerSettings(player.getUniqueId()).getWantedTeam());
                        PlayerSettings playerSettings = Warlords.getPlayerSettings(player.getUniqueId());
                        playerSettings.setSelectedSpec(subClass);

                        AbstractPlayerClass apc = subClass.create.get();
                        player.getInventory().setItem(1, new ItemBuilder(apc.getWeapon().getItem(playerSettings.getWeaponSkins()
                                .getOrDefault(subClass, Weapons.FELFLAME_BLADE).item)).name("§aWeapon Skin Preview")
                                .lore("")
                                .get());

                        openClassMenu(player, selectedGroup);

                        if (DatabaseManager.playerService == null) return;
                        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(player.getUniqueId());
                        databasePlayer.setLastSpec(subClass);
                        DatabaseManager.updatePlayerAsync(databasePlayer);
                    }
            );
        }
        menu.setItem(4, 3, MENU_BACK_PREGAME, (m, e) -> openMainMenu(player));

        menu.openForPlayer(player);
    }

    public static void openSkillBoostMenu(Player player, Specializations selectedSpec) {
        SkillBoosts selectedBoost = Warlords.getPlayerSettings(player.getUniqueId()).getSkillBoostForClass();
        Menu menu = new Menu("技能加成", 9 * 6);
        List<SkillBoosts> values = selectedSpec.skillBoosts;
        for (int i = 0; i < values.size(); i++) {
            SkillBoosts skillBoost = values.get(i);
            ItemBuilder builder = new ItemBuilder(selectedSpec.specType.itemStack)
                    .name(skillBoost == selectedBoost ? ChatColor.GREEN + skillBoost.name + " (" + selectedSpec.name + ")" : ChatColor.RED + skillBoost.name + " (" + selectedSpec.name + ")")
                    .flags(ItemFlag.HIDE_ENCHANTS);
            List<String> lore = new ArrayList<>();
            lore.add(skillBoost == selectedBoost ? skillBoost.selectedDescription : skillBoost.description);
            lore.add("");
            if (skillBoost == selectedBoost) {
                lore.add(ChatColor.GREEN + "已选择!");
                builder.enchant(Enchantment.OXYGEN, 1);
            } else {
                lore.add(ChatColor.YELLOW + "点击激活!");
            }
            builder.lore(lore);
            menu.setItem(
                    i + 2,
                    3,
                    builder.get(),
                    (m, e) -> {
                        player.sendMessage(ChatColor.GREEN + "你已修改你的武器加成为: §b" + skillBoost.name + "!");
                        Warlords.getPlayerSettings(player.getUniqueId()).setSkillBoostForSelectedSpec(skillBoost);
                        openSkillBoostMenu(player, selectedSpec);

                        if (DatabaseManager.playerService == null) return;
                        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(player.getUniqueId());
                        databasePlayer.getSpec(selectedSpec).setSkillBoost(skillBoost);
                        DatabaseManager.updatePlayerAsync(databasePlayer);
                    }
            );
        }

        //showing change of ability
        PlayerSettings playerSettings = Warlords.getPlayerSettings(player.getUniqueId());
        AbstractPlayerClass apc = selectedSpec.create.get();
        AbstractPlayerClass apc2 = selectedSpec.create.get();
        if (apc2.getWeapon().getClass() == selectedBoost.ability) {
            apc2.getWeapon().boostSkill(selectedBoost, apc2);
            apc.getWeapon().updateDescription(player);
            apc2.getWeapon().updateDescription(player);
            menu.setItem(3, 1, apc.getWeapon().getItem(playerSettings.getWeaponSkins().getOrDefault(selectedSpec, Weapons.FELFLAME_BLADE).item), ACTION_DO_NOTHING);
            menu.setItem(5, 1, apc2.getWeapon().getItem(playerSettings.getWeaponSkins().getOrDefault(selectedSpec, Weapons.FELFLAME_BLADE).item), ACTION_DO_NOTHING);
        } else if (apc2.getRed().getClass() == selectedBoost.ability) {
            apc2.getRed().boostSkill(selectedBoost, apc2);
            apc.getRed().updateDescription(player);
            apc2.getRed().updateDescription(player);
            menu.setItem(3, 1, apc.getRed().getItem(new ItemStack(Material.INK_SACK, 1, (byte) 1)), ACTION_DO_NOTHING);
            menu.setItem(5, 1, apc2.getRed().getItem(new ItemStack(Material.INK_SACK, 1, (byte) 1)), ACTION_DO_NOTHING);
        } else if (apc2.getPurple().getClass() == selectedBoost.ability) {
            apc2.getPurple().boostSkill(selectedBoost, apc2);
            apc.getPurple().updateDescription(player);
            apc2.getPurple().updateDescription(player);
            menu.setItem(3, 1, apc.getPurple().getItem(new ItemStack(Material.GLOWSTONE_DUST)), ACTION_DO_NOTHING);
            menu.setItem(5, 1, apc2.getPurple().getItem(new ItemStack(Material.GLOWSTONE_DUST)), ACTION_DO_NOTHING);
        } else if (apc2.getBlue().getClass() == selectedBoost.ability) {
            apc2.getBlue().boostSkill(selectedBoost, apc2);
            apc.getBlue().updateDescription(player);
            apc2.getBlue().updateDescription(player);
            menu.setItem(3, 1, apc.getBlue().getItem(new ItemStack(Material.INK_SACK, 1, (byte) 10)), ACTION_DO_NOTHING);
            menu.setItem(5, 1, apc2.getBlue().getItem(new ItemStack(Material.INK_SACK, 1, (byte) 10)), ACTION_DO_NOTHING);
        } else if (apc2.getOrange().getClass() == selectedBoost.ability) {
            apc2.getOrange().boostSkill(selectedBoost, apc2);
            apc.getOrange().updateDescription(player);
            apc2.getOrange().updateDescription(player);
            menu.setItem(3, 1, apc.getOrange().getItem(new ItemStack(Material.INK_SACK, 1, (byte) 14)), ACTION_DO_NOTHING);
            menu.setItem(5, 1, apc2.getOrange().getItem(new ItemStack(Material.INK_SACK, 1, (byte) 14)), ACTION_DO_NOTHING);
        }
        menu.setItem(4, 5, MENU_BACK_PREGAME, (m, e) -> openMainMenu(player));
        menu.openForPlayer(player);
    }

    public static void openWeaponMenu(Player player, int pageNumber) {
        Specializations selectedSpec = Warlords.getPlayerSettings(player.getUniqueId()).getSelectedSpec();
        Weapons selectedWeapon = Weapons.getSelected(player, selectedSpec);
        Menu menu = new Menu("武器皮肤选择", 9 * 6);
        List<Weapons> values = new ArrayList<>(Arrays.asList(Weapons.values()));
        for (int i = (pageNumber - 1) * 21; i < pageNumber * 21 && i < values.size(); i++) {
            Weapons weapon = values.get(i);
            ItemBuilder builder;

            if (weapon.isUnlocked) {

                builder = new ItemBuilder(weapon.item)
                        .name(ChatColor.GREEN + weapon.name)
                        .flags(ItemFlag.HIDE_ENCHANTS);
                List<String> lore = new ArrayList<>();

                if (weapon == selectedWeapon) {
                    lore.add(ChatColor.GREEN + "已选择!");
                    builder.enchant(Enchantment.OXYGEN, 1);
                } else {
                    lore.add(ChatColor.YELLOW + "点击激活!");
                }

                builder.lore(lore);
            } else {
                builder = new ItemBuilder(Material.BARRIER).name(ChatColor.RED + "锁定武器皮肤");
            }

            menu.setItem(
                    (i - (pageNumber - 1) * 21) % 7 + 1,
                    (i - (pageNumber - 1) * 21) / 7 + 1,
                    builder.get(),
                    (m, e) -> {
                        if (weapon.isUnlocked) {
                            player.sendMessage(ChatColor.GREEN + "你已修改你的 " + ChatColor.AQUA + selectedSpec.name + ChatColor.GREEN + "的武器皮肤为: §b" + weapon.name + "!");
                            Weapons.setSelected(player, selectedSpec, weapon);
                            openWeaponMenu(player, pageNumber);
                            PlayerSettings playerSettings = Warlords.getPlayerSettings(player.getUniqueId());
                            AbstractPlayerClass apc = selectedSpec.create.get();
                            player.getInventory().setItem(1, new ItemBuilder(apc.getWeapon().getItem(playerSettings.getWeaponSkins()
                                    .getOrDefault(selectedSpec, Weapons.FELFLAME_BLADE).item)).name("§a武器皮肤预览")
                                    .lore("")
                                    .get());

                            if (DatabaseManager.playerService == null) return;
                            DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(player.getUniqueId());
                            databasePlayer.getSpec(selectedSpec).setWeapon(weapon);
                            DatabaseManager.updatePlayerAsync(databasePlayer);
                        } else {
                            player.sendMessage(ChatColor.RED + "这个武器皮肤还没有被解锁!");
                        }
                    }
            );
        }
        if (pageNumber == 1) {
            menu.setItem(
                    8,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "下一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber + 1) + "页")
                            .get(),
                    (m, e) -> openWeaponMenu(player, pageNumber + 1));
        } else if (pageNumber == 2) {
            menu.setItem(
                    0,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "上一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber - 1) + "页")
                            .get(),
                    (m, e) -> openWeaponMenu(player, pageNumber - 1));
            menu.setItem(
                    8,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "下一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber + 1) + "页")
                            .get(),
                    (m, e) -> openWeaponMenu(player, pageNumber + 1));
        } else if (pageNumber == 3) {
            menu.setItem(
                    0,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "上一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber - 1) + "页")
                            .get(),
                    (m, e) -> openWeaponMenu(player, pageNumber - 1));
            menu.setItem(
                    8,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "下一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber + 1) + "页")
                            .get(),
                    (m, e) -> openWeaponMenu(player, pageNumber + 1));
        } else if (pageNumber == 4) {
            menu.setItem(
                    0,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "上一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber - 1) + "页")
                            .get(),
                    (m, e) -> openWeaponMenu(player, pageNumber - 1));
        }

        menu.setItem(4, 5, MENU_BACK_PREGAME, (m, e) -> openMainMenu(player));
        menu.openForPlayer(player);
    }

    public static void openArmorMenu(Player player, int pageNumber) {
        boolean onBlueTeam = Warlords.getGameManager().getPlayerGame(player.getUniqueId()).map(g -> g.getPlayerTeam(player.getUniqueId())).orElse(Team.BLUE) == Team.BLUE;
        List<Helmets> selectedHelmet = Helmets.getSelected(player);
        List<ArmorSets> selectedArmorSet = ArmorSets.getSelected(player);
        Menu menu = new Menu("盔甲套装与头盔", 9 * 6);
        List<Helmets> helmets = Arrays.asList(Helmets.values());
        for (int i = (pageNumber - 1) * 8; i < pageNumber * 8 && i < helmets.size(); i++) {
            Helmets helmet = helmets.get(i);
            ItemBuilder builder = new ItemBuilder(onBlueTeam ? helmet.itemBlue : helmet.itemRed)
                    .name(onBlueTeam ? ChatColor.BLUE + helmet.name : ChatColor.RED + helmet.name)
                    .flags(ItemFlag.HIDE_ENCHANTS);
            List<String> lore = new ArrayList<>();
            lore.add(helmetDescription);
            lore.add("");
            if (selectedHelmet.contains(helmet)) {
                lore.add(ChatColor.GREEN + ">>> 选择 <<<");
                builder.enchant(Enchantment.OXYGEN, 1);
            } else {
                lore.add(ChatColor.YELLOW + "> 点击激活! <");
            }
            builder.lore(lore);
            menu.setItem(
                    (i - (pageNumber - 1) * 8) + 1,
                    2,
                    builder.get(),
                    (m, e) -> {
                        player.sendMessage(ChatColor.YELLOW + "已选择: " + ChatColor.GREEN + helmet.name);
                        if (
                                helmet == Helmets.SIMPLE_MAGE_HELMET ||
                                        helmet == Helmets.GREATER_MAGE_HELMET ||
                                        helmet == Helmets.MASTERWORK_MAGE_HELMET ||
                                        helmet == Helmets.LEGENDARY_MAGE_HELMET
                        ) {
                            Helmets.setSelectedMage(player, helmet);
                        } else if (
                                helmet == Helmets.SIMPLE_WARRIOR_HELMET ||
                                helmet == Helmets.GREATER_WARRIOR_HELMET ||
                                helmet == Helmets.MASTERWORK_WARRIOR_HELMET ||
                                helmet == Helmets.LEGENDARY_WARRIOR_HELMET
                        ) {
                            Helmets.setSelectedWarrior(player, helmet);
                        } else if (
                                helmet == Helmets.SIMPLE_PALADIN_HELMET ||
                                helmet == Helmets.GREATER_PALADIN_HELMET ||
                                helmet == Helmets.MASTERWORK_PALADIN_HELMET ||
                                helmet == Helmets.LEGENDARY_PALADIN_HELMET
                        ) {
                            Helmets.setSelectedPaladin(player, helmet);
                        } else if (
                                helmet == Helmets.SIMPLE_SHAMAN_HELMET ||
                                helmet == Helmets.GREATER_SHAMAN_HELMET ||
                                helmet == Helmets.MASTERWORK_SHAMAN_HELMET ||
                                helmet == Helmets.LEGENDARY_SHAMAN_HELMET
                        ) {
                            Helmets.setSelectedShaman(player, helmet);
                        } else if (
                                helmet == Helmets.SIMPLE_ROGUE_HELMET ||
                                        helmet == Helmets.GREATER_ROGUE_HELMET ||
                                        helmet == Helmets.MASTERWORK_ROGUE_HELMET ||
                                        helmet == Helmets.LEGENDARY_ROGUE_HELMET
                        ) {
                            Helmets.setSelectedRogue(player, helmet);
                        }
                        ArmorManager.resetArmor(player, Warlords.getPlayerSettings(player.getUniqueId()).getSelectedSpec(), Warlords.getPlayerSettings(player.getUniqueId()).getWantedTeam());

                        openArmorMenu(player, pageNumber);

                        if (DatabaseManager.playerService == null) return;
                        List<Helmets> selectedHelmets = Helmets.getSelected(player);
                        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(player.getUniqueId());
                        databasePlayer.getMage().setHelmet(selectedHelmets.get(0));
                        databasePlayer.getWarrior().setHelmet(selectedHelmets.get(1));
                        databasePlayer.getPaladin().setHelmet(selectedHelmets.get(2));
                        databasePlayer.getShaman().setHelmet(selectedHelmets.get(3));
                        databasePlayer.getRogue().setHelmet(selectedHelmets.get(4));
                        DatabaseManager.updatePlayerAsync(databasePlayer);
                    }
            );
        }
        List<ArmorSets> armorSets = Arrays.asList(ArmorSets.values());
        int xPosition = 1;
        for (int i = (pageNumber - 1) * 6; i < pageNumber * 6; i++) {
            if (pageNumber == 3 && i == 15) {
                break;
            }
            ArmorSets armorSet = armorSets.get(i);
            ItemBuilder builder = new ItemBuilder(i % 3 == 0 ? ArmorSets.applyColor(armorSet.itemBlue, onBlueTeam) : armorSet.itemBlue)
                    .name(onBlueTeam ? ChatColor.BLUE + armorSet.name : ChatColor.RED + armorSet.name)
                    .flags(ItemFlag.HIDE_ENCHANTS);
            List<String> lore = new ArrayList<>();
            lore.add(armorDescription);
            lore.add("");
            if (selectedArmorSet.contains(armorSet)) {
                lore.add(ChatColor.GREEN + ">>> 选择 <<<");
                builder.enchant(Enchantment.OXYGEN, 1);
            } else {
                lore.add(ChatColor.YELLOW + "> 点击激活! <");
            }
            builder.lore(lore);
            menu.setItem(
                    xPosition,
                    3,
                    builder.get(),
                    (m, e) -> {
                        player.sendMessage(ChatColor.YELLOW + "已选择: " + ChatColor.GREEN + armorSet.name);
                        if (armorSet == ArmorSets.SIMPLE_CHESTPLATE_MAGE || armorSet == ArmorSets.GREATER_CHESTPLATE_MAGE || armorSet == ArmorSets.MASTERWORK_CHESTPLATE_MAGE) {
                            ArmorSets.setSelectedMage(player, armorSet);
                        } else if (armorSet == ArmorSets.SIMPLE_CHESTPLATE_WARRIOR || armorSet == ArmorSets.GREATER_CHESTPLATE_WARRIOR || armorSet == ArmorSets.MASTERWORK_CHESTPLATE_WARRIOR) {
                            ArmorSets.setSelectedWarrior(player, armorSet);
                        } else if (armorSet == ArmorSets.SIMPLE_CHESTPLATE_PALADIN || armorSet == ArmorSets.GREATER_CHESTPLATE_PALADIN || armorSet == ArmorSets.MASTERWORK_CHESTPLATE_PALADIN) {
                            ArmorSets.setSelectedPaladin(player, armorSet);
                        } else if (armorSet == ArmorSets.SIMPLE_CHESTPLATE_SHAMAN || armorSet == ArmorSets.GREATER_CHESTPLATE_SHAMAN || armorSet == ArmorSets.MASTERWORK_CHESTPLATE_SHAMAN) {
                            ArmorSets.setSelectedShaman(player, armorSet);
                        } else if (armorSet == ArmorSets.SIMPLE_CHESTPLATE_ROGUE || armorSet == ArmorSets.GREATER_CHESTPLATE_ROGUE || armorSet == ArmorSets.MASTERWORK_CHESTPLATE_ROGUE) {
                            ArmorSets.setSelectedRogue(player, armorSet);
                        }

                        openArmorMenu(player, pageNumber);

                        if (DatabaseManager.playerService == null) return;
                        List<ArmorSets> armorSetsList = ArmorSets.getSelected(player);
                        DatabasePlayer databasePlayer = DatabaseManager.playerService.findByUUID(player.getUniqueId());
                        databasePlayer.getMage().setArmor(armorSetsList.get(0));
                        databasePlayer.getWarrior().setArmor(armorSetsList.get(1));
                        databasePlayer.getPaladin().setArmor(armorSetsList.get(2));
                        databasePlayer.getShaman().setArmor(armorSetsList.get(3));
                        databasePlayer.getRogue().setArmor(armorSetsList.get(4));
                        DatabaseManager.updatePlayerAsync(databasePlayer);
                    }
            );
            if (xPosition == 3) {
                xPosition += 2;
            } else {
                xPosition++;
            }
        }

        if (pageNumber == 1) {
            menu.setItem(
                    8,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "下一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber + 1) + "页")
                            .get(),
                    (m, e) -> openArmorMenu(player, pageNumber + 1));
        } else if (pageNumber == 2) {
            menu.setItem(
                    8,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "下一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber + 1) + "页")
                            .get(),
                    (m, e) -> openArmorMenu(player, pageNumber + 1));
            menu.setItem(
                    0,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "上一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber - 1) + "页")
                            .get(),
                    (m, e) -> openArmorMenu(player, pageNumber - 1));
        } else if (pageNumber == 3) {
            menu.setItem(
                    0,
                    5,
                    new ItemBuilder(Material.ARROW)
                            .name(ChatColor.GREEN + "上一页")
                            .lore(ChatColor.YELLOW + "第" + (pageNumber - 1) + "页")
                            .get(),
                    (m, e) -> openArmorMenu(player, pageNumber - 1));
        }

        menu.setItem(4, 5, MENU_BACK_PREGAME, (m, e) -> openMainMenu(player));
        menu.openForPlayer(player);
    }

    public static void openSettingsMenu(Player player) {
        Powerup selectedPowerup = Powerup.getSelected(player);
        HotkeyMode selectedHotkeyMode = HotkeyMode.getSelected(player);

        Menu menu = new Menu("设置", 9 * 4);
        menu.setItem(
                3,
                1,
                selectedHotkeyMode.item,
                (m, e) -> {
                    player.sendMessage(selectedHotkeyMode == HotkeyMode.NEW_MODE ? ChatColor.GREEN + "Hotkey Mode " + ChatColor.AQUA + "Classic " + ChatColor.GREEN + "enabled." : ChatColor.GREEN + "Hotkey Mode " + ChatColor.YELLOW + "NEW " + ChatColor.GREEN + "enabled.");
                    HotkeyMode.setSelected(player, selectedHotkeyMode == HotkeyMode.NEW_MODE ? HotkeyMode.CLASSIC_MODE : HotkeyMode.NEW_MODE);
                    openSettingsMenu(player);
                }
        );
        menu.setItem(
                1,
                1,
                MENU_SETTINGS_PARTICLE_QUALITY,
                (m, e) -> openParticleQualityMenu(player)
        );

        menu.setItem(4, 3, MENU_BACK_PREGAME, (m, e) -> openMainMenu(player));
        menu.openForPlayer(player);
    }

    public static void openParticleQualityMenu(Player player) {
        ParticleQuality selectedParticleQuality = ParticleQuality.getSelected(player);

        Menu menu = new Menu("粒子质量", 9 * 4);

        ParticleQuality[] particleQualities = ParticleQuality.values();
        for (int i = 0; i < particleQualities.length; i++) {
            ParticleQuality particleQuality = particleQualities[i];

            menu.setItem(
                    i + 3,
                    1,
                    new ItemBuilder(particleQuality.item)
                            .lore(particleQuality.description, "", selectedParticleQuality == particleQuality ? ChatColor.GREEN + "SELECTED" : ChatColor.YELLOW + "Click to select!")
                            .flags(ItemFlag.HIDE_ENCHANTS)
                            .get(),
                    (m, e) -> {
                        Bukkit.getServer().dispatchCommand(player, "pq " + particleQuality.name());
                        openParticleQualityMenu(player);
                    }
            );
        }
        menu.setItem(4, 3, MENU_BACK_PREGAME, (m, e) -> openMainMenu(player));
        menu.openForPlayer(player);
    }

    public static void openTeamMenu(Player player) {
        Team selectedTeam = Warlords.getPlayerSettings(player.getUniqueId()).getWantedTeam();
        Menu menu = new Menu("队伍选择器", 9 * 4);
        List<Team> values = new ArrayList<>(Arrays.asList(Team.values()));
        for (int i = 0; i < values.size(); i++) {
            Team team = values.get(i);
            ItemBuilder builder = new ItemBuilder(team.getItem())
                    .name(team.teamColor() + team.getName())
                    .flags(ItemFlag.HIDE_ENCHANTS);
            List<String> lore = new ArrayList<>();
            if (team == selectedTeam) {
                lore.add(ChatColor.GREEN + "目前已选定!");
                builder.enchant(Enchantment.OXYGEN, 1);
            } else {
                lore.add(ChatColor.YELLOW + "点击激活!");
            }
            builder.lore(lore);
            menu.setItem(
                    9 / 2 - values.size() % 2 + i * 2 - 1,
                    1,
                    builder.get(),
                    (m, e) -> {
                        if (selectedTeam != team) {
                            player.sendMessage(ChatColor.GREEN + "你已加入 " + team.teamColor() + team.getName() + ChatColor.GREEN + " 队!");
                            Optional<Game> playerGame = Warlords.getGameManager().getPlayerGame(player.getUniqueId());
                            if (playerGame.isPresent()) {
                                Game game = playerGame.get();
                                Team oldTeam = game.getPlayerTeam(player.getUniqueId());
                                game.setPlayerTeam(player, team);
                                LobbyLocationMarker randomLobbyLocation = LobbyLocationMarker.getRandomLobbyLocation(game, team);
                                if (randomLobbyLocation != null) {
                                    Location teleportDestination = MapSymmetryMarker.getSymmetry(game)
                                            .getOppositeLocation(game, oldTeam, team, player.getLocation(), randomLobbyLocation.getLocation());
                                    player.teleport(teleportDestination);
                                    Warlords.setRejoinPoint(player.getUniqueId(), teleportDestination);
                                }
                            }
                            ArmorManager.resetArmor(player, Warlords.getPlayerSettings(player.getUniqueId()).getSelectedSpec(), team);
                            Warlords.getPlayerSettings(player.getUniqueId()).setWantedTeam(team);
                        }
                        openTeamMenu(player);
                    }
            );
        }

        menu.setItem(4, 3, Menu.MENU_CLOSE, ACTION_CLOSE_MENU);
        menu.openForPlayer(player);
    }

    public static void openLobbyAbilityMenu(Player player) {
        Menu menu = new Menu("职业信息", 9);
        PlayerSettings playerSettings = Warlords.getPlayerSettings(player.getUniqueId());
        Specializations selectedSpec = playerSettings.getSelectedSpec();
        AbstractPlayerClass apc = selectedSpec.create.get();

        ItemBuilder icon = new ItemBuilder(selectedSpec.specType.itemStack);
        icon.name(ChatColor.GREEN + selectedSpec.name);
        icon.lore(
                selectedSpec.description,
                "",
                "§6Specialization Stats:",
                "",
                "§7Health: §a" + apc.getMaxHealth(),
                "§7Energy: §a" + apc.getMaxEnergy() + " §7/ §a+" + apc.getEnergyPerSec() + " §7per sec §7/ §a+" + apc.getEnergyOnHit() + " §7per hit",
                "",
                selectedSpec == APOTHECARY ? "§7Speed: §e10%" : null,
                apc.getDamageResistance() == 0 ? "§7Damage Reduction: §cNone" : "§7Damage Reduction: §e" + apc.getDamageResistance() + "%"
        );

        SkillBoosts selectedBoost = playerSettings.getSkillBoostForClass();
        if (selectedBoost != null) {
            if (apc.getWeapon().getClass() == selectedBoost.ability) {
                apc.getWeapon().boostSkill(selectedBoost, apc);
            } else if (apc.getRed().getClass() == selectedBoost.ability) {
                apc.getRed().boostSkill(selectedBoost, apc);
            } else if (apc.getPurple().getClass() == selectedBoost.ability) {
                apc.getPurple().boostSkill(selectedBoost, apc);
            } else if (apc.getBlue().getClass() == selectedBoost.ability) {
                apc.getBlue().boostSkill(selectedBoost, apc);
            } else if (apc.getOrange().getClass() == selectedBoost.ability) {
                apc.getOrange().boostSkill(selectedBoost, apc);
            }
        }

        apc.getWeapon().updateDescription(player);
        apc.getRed().updateDescription(player);
        apc.getPurple().updateDescription(player);
        apc.getBlue().updateDescription(player);
        apc.getOrange().updateDescription(player);

        menu.setItem(0, icon.get(), ACTION_DO_NOTHING);
        menu.setItem(2, apc.getWeapon().getItem(playerSettings.getWeaponSkins().getOrDefault(selectedSpec, Weapons.FELFLAME_BLADE).item), ACTION_DO_NOTHING);
        menu.setItem(3, apc.getRed().getItem(new ItemStack(Material.INK_SACK, 1, (byte) 1)), ACTION_DO_NOTHING);
        menu.setItem(4, apc.getPurple().getItem(new ItemStack(Material.GLOWSTONE_DUST)), ACTION_DO_NOTHING);
        menu.setItem(5, apc.getBlue().getItem(new ItemStack(Material.INK_SACK, 1, (byte) 10)), ACTION_DO_NOTHING);
        menu.setItem(6, apc.getOrange().getItem(new ItemStack(Material.INK_SACK, 1, (byte) 14)), ACTION_DO_NOTHING);
        menu.setItem(8, MENU_BACK_PREGAME, (m, e) -> openMainMenu(player));

        menu.openForPlayer(player);
    }

    private static double map(double value, double min, double max) {
        return value * (max - min) + min;
    }

    public static void openArcadeMenu(Player player) {
        Menu menu = new Menu("小游戏", 9 * 4);

        ItemBuilder icon = new ItemBuilder(Material.GOLD_INGOT);
        icon.name(ChatColor.GREEN + "武器抽奖");
        icon.lore(
                "§7Is RNG with you today?"
        );

        menu.setItem(3, 1, icon.get(), (m, e) -> {
            double difficulty = 1;
            double base = random.nextDouble() * (1 - difficulty);

            double meleeDamageMin = random.nextDouble() * difficulty + base;
            double meleeDamageMax = random.nextDouble() * difficulty + base;
            double critChance = random.nextDouble() * difficulty + base;
            double critMultiplier = random.nextDouble() * difficulty + base;
            double skillBoost = random.nextDouble() * difficulty + base;
            double health = random.nextDouble() * difficulty + base;
            double energy = random.nextDouble() * difficulty + base;
            double cooldown = random.nextDouble() * difficulty + base;
            double speed = random.nextDouble() * difficulty + base;

            double score =
                    (
                        meleeDamageMin +
                        meleeDamageMax +
                        critChance +
                        critMultiplier +
                        skillBoost +
                        health +
                        energy +
                        cooldown +
                        speed
                    ) / 9;

            meleeDamageMin = map(meleeDamageMin, 122, 132);
            meleeDamageMax = map(meleeDamageMax, 166, 179);
            critChance = map(critChance, 15, 25);
            critMultiplier = map(critMultiplier, 180, 200);
            skillBoost = map(skillBoost, 13, 20);
            health = map(health, 500, 800);
            energy = map(energy, 30, 35);
            cooldown = map(cooldown, 7, 13);
            speed = map(speed, 7, 13);

            if (meleeDamageMin > meleeDamageMax) {
                double temp = meleeDamageMin;
                meleeDamageMin = meleeDamageMax;
                meleeDamageMax = temp;
            }

            String displayScore = "§7你的武器分数为:§a" + NumberFormat.formatOptionalTenths(score * 100);

            PlayerSettings playerSettings = Warlords.getPlayerSettings(player.getUniqueId());
            Specializations selectedSpec = playerSettings.getSelectedSpec();
            AbstractPlayerClass apc = selectedSpec.create.get();

            ItemStack weapon = new ItemStack(Weapons.FELFLAME_BLADE.item);
            ItemMeta weaponMeta = weapon.getItemMeta();
            weaponMeta.setDisplayName("§6战争领主的邪火之刃-" + apc.getWeapon().getName() + "武器");
            ArrayList<String> weaponLore = new ArrayList<>();
            weaponLore.add("§7伤害: §c" + round(meleeDamageMin) + "§7 - §c" + round(meleeDamageMax));
            weaponLore.add("§7暴击率: §c" + round(critChance) + "%");
            weaponLore.add("§7暴击效果: §c" + round(critMultiplier) + "%");
            weaponLore.add("");
            String classNamePath = apc.getClass().getGenericSuperclass().getTypeName();
            weaponLore.add("§a" + classNamePath.substring(classNamePath.indexOf("Abstract") + 8) + " (" + apc.getClass().getSimpleName() + "):");
            weaponLore.add("§aIncreases the damage you");
            weaponLore.add("§adeal with " + apc.getWeapon().getName() + " by §c" + round(skillBoost) + "%");
            weaponLore.add("");
            weaponLore.add("§7生命: §a+" + round(health));
            weaponLore.add("§7最大能量: §a+" + round(energy));
            weaponLore.add("§7冷却缩减: §a+" + round(cooldown) + "%");
            weaponLore.add("§7速度: §a+" + round(speed) + "%");
            weaponLore.add("");
            weaponLore.add("§3合成的武器");
            weaponLore.add("");
            weaponLore.add(displayScore);
            weaponLore.add("");
            weaponLore.add("§7单击左键 再次抽取!");
            weaponMeta.setLore(weaponLore);
            weapon.setItemMeta(weaponMeta);
            m.getInventory().setItem(e.getRawSlot(), weapon);

            if (score > 0.85) {
                Bukkit.broadcastMessage("§6" + player.getDisplayName() + " §f重置的武器分数为§6" + NumberFormat.formatOptionalTenths(score * 100) + "§f!");
            }

            if (score < 0.15) {
                Bukkit.broadcastMessage("§6" + player.getDisplayName() + " §f重置的武器分数为§c" + NumberFormat.formatOptionalTenths(score * 100) + "§f!");
            }
        });

        ItemBuilder icon2 = new ItemBuilder(Material.SULPHUR);
        icon2.name(ChatColor.GREEN + "Skin Shard Roller");
        icon2.lore(
                "§7Is RNG with you to give everyone a new awesome skin?",
                "",
                "§7Left-click to roll 10 skin shards!"
        );

        menu.setItem(5, 1, icon2.get(), (m, e) -> {

            Long weaponCooldown = openWeaponCooldown.get(player.getUniqueId());

            Map<WeaponsRarity, Integer> foundWeaponCount = new EnumMap<>(WeaponsRarity.class);

            for(WeaponsRarity rarity : WeaponsRarity.values()) {
                foundWeaponCount.put(rarity, 0);
            }

            if (Bukkit.getOnlinePlayers().size() >= 1) {

                if (weaponCooldown == null || weaponCooldown < System.currentTimeMillis()) {
                    openWeaponCooldown.put(player.getUniqueId(), System.currentTimeMillis() + 8 * 60 * 1000);
                    player.playSound(player.getLocation(), Sound.NOTE_PLING, 1, 2);
                    for (int i = 0; i < 10; i++) {
                        String legendaryName = legendaryNames[random.nextInt(legendaryNames.length)];
                        String mythicName = mythicNames[random.nextInt(mythicNames.length)];

                        double chance = random.nextDouble() * 100;

                        WeaponsRarity rarity;

                        PlayerSettings playerSettings = Warlords.getPlayerSettings(player.getUniqueId());
                        Specializations selectedSpec = playerSettings.getSelectedSpec();

                        if (chance < 96.38) {
                            rarity = WeaponsRarity.RARE;
                        } else if (chance < 96.38 + 3) {
                            rarity = WeaponsRarity.EPIC;
                        } else if (chance < 96.38 + 3 + 0.6) {
                            rarity = WeaponsRarity.LEGENDARY;
                        } else {
                            rarity = WeaponsRarity.MYTHIC;
                        }

                        foundWeaponCount.compute(rarity, (key, value) -> value == null ? 1 : value + 1);
                        List<Weapons> weapons = weaponByRarity.get(rarity);

                        Weapons weapon = weapons.get(random.nextInt(weapons.size()));
                        String message = rarity.getWeaponChatColor() + legendaryName + "'s " + weapon.getName() + " of the " + selectedSpec.name;
                        String mythicMessage = rarity.getWeaponChatColor() + "§l" + mythicName + " " + weapon.getName() + " of the " + selectedSpec.name;

                        if (rarity == WeaponsRarity.EPIC) {
                            Bukkit.broadcastMessage(ChatColor.AQUA + player.getDisplayName() + " §fgot lucky and found " + message);
                        }

                        if (rarity == WeaponsRarity.LEGENDARY) {
                            Utils.playGlobalSound(player.getLocation(), "legendaryfind", 1, 1);

                            Bukkit.broadcastMessage(ChatColor.AQUA + player.getDisplayName() + " §fgot lucky and found " + message);
                            player.getWorld().spigot().strikeLightningEffect(player.getLocation(), false);
                        }

                        if (rarity == WeaponsRarity.MYTHIC) {
                            Utils.playGlobalSound(player.getLocation(), "legendaryfind", 500, 0.8f);
                            Utils.playGlobalSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 500, 0.8f);

                            Bukkit.broadcastMessage(ChatColor.AQUA + player.getDisplayName() + " §fgot lucky and found " + mythicMessage);

                            for (int j = 0; j < 10; j++) {
                                player.getWorld().spigot().strikeLightningEffect(player.getLocation(), false);
                            }
                        }

                        if (!weapon.isUnlocked) {
                            weapon.isUnlocked = true;
                            Warlords.getInstance().saveWeaponConfig();
                            Bukkit.broadcastMessage("");
                            Bukkit.broadcastMessage("§l" + rarity.getWeaponChatColor() + weapon.getName() + " §l§fis now unlocked for everyone!");
                            Bukkit.broadcastMessage("");
                        } else {
                            if (rarity == WeaponsRarity.MYTHIC) {
                                Bukkit.broadcastMessage("");
                                Bukkit.broadcastMessage("§l" + rarity.getWeaponChatColor() + weapon.getName() + " §fwas already found! Unlucky!");
                                Bukkit.broadcastMessage("");
                            }
                        }
                    }

                    player.sendMessage("");
                    player.sendMessage("§7You found:");
                    player.sendMessage("§7Rare: §9" + foundWeaponCount.get(WeaponsRarity.RARE));
                    player.sendMessage("§7Epic: §5" + foundWeaponCount.get(WeaponsRarity.EPIC));
                    player.sendMessage("§7Legendary: §6" + foundWeaponCount.get(WeaponsRarity.LEGENDARY));
                    player.sendMessage("§7Mythic: §c" + foundWeaponCount.get(WeaponsRarity.MYTHIC));
                } else {
                    long remainingTime = (weaponCooldown - System.currentTimeMillis()) / 1000;
                    long remainingTimeinMinutes = remainingTime / 60;
                    player.sendMessage(ChatColor.RED + "Please wait " + (remainingTime > 60 ? remainingTimeinMinutes + " minutes" : remainingTime + " seconds") + " before opening skin shards again!");
                }
            } else {
                player.sendMessage(ChatColor.RED + "There must be at least 16 players online to roll skin shards!");
            }
        });

        menu.setItem(4, 3, MENU_BACK_PREGAME, (m, e) -> openMainMenu(player));
        menu.openForPlayer(player);
    }
}