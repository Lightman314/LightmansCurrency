package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestMagnetUpgrade;

import java.util.function.Supplier;

public class MagnetUpgradeItem extends UpgradeItem{

	private final Supplier<Integer> radius;

	public MagnetUpgradeItem(Supplier<Integer> radius, Properties properties)
	{
		super(UpgradeType.COIN_CHEST_MAGNET, properties);
		this.radius = radius;
	}

	@Override
	public void fillUpgradeData(UpgradeData data) { data.setValue(CoinChestMagnetUpgrade.RANGE, Math.max(this.radius.get(), 1)); }
	
}
