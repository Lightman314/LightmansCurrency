package io.github.lightman314.lightmanscurrency.common.traders.paygate.tabs;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.builtin.BasicTradeEditTab;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.client.tabs.PaygateTradeEditClientTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.PaygateTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.trade.PaygateTradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class PaygateTradeEditTab extends TraderStorageNodeTab<PaygateTradeNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("paygate_trade_edit");

	public PaygateTradeEditTab(ITraderStorageMenu menu) { super(PaygateTradeNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

	@Override
	public Object createClientTab(Object screen) { return new PaygateTradeEditClientTab(screen, this); }

	@Override
	public boolean canOpenTab(Player player) { return this.menu.hasPermission(Permissions.EDIT_TRADES); }
	
	private int tradeIndex = -1;
	public int getTradeIndex() { return this.tradeIndex; }
	public PaygateTradeData getTrade() {
        PaygateTradeNode node = this.getNode();
		if(node != null)
		{
			if(this.tradeIndex >= node.getTradeCount() || this.tradeIndex < 0)
			{
				this.menu.ChangeTab(BasicTradeEditTab.KEY);
				return null;
			}
			return node.getTrade(this.tradeIndex);
		}
		return null;
	}
	
	public void setPrice(MoneyValue price) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setCost(price);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setMoneyValue("NewPrice", price));
		}
	}
	
	public void setTicket(ItemStack ticket) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setTicket(ticket);
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder()
						.setBoolean("NewTicket", true)
						.setItem("Ticket", ticket));
			}
		}
	}
	
	public void setDuration(int duration) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setDuration(duration);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("NewDuration", duration));
		}
	}

	public void setLevel(int level) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setRedstoneLevel(level);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("NewLevel",level));
		}
	}

	public void setDescription(String description) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setDescription(description);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setString("NewDescription",description));
		}
	}

	public void setTooltip(String tooltip) {
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setTooltip(tooltip);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setString("NewTooltip",tooltip));
		}
	}

	public void setTicketStubHandling(boolean storeTicketStubs)
	{
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.setStoreTicketStubs(storeTicketStubs);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setBoolean("StoreTicketStubs", storeTicketStubs));
		}
	}

	public void setOutputSide(Direction side, DirectionalSettingsState state)
	{
		PaygateTradeData trade = this.getTrade();
		if(trade != null)
		{
			trade.getOutputSides().setState(side,state);
			if(this.menu.isClient())
				this.menu.SendMessage(this.builder().setInt("SetOutputSide",side.get3DDataValue()).setString("State",state.toString()));
		}
	}

	@Override
	public void OpenMessage(LazyPacketData message) {
		if(message.contains("TradeIndex"))
			this.tradeIndex = message.getInt("TradeIndex");
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("NewPrice"))
		{
			this.setPrice(message.getMoneyValue("NewPrice"));
		}
		else if(message.contains("NewTicket"))
		{
			ItemStack ticket = ItemStack.EMPTY;
			if(message.contains("Ticket"))
				ticket = message.getItem("Ticket");
			this.setTicket(ticket);
		}
		else if(message.contains("NewDuration"))
		{
			this.setDuration(message.getInt("NewDuration"));
		}
		else if(message.contains("NewLevel"))
		{
			this.setLevel(message.getInt("NewLevel"));
		}
		else if(message.contains("NewDescription"))
		{
			this.setDescription(message.getString("NewDescription"));
		}
		else if(message.contains("NewTooltip"))
		{
			this.setTooltip(message.getString("NewTooltip"));
		}
		else if(message.contains("StoreTicketStubs"))
		{
			this.setTicketStubHandling(message.getBoolean("StoreTicketStubs"));
		}
		else if(message.contains("SetOutputSide"))
		{
			Direction side = Direction.from3DDataValue(message.getInt("SetOutputSide"));
			DirectionalSettingsState state = DirectionalSettingsState.parse(message.getString("State"));
			this.setOutputSide(side,state);
		}
	}

}
