package io.github.lightman314.lightmanscurrency.upgrades;

import java.util.List;

import com.google.common.collect.Lists;

public abstract class CapacityUpgrade extends UpgradeType{

	public static String CAPACITY = "capacity"; 
	private static final List<String> DATA_TAGS = Lists.newArrayList(CAPACITY);

	@Override
	protected List<String> getDataTags() {
		return DATA_TAGS;
	}

	@Override
	protected Object defaultTagValue(String tag) {
		if(tag == CAPACITY)
			return 1;
		return null;
	}
	
}
