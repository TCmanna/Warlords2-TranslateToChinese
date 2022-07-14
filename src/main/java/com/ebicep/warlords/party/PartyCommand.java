package com.ebicep.warlords.party;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.poll.polls.PartyPoll;
import com.ebicep.warlords.queuesystem.QueueManager;
import com.ebicep.warlords.util.chat.ChatUtils;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class PartyCommand implements TabExecutor {

    private static final String[] partyOptions = {
            "invite", "join", "leave", "disband", "list", "promote", "demote",
            "kick", "remove", "transfer", "poll", "afk", "close", "open",
            "outside", "leader", "forcejoin", "allinvite", "invitequeue",
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String s, String[] args) {

        switch (s) {
            case "party":
            case "p":
                Player player = (Player) sender;
                if (args.length <= 0) {
                    ChatUtils.sendMessageToPlayer(player, ChatColor.GOLD + "Party Commands: \n" +
                                    ChatColor.YELLOW + "/p create" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "创建一个队伍" + "\n" +
                                    ChatColor.YELLOW + "/p invite <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "邀请其他玩家来加入你的队伍" + "\n" +
                                    ChatColor.YELLOW + "/p list" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "列出当前队伍中所有玩家" + "\n" +
                                    ChatColor.YELLOW + "/p leave" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "离开当前队伍" + "\n" +
                                    ChatColor.YELLOW + "/p disband" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "解散组队" + "\n" +
                                    ChatColor.YELLOW + "/p (kick/remove) <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "从你的队伍中移除该玩家" + "\n" +
                                    ChatColor.YELLOW + "/p transfer <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "将队长转移给另一名玩家" + "\n" +
                                    ChatColor.YELLOW + "/p afk" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "切换AFK" + "\n" +
                                    ChatColor.YELLOW + "/p promote <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "提升一名队伍成员权限" + "\n" +
                                    ChatColor.YELLOW + "/p demote <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "降低一名队伍成员权限" + "\n" +
                                    ChatColor.YELLOW + "/p poll <question/answer/answer...>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "创建一个投票" + "\n" +
                                    ChatColor.YELLOW + "/p (open/close)" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "开放或关闭队伍" + "\n" +
                                    ChatColor.YELLOW + "/p outside" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "显示队伍外的所有玩家" + "\n" +
                                    ChatColor.YELLOW + "/p leader" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "Takes leader from current leader" + "\n" +
                                    ChatColor.YELLOW + "/p forcejoin <player>" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "强制玩家加入你的队伍" + "\n" +
                                    ChatColor.YELLOW + "/p allinvite" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "切换全体邀请权限" + "\n" +
                                    ChatColor.YELLOW + "/p invitequeue" + ChatColor.DARK_GRAY + " - " + ChatColor.GRAY + ChatColor.ITALIC + "邀请所有等待中的玩家" + "\n"
                            ,
                            ChatColor.BLUE, false);
                    return true;
                }
                Optional<Party> currentParty = Warlords.partyManager.getPartyFromAny(player.getUniqueId());
                String input = args[0];
                if (input.equalsIgnoreCase("create")) {
                    if (currentParty.isPresent()) {
                        ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "你已经拥有一个队伍了!", ChatColor.BLUE, true);
                        return true;
                    }
                    Party party = new Party((player).getUniqueId(), false);
                    Warlords.partyManager.getParties().add(party);
                    Bukkit.dispatchCommand(sender, "p list");
                    return true;
                }
                if (!input.equalsIgnoreCase("join") && !input.equalsIgnoreCase("invite") && Bukkit.getOnlinePlayers().stream().noneMatch(p -> p.getName().equalsIgnoreCase(input))) {
                    if (!currentParty.isPresent()) {
                        ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "你当前没有队伍!", ChatColor.BLUE, true);
                        return true;
                    }
                }
                switch (input.toLowerCase()) {
                    case "invite": {
                        if (args.length <= 1) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "Invalid Arguments!", ChatColor.BLUE, true);
                            return true;
                        }
                        String playerToInvite = args[1];
                        Player invitedPlayer = Bukkit.getPlayer(playerToInvite);
                        if (!currentParty.isPresent()) {
                            if (invitedPlayer == sender) {
                                ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "You can't invite yourself to a party!", ChatColor.BLUE, true);
                                return true;
                            }
                            Party party = new Party((player).getUniqueId(), false);
                            Warlords.partyManager.getParties().add(party);
                            currentParty = Optional.of(party);
                        }
                        if (!currentParty.get().isAllInvite() && !currentParty.get().getPartyLeader().getUuid().equals(player.getUniqueId()) && currentParty.get().getPartyModerators().stream().noneMatch(partyPlayer -> partyPlayer.getUuid().equals(player.getUniqueId()))) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "全队邀请已禁用!", ChatColor.BLUE, true);
                            return true;
                        }
                        Player partyLeader = Bukkit.getPlayer(currentParty.get().getPartyLeader().getUuid());
                        if (invitedPlayer == null) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "无法邀请该玩家!", ChatColor.BLUE, true);
                            return true;
                        }
                        if (Warlords.partyManager.inSameParty(player.getUniqueId(), invitedPlayer.getUniqueId())) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "这名玩家已在队伍中!", ChatColor.BLUE, true);
                            return true;
                        }
                        if (currentParty.get().getInvites().containsKey(invitedPlayer.getUniqueId())) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "这名玩家已被邀请! (" + currentParty.get().getInvites().get(invitedPlayer.getUniqueId()) + ")", ChatColor.BLUE, true);
                            return true;
                        }
                        currentParty.get().invite(playerToInvite);
                        currentParty.get().sendMessageToAllPartyPlayers(
                                ChatColor.AQUA + player.getName() + ChatColor.YELLOW + "正在邀请" + ChatColor.AQUA + invitedPlayer.getName() + ChatColor.YELLOW + "加入队伍!\n" +
                                        ChatColor.YELLOW + "他们有" + ChatColor.RED + "60" + ChatColor.YELLOW + "秒的时间接受!",
                                ChatColor.BLUE, true
                        );
                        ChatUtils.sendCenteredMessage(invitedPlayer, ChatColor.BLUE.toString() + ChatColor.BOLD + "------------------------------------------");
                        ChatUtils.sendCenteredMessage(invitedPlayer, ChatColor.AQUA + player.getName() + ChatColor.YELLOW + "邀请你加入" + (partyLeader.equals(player) ? "他们的队伍!" : ChatColor.AQUA + partyLeader.getName() + ChatColor.YELLOW + "的队伍!"));
                        TextComponent message = new TextComponent(ChatColor.YELLOW + "你有" + ChatColor.RED + "60" + ChatColor.YELLOW + "秒时间来接受邀请。 " + ChatColor.GOLD + "点击此处来加入!");
                        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(ChatColor.GREEN + "Click to join the party!").create()));
                        message.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/party join " + partyLeader.getName()));
                        ChatUtils.sendCenteredMessageWithEvents(invitedPlayer, Collections.singletonList(message));
                        ChatUtils.sendCenteredMessage(invitedPlayer, ChatColor.BLUE.toString() + ChatColor.BOLD + "------------------------------------------");
                        return true;
                    }
                    case "join": {
                        if (args.length <= 1) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "无效参数!", ChatColor.BLUE, true);
                            return true;
                        }
                        String playerToJoin = args[1];
                        if (Warlords.partyManager.inAParty(player.getUniqueId())) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "你已经加入队伍!", ChatColor.BLUE, true);
                            return true;
                        }
                        if (Bukkit.getOnlinePlayers().stream().noneMatch(p -> p.getName().equalsIgnoreCase(playerToJoin))) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "找不到该玩家名称!", ChatColor.BLUE, true);
                            return true;
                        }
                        if (Bukkit.getOnlinePlayers().stream().noneMatch(p -> p.getName().equalsIgnoreCase(playerToJoin))) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "这名玩家没有队伍!", ChatColor.BLUE, true);
                            return true;
                        }
                        Player partyLeader = Bukkit.getOnlinePlayers().stream().filter(p -> p.getName().equalsIgnoreCase(playerToJoin)).findAny().get();
                        Optional<Party> party = Warlords.partyManager.getPartyFromLeader(partyLeader.getUniqueId());
                        if (!party.isPresent()) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "这名玩家没有队伍!", ChatColor.BLUE, true);
                            return true;
                        }
                        if (!party.get().isOpen() && !party.get().getInvites().containsKey(player.getUniqueId())) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "邀请已超时或队伍已解散!", ChatColor.BLUE, true);
                            return true;
                        }
                        party.get().join(player.getUniqueId());
                        QueueManager.queue.remove(player.getUniqueId());
                        return true;
                    }
                    case "leave":
                        currentParty.get().leave(player.getUniqueId());
                        ChatUtils.sendMessageToPlayer(player, ChatColor.GREEN + "你已离开队伍", ChatColor.BLUE, true);
                        return true;
                    case "disband":
                        if (currentParty.get().getPartyLeader().getUuid().equals(player.getUniqueId())) {
                            currentParty.get().disband();
                        } else {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "你不是队伍队长!", ChatColor.BLUE, true);
                        }
                        return true;
                    case "list":
                        sender.sendMessage(currentParty.get().getPartyList());
                        return true;
                    case "promote":
                    case "demote":
                    case "kick":
                    case "remove":
                    case "transfer": {
                        if (args.length <= 1) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "无效参数!", ChatColor.BLUE, true);
                            return true;
                        }
                        String targetPlayer = args[1];
                        Party party = currentParty.get();
                        //player to act on cannot be leader
                        //player is moderator and commands are not promote, demote, or transfer
                        //player cannot be a member
                        boolean isModerator = party.getPartyModerators().stream().anyMatch(partyPlayer -> partyPlayer.getUuid().equals(player.getUniqueId()));
                        boolean isMember = party.getPartyMembers().stream().anyMatch(partyPlayer -> partyPlayer.getUuid().equals(player.getUniqueId()));
                        if (
                                (party.getLeaderName().equalsIgnoreCase(targetPlayer)) ||
                                        (isModerator && (input.equalsIgnoreCase("promote") || input.equalsIgnoreCase("demote") || input.equalsIgnoreCase("transfer"))) ||
                                        (isMember)
                        ) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "权限不足!", ChatColor.BLUE, true);
                            return true;
                        }
                        if (player.getName().equalsIgnoreCase(targetPlayer)) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "你无法对自己这么做!", ChatColor.BLUE, true);
                            return true;
                        }
                        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(targetPlayer);
                        if (offlinePlayer == null || !party.hasUUID(offlinePlayer.getUniqueId())) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "找不到玩家!", ChatColor.BLUE, true);
                            return true;
                        }
                        if (input.equalsIgnoreCase("promote")) {
                            party.promote(targetPlayer);
                        } else if (input.equalsIgnoreCase("demote")) {
                            party.demote(targetPlayer);
                        } else if (input.equalsIgnoreCase("remove") || input.equalsIgnoreCase("kick")) {
                            party.remove(targetPlayer);
                        } else {
                            party.transfer(targetPlayer);
                        }
                        return true;
                    }
                    case "poll": {
                        if (args.length <= 1) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "Invalid Arguments!", ChatColor.BLUE, true);
                            return true;
                        }
                        Party party = currentParty.get();
                        if (party.getPartyMembers().stream().anyMatch(partyPlayer -> partyPlayer.getUuid().equals(player.getUniqueId()))) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "Insufficient Permissions!", ChatColor.BLUE, true);
                            return true;
                        }
                        if (!currentParty.get().getPolls().isEmpty()) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "There is already an ongoing poll!", ChatColor.BLUE, true);
                            return true;
                        }
                        String pollInfo = args[1];
                        int numberOfSlashes = (int) pollInfo.chars().filter(ch -> ch == '/').count();
                        if (numberOfSlashes <= 1) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "You must have a question and more than 1 answer!", ChatColor.BLUE, true);
                        } else {
                            List<String> pollOptions = new ArrayList<>(Arrays.asList(pollInfo.split("/")));
                            String question = pollOptions.get(0);
                            pollOptions.remove(question);
                            currentParty.get().addPoll(new PartyPoll.Builder(currentParty.get())
                                    .setQuestion(question)
                                    .setOptions(pollOptions)
                                    .get()
                            );
                        }
                        return true;
                    }
                    case "afk":
                        currentParty.get().afk(player.getUniqueId());
                        return true;
                    case "close":
                    case "open": {
                        if (!currentParty.get().getPartyLeader().getUuid().equals((player).getUniqueId())) {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "Insufficient Permissions!", ChatColor.BLUE, true);
                            return true;
                        }
                        currentParty.get().setOpen(!input.equalsIgnoreCase("close"));
                        return true;
                    }
                    case "outside": {
                        if (!currentParty.isPresent()) {
                            sender.sendMessage(ChatColor.RED + "You are currently not in a party!");
                            return true;
                        }
                        StringBuilder outside = new StringBuilder(ChatColor.YELLOW + "Players Outside Party: ");
                        int numberOfPlayersOutside = 0;
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (currentParty.get().getPartyPlayers().stream().noneMatch(partyPlayer -> partyPlayer.getUuid().equals(p.getUniqueId()))) {
                                numberOfPlayersOutside++;
                                outside.append(ChatColor.GREEN).append(p.getName()).append(ChatColor.GRAY).append(", ");
                            }
                        }
                        outside.setLength(outside.length() - 2);
                        if (numberOfPlayersOutside == 0) {
                            sender.sendMessage(ChatColor.YELLOW + "There are no players outside of the party");
                        } else {
                            sender.sendMessage(outside.toString());
                        }
                        return true;
                    }
                    case "leader": {
                        currentParty.ifPresent(party -> {
                            if (sender.hasPermission("warlords.party.forceleader")) {
                                party.transfer(sender.getName());
                            } else {
                                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                            }
                        });
                        return true;
                    }
                    case "forcejoin": {
                        currentParty.ifPresent(party -> {
                            if (sender.hasPermission("warlords.party.forcejoin")) {
                                if (args.length < 2) {
                                    sender.sendMessage(ChatColor.RED + "Invalid Arguments! /p forcejoin [NAME or @a]");
                                    return;
                                }
                                String name = args[1];
                                if (name.equalsIgnoreCase("@a")) {
                                    Bukkit.getOnlinePlayers().stream()
                                            .filter(p -> !p.getName().equalsIgnoreCase(party.getLeaderName()) && !party.hasUUID(p.getUniqueId()))
                                            .forEach(p -> Bukkit.getServer().dispatchCommand(p, "p join " + party.getLeaderName()));
                                    return;
                                }
                                Player playerToForceInvite = Bukkit.getPlayer(name);
                                if (playerToForceInvite == null) {
                                    ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "Cannot find a player with that name!", ChatColor.BLUE, true);
                                    return;
                                }
                                if (Warlords.partyManager.inSameParty(player.getUniqueId(), playerToForceInvite.getUniqueId())) {
                                    ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "That player is already in the party!", ChatColor.BLUE, true);
                                    return;
                                }
                                Bukkit.getServer().dispatchCommand(playerToForceInvite, "p join " + party.getLeaderName());

                            } else {
                                sender.sendMessage(ChatColor.RED + "You do not have permission to do that.");
                            }
                        });
                        return true;
                    }
                    case "allinvite": {
                        Party party = currentParty.get();
                        if (party.getPartyLeader().getUuid().equals((player).getUniqueId())) {
                            party.setAllInvite(!party.isAllInvite());
                            if (party.isAllInvite()) {
                                party.sendMessageToAllPartyPlayers(ChatColor.GREEN + "All invite is now on", ChatColor.BLUE, true);
                            } else {
                                party.sendMessageToAllPartyPlayers(ChatColor.RED + "All invite is now off", ChatColor.BLUE, true);
                            }
                        } else {
                            ChatUtils.sendMessageToPlayer(player, ChatColor.RED + "Insufficient Permissions!", ChatColor.BLUE, true);
                        }
                        return true;
                    }
                    case "invitequeue":
                        int partySize = currentParty.get().getPartyPlayers().size();
                        if (partySize != 24) {
                            int availableSpots = 24 - partySize;
                            int onlineQueueSize = (int) QueueManager.queue.stream().filter(uuid -> Bukkit.getPlayer(uuid) != null).count();
                            List<UUID> toInvite = new ArrayList<>();
                            int inviteNumber;
                            if (availableSpots % 2 == 0) { //even spots
                                inviteNumber = Math.min(availableSpots, onlineQueueSize % 2 == 0 ? onlineQueueSize : onlineQueueSize - 1);
                            } else { //odd spots
                                inviteNumber = Math.min(availableSpots, onlineQueueSize % 2 != 0 ? onlineQueueSize : onlineQueueSize - 1);
                            }
                            int counter = 0;
                            if (inviteNumber != 0) {
                                for (UUID uuid : QueueManager.queue) {
                                    Player invitePlayer = Bukkit.getPlayer(uuid);
                                    if (invitePlayer != null) {
                                        toInvite.add(uuid);
                                        counter++;
                                        if (counter >= inviteNumber) {
                                            break;
                                        }
                                    }
                                }
                                toInvite.forEach(uuid -> {
                                    Bukkit.dispatchCommand(sender, "p invite " + Bukkit.getPlayer(uuid).getName());
                                });
                                QueueManager.queue.removeIf(uuid -> Bukkit.getPlayer(uuid) == null);
                                QueueManager.sendNewQueue();
                            }
                        }
                        return true;
                    default:
                        Bukkit.getServer().dispatchCommand(sender, "p invite " + input);
                        return true;
                }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String s, String[] args) {
        String lastArg = args[args.length - 1];
        List<String> output;
        if (args.length > 1) {
            output = Bukkit.getOnlinePlayers().stream()
                    .filter(e -> e.getName().toLowerCase().startsWith(lastArg.toLowerCase()))
                    .map(e -> e.getName().charAt(0) + e.getName().substring(1))
                    .collect(Collectors.toList());
            Warlords.partyManager.getPartyFromAny(((Player) commandSender).getUniqueId()).ifPresent(party -> party.getPartyPlayers().stream()
                    .filter(PartyPlayer::isOffline)
                    .map(partyPlayer -> Bukkit.getOfflinePlayer(partyPlayer.getUuid()).getName())
                    .forEach(output::add));
            return output;
        }
        output = Bukkit.getOnlinePlayers().stream()
                .map(HumanEntity::getName)
                .filter(e -> e.toLowerCase().startsWith(lastArg.toLowerCase(Locale.ROOT)))
                .collect(Collectors.toList());
        Arrays.stream(partyOptions)
                .filter(e -> e.startsWith(lastArg.toLowerCase(Locale.ROOT)))
                .map(e -> e.charAt(0) + e.substring(1).toLowerCase(Locale.ROOT))
                .forEach(output::add);
        Warlords.partyManager.getPartyFromAny(((Player) commandSender).getUniqueId()).ifPresent(party -> party.getPartyPlayers().stream()
                .filter(PartyPlayer::isOffline)
                .map(partyPlayer -> Bukkit.getOfflinePlayer(partyPlayer.getUuid()).getName())
                .forEach(output::add));
        return output;

    }

    public void register(Warlords instance) {
        instance.getCommand("party").setExecutor(this);
        instance.getCommand("party").setTabCompleter(this);
    }

}