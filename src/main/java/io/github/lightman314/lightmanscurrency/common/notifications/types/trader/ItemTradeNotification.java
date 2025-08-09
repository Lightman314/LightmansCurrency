package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.taxes.notifications.SingleLineTaxableNotification;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeDirection;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemData;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class ItemTradeNotification extends SingleLineTaxableNotification {

	public static final NotificationType<ItemTradeNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("item_trade"),ItemTradeNotification::new);
	
	TraderCategory traderData;
	
	TradeDirection tradeType;
	List<ItemData> items;
	MoneyValue cost = MoneyValue.empty();
	
	String customer;

	private ItemTradeNotification(){}

	public ItemTradeNotification(ItemTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory traderData, MoneyValue taxesPaid) {

		super(taxesPaid);

		this.traderData = traderData;
		this.tradeType = trade.getTradeDirection();
		
		this.items = new ArrayList<>();
		this.items.add(new ItemData(trade.getSellItem(0), trade.isPurchase() ? "" : trade.getCustomName(0)));
		this.items.add(new ItemData(trade.getSellItem(1), trade.isPurchase() ? "" : trade.getCustomName(1)));
		
		if(trade.isBarter())
		{
			this.items.add(new ItemData(trade.getBarterItem(0),""));
			this.items.add(new ItemData(trade.getBarterItem(1),""));
		}
		else
			this.cost = cost;
		
		this.customer = customer.getName(false);
		
	}

	public static Supplier<Notification> create(ItemTradeData trade, MoneyValue cost, PlayerReference customer, TraderCategory trader, MoneyValue taxesPaid) { return () -> new ItemTradeNotification(trade, cost, customer, trader, taxesPaid); }
	
	@Nonnull
	@Override
	protected NotificationType<ItemTradeNotification> getType() { return TYPE; }

	@Nonnull
	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Nonnull
	@Override
	public MutableComponent getNormalMessage() {
		
		Component action = this.tradeType.getActionPhrase();

		Component itemText = ItemData.format(this.items.get(0), this.items.get(1));

		Component cost;
		if(this.tradeType == TradeDirection.BARTER)
		{
			//Flip the cost and item text, as for barters the text is backwards "bartered *barter items* for *sold items*"
			cost = itemText;
			itemText = ItemData.format(this.items.get(2), this.items.get(3));
		}
		else
			cost = this.cost.getText("NULL");

		//Create log from stored data
		return LCText.NOTIFICATION_TRADE_ITEM.get(this.customer, action, itemText, cost);
		
	}

	@Override
	protected void saveNormal(CompoundTag compound) {
		
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeType", this.tradeType.index);
		ListTag itemList = new ListTag();
		for(ItemData item : this.items)
			itemList.add(item.save());
		compound.put("Items", itemList);
		if(this.tradeType != TradeDirection.BARTER)
			compound.put("Price", this.cost.save());
		compound.putString("Customer", this.customer);
		
	}

	@Override
	protected void loadNormal(CompoundTag compound) {
		
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeType = TradeDirection.fromIndex(compound.getInt("TradeType"));
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(ItemData.load(itemList.getCompound(i)));
		if(this.tradeType != TradeDirection.BARTER)
			this.cost = MoneyValue.safeLoad(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}

	@Override
	protected boolean canMerge(@Nonnull Notification other) {
		if(other instanceof ItemTradeNotification itn)
		{
			if(!itn.traderData.matches(this.traderData))
				return false;
			if(itn.tradeType != this.tradeType)
				return false;
			if(itn.items.size() != this.items.size())
				return false;
			for(int i = 0; i < this.items.size(); ++i)
			{
				if(!this.items.get(i).matches(itn.items.get(i)))
					return false;
			}
			if(!itn.cost.equals(this.cost))
				return false;
			if(!itn.customer.equals(this.customer))
				return false;
			//Passed all checks. Allow merging.
			return this.TaxesMatch(itn);
		}
		return false;
	}
	
}
