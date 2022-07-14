package com.ebicep.warlords.classes.mage.specs;

import com.ebicep.warlords.abilties.*;
import com.ebicep.warlords.classes.mage.AbstractMage;

public class Aquamancer extends AbstractMage {
    public Aquamancer() {
        super("Aquamancer", 5200, 355, 20, 14, 0,
                new WaterBolt(),
                new WaterBreath(),
                new TimeWarp(),
                new ArcaneShield(),
                new HealingRain());
    }
}
