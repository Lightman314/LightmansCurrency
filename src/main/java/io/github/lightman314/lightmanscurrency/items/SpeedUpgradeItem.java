package io.github.lightman314.lightmanscurrency.items;

import io.github.lightman314.lightmanscurrency.upgrades.SpeedUpgrade;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.upgrades.UpgradeType.UpgradeData;

public class SpeedUpgradeItem extends UpgradeItem{

	private final int delayAmount;
	
	public SpeedUpgradeItem(int delayAmount, Properties properties)
	{
		super(UpgradeType.SPEED, properties);
		this.delayAmount = delayAmount;
	}

	@Override
	public void fillUpgradeData(UpgradeData data) {
		data.setValue(SpeedUpgrade.DELAY_AMOUNT, Math.max(this.delayAmount, 1));
	}
	
}
