package io.github.lightman314.lightmanscurrency.api.upgrades;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.common.items.UpgradeItem;
import io.github.lightman314.lightmanscurrency.common.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public abstract class UpgradeType {

	private final List<Component> possibleTargets = new ArrayList<>();

	public boolean isUnique() { return false; }

	public List<Component> getTooltip(UpgradeData data) { return new ArrayList<>(); }

	public List<Component> getTooltipWithContext(UpgradeData data, Item.TooltipContext context) { return new ArrayList<>(); }

	public boolean clearDataFromStack(ItemStack stack) { return false; }
    protected final boolean clearData(ItemStack stack, Supplier<? extends DataComponentType<?>> type)
	{
		if(stack.has(type))
		{
			stack.remove(type);
			return true;
		}
		return false;
	}
	
	public static boolean hasUpgrade(UpgradeType type, UpgradeStackHandler container) {
		for(int i = 0; i < container.getSlots(); ++i)
		{
			ItemStack stack = container.getStackInSlot(i);
			if(stack.getItem() instanceof UpgradeItem upgradeItem)
			{
				if(upgradeItem.getUpgradeType() == type)
					return true;
			}
		}
		return false;
	}

	public final void addTarget(Component target) { this.possibleTargets.add(target); }
	public final void addTarget(ItemLike target) { this.addTarget(formatTarget(target)); }
	public final void addTarget(Supplier<? extends ItemLike> target) { this.addTarget(formatTarget(target)); }

	protected static Component formatTarget(ItemLike target) { return new ItemStack(target).getHoverName(); }
	protected static Component formatTarget(Supplier<? extends ItemLike> target) { return formatTarget(target.get()); }

	
	public final List<Component> getPossibleTargets() {
		List<Component> temp = new ArrayList<>();
		temp.addAll(this.getBuiltInTargets());
		temp.addAll(this.possibleTargets);
		return ImmutableList.copyOf(temp);
	}

	
	protected List<Component> getBuiltInTargets() { return new ArrayList<>(); }
	
	public static class Simple extends UpgradeType {

		private final boolean unique;
		private final Supplier<List<Component>> tooltips;
		private final List<Component> targets;
		private final List<OptionalTooltip> optionalTooltips;

		@Override
		public boolean isUnique() { return this.unique; }

		@Deprecated
		public Simple(Component... tooltips) { this(false,tooltips); }
		@Deprecated
		public Simple(boolean unique, Component... tooltips) {
			this.unique = unique;
			this.tooltips = () -> ImmutableList.copyOf(tooltips);
			this.targets = new ArrayList<>();
			this.optionalTooltips = ImmutableList.of();
		}
		private Simple(Builder builder)
		{
			this.unique = builder.unique;
			this.tooltips = builder.buildSupplier();
			this.targets = ImmutableList.copyOf(builder.targets);
			this.optionalTooltips = ImmutableList.copyOf(builder.optionalTooltips);
		}

		
		@Override
		public List<Component> getTooltip(UpgradeData data) { return this.tooltips.get(); }

		
		@Override
		public List<Component> getTooltipWithContext(UpgradeData data, Item.TooltipContext context) {
			List<Component> tooltips = new ArrayList<>();
			for(OptionalTooltip ot : this.optionalTooltips)
			{
				if(ot.shouldShow.test(context))
					tooltips.addAll(ot.tooltip);
			}
			return tooltips;
		}

		
		@Override
		protected List<Component> getBuiltInTargets() { return this.targets; }

		@Deprecated
		public final Simple withTarget(Component target) { this.targets.add(target); return this; }
		@Deprecated
		public final Simple withTarget(ItemLike target) { this.targets.add(formatTarget(target)); return this; }
		@Deprecated
		public final Simple withTarget(Supplier<? extends ItemLike> target) { this.targets.add(formatTarget(target)); return this; }

		
		public static Builder builder() { return new Builder(); }

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

			public Builder optionalTooltip(Predicate<Item.TooltipContext> shouldDisplay, Component tooltip) { return this.optionalTooltip(shouldDisplay,ImmutableList.of(tooltip)); }
			public Builder optionalTooltip(Predicate<Item.TooltipContext> shouldDisplay, TextEntry tooltip) { return this.optionalTooltip(shouldDisplay,ImmutableList.of(tooltip.get())); }
			public Builder optionalTooltip(Predicate<Item.TooltipContext> shouldDisplay, List<Component> tooltip) { this.optionalTooltips.add(new OptionalTooltip(shouldDisplay,tooltip)); return this; }

			public Builder optionalTooltip(Supplier<Boolean> shouldDisplay, Component tooltip) { return this.optionalTooltip(shouldDisplay,ImmutableList.of(tooltip)); }
			public Builder optionalTooltip(Supplier<Boolean> shouldDisplay, TextEntry tooltip) { return this.optionalTooltip(shouldDisplay,ImmutableList.of(tooltip.get())); }
			public Builder optionalTooltip(Supplier<Boolean> shouldDisplay, List<Component> tooltip) { this.optionalTooltips.add(new OptionalTooltip(c -> shouldDisplay.get(),tooltip)); return this; }

			public Simple build() { return new Simple(this); }

		}

		private record OptionalTooltip(Predicate<Item.TooltipContext> shouldShow, List<Component> tooltip) { }

	}


	
}
