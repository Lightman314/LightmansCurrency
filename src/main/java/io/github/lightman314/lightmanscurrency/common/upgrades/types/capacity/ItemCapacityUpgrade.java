package io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.util.text.ITextComponent;

public class ItemCapacityUpgrade extends CapacityUpgrade {
	
	@Override
	public List<ITextComponent> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(EasyText.translatable("tooltip.lightmanscurrency.upgrade.item_capacity", data.getIntValue(CapacityUpgrade.CAPACITY)));
	}

}
