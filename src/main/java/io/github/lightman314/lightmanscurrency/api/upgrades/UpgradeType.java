package io.github.lightman314.lightmanscurrency.api.upgrades;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;

public abstract class UpgradeType {

	private final List<Component> possibleTargets = new ArrayList<>();

	public boolean isUnique() { return false; }

	@Nonnull
	public List<Component> getTooltip(@Nonnull UpgradeData data) { return new ArrayList<>(); }

	@Nonnull
	public List<Component> getTooltipWithContext(@Nonnull UpgradeData data, @Nullable Level context) { return new ArrayList<>(); }

	public boolean clearDataFromStack(@Nonnull ItemStack stack) { return false; }
	protected final boolean clearData(@Nonnull ItemStack stack, @Nonnull String... tagKey)
	{
		CompoundTag tag = stack.getTag();
		if(tag != null)
		{
			boolean found = false;
			for(String key : tagKey)
			{
				if(tag.contains(key))
				{
					tag.remove(key);
					found = true;
				}
			}
			return found;
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
	public final void addTarget(@Nonnull Supplier<? extends ItemLike> target) { this.addTarget(formatTarget(target)); }

	protected static Component formatTarget(@Nonnull ItemLike target) { return new ItemStack(target).getHoverName(); }
	protected static Component formatTarget(@Nonnull Supplier<? extends ItemLike> target) { return formatTarget(target.get()); }

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
		private final Supplier<List<Component>> tooltips;
		private final List<Component> targets;
		private final List<OptionalTooltip> optionalTooltips;

		@Override
		public boolean isUnique() { return this.unique; }

		private Simple(@Nonnull Builder builder)
		{
			this.unique = builder.unique;
			this.tooltips = builder.buildSupplier();
			this.targets = ImmutableList.copyOf(builder.targets);
			this.optionalTooltips = ImmutableList.copyOf(builder.optionalTooltips);
		}

		@Nonnull
		@Override
		public List<Component> getTooltip(@Nonnull UpgradeData data) { return this.tooltips.get(); }

		@Nonnull
		@Override
		public List<Component> getTooltipWithContext(@Nonnull UpgradeData data, @Nullable Level context) {
			List<Component> tooltips = new ArrayList<>();
			for(OptionalTooltip ot : this.optionalTooltips)
			{
				if(ot.shouldShow.test(context))
					tooltips.addAll(ot.tooltip);
			}
			return tooltips;
		}

		@Nonnull
		@Override
		protected List<Component> getBuiltInTargets() { return this.targets; }

		@Nonnull
		public static Builder builder() { return new Builder(); }

		@MethodsReturnNonnullByDefault
		@ParametersAreNonnullByDefault
		public static class Builder
		{

			private Builder() {}

			private final List<Component> tooltips = new ArrayList<>();
			private final List<Supplier<List<Component>>> flexibleTooltips = new ArrayList<>();
			private Supplier<List<Component>> buildSupplier() {
				return () -> {
					List<Component> list = new ArrayList<>(this.tooltips);
					for(var supplier : this.flexibleTooltips)
						list.addAll(supplier.get());
					return list;
				};
			}
			private final List<OptionalTooltip> optionalTooltips = new ArrayList<>();
			private final List<Component> targets = new ArrayList<>();
			private boolean unique = false;

			public Builder unique() { this.unique = true; return this; }
			public Builder tooltip(Component tooltip) { this.tooltips.add(tooltip); return this; }
			public Builder tooltip(TextEntry entry) { this.tooltips.add(entry.get()); return this; }
			public Builder tooltip(List<Component> tooltips) { this.tooltips.addAll(tooltips); return this; }
			public Builder tooltip(MultiLineTextEntry tooltips) { this.flexibleTooltips.add(tooltips::get); return this; }
			public Builder tooltip(Supplier<List<Component>> tooltipSource) { this.flexibleTooltips.add(tooltipSource); return this; }

			public Builder target(Component target) { this.targets.add(target); return this; }
			public Builder target(TextEntry target) { this.targets.add(target.get()); return this; }
			public Builder target(ItemLike target) { this.targets.add(formatTarget(target)); return this; }
			public Builder target(Supplier<? extends ItemLike> target) { this.targets.add(formatTarget(target)); return this; }

			public Builder optionalTooltip(Predicate<Level> shouldDisplay, Component tooltip) { return this.optionalTooltip(shouldDisplay,ImmutableList.of(tooltip)); }
			public Builder optionalTooltip(Predicate<Level> shouldDisplay, TextEntry tooltip) { return this.optionalTooltip(shouldDisplay,ImmutableList.of(tooltip.get())); }
			public Builder optionalTooltip(Predicate<Level> shouldDisplay, List<Component> tooltip) { this.optionalTooltips.add(new OptionalTooltip(shouldDisplay,tooltip)); return this; }

			public Builder optionalTooltip(Supplier<Boolean> shouldDisplay, Component tooltip) { return this.optionalTooltip(shouldDisplay,ImmutableList.of(tooltip)); }
			public Builder optionalTooltip(Supplier<Boolean> shouldDisplay, TextEntry tooltip) { return this.optionalTooltip(shouldDisplay,ImmutableList.of(tooltip.get())); }
			public Builder optionalTooltip(Supplier<Boolean> shouldDisplay, List<Component> tooltip) { this.optionalTooltips.add(new OptionalTooltip(c -> shouldDisplay.get(),tooltip)); return this; }

			public Simple build() { return new Simple(this); }

		}

		private record OptionalTooltip(@Nonnull Predicate<Level> shouldShow, @Nonnull List<Component> tooltip) { }

	}



}