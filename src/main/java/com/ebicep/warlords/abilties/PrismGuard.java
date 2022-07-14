package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.effects.circle.CircleEffect;
import com.ebicep.warlords.effects.circle.CircumferenceEffect;
import com.ebicep.warlords.events.WarlordsDamageHealingEvent;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static com.ebicep.warlords.effects.EffectUtils.playSphereAnimation;

public class PrismGuard extends AbstractAbility {

    private int bubbleRadius = 4;
    private int duration = 4;

    public PrismGuard() {
        super("棱镜护盾", 0, 0, 24, 40, -1, 100);
    }

    @Override
    public void updateDescription(Player player) {
        String healingString = duration == 5 ? "§a750§7+§a25%" : "§a600 §7+§a20%";
        description = "§7在自身周围建立一个泡沫护盾\n" +
                "§7持续§6" + duration + " §7秒。所有穿过的飞行技能\n" +
                "§7其伤害减少§c60%§7。其次，在护盾内的队友\n" +
                "§7受到的其他伤害，减少§c25%§7。\n" +
                "\n\n" +
                "§7在§6" + duration + "§7秒后，护盾破碎并治愈你\n" +
                healingString + "§7生命值，并且根据在队友所待在\n" +
                "§7护盾中时间的长短恢复你一半的治疗量。\n";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {
        wp.subtractEnergy(energyCost);
        PrismGuard tempWideGuard = new PrismGuard();
        Set<WarlordsPlayer> isInsideBubble = new HashSet<>();

        wp.getCooldownManager().addCooldown(new RegularCooldown<PrismGuard>(
                "棱镜",
                "护盾",
                PrismGuard.class,
                tempWideGuard,
                wp,
                CooldownTypes.ABILITY,
                cooldownManager -> {
                    float healingValue = 600 + (wp.getMaxHealth() - wp.getHealth()) * 0.2f;
                    wp.addHealingInstance(
                            wp,
                            name,
                            healingValue,
                            healingValue,
                            -1,
                            100,
                            false,
                            false
                    );
                },
                duration * 20
        ) {
            @Override
            public float modifyDamageAfterInterveneFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue) {
                String ability = event.getAbility();
                if (
                    ability.equals("Fireball") ||
                    ability.equals("Frostbolt") ||
                    ability.equals("Water Bolt") ||
                    ability.equals("Lightning Bolt") ||
                    ability.equals("Flame Burst") ||
                    ability.equals("Fallen Souls")
                ) {
                    if (isInsideBubble.contains(event.getAttacker())) {
                        return currentDamageValue;
                    } else {
                        return currentDamageValue * .4f;
                    }
                } else {
                    return currentDamageValue * .75f;
                }
            }
        });

        Utils.playGlobalSound(wp.getLocation(), "mage.timewarp.teleport", 2, 2);
        Utils.playGlobalSound(player.getLocation(), "warrior.intervene.impact", 2, 0.1f);

        // First Particle Sphere
        playSphereAnimation(wp.getLocation(), bubbleRadius + 2.5, 68, 176, 176);

        // Second Particle Sphere
        new GameRunnable(wp.getGame()) {
            @Override
            public void run() {
                playSphereAnimation(wp.getLocation(), bubbleRadius + 1, 65, 185, 185);
                Utils.playGlobalSound(wp.getLocation(), "warrior.intervene.impact", 2, 0.2f);
            }
        }.runTaskLater(3);

        HashMap<WarlordsPlayer, Integer> timeInBubble = new HashMap<>();

        // Third Particle Sphere
        new GameRunnable(wp.getGame()) {
            @Override
            public void run() {
                if (wp.getCooldownManager().hasCooldown(tempWideGuard)) {

                    playSphereAnimation(wp.getLocation(), bubbleRadius, 190, 190, 190);
                    Utils.playGlobalSound(wp.getLocation(), Sound.CREEPER_DEATH, 2, 2);

                    isInsideBubble.clear();
                    for (WarlordsPlayer enemyInsideBubble : PlayerFilter
                            .entitiesAround(wp, bubbleRadius, bubbleRadius, bubbleRadius)
                            .aliveEnemiesOf(wp)
                    ) {
                        isInsideBubble.add(enemyInsideBubble);
                    }

                    for (WarlordsPlayer bubblePlayer : PlayerFilter
                            .entitiesAround(wp, bubbleRadius, bubbleRadius, bubbleRadius)
                            .aliveTeammatesOfExcludingSelf(wp)
                    ) {
                        bubblePlayer.getCooldownManager().removeCooldown(PrismGuard.class);
                        bubblePlayer.getCooldownManager().addCooldown(new RegularCooldown<PrismGuard>(
                                "Prism Guard",
                                "GUARD",
                                PrismGuard.class,
                                tempWideGuard,
                                wp,
                                CooldownTypes.ABILITY,
                                cooldownManager -> {
                                },
                                20
                        ) {
                            @Override
                            public float modifyDamageAfterInterveneFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue) {
                                String ability = event.getAbility();
                                if (
                                    ability.equals("Fireball") ||
                                    ability.equals("Frostbolt") ||
                                    ability.equals("Water Bolt") ||
                                    ability.equals("Lightning Bolt") ||
                                    ability.equals("Flame Burst") ||
                                    ability.equals("Fallen Souls")
                                ) {
                                    if (isInsideBubble.contains(event.getAttacker())) {
                                        return currentDamageValue;
                                    } else {
                                        return currentDamageValue * .4f;
                                    }
                                } else {
                                    return currentDamageValue * .75f;
                                }
                            }
                        });
                        timeInBubble.compute(bubblePlayer, (k, v) -> v == null ? 1 : v + 1);
                    }
                } else {
                    this.cancel();

                    Utils.playGlobalSound(wp.getLocation(), "paladin.holyradiance.activation", 2, 1.4f);
                    Utils.playGlobalSound(wp.getLocation(), Sound.AMBIENCE_THUNDER, 2, 1.5f);

                    for (Map.Entry<WarlordsPlayer, Integer> entry : timeInBubble.entrySet()) {
                        // 5% missing health * 4
                        float healingValue = 150 + (entry.getKey().getMaxHealth() - entry.getKey().getHealth()) * 0.05f;
                        int timeInSeconds = entry.getValue() * 3 / 20;
                        float totalHealing = (timeInSeconds * healingValue);
                        entry.getKey().addHealingInstance(
                                wp,
                                name,
                                totalHealing / 2,
                                totalHealing / 2,
                                -1,
                                100,
                                false,
                                false
                        );
                    }

                    CircleEffect circle = new CircleEffect(wp.getGame(), wp.getTeam(), wp.getLocation(), bubbleRadius);
                    circle.addEffect(new CircumferenceEffect(ParticleEffect.SPELL).particlesPerCircumference(2));
                    circle.playEffects();
                }
            }
        }.runTaskTimer(5, 3);

        return true;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getBubbleRadius() {
        return bubbleRadius;
    }

    public void setBubbleRadius(int bubbleRadius) {
        this.bubbleRadius = bubbleRadius;
    }
}
