package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractStrikeBase;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;

public class RighteousStrike extends AbstractStrikeBase {

    public RighteousStrike() {
        super("正义之斩", 412, 523, 0, 90, 20, 175);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7攻击敌人造成§c" + format(minDamageHeal) + " §7-§c" + format(maxDamageHeal) + "§7伤害。\n" +
                "§7每次攻击都会增加敌人§60.5，\n" +
                "§7秒主动攻击冷却时间。" +
                "\n\n" +
                "§7此外，如果你所攻击的目标以被沉默，\n" +
                "§7则每次攻击减少你的棱镜护盾§60.8§7冷却时间\n" +
                "§7并减少敌人主动技能冷却时间§60.8§7秒";
    }

    @Override
    protected void onHit(@Nonnull WarlordsPlayer wp, @Nonnull Player player, @Nonnull WarlordsPlayer nearPlayer) {
        if (nearPlayer.getCooldownManager().hasCooldown(SoulShackle.class)) {
            nearPlayer.getCooldownManager().subtractTicksOnRegularCooldowns(CooldownTypes.ABILITY, 16);
            wp.getSpec().getBlue().subtractCooldown(0.8f);
        } else {
            nearPlayer.getCooldownManager().subtractTicksOnRegularCooldowns(CooldownTypes.ABILITY, 10);
        }
        nearPlayer.addDamageInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
    }
}
