package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;

public class SimpleUpgradeItem extends UpgradeItem {
    public SimpleUpgradeItem(UpgradeType upgradeType, Properties properties) {
        super(upgradeType, properties);
    }

    @Override
    public void setDefaultValues(UpgradeData.Mutable data) {
    }
}
