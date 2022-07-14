package com.ebicep.warlords.abilties;

import com.ebicep.warlords.abilties.internal.AbstractAbility;
import com.ebicep.warlords.effects.ParticleEffect;
import com.ebicep.warlords.events.WarlordsDamageHealingEvent;
import com.ebicep.warlords.game.option.marker.FlagHolder;
import com.ebicep.warlords.player.WarlordsPlayer;
import com.ebicep.warlords.player.cooldowns.CooldownTypes;
import com.ebicep.warlords.player.cooldowns.cooldowns.RegularCooldown;
import com.ebicep.warlords.util.warlords.GameRunnable;
import com.ebicep.warlords.util.warlords.PlayerFilter;
import com.ebicep.warlords.util.warlords.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nonnull;
import java.util.Objects;

public class OrderOfEviscerate extends AbstractAbility {

    private int duration = 8;
    private WarlordsPlayer markedPlayer;

    public OrderOfEviscerate() {
        super("绞杀命令", 0, 0, 50, 60, -1, 100);
    }

    @Override
    public void updateDescription(Player player) {
        description = "§7隐藏自己§6" + duration + "§7秒，并且获得§e40%§7的移动速度加成，\n" +
                "§7在持续时间内§e隐身§7不会被敌人发现\n" +
                "§7然而，受到任何形式的伤害和摔落伤害\n" +
                "§7达到两次，将会立刻解除隐身效果。\n" +
                "\n\n" +
                "§7你对敌人的所有攻击都会使他们§c易伤，\n" +
                "§7有§c易伤§7效果的玩家受到的伤害增加§c20%§7，\n" +
                "§7此外，从后方攻击敌人会额外增加§c10%§7伤害。" +
                "\n\n" +
                "§7成功击杀所标记的敌人将会§e重置§7你的暗影步法\n" +
                "§7与绞杀命令的冷却时间，并且返还相应的能量消耗。\n" +
                "§7协助击杀则只会返还一半的冷却时间。";
    }

