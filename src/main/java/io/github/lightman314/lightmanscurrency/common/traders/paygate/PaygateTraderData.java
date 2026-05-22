package io.github.lightman314.lightmanscurrency.common.traders.paygate;

import java.util.Map;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketCollectionResult;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.templates.NormalTraderData;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.PaygateBlockEntity;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.PaygateNotification;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.PaygateTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.nodes.TicketStubNode;
import io.github.lightman314.lightmanscurrency.common.traders.paygate.trade.PaygateTradeData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class PaygateTraderData extends NormalTraderData {

	public static final TraderType<PaygateTraderData> TYPE = TraderType.simple(PaygateTraderData::new,PaygateTraderData::new);
	
	public static final int DURATION_MIN = 1;
	public static int getMaxDuration() {
		int val = LCConfig.SERVER.paygateMaxDuration.get();
		if(val <= 0)
			return Integer.MAX_VALUE;
		return val;
	}

	private PaygateTraderData() { super(); }
    public PaygateTraderData(Level level, BlockPos pos) { super(level, pos); }
    private PaygateTraderData(long id, Map<TraderNodeType<?>, TraderNode> nodes) { super(id,nodes); }
    @Override
    public TraderType<?> getType() { return TYPE; }

    @Override
    protected void addCustomNodes(NodeCollector collector) {
        collector.addNode(TicketStubNode.TYPE);
        collector.addNode(PaygateTradeNode.TYPE);
    }

    @Override
	public IconData getIcon() { return ItemIcon.ofItem(Items.REDSTONE_BLOCK); }

	private PaygateBlockEntity getPaygate() {
		if(this.getBlockEntity() instanceof PaygateBlockEntity be)
			return be;
		return null;
	}
	
	public boolean isActive(DirectionalSettings outputSides) {
        OutputConflictHandling handling = this.findNodeValue(PaygateTradeNode.TYPE,PaygateTradeNode::getConflictHandling,OutputConflictHandling.DENY_ANY);
		PaygateBlockEntity be = this.getPaygate();
		if(be != null)
		{
			if(handling == OutputConflictHandling.DENY_ANY)
				return be.isActive();
			for(Direction side : be.getActiveSides())
			{
				if(outputSides.allowOutputs(side))
					return true;
			}
		}
		return false;
	}

	public int getTimeRemaining(DirectionalSettings outputSides) {
		PaygateBlockEntity be = this.getPaygate();
		if(be != null)
			return be.getTimeRemaining(outputSides);
		return 0;
	}
	
	private void activate(int duration, int level, DirectionalSettings outputSides, @Nullable String name) {
		PaygateBlockEntity be = this.getPaygate();
		if(be != null)
			be.activate(duration,level,outputSides,this.findNodeValue(PaygateTradeNode.TYPE,PaygateTradeNode::getConflictHandling,OutputConflictHandling.DENY_ANY),name);
	}

	@Override
	public TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {

        PaygateTradeNode tradeNode = this.assertNode(PaygateTradeNode.TYPE);
        TicketStubNode stubNode = this.assertNode(TicketStubNode.TYPE);
        MoneyStorageNode moneyNode = this.assertNode(MoneyStorageNode.TYPE);
		PaygateTradeData trade = tradeNode.getTrade(tradeIndex);
		//Abort if the trade is null
		if(trade == null)
		{
			LightmansCurrency.LogError("Trade at index " + tradeIndex + " is null. Cannot execute trade!");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		//Abort if the trade is not valid
		if(!trade.isValid())
		{
			LightmansCurrency.LogWarning("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		//Abort if the paygate is already activated
		if(this.isActive(trade.getOutputSides()) && !tradeNode.getConflictHandling().allowsConflicts)
		{
			LightmansCurrency.LogWarning("Paygate is already activated. It cannot be activated until the previous timer is completed.");
			return TradeResult.FAIL_OUT_OF_STOCK;
		}
		
		//Abort if no player context is given
		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;
		
		//Check if the player is allowed to do the trade
		if(this.runPreTradeEvent(trade, context).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;

		MoneyValue price = MoneyValue.empty();
		MoneyValue taxesPaid = MoneyValue.empty();

		//Process a ticket trade
		if(trade.isTicketTrade())
		{
			//Abort if we don't have a valid ticket to extract
			if(!trade.canAfford(context))
			{
				LightmansCurrency.LogDebug("Ticket ID " + trade.getTicketID() + " could not be found in the players items to pay for trade " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}

			boolean hasPass = context.hasInfinitePass(trade.getTicketID());

			if(!hasPass)
			{

				ItemStack ticketStub = trade.getTicketStub();

				//Trade is valid, collect the ticket
                TicketCollectionResult result = context.collectTicket(trade.getTicketID());
				if(result.failed())
				{
					LightmansCurrency.LogDebug("Unable to collect the ticket. Aborting Trade!");
					return TradeResult.FAIL_CANNOT_AFFORD;
				}

				//Store the ticket stub if flagged to do so
                if(result.spawnTicketStub())
                {
                    if(trade.shouldStoreTicketStubs())
                        stubNode.addTicketStub(ticketStub);
                    else //Give the ticket stub
                        context.putItem(ticketStub);
                }

			}
			
			//Activate the paygate
			this.activate(trade.getDuration(),trade.getRedstoneLevel(),trade.getOutputSides(),trade.getDescription());
			
			//Push Notification
			this.pushNotification(PaygateNotification.createTicket(trade, hasPass, context.getPlayerReference(), this.getNotificationCategory()));

		}
		//Process a coin trade
		else
		{
			//Get the cost of the trade
			price = trade.getCost(context);

			//Abort if we don't have enough money
			if(!context.getPayment(price))
			{
				LightmansCurrency.LogDebug("Not enough money is present for the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//We have collected the payment, activate the paygate
			this.activate(trade.getDuration(),trade.getRedstoneLevel(),trade.getOutputSides(),trade.getDescription());

			//Don't store money if the trader is creative
			if(!this.hasInfiniteStock())
			{
				//Give the paid cost to storage
				taxesPaid = moneyNode.addStoredMoney(price, context.getTaxContext());
			}

			//Handle Stats
			this.incrementStat(StatKeys.Traders.MONEY_EARNED, price);
			if(!taxesPaid.isEmpty())
				this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);

			//Push Notification
			this.pushNotification(PaygateNotification.createMoney(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));

		}
		//Push the post-trade event
		return this.runPostTradeEvent(trade, context, price, taxesPaid);
	}

}
