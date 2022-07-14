package com.ebicep.warlords.player;

import com.ebicep.warlords.classes.AbstractPlayerClass;
import com.ebicep.warlords.classes.mage.specs.Aquamancer;
import com.ebicep.warlords.classes.mage.specs.Cryomancer;
import com.ebicep.warlords.classes.mage.specs.Pyromancer;
import com.ebicep.warlords.classes.paladin.specs.Avenger;
import com.ebicep.warlords.classes.paladin.specs.Crusader;
import com.ebicep.warlords.classes.paladin.specs.Protector;
import com.ebicep.warlords.classes.rogue.specs.Apothecary;
import com.ebicep.warlords.classes.rogue.specs.Assassin;
import com.ebicep.warlords.classes.rogue.specs.Vindicator;
import com.ebicep.warlords.classes.shaman.specs.earthwarden.Earthwarden;
import com.ebicep.warlords.classes.shaman.specs.spiritguard.Spiritguard;
import com.ebicep.warlords.classes.shaman.specs.thunderlord.Thunderlord;
import com.ebicep.warlords.classes.warrior.specs.berserker.Berserker;
import com.ebicep.warlords.classes.warrior.specs.defender.Defender;
import com.ebicep.warlords.classes.warrior.specs.revenant.Revenant;
import com.ebicep.warlords.util.bukkit.WordWrap;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.ebicep.warlords.player.SkillBoosts.*;

