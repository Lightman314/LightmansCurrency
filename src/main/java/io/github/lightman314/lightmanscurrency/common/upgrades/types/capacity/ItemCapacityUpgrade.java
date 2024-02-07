package io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class ItemCapacityUpgrade extends CapacityUpgrade {
	
	@Nonnull
	@Override
	public List<Component> getTooltip(@Nonnull UpgradeData data)
	{
		return Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.upgrade.item_capacity", data.getIntValue(CapacityUpgrade.CAPACITY)));
	}

}
