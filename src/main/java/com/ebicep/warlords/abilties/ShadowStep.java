package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.FireWorkEffectPlayer;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ShadowStep extends AbstractAbility {

    public ShadowStep() {
        super("暗影步法", 466, 598, 12, 20, 15, 175);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7向前跃起,在释放技能和落地时\n"+
                "§7对所有接触到的敌人造成\n" +
                "§c"+format(minDamageHeal) + "§7-§c" + format(maxDamageHeal) + "§7的伤害\n" +
                "§7并减少本次摔落伤害" +
                "\n\n" +
                "§7携带旗帜时，暗影步法范围缩小";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {
        Location playerLoc = wp.getLocation();

        wp.setFlagPickCooldown(2);

        if (wp.getCarriedFlag() != null) {
            player.setVelocity(playerLoc.getDirection().multiply(1).setY(0.35));
            player.setFallDistance(-5);
        } else {
            player.setVelocity(playerLoc.getDirection().multiply(1.5).setY(0.7));
            player.setFallDistance(-10);
        }

        Utils.playGlobalSound(player.getLocation(), "rogue.drainingmiasma.activation", 1, 2);
        Utils.playGlobalSound(playerLoc, Sound.AMBIENCE_THUNDER, 2, 2);

        FireWorkEffectPlayer.playFirework(wp.getLocation(), FireworkEffect.builder()
                .withColor(Color.BLACK)
                .with(FireworkEffect.Type.BALL)
                .build());

        List<WarlordsPlayer> playersHit = new ArrayList<>();
        for (WarlordsPlayer assaultTarget : PlayerFilter
                .entitiesAround(player, 5, 5, 5)
                .aliveEnemiesOf(wp)
        ) {
            assaultTarget.addDamageInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
            Utils.playGlobalSound(playerLoc, "warrior.revenant.orbsoflife", 2, 1.9f);
            playersHit.add(assaultTarget);
        }

        new GameRunnable(wp.getGame()) {
            double y = playerLoc.getY();
            boolean wasOnGround = true;
            int counter = 0;
            @Override
            public void run() {
                counter++;

                // if player never lands in the span of 10 seconds, remove damage.
                if (counter == 200 || wp.isDead()) {
                    this.cancel();
                }

                wp.getLocation(playerLoc);
                boolean hitGround = player.isOnGround() || wp.onHorse();
                y = playerLoc.getY();

                if (wasOnGround && !hitGround) {
                    wasOnGround = false;
                }

                if (!wasOnGround && hitGround) {
                    wasOnGround = true;

                    for (WarlordsPlayer landingTarget : PlayerFilter
                            .entitiesAround(player, 5, 5, 5)
                            .aliveEnemiesOf(wp)
                            .excluding(playersHit)
                    ) {
                        landingTarget.addDamageInstance(wp, name, minDamageHeal, maxDamageHeal, critChance, critMultiplier, false);
                        Utils.playGlobalSound(playerLoc, "warrior.revenant.orbsoflife", 2, 1.9f);
                    }

                    FireWorkEffectPlayer.playFirework(wp.getLocation(), FireworkEffect.builder()
                            .withColor(Color.BLACK)
                            .with(FireworkEffect.Type.BALL)
                            .build());

                    this.cancel();
                }
            }
        }.runTaskTimer(0, 0);

        return true;
    }
}
