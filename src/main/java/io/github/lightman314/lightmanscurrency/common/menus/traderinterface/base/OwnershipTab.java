package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.OwnershipClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class OwnershipTab extends TraderInterfaceTab {

	public OwnershipTab(TraderInterfaceMenu menu) { super(menu); }

	@Override
	@Nonnull
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new OwnershipClientTab(screen, this); }

	private boolean isAdmin() { return this.menu.getBE().isOwner(this.menu.player); }
	
	@Override
	public boolean canOpen(Player player) { return this.isAdmin(); }
	
	public void setPlayerOwner(String newOwner) {
		if(this.isAdmin())
		{
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder().setString("NewPlayerOwner", newOwner));
			}
			else
			{
				PlayerReference player = PlayerReference.of(false, newOwner);
				if(player != null)
					this.menu.getBE().owner.SetOwner(PlayerOwner.of(player));
			}
		}
	}
	
	public void setOwner(Owner newOwner) {
		if(this.isAdmin())
		{
			this.menu.getBE().owner.SetOwner(newOwner);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setOwner("NewOwner", newOwner));
		}
	}

	@Override
	public void handleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("NewPlayerOwner"))
			this.setPlayerOwner(message.getString("NewPlayerOwner"));
		if(message.contains("NewOwner"))
			this.setOwner(message.getOwner("NewOwner"));
	}

}
