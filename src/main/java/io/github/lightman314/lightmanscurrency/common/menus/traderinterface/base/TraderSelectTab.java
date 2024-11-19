package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TraderSelectClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.api.trader_interface.menu.TraderInterfaceTab;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TraderSelectTab extends TraderInterfaceTab {

	public TraderSelectTab(TraderInterfaceMenu menu) { super(menu); }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public Object createClientTab(@Nonnull Object screen) { return new TraderSelectClientTab(screen, this); }

	@Override
	public boolean canOpen(Player player) { return true; }
	
	public void setTrader(long traderID) {
		this.menu.getBE().setTrader(traderID);
		//Don't need to mark dirty, as that's done on the BE's side automatically
		if(this.menu.isClient())
		{
			if(traderID >= 0)
				this.menu.SendMessage(this.builder().setLong("NewTrader", traderID));
			else
				this.menu.SendMessage(this.builder().setFlag("NullTrader"));
		}
	}
	
	@Override
	public void handleMessage(@Nonnull LazyPacketData message) {
		if(message.contains("NewTrader"))
			this.setTrader(message.getLong("NewTrader"));
		else if(message.contains("NullTrader"))
			this.setTrader(-1);
	}
	
}
