package io.github.lightman314.lightmanscurrency.api.upgrades;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public abstract class UpgradeType {

	@Nonnull
	protected abstract List<String> getDataTags();
	@Nullable
	protected abstract Object defaultTagValue(String tag);
	@Nonnull
	public List<Component> getTooltip(@Nonnull UpgradeData data) { return new ArrayList<>(); }
	@Nonnull
	public final UpgradeData getDefaultData() { return new UpgradeData(this); }

	public boolean clearDataFromStack(@Nonnull CompoundTag itemTag) { return false; }
	
	public static boolean hasUpgrade(@Nonnull UpgradeType type, @Nonnull Container upgradeContainer) {
		for(int i = 0; i < upgradeContainer.getContainerSize(); ++i)
		{
			ItemStack stack = upgradeContainer.getItem(i);
			if(stack.getItem() instanceof UpgradeItem upgradeItem)
			{
				if(upgradeItem.getUpgradeType() == type)
					return true;
			}
		}
		return false;
	}
	
	public static class Simple extends UpgradeType {

		private final List<Component> tooltips;
		public Simple(@Nonnull Component... tooltips) { this.tooltips = ImmutableList.copyOf(tooltips); }
		
		@Nonnull
		@Override
		protected List<String> getDataTags() { return new ArrayList<>(); }

		@Override
		protected Object defaultTagValue(String tag) { return null; }
		
		@Nonnull
		@Override
		public List<Component> getTooltip(@Nonnull UpgradeData data) { return this.tooltips; }
		
	}
	
}
