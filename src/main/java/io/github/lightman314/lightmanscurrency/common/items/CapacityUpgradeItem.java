package io.github.lightman314.lightmanscurrency.common.items;

import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity.CapacityUpgrade;

import javax.annotation.Nonnull;

public class CapacityUpgradeItem extends UpgradeItem{

	private final Supplier<Integer> capacityAmount;

	public CapacityUpgradeItem(CapacityUpgrade upgradeType, int capacityAmount, Properties properties) { this(upgradeType, () -> capacityAmount, properties); }
	
	public CapacityUpgradeItem(CapacityUpgrade upgradeType, Supplier<Integer> capacityAmount, Properties properties)
	{
		super(upgradeType, properties);
		this.capacityAmount = capacityAmount;
	}

	@Override
	public void setDefaultValues(@Nonnull UpgradeData.Mutable data) { data.setIntValue(CapacityUpgrade.CAPACITY, Math.max(this.capacityAmount.get(), 1)); }
	
}
