package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderSelectClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TraderSelectTab extends TraderInterfaceTab {

	public TraderSelectTab(TraderInterfaceMenu menu) { super(menu); }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new TraderSelectClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return true; }

	public void toggleTrader(long traderID) {
		this.menu.getBE().toggleTrader(traderID);
		//Don't need to mark dirty, as that's done on the BE's side automatically
		if(this.menu.isClient())
			this.menu.SendMessage(this.builder().setLong("ToggleTrader",traderID));
	}

	@Override
	public void handleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("ToggleTrader"))
			this.toggleTrader(message.getLong("ToggleTrader"));
	}

}