public enum Specializations {
    PYROMANCER("Pyromancer",
            Pyromancer::new,
            WordWrap.wrapWithNewline("§7A damage-oriented Mage specialization that uses the destructive Fire spells to obliterate enemies.", 200),
            SpecType.DAMAGE,
            FIREBALL, FLAME_BURST, TIME_WARP_PYROMANCER, ARCANE_SHIELD_PYROMANCER, INFERNO),
    CRYOMANCER("Cryomancer",
            Cryomancer::new,
            WordWrap.wrapWithNewline("§7A defense-oriented Mage specialization that uses Ice spells to slow down enemies and to creative defensive barriers.", 200),
            SpecType.TANK,
            FROST_BOLT, FREEZING_BREATH, TIME_WARP_CRYOMANCER, ARCANE_SHIELD_CRYOMANCER, ICE_BARRIER),
    AQUAMANCER("Aquamancer",
            Aquamancer::new,
            WordWrap.wrapWithNewline("§7A healing-oriented Mage specialization that uses Water spells to heal allies and to deal minor damage to enemies. This specialization has access to the 'Overheal' ability.", 200),
            SpecType.HEALER,
            WATER_BOLT, WATER_BREATH, TIME_WARP_AQUAMANCER, ARCANE_SHIELD_AQUAMANCER, HEALING_RAIN),
    BERSERKER("Berserker",
            Berserker::new,
            WordWrap.wrapWithNewline("§7A damage-oriented Warrior specialization with a lust for blood and anger issues.", 200),
            SpecType.DAMAGE,
            WOUNDING_STRIKE_BERSERKER, SEISMIC_WAVE_BERSERKER, GROUND_SLAM_BERSERKER, BLOOD_LUST, BERSERK),
    DEFENDER("Defender",
            Defender::new,
            WordWrap.wrapWithNewline("§7A defense-oriented Warrior specialization that can protect teammates by mitigating damage and intercepting enemy hits.", 200),
            SpecType.TANK,
            WOUNDING_STRIKE_DEFENDER, SEISMIC_WAVE_DEFENDER, GROUND_SLAM_DEFENDER, INTERVENE, LAST_STAND),
    REVENANT("Revenant",
            Revenant::new,
            WordWrap.wrapWithNewline("§7A support-oriented Warrior specialization that can give allies a second chance of life.", 200),
            SpecType.HEALER,
            CRIPPLING_STRIKE, RECKLESS_CHARGE, GROUND_SLAM_REVENANT, ORBS_OF_LIFE, UNDYING_ARMY),
    AVENGER("Avenger",
            Avenger::new,
            WordWrap.wrapWithNewline("§7A damage-oriented Paladin specialization that focuses on draining energy from enemies and has access to minor healing.", 200),
            SpecType.DAMAGE,
            AVENGER_STRIKE, CONSECRATE_AVENGER, LIGHT_INFUSION_AVENGER, HOLY_RADIANCE_AVENGER, AVENGERS_WRATH),
    CRUSADER("Crusader",
            Crusader::new,
            WordWrap.wrapWithNewline("§7A defense-oriented Paladin specialization that inspires allies by granting them more energy in battle and has access to minor healing.", 200),
            SpecType.TANK,
            CRUSADER_STRIKE, CONSECRATE_CRUSADER, LIGHT_INFUSION_CRUSADER, HOLY_RADIANCE_CRUSADER, INSPIRING_PRESENCE),
    PROTECTOR("Protector",
            Protector::new,
            WordWrap.wrapWithNewline("§7A healing-oriented Paladin specialization that converts damage into healing for his allies and has access to greater healing abilities.", 200),
            SpecType.HEALER,
            PROTECTOR_STRIKE, CONSECRATE_PROTECTOR, LIGHT_INFUSION_PROTECTOR, HOLY_RADIANCE_PROTECTOR, HAMMER_OF_LIGHT),
    THUNDERLORD("Thunderlord",
            Thunderlord::new,
            WordWrap.wrapWithNewline("§7A damage-oriented Shaman specialization that calls upon the power of Lightning to electrocute enemies.", 200),
            SpecType.DAMAGE,
            LIGHTNING_BOLT, CHAIN_LIGHTNING, WINDFURY_WEAPON, LIGHTNING_ROD, CAPACITOR_TOTEM),
    SPIRITGUARD("Spiritguard",
            Spiritguard::new,
            WordWrap.wrapWithNewline("§7A defense-oriented Shaman specialization that calls upon the aid of spirits old and new to mitigate damage and avoid death.", 200),
            SpecType.TANK,
            FALLEN_SOULS, SPIRIT_LINK, SOULBINDING_WEAPON, REPENTANCE, DEATHS_DEBT),
    EARTHWARDEN("Earthwarden",
            Earthwarden::new,
            WordWrap.wrapWithNewline("§7A healing-oriented Shaman specialization that calls upon the power of Earth to crush enemies and to aid allies.", 200),
            SpecType.HEALER,
            EARTHEN_SPIKE, BOULDER, EARTHLIVING_WEAPON, CHAIN_HEAL, HEALING_TOTEM),
    ASSASSIN("暗杀者",
            Assassin::new,
            WordWrap.wrapWithNewline("§7输出型盗贼能够实现完美隐身，将以绝对的速度击杀经过他们的敌人", 200),
            SpecType.DAMAGE,
            JUDGEMENT_STRIKE, INCENDIARY_CURSE, BLINDING_ASSAULT, SOUL_SWITCH, ORDER_OF_EVISCERATE),
    VINDICATOR("拥护者",
            Vindicator::new,
            WordWrap.wrapWithNewline("§7防御型盗贼能够欺骗敌人限制他们的力量，并极尽全力保护队友", 200),
            SpecType.TANK,
            RIGHTEOUS_STRIKE, SOUL_SHACKLE, HEART_TO_HEART, PRISM_GUARD, VINDICATE),
    APOTHECARY("炼金者",
            Apothecary::new,
            WordWrap.wrapWithNewline("§7治愈型盗贼能够使用特制的炼金剂与药酒的力量，削弱敌人并保护队友", 200),
            SpecType.HEALER,
            IMPALING_STRIKE, SOOTHING_PUDDLE, VITALITY_LIQUOR, REMEDIC_CHAINS, DRAINING_MIASMA),

    ;

    public final String name;
    public final Supplier<AbstractPlayerClass> create;
    public final String description;
    public final SpecType specType;
    public final List<SkillBoosts> skillBoosts;

    Specializations(String name, Supplier<AbstractPlayerClass> create, String description, SpecType specType, SkillBoosts... skillBoosts) {
        this.name = name;
        this.create = create;
        this.description = description;
        this.specType = specType;
        this.skillBoosts = Arrays.asList(skillBoosts);
    }

    public static Specializations getSpecFromName(String name) {
        if (name == null) {
            return PYROMANCER;
        }
        for (Specializations value : Specializations.values()) {
            if (value.name.equalsIgnoreCase(name)) {
                return value;
            }
        }
        return PYROMANCER;
    }

    public static Classes getClass(Specializations selected) {
        return Arrays.stream(Classes.values()).filter(o -> o.subclasses.contains(selected)).collect(Collectors.toList()).get(0);
    }

    public static Classes getClass(String specName) {
        return Arrays.stream(Classes.values()).filter(o -> o.subclasses.stream().anyMatch(subClass -> subClass.name.equalsIgnoreCase(specName))).collect(Collectors.toList()).get(0);
    }

}