package io.github.lightman314.lightmanscurrency.menus.traderinterface.base;

import java.util.UUID;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderSelectClientTab;
import io.github.lightman314.lightmanscurrency.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.menus.traderinterface.TraderInterfaceTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TraderSelectTab extends TraderInterfaceTab {

	public TraderSelectTab(TraderInterfaceMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new TraderSelectClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return true; }

	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }
	
	public void setTrader(UUID traderID) {
		this.menu.getBE().setTrader(traderID);
		//Don't need to mark dirty, as that's done on the BE's side automatically
		if(this.menu.isClient())
		{
			CompoundTag message = new CompoundTag();
			if(traderID != null)
				message.putUUID("NewTrader", traderID);
			else
				message.putBoolean("NullTrader", true);
			this.menu.sendMessage(message);
		}
	}
	
	@Override
	public void receiveMessage(CompoundTag message) {
		if(message.contains("NewTrader"))
		{
			this.setTrader(message.getUUID("NewTrader"));
		}
		else if(message.contains("NullTrader"))
		{
			this.setTrader(null);
		}
	}
	
}
