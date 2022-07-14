package com.ebicep.warlords.classes.paladin.specs;

import com.ebicep.warlords.abilties.*;
import com.ebicep.warlords.classes.paladin.AbstractPaladin;

public class Avenger extends AbstractPaladin {

    public Avenger() {
        super("Avenger", 6300, 305, 0,
                new AvengersStrike(),
                new Consecrate(158.4f, 213.6f, 50, 20, 175, 20, 5),
                new LightInfusion(15.66f, -120),
                new HolyRadianceAvenger(582, 760, 19.57f, 20, 15, 175),
                new AvengersWrath());
    }
}
