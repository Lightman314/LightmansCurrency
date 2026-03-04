package io.github.lightman314.lightmanscurrency.api.upgrades.types;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeStackHandler;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;

public abstract class CapacityUpgrade extends UpgradeType {

	public static String CAPACITY = "capacity";

    public static int getBonusCapacity(UpgradeStackHandler upgrades,CapacityUpgrade upgrade)
    {
        int total = 0;
        for(var entry : upgrades)
        {
            if(entry.getFirst() == upgrade)
                total += entry.getSecond().getIntValue(CAPACITY);
        }
        return total;
    }

}
