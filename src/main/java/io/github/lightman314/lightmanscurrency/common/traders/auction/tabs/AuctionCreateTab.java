package io.github.lightman314.lightmanscurrency.common.traders.auction.tabs;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.TraderStorageNodeTab;
import io.github.lightman314.lightmanscurrency.common.traders.auction.client.tabs.AuctionCreateClientTab;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.EasySlot;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.AuctionTradesNode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;

public class AuctionCreateTab extends TraderStorageNodeTab<AuctionTradesNode> {

    public static final ResourceLocation KEY = LightmansCurrency.id("auction_create");

	public AuctionCreateTab(ITraderStorageMenu menu) { super(AuctionTradesNode.TYPE,menu); }

    @Override
    public ResourceLocation tabKey() { return KEY; }

    @Override
    public int getSortPriority() { return SORT_STORAGE + 10; }

    @Override
	public Object createClientTab(Object screen) { return new AuctionCreateClientTab(screen, this); }
	
	List<EasySlot> slots = new ArrayList<>();
	public List<EasySlot> getSlots() { return this.slots; }
	LCItemStackHandler auctionItems = new LCItemStackHandler(2);
	public LCItemStackHandler getAuctionItems() { return this.auctionItems; }
	
	@Override
	public void addStorageMenuSlots(Function<Slot, Slot> addSlot) {
		
		for(int i = 0; i < this.auctionItems.getSlots(); ++i)
		{
			EasySlot newSlot = new EasySlot(this.auctionItems, i, TraderMenu.SLOT_OFFSET + 8 + i * 18, 122);
			addSlot.apply(newSlot);
			this.slots.add(newSlot);
		}
		EasySlot.SetActive(this.slots, false);
		
	}
	
	@Override
	public void onTabOpen() {
		EasySlot.SetActive(this.slots,true);
        EasySlot.SetLocked(this.slots,false);
	}
	
	@Override
	public void onTabClose() {
		EasySlot.SetActive(this.slots,false);
		this.menu.clearContainer(this.auctionItems);
	}
	
	@Override
	public void onMenuClose() { this.menu.clearContainer(this.auctionItems); }
	
	public void createAuction(AuctionTradeData trade) {
		AuctionTradesNode node = this.getNode();
		if(node != null)
		{
			if(this.menu.isClient())
			{
				this.menu.SendMessage(this.builder().setCustom("CreateAuction",trade,ModLazyPackets.AUCTION_TRADE));
				return;
			}
			//Set the trade's auction items based on the items currently in the auction item slots
			trade.setAuctionItems(this.auctionItems);
			if(!trade.isValid())
			{
				//Send failure message to the client.
				this.menu.SendMessage(this.builder().setBoolean("AuctionCreated", false));
				//LightmansCurrency.LogInfo("Failed to create the auction as the auction is not valid.");
				return;
			}
			boolean success = node.addTrade(trade,this.menu.getPlayer(),false);
			if(success)
			{
				//Delete the items of the auctionItems
				this.auctionItems.clear();
			}
			//Send response message to the client
			this.menu.SendMessage(this.builder().setBoolean("AuctionCreated", success));
            EasySlot.SetLocked(this.slots,true);
			//LightmansCurrency.LogInfo("Successfully created the auction!");
		}
	}

	@Override
	public void receiveMessage(LazyPacketData message) {
		if(message.contains("CreateAuction"))
		{
			//LightmansCurrency.LogInfo("Received Auction from the client.\n" + message.getCompound("CreateAuction").getAsString());
			this.createAuction(message.getCustom("CreateAuction",ModLazyPackets.AUCTION_TRADE));
		}
	}

}
