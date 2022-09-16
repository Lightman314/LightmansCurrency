package io.github.lightman314.lightmanscurrency.upgrades.types.capacity;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;

public class ItemCapacityUpgrade extends CapacityUpgrade {
	
	@Override
	public List<Component> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(Component.translatable("tooltip.lightmanscurrency.upgrade.item_capacity", data.getIntValue(CapacityUpgrade.CAPACITY)));
	}

}
