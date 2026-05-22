package io.github.lightman314.lightmanscurrency.common.traders.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.stats.StatKeys;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.trade.*;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.InputNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.InterfaceSupportNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.MoneyStorageNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.UpgradesNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.templates.PersistentSupportingTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemHandler;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.ItemTradeNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.types.trader.OutOfStockNotification;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

public class ItemTraderData extends PersistentSupportingTraderData {

    public static final TraderType<ItemTraderData> TYPE = TraderType.simple(ItemTraderData::new,ItemTraderData::new);

	public static final List<UpgradeType> ALLOWED_UPGRADES = Lists.newArrayList(Upgrades.ITEM_CAPACITY);
	
	public static final int DEFAULT_STACK_LIMIT = 64 * 9;

	TraderItemHandler<ItemTraderData> itemHandler = new TraderItemHandler<>(this);
	
	public IItemHandler getItemHandler(Direction relativeSide) { return this.itemHandler.getHandler(relativeSide); }

	protected ItemTraderData() { this(new HashMap<>()); }
	protected ItemTraderData(Map<TraderNodeType<?>,Object> args) { super(args); }
	
	public ItemTraderData(int tradeCount, boolean alwaysNetwork, Level level, BlockPos pos) { this(new HashMap<>(),tradeCount,alwaysNetwork,level,pos); }
	protected ItemTraderData(Map<TraderNodeType<?>,Object> args,int tradeCount, boolean alwaysNetwork, Level level, BlockPos pos) { super(addArgs(args,tradeCount),alwaysNetwork,level, pos); }
    protected ItemTraderData(long id,Map<TraderNodeType<?>, TraderNode> data) { super(id,data); }

    private static Map<TraderNodeType<?>,Object> addArgs(Map<TraderNodeType<?>,Object> args, int tradeCount)
    {
        args.put(ItemTradeNode.TYPE,tradeCount);
        return args;
    }

    @Override
    public TraderType<?> getType() { return TYPE; }

    @Override
    public void addCustomNodes(NodeCollector collector) {
        //Upgrades Node
        UpgradesNode.addNode(collector,5);
        //Input Node for input/output settings
        collector.addNode(InputNode.TYPE);
        //Interface support node to make item trader interfaces functional
        collector.addNode(InterfaceSupportNode.TYPE);
        //Storage node
        collector.addNode(ItemStorageNode.TYPE);
        //Item Trade node
        collector.addNode(ItemTradeNode.TYPE);
    }

    @Override
    public List<TradeDirection> validDirectionOptions() { return ImmutableList.of(TradeDirection.SALE,TradeDirection.PURCHASE,TradeDirection.BARTER); }

	@Override
	public IconData getIcon() { return IconUtil.ICON_TRADER; }

