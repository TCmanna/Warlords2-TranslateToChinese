package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.EffectUtils;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.effects.circle.CircleEffect;
import com.ebicep.warlords.effects.circle.CircumferenceEffect;
import com.ebicep.warlords.events.WarlordsDamageHealingEvent;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class Vindicate extends AbstractAbility {

    private final int radius = 8;
    private final int vindicateDuration = 12;
    private final int vindicateSelfDuration = 8;
    private float vindicateDamageReduction = 30;

    public Vindicate() {
        super("博平反正", 0, 0, 55, 25, -1, 100);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7半径为§e" + radius + "§7格内的队友获得§6维护§7效果，\n" +
                "§7并清除所有负面效果。此外在§6维护§7状态\n" +
                "§7中的的队友不会受到减伤效果，\n" +
                "§7并且获得§650%§7防击退能力，持续§6" + vindicateDuration + "§7秒。\n" +
                "§7你将获得§e" + format(vindicateDamageReduction) + "%§7的伤害减免，持续§6"+ vindicateSelfDuration + "§7秒。";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {
        wp.subtractEnergy(energyCost);

        Utils.playGlobalSound(player.getLocation(), "rogue.vindicate.activation", 2, 0.7f);
        Utils.playGlobalSound(player.getLocation(), "shaman.capacitortotem.pulse", 2, 0.7f);

        Vindicate tempVindicate = new Vindicate();

        for (WarlordsPlayer vindicateTarget : PlayerFilter
                .entitiesAround(wp, radius, radius, radius)
                .aliveTeammatesOfExcludingSelf(wp)
                .closestFirst(wp)
        ) {
            wp.sendMessage(
                WarlordsPlayer.GIVE_ARROW_GREEN +
                ChatColor.GRAY + " 你的维护者正在保护 " +
                ChatColor.YELLOW + vindicateTarget.getName() +
                ChatColor.GRAY + "!"
            );

            vindicateTarget.sendMessage(
                WarlordsPlayer.RECEIVE_ARROW_GREEN + " " +
                ChatColor.GRAY + wp.getName() + "'s" +
                ChatColor.YELLOW + " 维护" +
                ChatColor.GRAY + " 正在保护你免疫负面效果 " +
                ChatColor.GOLD + vindicateDuration +
                ChatColor.GRAY + " 秒!"
            );

            // Vindicate Immunity
            vindicateTarget.getSpeed().removeSlownessModifiers();
            vindicateTarget.getCooldownManager().removeDebuffCooldowns();
            vindicateTarget.getCooldownManager().removeCooldownByName("Vindicate Debuff Immunity");
            vindicateTarget.getCooldownManager().addRegularCooldown(
                    "Vindicate Debuff Immunity",
                    "VIND",
                    Vindicate.class,
                    tempVindicate,
                    wp,
                    CooldownTypes.BUFF,
                    cooldownManager -> {},
                    vindicateDuration * 20
            );
        }

        wp.getCooldownManager().addCooldown(new RegularCooldown<Vindicate>(
                "Vindicate Resistance",
                "VIND RES",
                Vindicate.class,
                tempVindicate,
                wp,
                CooldownTypes.BUFF,
                cooldownManager -> {},
                vindicateSelfDuration * 20
        ) {
            @Override
            public float modifyDamageAfterInterveneFromSelf(WarlordsDamageHealingEvent event, float currentDamageValue) {
                return currentDamageValue * getVindicateDamageReduction();
            }
        });

        CircleEffect circle = new CircleEffect(wp.getGame(), wp.getTeam(), player.getLocation(), radius);
        circle.addEffect(new CircumferenceEffect(ParticleEffect.SPELL, ParticleEffect.REDSTONE).particlesPerCircumference(2));
        circle.playEffects();

        EffectUtils.playHelixAnimation(player, radius, 230, 130, 5);

        return true;
    }

    public float getVindicateDamageReduction() {
        return (100 - vindicateDamageReduction) / 100f;
    }

    public void setVindicateDamageReduction(int vindicateDamageReduction) {
        this.vindicateDamageReduction = vindicateDamageReduction;
    }
}
