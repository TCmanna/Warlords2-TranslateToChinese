package com.ebicep.warlords.player;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.ebicep.warlords.player.Specializations.*;

public enum Classes {
    MAGE(
            "法师",
            new ItemStack(Material.INK_SACK, 1, (short) 12),
            "§7The mage has access to powerful\n§7Arcane, Fire, Ice and Water magic.",
            PYROMANCER, CRYOMANCER, AQUAMANCER
    ),
    WARRIOR(
            "战士",
            new ItemStack(Material.COAL, 1, (short) 1),
            "§7The Warrior uses brute force to\n§7overpower their opponents in melee\n§7combat or to defend their allies.",
            BERSERKER, DEFENDER, REVENANT
    ),
    PALADIN(
            "圣骑士",
            new ItemStack(Material.INK_SACK, 1, (short) 11),
            "§7The Paladin's strongest ally is the\n§7light. They use it to empower their\n§7weapon in order to vanquish foes and\n§7protect teammates.",
            AVENGER, CRUSADER, PROTECTOR
    ),
    SHAMAN(
            "萨满",
            new ItemStack(Material.INK_SACK, 1, (short) 2),
            "§7The Shaman has an unbreakable bond\n§7with nature. This grants them access to\n§7devastating abilities that are\n§7empowered by the elements.",
            THUNDERLORD, SPIRITGUARD, EARTHWARDEN
    ),
    ROGUE(
            "盗贼",
            new ItemStack(Material.INK_SACK, 1, (short) 9),
            "§7盗贼拥有最强的欺骗性。\n§7总能隐秘在黑暗中\n§7出其不意占取先机。",
            ASSASSIN, VINDICATOR, APOTHECARY
    );

    public final String name;
    public final ItemStack item;
    public final String description;
    public final List<Specializations> subclasses;

    Classes(String name, ItemStack item, String description, Specializations... subclasses) {
        this.name = name;
        this.item = item;
        this.description = description;
        this.subclasses = Collections.unmodifiableList(Arrays.asList(subclasses));
    }
}