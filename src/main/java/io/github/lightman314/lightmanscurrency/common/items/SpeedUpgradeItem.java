package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.SpeedUpgrade;

import javax.annotation.Nonnull;

public class SpeedUpgradeItem extends UpgradeItem{

	private final int delayAmount;
	
	public SpeedUpgradeItem(int delayAmount, Properties properties)
	{
		super(Upgrades.SPEED, properties);
		this.delayAmount = delayAmount;
	}

	@Override
	public void setDefaultValues(@Nonnull UpgradeData.Mutable data) {
		data.setIntValue(SpeedUpgrade.DELAY_AMOUNT,Math.max(this.delayAmount,1));
	}
	
}
