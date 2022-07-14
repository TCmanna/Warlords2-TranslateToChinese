package com.ebicep.customentities.npc.traits;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.game.GameMode;
import com.ebicep.warlords.party.Party;
import net.citizensnpcs.api.event.NPCLeftClickEvent;
import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.trait.Trait;
import net.citizensnpcs.trait.HologramTrait;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class GameStartTrait extends Trait {
    public GameStartTrait() {
        super("GameStartTrait");
    }

    @Override
    public void run() {
        HologramTrait hologramTrait = npc.getOrAddTrait(HologramTrait.class);
        hologramTrait.setLine(0, ChatColor.YELLOW.toString() + ChatColor.BOLD + Warlords.getGameManager().getPlayerCount() + " 位玩家");
        hologramTrait.setLine(1, ChatColor.GRAY.toString() + Warlords.getGameManager().getPlayerCountInLobby() + " 位玩家正在大厅");
        hologramTrait.setLine(2, ChatColor.GRAY.toString() + Warlords.getGameManager().getQueueSize() + " 位玩家正在等待");
        hologramTrait.setLine(3, ChatColor.AQUA + "战争领主 2 公开等待模式");
        hologramTrait.setLine(4, ChatColor.YELLOW + ChatColor.BOLD.toString() + "点击加入");
        hologramTrait.setLine(5, ChatColor.YELLOW + ChatColor.BOLD.toString() + "[随机地图模式]");
    }

    @EventHandler
    public void onRightClick(NPCRightClickEvent event) {
        if (this.getNPC() == event.getNPC()) {
            if (!Warlords.getInstance().isEnabled()) {
                // Fix old NPC standing around on Windows + plugin reload after new deployment
                this.getNPC().destroy();
                return;
            }
            tryToJoinQueue(event.getClicker());
        }
    }

    @EventHandler
    public void onLeftClick(NPCLeftClickEvent event) {
        if (this.getNPC() == event.getNPC()) {
            if (!Warlords.getInstance().isEnabled()) {
                // Fix old NPC standing around on Windows + plugin reload after new deployment
                this.getNPC().destroy();
                return;
            }
            tryToJoinQueue(event.getClicker());
        }
    }

    private void tryToJoinQueue(Player player) {

        //check if player is in a party, they must be leader to join
        Optional<Party> party = Warlords.partyManager.getPartyFromAny(player.getUniqueId());
        List<Player> people = party.map(Party::getAllPartyPeoplePlayerOnline).orElseGet(() -> Collections.singletonList(player));
        if (party.isPresent()) {
            if (!party.get().getPartyLeader().getUuid().equals(player.getUniqueId())) {
                player.sendMessage(ChatColor.RED + "你不是这个队伍的队长");
                return;
            } else if (!party.get().allOnlineAndNoAFKs()) {
                player.sendMessage(ChatColor.RED + "所有队伍成员必须全部在线");
                return;
            }
        }

        Warlords.getGameManager()
                .newEntry(people)
                .setCategory(GameMode.CAPTURE_THE_FLAG)
                .setMap(null)
                .setPriority(0)
                .setExpiresTime(System.currentTimeMillis() + 60 * 1000)
                .setOnResult((result, game) -> {
                    if (game == null) {
                        player.sendMessage(ChatColor.RED + "无法加入/创建游戏: " + result);
                    }
                }).queue();
    }
    //sendMessageToQueue(ChatColor.AQUA + player.getName() + ChatColor.YELLOW + " has quit!");
}
