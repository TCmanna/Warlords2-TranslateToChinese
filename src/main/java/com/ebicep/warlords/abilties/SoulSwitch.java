package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;

public class SoulSwitch extends AbstractAbility {

    private int radius = 13;

    public SoulSwitch() {
        super("灵魂交换", 0, 0, 30, 40, -1, 50);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7与敌人交换位置并使其失明§61.5§7秒，\n" +
                "§7最佳范围于§e" + radius + "§7格方块。\n" +
                "§7灵魂交换的垂直范围稍低。";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {

        for (WarlordsPlayer swapTarget : PlayerFilter
                .entitiesAround(wp.getLocation(), radius, 6, radius)
                .aliveEnemiesOf(wp)
                .requireLineOfSight(wp)
                .closestFirst(wp)
        ) {
            if (swapTarget.getCarriedFlag() != null) {
                wp.sendMessage(ChatColor.RED + "You cannot Soul Switch with a player holding the flag!");
            } else if (wp.getCarriedFlag() != null) {
                wp.sendMessage(ChatColor.RED + "You cannot Soul Switch while holding the flag!");
            } else {
                Location swapLocation = swapTarget.getLocation();
                Location ownLocation = wp.getLocation();

                swapTarget.getEntity().addPotionEffect(
                        new PotionEffect(PotionEffectType.BLINDNESS, 30, 0, true, false), true);
                swapTarget.sendMessage(WarlordsPlayer.RECEIVE_ARROW_RED + ChatColor.GRAY + " You've been Soul Swapped by " + ChatColor.YELLOW + wp.getName() + "!");
                swapTarget.teleport(new Location(
                        wp.getWorld(),
                        ownLocation.getX(),
                        ownLocation.getY(),
                        ownLocation.getZ(),
                        swapLocation.getYaw(),
                        swapLocation.getPitch()));

                wp.sendMessage(WarlordsPlayer.GIVE_ARROW_GREEN + ChatColor.GRAY + " You swapped with " + ChatColor.YELLOW + swapTarget.getName() + "!");
                wp.teleport(new Location(
                        swapLocation.getWorld(),
                        swapLocation.getX(),
                        swapLocation.getY(),
                        swapLocation.getZ(),
                        ownLocation.getYaw(),
                        ownLocation.getPitch()));

                wp.subtractEnergy(energyCost);

                EffectUtils.playCylinderAnimation(swapLocation, 1.05, ParticleEffect.CLOUD, 1);
                EffectUtils.playCylinderAnimation(ownLocation, 1.05, ParticleEffect.CLOUD, 1);

                Utils.playGlobalSound(player.getLocation(), Sound.ENDERMAN_TELEPORT, 2, 1.5f);

                return true;
            }
        }

        return false;
    }

    public int getRadius() {
        return radius;
    }

    public void setRadius(int radius) {
        this.radius = radius;
    }
}
