package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestMagnetUpgrade;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

public class MagnetUpgradeItem extends UpgradeItem{

	private final Supplier<Integer> radius;

	public MagnetUpgradeItem(Supplier<Integer> radius, Properties properties)
	{
		super(Upgrades.COIN_CHEST_MAGNET, properties);
		this.radius = radius;
	}

	@Override
	public void setDefaultValues(@Nonnull UpgradeData.Mutable data) { data.setIntValue(CoinChestMagnetUpgrade.RANGE, Math.max(this.radius.get(), 1)); }
	
}