	@Override
	protected TradeResult ExecuteTrade(TradeContext context, int tradeIndex) {
        ItemTradeNode tradeNode = this.assertNode(ItemTradeNode.TYPE);
		ItemTradeData trade = tradeNode.getTrade(tradeIndex);
        ItemStorageNode storageNode = this.assertNode(ItemStorageNode.TYPE);
        MoneyStorageNode moneyNode = this.assertNode(MoneyStorageNode.TYPE);
		//Abort if the trade is null
		if(trade == null)
		{
			//LightmansCurrency.LogDebug("Trade at index " + tradeIndex + " is null. Cannot execute trade!");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		//Abort if the trade is not valid
		if(!trade.isValid())
		{
			//LightmansCurrency.LogDebug("Trade at index " + tradeIndex + " is not a valid trade. Cannot execute trade.");
			return TradeResult.FAIL_INVALID_TRADE;
		}
		
		if(!context.hasPlayerReference())
			return TradeResult.FAIL_NULL;
		
		//Check if the player is allowed to do the trade
		if(this.runPreTradeEvent(trade, context).isCanceled())
			return TradeResult.FAIL_TRADE_RULE_DENIAL;
		
		//Get the cost of the trade
		//Update: Use TradeData#getCost(TradeContext) as it will run the trade cost event automatically
		MoneyValue price = trade.getCost(context);
		
		//Process a sale
		if(trade.isSale())
		{
			if(trade.outOfStock(context) && !this.hasInfiniteStock())
			{
				//LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}

			//Randomize the items to be sold
			List<ItemStack> soldItems = trade.getRandomSellItems(this);
			//Abort if not enough items in items
			if(soldItems == null)
			{
                LightmansCurrency.LogWarning("Trade failed to collect items being sold from storage, in spite of the trade claiming to have items in stock!");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}

			//Abort if not enough room to put the sold item
			if(!context.canFitItems(soldItems))
			{
				//LightmansCurrency.LogDebug("Not enough room for the output item. Aborting trade!");
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			}
			
			if(!context.getPayment(price))
			{
				//LightmansCurrency.LogDebug("Not enough money is present for the trade at index " + tradeIndex + ". Cannot execute trade." +
				//		"\nPrice: " + price.getString("Null") + "\nAvailable Funds: " + context.getAvailableFunds().getString());
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//We have enough money, and the trade is valid. Execute the trade
			//Get the trade itemStack
			//Give the trade item
			for(int i = 0; i < soldItems.size(); ++i)
			{
				if(!context.putItem(soldItems.get(i)))//If there's not enough room to give the item to the output item, abort the trade
				{
					LightmansCurrency.LogError("Not enough room for the output item. Giving refund & aborting Trade!");
					//Collect the items already given
					for(int x = 0; x < i; ++x)
						context.collectItem(soldItems.get(x));
					//Give a refund
					context.givePayment(price);
					return TradeResult.FAIL_NO_OUTPUT_SPACE;
				}
			}

			//Give the paid cost to storage
			MoneyValue taxesPaid = MoneyValue.empty();
			if(this.shouldStoreMoney())
                taxesPaid = moneyNode.addStoredMoney(price, context.getTaxContext());

			//Ignore editing internal storage if this is flagged as creative.
			if(!this.hasInfiniteStock())
			{
				//Remove the sold items from storage
				trade.RemoveItemsFromStorage(storageNode.getStorage(),soldItems);
			}

			//Handle Stats
			this.incrementStat(StatKeys.Traders.MONEY_EARNED, price);
			if(!taxesPaid.isEmpty())
				this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);

			//Push Notification
			this.pushNotification(ItemTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));
			//Push out of stock notification
			if(!trade.hasStock(this))
				this.pushNotification(OutOfStockNotification.create(this.getNotificationCategory(), tradeIndex));

			//Push the post-trade event
			return this.runPostTradeEvent(trade,context,price,taxesPaid,soldItems);
			
		}
		//Process a purchase
		else if(trade.isPurchase())
		{
			List<ItemStack> collectableItems = context.getCollectableItems(trade.getItemRequirement(0), trade.getItemRequirement(1));
			//Abort if not enough items in the item slots
			if(!context.hasItems(collectableItems))
			{
				//LightmansCurrency.LogDebug("Not enough items in the item slots to make the purchase.");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}
			
			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!trade.hasSpace(this, collectableItems) && !this.hasInfiniteStock())
			{
				//LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return TradeResult.FAIL_NO_INPUT_SPACE;
			}
			//Abort if not enough money to pay them back
			if(trade.outOfStock(context) && !this.hasInfiniteStock())
			{
				//LightmansCurrency.LogDebug("Not enough money in storage to pay for the purchased items.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}
			//Passed the checks. Take the item(s) from the input slot
			context.collectItems(collectableItems);
			//Put the payment in the purchasers' wallet, coin slot, etc.
			context.givePayment(price);

			MoneyValue taxesPaid = MoneyValue.empty();

			//Ignore editing internal storage if this is flagged as creative or a void upgrade is equipped
			if(this.shouldStorePurchases())
			{
				//Put the item(s) in storage
				for(ItemStack item : collectableItems)
					storageNode.getStorage().forceAddItem(item);
			}
			if(!this.hasInfiniteStock())
			{
				//Remove the coins from storage
				taxesPaid = moneyNode.removeStoredMoney(price, context.getTaxContext());
			}

			//Handle Stats
			this.incrementStat(StatKeys.Traders.MONEY_PAID, price);
			if(!taxesPaid.isEmpty())
				this.incrementStat(StatKeys.Taxables.TAXES_PAID,taxesPaid);

			//Push Notification
			this.pushNotification(ItemTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), taxesPaid));
			//Push out of stock notification
			if(!trade.hasStock(this))
				this.pushNotification(OutOfStockNotification.create(this.getNotificationCategory(), tradeIndex));

			//Push the post-trade event
			return this.runPostTradeEvent(trade,context,price,taxesPaid,collectableItems);
			
		}
		//Process a barter
		else if(trade.isBarter())
		{
			//Collect items that will be taken from the customer.
			List<ItemStack> collectableItems = context.getCollectableItems(trade.getItemRequirement(2), trade.getItemRequirement(3));
			//Abort if not enough items in the item slots
			if(collectableItems == null)
			{
				//LightmansCurrency.LogDebug("Collectable items returned a null list!");
				return TradeResult.FAIL_CANNOT_AFFORD;
			}

			//Abort if not enough room to store the purchased items (unless we're creative)
			if(!trade.hasSpace(this, collectableItems) && !this.hasInfiniteStock())
			{
				//LightmansCurrency.LogDebug("Not enough room in storage to store the purchased items.");
				return TradeResult.FAIL_NO_INPUT_SPACE;
			}

			if(trade.outOfStock(context) && !this.hasInfiniteStock())
			{
				//LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}

			List<ItemStack> soldItems = trade.getRandomSellItems(this);
			//Abort if not enough items in items
			if(soldItems == null)
			{
				//LightmansCurrency.LogDebug("Not enough items in storage to carry out the trade at index " + tradeIndex + ". Cannot execute trade.");
				return TradeResult.FAIL_OUT_OF_STOCK;
			}

			//Abort if no space to put the sold items
			if(!context.canFitItems(soldItems))
			{
				//LightmansCurrency.LogDebug("Not enough space to store the purchased items.");
				return TradeResult.FAIL_NO_OUTPUT_SPACE;
			}
			
			//Passed the checks. Take the item(s) from the input slot
			context.collectItems(collectableItems);
			//Check if there's room for the new items
			for(int i = 0; i < soldItems.size(); ++i)
			{
				//If there's not enough room to give the item to the output item, abort the trade
				if(!context.putItem(soldItems.get(i)))
				{
					LightmansCurrency.LogError("Not enough room for the output item. Giving refund & aborting Trade!");
					//Collect the items already given
					for(int x = 0; x < i; ++x)
						context.collectItem(soldItems.get(x));
					//Give a refund
					context.givePayment(price);
					return TradeResult.FAIL_NO_OUTPUT_SPACE;
				}
			}

			//Ignore editing internal storage if this is flagged as creative or a void upgrade is equipped
			boolean storageChanged = false;
			if(this.shouldStorePurchases())
			{
				//Put the item in storage
				for(ItemStack item : collectableItems)
					storageNode.getStorage().forceAddItem(item);
			}
			if(!this.hasInfiniteStock())
			{
				//Remove the item from storage
				trade.RemoveItemsFromStorage(storageNode.getStorage(), soldItems);
			}

			//Push Notification
			this.pushNotification(ItemTradeNotification.create(trade, price, context.getPlayerReference(), this.getNotificationCategory(), MoneyValue.empty()));
			//Push out of stock notification
			if(!trade.hasStock(this))
				this.pushNotification(OutOfStockNotification.create(this.getNotificationCategory(), tradeIndex));

            List<List<ItemStack>> product = Lists.newArrayList(soldItems,collectableItems);
			//Push the post-trade event
			return this.runPostTradeEvent(trade,context,price,MoneyValue.empty(),product);
		}
		
		return TradeResult.FAIL_INVALID_TRADE;
	}

}
