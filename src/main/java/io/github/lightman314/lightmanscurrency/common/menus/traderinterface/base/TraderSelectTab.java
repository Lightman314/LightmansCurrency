package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderSelectClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceTab;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TraderSelectTab extends TraderInterfaceTab {

	public TraderSelectTab(TraderInterfaceMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new TraderSelectClientTab(screen, this); }

	@Override
	public boolean canOpen(PlayerEntity player) { return true; }

	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }
	
	public void setTrader(long traderID) {
		this.menu.getBE().setTrader(traderID);
		//Don't need to mark dirty, as that's done on the BE's side automatically
		if(this.menu.isClient())
		{
			CompoundNBT message = new CompoundNBT();
			if(traderID >= 0)
				message.putLong("NewTrader", traderID);
			else
				message.putBoolean("NullTrader", true);
			this.menu.sendMessage(message);
		}
	}
	
	@Override
	public void receiveMessage(CompoundNBT message) {
		if(message.contains("NewTrader"))
		{
			this.setTrader(message.getLong("NewTrader"));
		}
		else if(message.contains("NullTrader"))
		{
			this.setTrader(-1);
		}
	}
	
}