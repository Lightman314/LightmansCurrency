package io.github.lightman314.lightmanscurrency.common.upgrades.types;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;

public class SpeedUpgrade extends UpgradeType {

	public static String DELAY_AMOUNT = "delay";
	
	@Nonnull
	@Override
	public List<Component> getTooltip(@Nonnull UpgradeData data)
	{
		return Lists.newArrayList(LCText.TOOLTIP_UPGRADE_SPEED.get(data.getIntValue(DELAY_AMOUNT)));
	}

	@Nonnull
	@Override
	protected List<Component> getBuiltInTargets() { return ImmutableList.of(LCText.TOOLTIP_UPGRADE_TARGET_TRADER_INTERFACE.get()); }

}
