package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.InfoClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InfoTab extends TraderInterfaceTab {

	public InfoTab(TraderInterfaceMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new InfoClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return true; }

	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }
	
	public void acceptTradeChanges() {
		if(this.menu.getBE().canAccess(this.menu.player))
		{
			this.menu.getBE().acceptTradeChanges();
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putBoolean("AcceptTradeChanges", true);
				this.menu.sendMessage(message);
			}
		}
	}
	
	public void changeInteractionType(InteractionType newType) {
		if(this.menu.getBE().canAccess(this.menu.player))
		{
			this.menu.getBE().setInteractionType(newType);
			if(this.menu.isClient())
			{
				CompoundTag message = new CompoundTag();
				message.putInt("NewInteractionType", newType.index);
				this.menu.sendMessage(message);
			}
		}
	}

	@Override
	public void receiveMessage(CompoundTag message) {
		if(message.contains("NewInteractionType"))
		{
			InteractionType newType = InteractionType.fromIndex(message.getInt("NewInteractionType"));
			this.changeInteractionType(newType);
		}
		if(message.contains("AcceptTradeChanges"))
		{
			this.acceptTradeChanges();
		}
	}

}
