package io.github.lightman314.lightmanscurrency.common.upgrades.types.capacity;

import java.util.List;
import java.util.Objects;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;

public abstract class CapacityUpgrade extends UpgradeType {

	public static String CAPACITY = "capacity"; 
	private static final List<String> DATA_TAGS = Lists.newArrayList(CAPACITY);

	@Override
	protected List<String> getDataTags() { return DATA_TAGS; }

	@Override
	protected Object defaultTagValue(String tag) {
		if(Objects.equals(tag, CAPACITY))
			return 1;
		return null;
	}

	
	
}
