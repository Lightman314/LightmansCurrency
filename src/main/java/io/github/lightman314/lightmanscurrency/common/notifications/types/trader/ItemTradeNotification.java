package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemWriteData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.TaxableNotification;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.NonNullSupplier;

import javax.annotation.Nonnull;

public class ItemTradeNotification extends TaxableNotification {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trade");

	TraderCategory traderData;

	ItemTradeType tradeType;
	List<ItemWriteData> items;
	CoinValue cost = CoinValue.EMPTY;

	String customer;

	public ItemTradeNotification(ItemTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData, CoinValue taxesPaid) {

		super(taxesPaid);

		this.traderData = traderData;
		this.tradeType = trade.getTradeType();

		this.items = new ArrayList<>();
		this.items.add(new ItemWriteData(trade.getSellItem(0), trade.isPurchase() ? "" : trade.getCustomName(0)));
		this.items.add(new ItemWriteData(trade.getSellItem(1), trade.isPurchase() ? "" : trade.getCustomName(1)));

		if(trade.isBarter())
		{
			this.items.add(new ItemWriteData(trade.getBarterItem(0),""));
			this.items.add(new ItemWriteData(trade.getBarterItem(1),""));
		}
		else
		{
			this.cost = cost;
			LightmansCurrency.LogDebug("Created Item Trade Notification of cost " + this.cost.getString("NADA"));
		}

		this.customer = customer.getName(false);

	}

	public static NonNullSupplier<Notification> create(ItemTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory trader, CoinValue taxesPaid) { return () -> new ItemTradeNotification(trade, cost, customer, trader, taxesPaid); }

	public ItemTradeNotification(CompoundTag compound) { this.load(compound); }

	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Nonnull
	@Override
	public MutableComponent getNormalMessage() {

		Component boughtText = EasyText.translatable("log.shoplog." + this.tradeType.name().toLowerCase());

		Component itemText = ItemWriteData.getItemNames(this.items.get(0), this.items.get(1));

		Component cost;
		if(this.tradeType == ItemTradeType.BARTER)
		{
			//Flip the cost and item text, as for barters the text is backwards "bartered *barter items* for *sold items*"
			cost = itemText;
			itemText = ItemWriteData.getItemNames(this.items.get(2), this.items.get(3));
		}
		else
			cost = this.cost.getComponent("0");

		//Create log from stored data
		return EasyText.translatable("notifications.message.item_trade", this.customer, boughtText, itemText, cost);

	}

	@Override
	protected void saveNormal(CompoundTag compound) {

		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeType", this.tradeType.index);
		ListTag itemList = new ListTag();
		for(ItemWriteData item : this.items)
			itemList.add(item.save());
		compound.put("Items", itemList);
		if(this.tradeType != ItemTradeType.BARTER)
			compound.put("Price", this.cost.save());
		compound.putString("Customer", this.customer);

	}

	@Override
	protected void loadNormal(CompoundTag compound) {

		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeType = ItemTradeType.fromIndex(compound.getInt("TradeType"));
		ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(new ItemWriteData(itemList.getCompound(i)));
		if(this.tradeType != ItemTradeType.BARTER)
			this.cost = CoinValue.safeLoad(compound, "Price");
		this.customer = compound.getString("Customer");

	}

	@Override
	protected boolean canMerge(Notification other) {
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
				ItemWriteData i1 = this.items.get(i);
				ItemWriteData i2 = itn.items.get(i);
				if(!i1.itemName.getString().equals(i2.itemName.getString()))
					return false;
				if(i1.count != i2.count)
					return false;
			}
			if(itn.cost.getValueNumber() != this.cost.getValueNumber())
				return false;
			if(!itn.customer.equals(this.customer))
				return false;
			//Passed all checks. Allow merging.
			return this.TaxesMatch(itn);
		}
		return false;
	}

}