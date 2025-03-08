package io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class ItemCapacityUpgrade extends CapacityUpgrade {
	
	@Nonnull
	@Override
	public List<Component> getTooltip(@Nonnull UpgradeData data)
	{
		return Lists.newArrayList(LCText.TOOLTIP_UPGRADE_ITEM_CAPACITY.get(NumberUtil.GetPrettyString(data.getIntValue(CapacityUpgrade.CAPACITY))));
	}

	@Nonnull
	@Override
	protected List<Component> getBuiltInTargets() { return ImmutableList.of(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_ITEM.get(), formatTarget(ModBlocks.SLOT_MACHINE), LCText.TOOLTIP_UPGRADE_TARGET_TRADER_GACHA_MACHINE.get(), formatTarget(ModBlocks.ITEM_TRADER_INTERFACE)); }

}
