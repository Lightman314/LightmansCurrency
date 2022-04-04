package io.github.lightman314.lightmanscurrency.upgrades;

import java.util.List;

import com.google.common.collect.Lists;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public class ItemCapacityUpgrade extends CapacityUpgrade {
	
	@Override
	public List<Component> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(new TranslatableComponent("tooltip.lightmanscurrency.upgrade.item_capacity", data.getIntValue(CapacityUpgrade.CAPACITY)));
	}

}
