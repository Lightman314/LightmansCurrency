package io.github.lightman314.lightmanscurrency.containers.slots;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.tradedata.ItemTradeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.PlayerContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TradeInputSlot extends Slot{
	
	ItemTradeData trade;
	final Entity player;
	
	public TradeInputSlot(IInventory inventory, int index, int x, int y, ItemTradeData trade, Entity player)
	{
		super(inventory, index, x, y);
		this.trade = trade;
		this.player = player;
	}
	
	public void updateTrade(ItemTradeData trade)
	{
		this.trade = trade;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
	
	public boolean isTradeItemValid(ItemStack stack)
	{
		if(this.trade.getRestriction() == ItemTradeData.ItemTradeRestrictions.ARMOR_HEAD)
			return stack.canEquip(EquipmentSlotType.HEAD, this.player);
		else if(this.trade.getRestriction() == ItemTradeData.ItemTradeRestrictions.ARMOR_CHEST)
			return stack.canEquip(EquipmentSlotType.CHEST, this.player);
		else if(this.trade.getRestriction() == ItemTradeData.ItemTradeRestrictions.ARMOR_LEGS)
			return stack.canEquip(EquipmentSlotType.LEGS, this.player);
		else if(this.trade.getRestriction() == ItemTradeData.ItemTradeRestrictions.ARMOR_FEET)
			return stack.canEquip(EquipmentSlotType.FEET, this.player);
		
        return true;
	}
	
	@Override
	public boolean canTakeStack(PlayerEntity player)
	{
		return false;
	}
	
	@Override
	public ItemStack decrStackSize(int amount)
	{
		//Return nothing, as nothing can be taken
		return ItemStack.EMPTY;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public Pair<ResourceLocation, ResourceLocation> getBackground() {
		if(this.trade.getRestriction() == ItemTradeData.ItemTradeRestrictions.ARMOR_HEAD)
			return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_HELMET);
		else if(this.trade.getRestriction() == ItemTradeData.ItemTradeRestrictions.ARMOR_CHEST)
			return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_CHESTPLATE);
		else if(this.trade.getRestriction() == ItemTradeData.ItemTradeRestrictions.ARMOR_LEGS)
			return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_LEGGINGS);
		else if(this.trade.getRestriction() == ItemTradeData.ItemTradeRestrictions.ARMOR_FEET)
			return Pair.of(PlayerContainer.LOCATION_BLOCKS_TEXTURE, PlayerContainer.EMPTY_ARMOR_SLOT_BOOTS);
       return super.getBackground();
    }
	
	@OnlyIn(Dist.CLIENT)
	public boolean isMouseOver(int mouseX, int mouseY, int guiLeft, int guiTop)
	{
		int startX = this.xPos + guiLeft;
		int startY = this.yPos + guiTop;
		return (mouseX >= startX && mouseX < startX + 16) && (mouseY >= startY && mouseY < startY + 16);
	}

}
