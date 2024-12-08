package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.InteractionUpgrade;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class InteractionUpgradeItem extends UpgradeItem {

    private final Supplier<Integer> interactionCount;

    public InteractionUpgradeItem(Properties properties, Supplier<Integer> interactionCount) {
        super(Upgrades.INTERACTION, properties);
        this.interactionCount = interactionCount;
    }

    @Override
    public void setDefaultValues(@Nonnull UpgradeData.Mutable data) {
        data.setIntValue(InteractionUpgrade.INTERACTIONS,this.interactionCount.get());
    }
}