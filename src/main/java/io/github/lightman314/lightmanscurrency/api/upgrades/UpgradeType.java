package io.github.lightman314.lightmanscurrency.api.upgrades;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public abstract class UpgradeType {

	private final List<Component> possibleTargets = new ArrayList<>();

	public boolean isUnique() { return false; }

	@Nonnull
	public List<Component> getTooltip(@Nonnull UpgradeData data) { return new ArrayList<>(); }

	public boolean clearDataFromStack(@Nonnull ItemStack stack) { return false; }
	protected final boolean clearData(@Nonnull ItemStack stack, @Nonnull String... tagKeys)
	{
		if(stack.hasTag())
		{
			CompoundTag tag = stack.getTag();
			boolean flag = false;
			for(String key : tagKeys)
			{
				if(tag.contains(key))
				{
					tag.remove(key);
					flag = true;
				}
			}
			return flag;
		}
		return false;
	}

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

	public final void addTarget(@Nonnull Component target) { this.possibleTargets.add(target); }
	public final void addTarget(@Nonnull ItemLike target) { this.addTarget(formatTarget(target)); }
	public final void addTarget(@Nonnull RegistryObject<? extends ItemLike> target) { this.addTarget(formatTarget(target)); }

	protected static Component formatTarget(@Nonnull ItemLike target) { return new ItemStack(target).getHoverName(); }
	protected static Component formatTarget(@Nonnull RegistryObject<? extends ItemLike> target) { return formatTarget(target.get()); }

	@Nonnull
	public final List<Component> getPossibleTargets() {
		List<Component> temp = new ArrayList<>();
		temp.addAll(this.getBuiltInTargets());
		temp.addAll(this.possibleTargets);
		return ImmutableList.copyOf(temp);
	}

	@Nonnull
	protected List<Component> getBuiltInTargets() { return new ArrayList<>(); }
	
	public static class Simple extends UpgradeType {

		private final boolean unique;
		private final List<Component> targets = new ArrayList<>();

		@Override
		public boolean isUnique() { return this.unique; }

		private final List<Component> tooltips;
		public Simple(@Nonnull Component... tooltips) { this(false,tooltips); }
		public Simple(boolean unique, @Nonnull Component... tooltips) { this.unique = unique; this.tooltips = ImmutableList.copyOf(tooltips); }
		
		@Nonnull
		@Override
		public List<Component> getTooltip(@Nonnull UpgradeData data) { return this.tooltips; }

		@Nonnull
		@Override
		protected List<Component> getBuiltInTargets() { return this.targets; }

		public final Simple withTarget(@Nonnull Component target) { this.targets.add(target); return this; }
		public final Simple withTarget(@Nonnull ItemLike target) { this.targets.add(formatTarget(target)); return this; }
		public final Simple withTarget(@Nonnull RegistryObject<? extends ItemLike> target) { this.targets.add(formatTarget(target)); return this; }

	}
	
}
