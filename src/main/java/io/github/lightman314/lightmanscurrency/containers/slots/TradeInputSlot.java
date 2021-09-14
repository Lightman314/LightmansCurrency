package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.ItemTradeData;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TradeInputSlot extends Slot{
	
	ItemTradeData trade;
	final Entity player;
	
	public TradeInputSlot(Container inventory, int index, int x, int y, ItemTradeData trade, Entity player)
	{
		super(inventory, index, x, y);
		this.trade = trade;
		this.player = player;
		
		this.setBackground();
	}
	
	public void updateTrade(ItemTradeData trade)
	{
		this.trade = trade;
	}
	
	@Override
	public boolean mayPlace(ItemStack stack) {
		return false;
	}
	
	public boolean isTradeItemValid(ItemStack stack)
	{
		if(this.trade.getRestriction() == ItemTradeData.TradeRestrictions.ARMOR_HEAD)
			return stack.canEquip(EquipmentSlot.HEAD, this.player);
		else if(this.trade.getRestriction() == ItemTradeData.TradeRestrictions.ARMOR_CHEST)
			return stack.canEquip(EquipmentSlot.CHEST, this.player);
		else if(this.trade.getRestriction() == ItemTradeData.TradeRestrictions.ARMOR_LEGS)
			return stack.canEquip(EquipmentSlot.LEGS, this.player);
		else if(this.trade.getRestriction() == ItemTradeData.TradeRestrictions.ARMOR_FEET)
			return stack.canEquip(EquipmentSlot.FEET, this.player);
		
        return true;
	}
	
	@Override
	public boolean mayPickup(Player player)
	{
		return false;
	}
	
	private void setBackground()
	{
		if(!this.player.level.isClientSide)
			return;
		if(this.trade.getRestriction() == ItemTradeData.TradeRestrictions.ARMOR_HEAD)
			this.setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_HELMET);
		else if(this.trade.getRestriction() == ItemTradeData.TradeRestrictions.ARMOR_CHEST)
			this.setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE);
		else if(this.trade.getRestriction() == ItemTradeData.TradeRestrictions.ARMOR_LEGS)
			this.setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS);
		else if(this.trade.getRestriction() == ItemTradeData.TradeRestrictions.ARMOR_FEET)
			this.setBackground(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS);
	}
	
	@OnlyIn(Dist.CLIENT)
	public boolean isMouseOver(int mouseX, int mouseY, int guiLeft, int guiTop)
	{
		int startX = this.x + guiLeft;
		int startY = this.y + guiTop;
		return (mouseX >= startX && mouseX < startX + 16) && (mouseY >= startY && mouseY < startY + 16);
	}

}
