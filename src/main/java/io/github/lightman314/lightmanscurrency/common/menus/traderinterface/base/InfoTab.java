package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.InfoClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceTab;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InfoTab extends TraderInterfaceTab {

	public InfoTab(TraderInterfaceMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new InfoClientTab(screen, this); }

	@Override
	public boolean canOpen(PlayerEntity player) { return true; }

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
				CompoundNBT message = new CompoundNBT();
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
				CompoundNBT message = new CompoundNBT();
				message.putInt("NewInteractionType", newType.index);
				this.menu.sendMessage(message);
			}
		}
	}

	@Override
	public void receiveMessage(CompoundNBT message) {
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
