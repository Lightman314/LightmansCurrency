package io.github.lightman314.lightmanscurrency.common.upgrades.types;

import java.util.List;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import net.minecraft.network.chat.Component;

public class SpeedUpgrade extends UpgradeType {

	public static String DELAY_AMOUNT = "delay";
	private static final List<String> DATA_TAGS = Lists.newArrayList(DELAY_AMOUNT);

	@Override
	protected List<String> getDataTags() {
		return DATA_TAGS;
	}

	@Override
	protected Object defaultTagValue(String tag) {
		if(tag == DELAY_AMOUNT)
			return 1;
		return null;
	}
	
	@Override
	public List<Component> getTooltip(UpgradeData data)
	{
		return Lists.newArrayList(Component.translatable("tooltip.lightmanscurrency.upgrade.speed", data.getIntValue(DELAY_AMOUNT)));
	}
	
}
