package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractStrikeBase;
import com.ebicep.warlords.events.WarlordsDamageHealingEvent;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.cooldowns.cooldowns.DamageHealCompleteCooldown;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class JudgementStrike extends AbstractStrikeBase {

    int attacksDone = 0;

    public JudgementStrike() {
        super("裁决之斩", 326, 441, 0, 70, 20, 185);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7攻击目标敌人, 造成§c" + format(minDamageHeal) + "§7-§c" + format(maxDamageHeal) + "§7伤害，\n" +
                "§7每攻击三次将 §c绝对 §7触发一次致命一击。\n" +
                "§7致命一击会暂时增加§e25%§7的移动速度，持续§62§7秒。";
    }

    @Override
    protected void onHit(@Nonnull WarlordsPlayer wp, @Nonnull Player player, @Nonnull WarlordsPlayer nearPlayer) {

        attacksDone++;
        int critChance = this.critChance;
        if (attacksDone == 3) {
            attacksDone = 0;
            critChance = 100;
        }

        wp.getCooldownManager().addCooldown(new DamageHealCompleteCooldown<JudgementStrike>(
                "裁决之斩",
                "",
                JudgementStrike.class,
                new JudgementStrike(),
                wp,
                CooldownTypes.ABILITY,
                cooldownManager -> {
                }
        ) {
            @Override
            public void onDamageFromAttacker(WarlordsDamageHealingEvent event, float currentDamageValue, boolean isCrit) {
                if (event.getAbility().equals("裁决之斩") && isCrit) {
                    event.getAttacker().getSpeed().addSpeedModifier("审判时刻", 25, 2 * 20, "BASE");
                }
            }
        });
        nearPlayer.addDamageInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
    }
}
