package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.trader_interface.blockentity.TraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.InfoClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class InfoTab extends TraderInterfaceTab {

	public InfoTab(TraderInterfaceMenu menu) { super(menu); }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new InfoClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return true; }

	public void acceptTradeChanges(int reference) {
		if(this.menu.getBE().canAccess(this.menu.player))
		{
			this.menu.getBE().acceptTradeChanges(reference);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("AcceptTradeChanges",reference));
		}
	}

	public void changeInteractionType(InteractionType newType) {
		if(this.menu.getBE().canAccess(this.menu.player))
		{
			this.menu.getBE().setInteractionType(newType);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("NewInteractionType", newType.index));
		}
	}

	@Override
	public void handleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("NewInteractionType"))
			this.changeInteractionType(InteractionType.fromIndex(message.getInt("NewInteractionType")));
		if(message.contains("AcceptTradeChanges"))
			this.acceptTradeChanges(message.getInt("AcceptTradeChanges"));
	}

}