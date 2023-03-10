package io.github.lightman314.lightmanscurrency.common.menus.traderinterface.base;

import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderinterface.TradeSelectClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceTab;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TradeSelectTab extends TraderInterfaceTab{

	public TradeSelectTab(TraderInterfaceMenu menu) { super(menu); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TraderInterfaceClientTab<?> createClientTab(TraderInterfaceScreen screen) { return new TradeSelectClientTab(screen, this); }

	@Override
	public boolean canOpen(PlayerEntity player) {
		return this.menu.getBE().getInteractionType().trades && this.menu.getBE().getTrader() != null;
	}

	@Override
	public void onTabOpen() { }

	@Override
	public void onTabClose() { }

	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) { }
	
	public void setTradeIndex(int tradeIndex) {
		//LightmansCurrency.LogInfo("Setting trade index to " + tradeIndex + " on the " + DebugUtil.getSideText(this.menu.player));
		if(this.menu.getBE().canAccess(this.menu.player))
		{
			this.menu.getBE().setTradeIndex(tradeIndex);
			if(this.menu.isClient())
			{
				CompoundNBT message = new CompoundNBT();
				message.putInt("NewTradeIndex", tradeIndex);
				this.menu.sendMessage(message);
			}
		}
	}
	
	@Override
	public void receiveMessage(CompoundNBT message) {
		if(message.contains("NewTradeIndex"))
		{
			this.setTradeIndex(message.getInt("NewTradeIndex"));
		}
	}
	
}
