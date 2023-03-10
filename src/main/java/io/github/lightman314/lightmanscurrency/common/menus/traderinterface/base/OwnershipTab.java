package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.OwnershipClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceTab;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class OwnershipTab extends TraderInterfaceTab {

	public OwnershipTab(TraderInterfaceMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new OwnershipClientTab(screen, this); }

	private boolean isAdmin() { return this.menu.getBE().isOwner(this.menu.player); }
	
	@Override
	public boolean canOpen(PlayerEntity player) { return this.isAdmin(); }

	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }
	
	public void setNewOwner(String newOwner) {
		if(this.isAdmin())
		{
			if(this.menu.isClient())
			{
				CompoundNBT message = new CompoundNBT();
				message.putString("NewOwner", newOwner);
				this.menu.sendMessage(message);
			}
			else
				this.menu.getBE().setOwner(newOwner);
		}
	}
	
	public void setNewTeam(long team) {
		if(this.isAdmin() && team >= 0)
		{
			this.menu.getBE().setTeam(team);
			if(this.menu.isClient())
			{
				CompoundNBT message = new CompoundNBT();
				message.putLong("NewTeam", team);
				this.menu.sendMessage(message);
			}
		}
	}

	@Override
	public void receiveMessage(CompoundNBT message) {
		if(message.contains("NewOwner"))
		{
			this.setNewOwner(message.getString("NewOwner"));
		}
		if(message.contains("NewTeam"))
		{
			this.setNewTeam(message.getLong("NewTeam"));
		}
	}

}