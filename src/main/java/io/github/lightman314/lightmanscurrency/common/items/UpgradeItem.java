package io.github.lightman314.lightmanscurrency.common.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType.IUpgradeItem;
import io.github.lightman314.lightmanscurrency.common.upgrades.UpgradeType.UpgradeData;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants;

public abstract class UpgradeItem extends Item implements IUpgradeItem{

	protected final UpgradeType upgradeType;
	private boolean addTooltips = true;
	Function<UpgradeData,List<ITextComponent>> customTooltips = null;
	
	public UpgradeItem(UpgradeType upgradeType, Properties properties)
	{
		super(properties);
		this.upgradeType = upgradeType;
	}
	
	public final boolean addsTooltips() { return this.addTooltips; }
	protected final void ignoreTooltips() { this.addTooltips = false; }
	protected final void setCustomTooltips(Function<UpgradeData,List<ITextComponent>> customTooltips) { this.customTooltips = customTooltips; }
	
	@Override
	public UpgradeType getUpgradeType() { return this.upgradeType; }
	
	@Override
	public UpgradeData getDefaultUpgradeData()
	{
		UpgradeData data = this.upgradeType.getDefaultData();
		this.fillUpgradeData(data);
		return data;
	}
	
	protected abstract void fillUpgradeData(UpgradeData data);
	
	public static UpgradeData getUpgradeData(ItemStack stack)
	{
		if(stack.getItem() instanceof UpgradeItem)
		{
			UpgradeData data = ((UpgradeItem)stack.getItem()).getDefaultUpgradeData();
			if(stack.hasTag())
			{
				CompoundNBT tag = stack.getTag();
				if(tag.contains("UpgradeData", Constants.NBT.TAG_COMPOUND))
					data.read(tag.getCompound("UpgradeData"));
			}
			return data;
		}
		return UpgradeData.EMPTY;
	}
	
	public static void setUpgradeData(ItemStack stack, UpgradeData data)
	{
		if(stack.getItem() instanceof UpgradeItem)
		{
			UpgradeItem upgradeItem = (UpgradeItem)stack.getItem();
			CompoundNBT tag = stack.getOrCreateTag();
			tag.put("UpgradeData",  data.writeToNBT(upgradeItem.upgradeType));
		}
		else
		{
			CompoundNBT tag = stack.getOrCreateTag();
			tag.put("UpgradeData", data.writeToNBT());
		}
	}
	
	public static List<ITextComponent> getUpgradeTooltip(ItemStack stack) { return getUpgradeTooltip(stack, false); }
	
	public static List<ITextComponent> getUpgradeTooltip(ItemStack stack, boolean forceCollection)
	{
		if(stack.getItem() instanceof UpgradeItem)
		{
			UpgradeItem item = (UpgradeItem)stack.getItem();
			if(!item.addTooltips && !forceCollection) //Block if tooltips have been blocked
				return Lists.newArrayList();
			UpgradeType type = item.getUpgradeType();
			UpgradeData data = getUpgradeData(stack);
			if(item.customTooltips != null)
				return item.customTooltips.apply(data);
			return type.getTooltip(data);
		}
		return Lists.newArrayList();
	}
	
	@Override
	public void appendHoverText(@Nonnull ItemStack stack, @Nullable World level, @Nonnull List<ITextComponent> tooltip, @Nonnull ITooltipFlag flagIn)
	{
		//Add upgrade tooltips
		List<ITextComponent> upgradeTooltips = getUpgradeTooltip(stack);
		if(upgradeTooltips != null)
			tooltip.addAll(upgradeTooltips);
		
		super.appendHoverText(stack, level, tooltip, flagIn);
		
	}
	
	public static class Simple extends UpgradeItem
	{
		public Simple(UpgradeType upgradeType, Properties properties) { super(upgradeType, properties); }
		@Override
		protected void fillUpgradeData(UpgradeData data) { }
	}
	
}
