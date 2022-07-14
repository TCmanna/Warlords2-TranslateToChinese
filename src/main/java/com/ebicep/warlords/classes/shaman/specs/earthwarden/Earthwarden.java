package com.ebicep.warlords.classes.shaman.specs.earthwarden;

import com.ebicep.warlords.abilties.*;
import com.ebicep.warlords.classes.shaman.AbstractShaman;

public class Earthwarden extends AbstractShaman {

    public Earthwarden() {
        super("Earthwarden", 5530, 355, 10,
                new EarthenSpike(),
                new Boulder(),
                new Earthliving(),
                new ChainHeal(),
                new HealingTotem()
        );
    }

}