    @Override
    public boolean onActivate(@Nonnull WarlordsPlayer wp, @Nonnull Player player) {
        wp.subtractEnergy(energyCost);

        wp.getCooldownManager().removeCooldown(OrderOfEviscerate.class);
        wp.getCooldownManager().addCooldown(new RegularCooldown<OrderOfEviscerate>(
                "绞杀命令",
                "ORDER",
                OrderOfEviscerate.class,
                new OrderOfEviscerate(),
                wp,
                CooldownTypes.ABILITY,
                cooldownManager -> {
                },
                duration * 20
        ) {
            int counter = 0;
            @Override
            public void doBeforeReductionFromSelf(WarlordsDamageHealingEvent event) {
                counter++;

                if (counter == 2) {
                    OrderOfEviscerate.removeCloak(wp, false);
                }
            }

            @Override
            public void doBeforeReductionFromAttacker(WarlordsDamageHealingEvent event) {
                //mark message here so it displays before damage
                WarlordsPlayer victim = event.getPlayer();
                if (victim != wp) {
                    if (!Objects.equals(this.getCooldownObject().getMarkedPlayer(), victim)) {
                        wp.sendMessage(WarlordsPlayer.GIVE_ARROW_GREEN + ChatColor.GRAY + " You have marked §e" + victim.getName());
                    }
                    this.getCooldownObject().setMarkedPlayer(victim);
                }
            }

            @Override
            public float modifyDamageBeforeInterveneFromAttacker(WarlordsDamageHealingEvent event, float currentDamageValue) {
                if (
                    Objects.equals(this.getCooldownObject().getMarkedPlayer(), event.getPlayer()) &&
                    !Utils.isLineOfSightAssassin(event.getPlayer().getEntity(), event.getAttacker().getEntity())
                ) {
                    return currentDamageValue * 1.3f;
                } else {
                    return currentDamageValue * 1.2f;
                }
            }

            @Override
            public void onDeathFromEnemies(WarlordsDamageHealingEvent event, float currentDamageValue, boolean isCrit, boolean isKiller) {
                if (!Objects.equals(event.getPlayer(), this.getCooldownObject().getMarkedPlayer())) {
                    return;
                }
                wp.getCooldownManager().removeCooldown(OrderOfEviscerate.class);
                wp.getCooldownManager().removeCooldownByName("Cloaked");
                if (isKiller) {
                    wp.sendMessage(WarlordsPlayer.GIVE_ARROW_GREEN + ChatColor.GRAY + " You killed your mark," + ChatColor.YELLOW + " your cooldowns have been reset" + ChatColor.GRAY + "!");
                    new GameRunnable(wp.getGame()) {

                        @Override
                        public void run() {
                            wp.getSpec().getPurple().setCurrentCooldown(0);
                            wp.getSpec().getOrange().setCurrentCooldown(0);
                            wp.updatePurpleItem();
                            wp.updateOrangeItem();
                            wp.subtractEnergy(-wp.getSpec().getOrange().getEnergyCost());
                        }
                    }.runTaskLater(2);
                } else {
                    new GameRunnable(wp.getGame()) {

                        @Override
                        public void run() {
                            wp.sendMessage(WarlordsPlayer.GIVE_ARROW_GREEN + ChatColor.GRAY + " You assisted in killing your mark," + ChatColor.YELLOW + " your cooldowns have been reduced by half" + ChatColor.GRAY + "!");

                            wp.getSpec().getPurple().setCurrentCooldown(wp.getSpec().getPurple().getCurrentCooldown() / 2);
                            wp.getSpec().getOrange().setCurrentCooldown(wp.getSpec().getOrange().getCurrentCooldown() / 2);
                            wp.updatePurpleItem();
                            wp.updateOrangeItem();
                            wp.subtractEnergy(-wp.getSpec().getOrange().getEnergyCost() / 2);
                        }
                    }.runTaskLater(2);
                }
                if (wp.getEntity() instanceof Player) {
                    ((Player) wp.getEntity()).playSound(wp.getLocation(), Sound.AMBIENCE_THUNDER, 1, 2);
                }
            }
        });

        if (!FlagHolder.isPlayerHolderFlag(wp)) {
            wp.getCooldownManager().removeCooldownByName("Cloaked");
            wp.getCooldownManager().addRegularCooldown(
                    "Cloaked",
                    "INVIS",
                    OrderOfEviscerate.class,
                    null,
                    wp,
                    CooldownTypes.BUFF,
                    cooldownManager -> {
                    },
                    duration * 20
            );
            player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, duration * 20, 0, true, false), true);
            wp.updateArmor();
            PlayerFilter.playingGame(wp.getGame())
                    .enemiesOf(wp)
                    .forEach(warlordsPlayer -> {
                        LivingEntity livingEntity = warlordsPlayer.getEntity();
                        if (livingEntity instanceof Player) {
                            ((Player) livingEntity).hidePlayer(player);
                        }
                    });
        }

        Runnable cancelSpeed = wp.getSpeed().addSpeedModifier("绞杀命令", 40, duration * 20, "BASE");
        Utils.playGlobalSound(player.getLocation(), Sound.GHAST_FIREBALL, 1.5f, 0.7f);

        new GameRunnable(wp.getGame()) {
            @Override
            public void run() {
                if (!wp.getCooldownManager().hasCooldown(OrderOfEviscerate.class)) {
                    this.cancel();
                    cancelSpeed.run();
                    removeCloak(wp, true);
                } else {
                    ParticleEffect.SMOKE_NORMAL.display(0, 0.2f, 0, 0.05f, 4, wp.getLocation(), 500);
                    Utils.playGlobalSound(wp.getLocation(), Sound.AMBIENCE_CAVE, 0.25f, 2);
                }
            }
        }.runTaskTimer(0, 1);

        return true;
    }

    public static void removeCloak(WarlordsPlayer warlordsPlayer, boolean forceRemove) {
        if (warlordsPlayer.getCooldownManager().hasCooldownFromName("Cloaked") || forceRemove) {
            warlordsPlayer.getCooldownManager().removeCooldownByName("Cloaked");
            warlordsPlayer.getEntity().removePotionEffect(PotionEffectType.INVISIBILITY);
            warlordsPlayer.updateArmor();
        }
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public WarlordsPlayer getMarkedPlayer() {
        return markedPlayer;
    }

    public void setMarkedPlayer(WarlordsPlayer markedPlayer) {
        this.markedPlayer = markedPlayer;
    }
}
