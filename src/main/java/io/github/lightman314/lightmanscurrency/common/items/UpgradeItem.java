package io.github.lightman314.lightmanscurrency.common.items;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.api.upgrades.IUpgradeItem;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeData;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public abstract class UpgradeItem extends Item implements IUpgradeItem{

	protected final UpgradeType upgradeType;
	private boolean addTooltips = true;
	Function<UpgradeData,List<Component>> customTooltips = null;
	
	public UpgradeItem(UpgradeType upgradeType, Properties properties)
	{
		super(properties);
		this.upgradeType = upgradeType;
	}

	@Nonnull
	@Override
	public InteractionResultHolder<ItemStack> use(@Nonnull Level level, @Nonnull Player player, @Nonnull InteractionHand hand) {
		//Delete stored override data if crouching + right-click
		ItemStack stack = player.getItemInHand(hand);
		if(stack.hasTag() && player.isCrouching())
		{
			boolean removed = false;
			CompoundTag tag = stack.getTag();
			if(tag.contains("UpgradeData"))
			{
				tag.remove("UpgradeData");
				removed = true;
			}
			if(tag.contains("Active"))
			{
				tag.remove("Active");
				removed = true;
			}
			removed = removed || this.upgradeType.clearDataFromStack(stack);
			if(removed)
			{
				level.playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.1F, (level.random.nextFloat() - level.random.nextFloat()) * 0.35F + 0.9F, false);
				return InteractionResultHolder.success(stack);
			}
		}
		return super.use(level, player, hand);
	}

	public final boolean addsTooltips() { return this.addTooltips; }
	protected final void ignoreTooltips() { this.addTooltips = false; }
	protected final void setCustomTooltips(Function<UpgradeData,List<Component>> customTooltips) { this.customTooltips = customTooltips; }

	@Nonnull
	@Override
	public UpgradeType getUpgradeType() { return this.upgradeType; }
	
	public abstract void setDefaultValues(@Nonnull UpgradeData.Mutable data);

	@Nonnull
	public static UpgradeData getUpgradeData(@Nonnull ItemStack stack)
	{
		if(stack.getItem() instanceof UpgradeItem upgrade)
		{
			UpgradeData.Mutable data = UpgradeData.EMPTY.makeMutable();
			upgrade.setDefaultValues(data);
			if(stack.hasTag())
			{
				CompoundTag tag = stack.getTag();
				if(tag.contains("UpgradeData", Tag.TAG_COMPOUND))
				{
					UpgradeData tagData = UpgradeData.parse(tag.getCompound("UpgradeData"));
					data.merge(tagData);
				}
			}
			return data.makeImmutable();
		}
		return UpgradeData.EMPTY;
	}

	public static void setUpgradeData(@Nonnull ItemStack stack, @Nonnull UpgradeData data)
	{
		if(stack.getItem() instanceof UpgradeItem upgradeItem)
		{
			CompoundTag tag = stack.getOrCreateTag();
			tag.put("UpgradeData",data.save());
		}
	}
	
	public static List<Component> getUpgradeTooltip(ItemStack stack)
	{
		try {
			return getUpgradeTooltip(stack, false);
		} catch (Throwable ignored) {}
		return new ArrayList<>();
	}
	
	public static List<Component> getUpgradeTooltip(ItemStack stack, boolean forceCollection)
	{
		if(stack.getItem() instanceof UpgradeItem item)
		{
			if(!item.addTooltips && !forceCollection) //Block if tooltips have been blocked
				return Lists.newArrayList();
			UpgradeType type = item.getUpgradeType();
			UpgradeData data = getUpgradeData(stack);
			if(item.customTooltips != null)
				return item.customTooltips.apply(data);
			//Get initial list and force it to be editable, as the UpgradeType#getTooltip may return an immutable list
            List<Component> tooltip = new ArrayList<>(type.getTooltip(data));
			if(type.isUnique())
				tooltip.add(LCText.TOOLTIP_UPGRADE_UNIQUE.getWithStyle(ChatFormatting.BOLD,ChatFormatting.GOLD));
			List<Component> targets = type.getPossibleTargets();
			if(!targets.isEmpty())
			{
				tooltip.add(LCText.TOOLTIP_UPGRADE_TARGETS.getWithStyle(ChatFormatting.GRAY));
				for(Component target : targets)
				{
					MutableComponent mc = EasyText.makeMutable(target);
					tooltip.add(mc.withStyle(ChatFormatting.GRAY));
				}
			}
			return tooltip;
		}
		return Lists.newArrayList();
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable Level level, @Nonnull List<Component> tooltip, @Nonnull TooltipFlag flagIn)
	{
		//Add upgrade tooltips
		List<Component> upgradeTooltips = getUpgradeTooltip(stack);
		if(upgradeTooltips != null)
			tooltip.addAll(upgradeTooltips);
		
		super.appendHoverText(stack, level, tooltip, flagIn);
		
	}
	
	public static class Simple extends UpgradeItem
	{
		public Simple(UpgradeType upgradeType, Properties properties) { super(upgradeType, properties); }
		@Override
		public void setDefaultValues(@Nonnull UpgradeData.Mutable data) { }
	}

	public static boolean noUniqueConflicts(@Nonnull UpgradeItem item, @Nonnull Container container)
	{
		UpgradeType type = item.getUpgradeType();
		if(type.isUnique())
		{
			for(int i = 0; i < container.getContainerSize(); ++i)
			{
				ItemStack stack = container.getItem(i);
				if(!stack.isEmpty() && stack.getItem() instanceof UpgradeItem otherItem)
				{
					if(otherItem.getUpgradeType() == type)
						return false;
				}
			}
		}
		return true;
	}
	
}
