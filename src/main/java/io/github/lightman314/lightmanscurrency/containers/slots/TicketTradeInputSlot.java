package io.github.lightman314.lightmanscurrency.containers.slots;

import io.github.lightman314.lightmanscurrency.core.ModItems;
import io.github.lightman314.lightmanscurrency.tradedata.TicketTradeData;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TicketTradeInputSlot extends Slot{
	
	TicketTradeData trade;
	final Entity player;
	
	public TicketTradeInputSlot(IInventory inventory, int index, int x, int y, TicketTradeData trade, Entity player)
	{
		super(inventory, index, x, y);
		this.trade = trade;
		this.player = player;
	}
	
	public void updateTrade(TicketTradeData trade)
	{
		this.trade = trade;
	}
	
	@Override
	public boolean isItemValid(ItemStack stack) {
		return false;
	}
	
	public boolean isTradeItemValid(ItemStack stack)
	{
        return stack.isEmpty() || stack.getItem() == ModItems.TICKET_MASTER;
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
	public boolean isMouseOver(int mouseX, int mouseY, int guiLeft, int guiTop)
	{
		int startX = this.xPos + guiLeft;
		int startY = this.yPos + guiTop;
		return (mouseX >= startX && mouseX < startX + 16) && (mouseY >= startY && mouseY < startY + 16);
	}

}
