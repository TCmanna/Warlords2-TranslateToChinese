package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.util.bukkit.PacketUtils;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import javax.annotation.Nonnull;

public class SoulShackle extends AbstractAbility {

    private final int shackleRange = 15;
    private float shacklePool = 0;
    private final int maxShackleTargets = 1;
    private int minSilenceDurationInTicks = 40;
    private int maxSilenceDurationInTicks = 70;

    public SoulShackle() {
        super("灵魂枷锁", 446, 589, 9, 40, 20, 175);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7锁住最多§e" + maxShackleTargets + "§7个敌人，造成§c" + format(minDamageHeal) + " §7-§c" + format(maxDamageHeal) + "§7伤害。\n" +
                "§7被锁住的敌人会被§c沉默§6" + format(minSilenceDurationInTicks / 20f) + "§7-§6" + format(maxSilenceDurationInTicks / 20f) + "§7秒，沉默的\n" +
                "§7持续时间内无法使用他们的主要攻击。\n" +
                "§7技能未释放前6秒内，每受到§c500§7伤害\n" +
                "§7则增加沉默时间§60.5§7秒。\n" +
                "§7锁住敌人后将获得§e40%§7移动速度，持续§61.5§7秒。\n" +
                "\n\n" +
                "§7最佳范围为§e" + shackleRange + "§7格方块。";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {
        SoulShackle tempSoulShackle = new SoulShackle();

        for (WarlordsPlayer shackleTarget : PlayerFilter
                .entitiesAround(wp, shackleRange, shackleRange, shackleRange)
                .aliveEnemiesOf(wp)
                .requireLineOfSight(wp)
                .closestFirst(wp)
                .lookingAtFirst(wp)
                .limit(maxShackleTargets)
        ) {
            wp.getSpeed().addSpeedModifier("Shackle Speed", 40, 30, "BASE");
            wp.subtractEnergy(energyCost);
            wp.sendMessage(
                WarlordsPlayer.GIVE_ARROW_GREEN +
                        ChatColor.GRAY + "桎梏" +
                        ChatColor.YELLOW + shackleTarget.getName() +
                        ChatColor.GRAY + "!"
            );

            int silenceDuration = minSilenceDurationInTicks + (int) (shacklePool / 1000) * 20;
            if (silenceDuration > maxSilenceDurationInTicks) {
                silenceDuration = maxSilenceDurationInTicks;
            }

            shackleTarget.addDamageInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
            shackleTarget.getCooldownManager().removeCooldown(SoulShackle.class);
            if (!shackleTarget.getCooldownManager().hasCooldownFromName("Vindicate Debuff Immunity")) {
                if (shackleTarget.getEntity() instanceof Player) {
                    PacketUtils.sendTitle((Player) shackleTarget.getEntity(), "", "§cSILENCED", 0, silenceDuration, 0);
                }
            }
            shackleTarget.getCooldownManager().addRegularCooldown(
                    "Shackle Silence",
                    "SILENCE",
                    SoulShackle.class,
                    tempSoulShackle,
                    wp,
                    CooldownTypes.DEBUFF,
                    cooldownManager -> {
                    },
                    silenceDuration
            );

            shacklePool = 0;

            Utils.playGlobalSound(player.getLocation(), "warrior.intervene.impact", 1.5f, 0.45f);
            Utils.playGlobalSound(player.getLocation(), "mage.fireball.activation", 1.5f, 0.3f);

            EffectUtils.playChainAnimation(wp, shackleTarget, new ItemStack(Material.PUMPKIN), 20);

            new GameRunnable(wp.getGame()) {
                @Override
                public void run() {
                    if (shackleTarget.getCooldownManager().hasCooldown(tempSoulShackle)) {
                        Location playerLoc = shackleTarget.getLocation();
                        Location particleLoc = playerLoc.clone();
                        for (int i = 0; i < 10; i++) {
                            for (int j = 0; j < 10; j++) {
                                double angle = j / 10D * Math.PI * 2;
                                double width = 1.075;
                                particleLoc.setX(playerLoc.getX() + Math.sin(angle) * width);
                                particleLoc.setY(playerLoc.getY() + i / 5D);
                                particleLoc.setZ(playerLoc.getZ() + Math.cos(angle) * width);

                                ParticleEffect.REDSTONE.display(new ParticleEffect.OrdinaryColor(25, 25, 25), particleLoc, 500);
                            }
                        }
                        Utils.playGlobalSound(shackleTarget.getLocation(), Sound.DIG_SAND, 2, 2);
                    } else {
                        this.cancel();
                    }
                }
            }.runTaskTimer(0, 10);

            return true;
        }

        return false;
    }

    public float getShacklePool() {
        return shacklePool;
    }

    public void addToShacklePool(float amount) {
        this.shacklePool += amount;
    }

    @Override
    public void runEverySecond() {
        if (shacklePool > 0) {
            float newPool = shacklePool - 200;
            shacklePool = Math.max(newPool, 0);
        }
    }

    public void setMaxSilenceDurationInTicks(int maxSilenceDurationInTicks) {
        this.maxSilenceDurationInTicks = maxSilenceDurationInTicks;
    }

    public void setMinSilenceDurationInTicks(int minSilenceDurationInTicks) {
        this.minSilenceDurationInTicks = minSilenceDurationInTicks;
    }
}
