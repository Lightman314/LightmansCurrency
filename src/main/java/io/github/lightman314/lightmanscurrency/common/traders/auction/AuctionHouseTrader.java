package io.github.lightman314.lightmanscurrency.common.traders.auction;

import java.util.*;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.IconIcon;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.FakeOwnerNode;
import io.github.lightman314.lightmanscurrency.common.core.ModStats;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.ticker.ICommonTicker;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.AuctionStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.nodes.AuctionTradesNode;
import io.github.lightman314.lightmanscurrency.common.traders.auction.trade.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.api.events.AuctionHouseEvent.AuctionEvent.AuctionBidEvent;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.network.message.auction.SPacketStartBid;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.NeoForge;

public class AuctionHouseTrader extends TraderData implements ICommonTicker {

	public static final TraderType<AuctionHouseTrader> TYPE = TraderType.simple(AuctionHouseTrader::new,AuctionHouseTrader::new);
	
	public static final IconData ICON = IconIcon.ofIcon(LightmansCurrency.id("auction_house"));

	public static boolean isEnabled() { return LCConfig.SERVER.auctionHouseEnabled.get(); }
	public static boolean shouldShowOnTerminal() { return isEnabled() && LCConfig.SERVER.auctionHouseOnTerminal.get(); }
	
	private AuctionHouseTrader() { }

    private AuctionHouseTrader(long id,Map<TraderNodeType<?>, TraderNode> nodes) { super(id,nodes); }

    @Override
    public TraderType<?> getType() { return TYPE; }

    @Override
    public void addDefaultNodes(NodeCollector collector) {
        collector.addNode(FakeOwnerNode.TYPE,LCText.GUI_TRADER_AUCTION_HOUSE_OWNER.get());
        collector.addNode(AuctionTradesNode.TYPE);
    }

    @Override
	public MutableComponent getName() { return LCText.GUI_TRADER_AUCTION_HOUSE.get(); }

	@Override
	public boolean readyForCustomers() { return true; }

	@Override
	public boolean showSearchBox() { return this.getTradeCount() > 10; }

	@Override
	public void tick() {

		//Can only delete trades if no player is currently using the trader, as we don't want to delete trades and mess up a trade index.
		boolean canDelete = this.getUserCount() <= 0;
        AuctionTradesNode tradesNode = this.getNode(AuctionTradesNode.TYPE);
        AuctionStorageNode storageNode = this.getNode(AuctionStorageNode.TYPE);
        if(tradesNode != null && storageNode != null)
            tradesNode.tickTrades(this,storageNode,canDelete);
	}

	@Override
	public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
		//Interaction should simply open the bid menu, so...
		if(!context.hasPlayer())
			return TradeResult.FAIL_NOT_SUPPORTED;
		else
		{
            AuctionTradesNode node = this.getNode(AuctionTradesNode.TYPE);
            if(node != null)
            {
                AuctionTradeData trade = node.getTrade(tradeIndex);
                //Open bid menu for the given trade index
                if(trade != null && trade.allowedToBid(context.getPlayer()))
                {
                    new SPacketStartBid(this.getID(), tradeIndex).sendTo(context.getPlayer());
                    return TradeResult.success(null);
                }
                else
                    return TradeResult.FAIL_TRADE_RULE_DENIAL;
            }
            return TradeResult.FAIL_INVALID_TRADE;
		}
	}
	
	public void makeBid(Player player, TraderMenu menu, int tradeIndex, MoneyValue bidAmount) {

        AuctionTradesNode node = this.assertNode(AuctionTradesNode.TYPE);
		AuctionTradeData trade = node.getTrade(tradeIndex);
		if(trade == null)
			return;
		if(trade.hasExpired(TimeUtil.getCurrentTime()))
			return;
		
		AuctionBidEvent.Pre e1 = new AuctionBidEvent.Pre(this, trade, player, bidAmount);
		if(NeoForge.EVENT_BUS.post(e1).isCanceled())
			return;
		
		bidAmount = e1.getBidAmount();

		TradeContext tradeContext = menu.getContext(this);

		if(tradeContext.hasFunds(bidAmount) && trade.tryMakeBid(this, player, bidAmount))
		{
			//Take money from the coin slots & players wallet second
			tradeContext.getPayment(bidAmount);
			
			AuctionBidEvent.Post e2 = new AuctionBidEvent.Post(this, trade, player, bidAmount);
			NeoForge.EVENT_BUS.post(e2);
		}

	}

    public void onPlayerJoin(Player player)
    {
        this.ifNodePresent(AuctionStorageNode.TYPE,node -> {
            AuctionPlayerStorage storage = node.getStorage(player);
            if(storage.pendingWinStats > 0)
            {
                player.awardStat(ModStats.STAT_AUCTION_WINS,storage.pendingWinStats);
                storage.pendingWinStats = 0;
                this.setChangedNoPacket();
            }
        });
    }

	@Override
	public IconData getIcon() { return ICON; }
	
	@Override
	public boolean shouldRemove(MinecraftServer server) { return false; }

	@Override
	public void getAdditionalContents(List<ItemStack> contents) { }

	@Override
	protected MutableComponent getDefaultName() { return this.getName(); }

	@Override
	public boolean hasValidTrade() { return true; }

}
