package io.github.lightman314.lightmanscurrency.menus;

import io.github.lightman314.lightmanscurrency.menus.interfaces.ITraderCashRegisterMenu;
import io.github.lightman314.lightmanscurrency.blockentity.CashRegisterBlockEntity;
import io.github.lightman314.lightmanscurrency.blockentity.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.core.ModContainers;
import net.minecraft.world.entity.player.Inventory;

public class ItemTraderMenuCR extends ItemTraderMenu implements ITraderCashRegisterMenu{
	
	public CashRegisterBlockEntity cashRegister;
	
	public ItemTraderMenuCR(int windowId, Inventory inventory, ItemTraderBlockEntity tileEntity, CashRegisterBlockEntity cashRegister)
	{
		
		super(ModContainers.ITEMTRADERCR, windowId, inventory, tileEntity);
		this.cashRegister = cashRegister;
		
	}
	
	public int getThisIndex()
	{
		return this.cashRegister.getTraderIndex(this.tileEntity);
	}
	
	public int getTotalCount()
	{
		return this.cashRegister.getPairedTraderSize();
	}
	
	public void OpenNextContainer(int direction)
	{
		this.cashRegister.OpenContainer(getThisIndex(), getThisIndex() + direction, direction, this.player);
	}
	
	public void OpenContainerIndex(int index)
	{
		int previousIndex = index - 1;
		if(previousIndex  < 0)
			previousIndex = this.cashRegister.getPairedTraderSize() - 1;
		this.cashRegister.OpenContainer(previousIndex, index, 1, this.player);
	}
	
}
