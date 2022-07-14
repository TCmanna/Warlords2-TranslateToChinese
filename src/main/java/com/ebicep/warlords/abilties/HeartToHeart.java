package com.ebicep.warlords.abilties;

import com.ebicep.warlords.Warlords;
import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.util.bukkit.Matrix4d;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import javax.annotation.Nonnull;

public class HeartToHeart extends AbstractAbility {

    private int radius = 15;
    private int verticalRadius = 15;
    private int vindDuration = 6;
    private float healthRestore = 600;

    public HeartToHeart() {
        super("心之守护", 0, 0, 12, 20, -1, 100);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7链接在范围§e15§7格方块内队友，\n" +
                "§7将维护者快速抓向此人，你和队友\n" +
                "§7同时获得§6维护§7并且免疫负面效果，\n" +
                "持续§6" + vindDuration + "§7秒。" +
                "§7抵达队友身边后，将治愈自己§a" + format(healthRestore)+"§7的生命值。"  +
                "\n\n" +
                "§7心之守护的范围将在持有旗帜时大幅缩减。";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {
        if (wp.hasFlag()) {
            radius = 9;
            verticalRadius = 2;
        } else {
            wp.setFlagPickCooldown(2);
            radius = 15;
            verticalRadius = 15;
        }

        for (WarlordsPlayer heartTarget : PlayerFilter
                .entitiesAround(wp, radius, verticalRadius, radius)
                .aliveTeammatesOfExcludingSelf(wp)
                .requireLineOfSight(wp)
                .lookingAtFirst(wp)
                .limit(1)
        ) {
            Utils.playGlobalSound(player.getLocation(), "rogue.hearttoheart.activation", 2, 1);
            Utils.playGlobalSound(player.getLocation(), "rogue.hearttoheart.activation.alt", 2, 1.2f);

            HeartToHeart tempHeartToHeart = new HeartToHeart();
            wp.subtractEnergy(energyCost);

            // remove other instances of vindicate buff to override
            heartTarget.getCooldownManager().removeCooldownByName("Vindicate Debuff Immunity");
            heartTarget.getCooldownManager().addRegularCooldown(
                    "维护免疫",
                    "维护",
                    HeartToHeart.class,
                    tempHeartToHeart,
                    wp,
                    CooldownTypes.BUFF,
                    cooldownManager -> {},
                    vindDuration * 20
            );

            wp.getCooldownManager().removeCooldownByName("维护免疫");
            wp.getCooldownManager().addRegularCooldown(
                    "维护免疫",
                    "维护",
                    HeartToHeart.class,
                    tempHeartToHeart,
                    wp,
                    CooldownTypes.BUFF,
                    cooldownManager -> {},
                    vindDuration * 20
            );

            new BukkitRunnable() {

                final Location playerLoc = wp.getLocation();
                int timer = 0;

                @Override
                public void run() {
                    timer++;

                    if (timer >= 8 || (heartTarget.isDead() || wp.isDead())) {
                        this.cancel();
                    }

                    double target = timer / 8D;
                    Location targetLoc = heartTarget.getLocation();
                    Location newLocation = new Location(
                            playerLoc.getWorld(),
                            Utils.lerp(playerLoc.getX(), targetLoc.getX(), target),
                            Utils.lerp(playerLoc.getY(), targetLoc.getY(), target),
                            Utils.lerp(playerLoc.getZ(), targetLoc.getZ(), target),
                            targetLoc.getYaw(),
                            targetLoc.getPitch()
                    );

                    EffectUtils.playChainAnimation(wp, heartTarget, new ItemStack(Material.LEAVES, 1, (short) 1), timer);

                    wp.teleportLocationOnly(newLocation);
                    player.setFallDistance(-5);
                    newLocation.add(0, 1, 0);
                    Matrix4d center = new Matrix4d(newLocation);
                    for (float i = 0; i < 6; i++) {
                        double angle = Math.toRadians(i * 90) + timer * 0.6;
                        double width = 1.5D;
                        ParticleEffect.SPELL_WITCH.display(0, 0, 0, 0, 1,
                                center.translateVector(playerLoc.getWorld(), 0, Math.sin(angle) * width, Math.cos(angle) * width), 500);
                    }

                    if (timer >= 8) {
                        wp.setVelocity(playerLoc.getDirection().multiply(0.4).setY(0.2));
                        wp.addHealingInstance(wp, name, healthRestore, healthRestore, -1, 100, false, false);
                    }
                }
            }.runTaskTimer(Warlords.getInstance(), 0, 1);

            return true;
        }

        return false;
    }

    public void setVindDuration(int vindDuration) {
        this.vindDuration = vindDuration;
    }

    public float getHealthRestore() {
        return healthRestore;
    }

    public void setHealthRestore(float healthRestore) {
        this.healthRestore = healthRestore;
    }
}
