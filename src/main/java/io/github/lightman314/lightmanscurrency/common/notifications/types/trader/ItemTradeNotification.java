package io.github.lightman314.lightmanscurrency.common.notifications.types.trader;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.TraderCategory;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData.ItemTradeType;
import io.github.lightman314.lightmanscurrency.common.money.CoinValue;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;

public class ItemTradeNotification extends Notification{

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "item_trade");
	
	TraderCategory traderData;
	
	ItemTradeType tradeType;
	List<ItemData> items;
	CoinValue cost = new CoinValue();
	
	String customer;
	
	public ItemTradeNotification(ItemTradeData trade, CoinValue cost, PlayerReference customer, TraderCategory traderData) {
		
		this.traderData = traderData;
		this.tradeType = trade.getTradeType();
		
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
	
	public ItemTradeNotification(CompoundNBT compound) { this.load(compound); }
	
	@Override
	protected ResourceLocation getType() { return TYPE; }

	@Override
	public NotificationCategory getCategory() { return this.traderData; }

	@Override
	public IFormattableTextComponent getMessage() {
		
		ITextComponent boughtText = EasyText.translatable("log.shoplog." + this.tradeType.name().toLowerCase());

		ITextComponent itemText = getItemNames(this.items.get(0), this.items.get(1));

		ITextComponent cost;
		if(this.tradeType == ItemTradeType.BARTER)
		{
			//Flip the cost and item text, as for barters the text is backwards "bartered *barter items* for *sold items*"
			cost = itemText;
			itemText = getItemNames(this.items.get(2), this.items.get(3));
		}
		else
			cost = this.cost.getComponent("0");
		
		//Create log from stored data
		return EasyText.translatable("notifications.message.item_trade", this.customer, boughtText, itemText, cost);
		
	}
	
	private ITextComponent getItemNames(ItemData item1, ItemData item2) {
		if(item1.isEmpty && item2.isEmpty)
			return EasyText.literal("ERROR");
		else if(item2.isEmpty)
			return item1.format();
		else if(item1.isEmpty)
			return item2.format();
		else
			return item1.formatWith(item2);
	}

	@Override
	protected void saveAdditional(CompoundNBT compound) {
		
		compound.put("TraderInfo", this.traderData.save());
		compound.putInt("TradeType", this.tradeType.index);
		ListNBT itemList = new ListNBT();
		for(ItemData item : this.items)
			itemList.add(item.save());
		compound.put("Items", itemList);
		if(this.tradeType != ItemTradeType.BARTER)
			this.cost.save(compound, "Price");
		compound.putString("Customer", this.customer);
		
	}

	@Override
	protected void loadAdditional(CompoundNBT compound) {
		
		this.traderData = new TraderCategory(compound.getCompound("TraderInfo"));
		this.tradeType = ItemTradeType.fromIndex(compound.getInt("TradeType"));
		ListNBT itemList = compound.getList("Items", Constants.NBT.TAG_COMPOUND);
		this.items = new ArrayList<>();
		for(int i = 0; i < itemList.size(); ++i)
			this.items.add(new ItemData(itemList.getCompound(i)));
		if(this.tradeType != ItemTradeType.BARTER)
			this.cost.load(compound, "Price");
		this.customer = compound.getString("Customer");
		
	}

	@Override
	protected boolean canMerge(Notification other) {
		if(other instanceof ItemTradeNotification)
		{
			ItemTradeNotification itn = (ItemTradeNotification)other;
			if(!itn.traderData.matches(this.traderData))
				return false;
			if(itn.tradeType != this.tradeType)
				return false;
			if(itn.items.size() != this.items.size())
				return false;
			for(int i = 0; i < this.items.size(); ++i)
			{
				ItemData i1 = this.items.get(i);
				ItemData i2 = itn.items.get(i);
				if(!i1.itemName.getString().equals(i2.itemName.getString()))
					return false;
				if(i1.count != i2.count)
					return false;
			}
			if(itn.cost.getRawValue() != this.cost.getRawValue())
				return false;
			if(!itn.customer.equals(this.customer))
				return false;
			//Passed all checks. Allow merging.
			return true;
		}
		return false;
	}
	
	public static class ItemData
	{
		final boolean isEmpty;
		final ITextComponent itemName;
		final int count;
		
		public ItemData(ItemStack item) { this(item, ""); }
		
		public ItemData(ItemStack item, String customName) {
			this.isEmpty = item.isEmpty();
			if(this.isEmpty)
			{
				this.itemName = EasyText.empty();
				this.count = 0;
				return;
			}
			if(customName.isEmpty())
				itemName = item.getHoverName();
			else
				this.itemName = EasyText.literal(customName);
			this.count = item.getCount();
		}
		
		public ItemData(CompoundNBT compound) {
			this.isEmpty = compound.contains("Empty");
			if(this.isEmpty)
			{
				this.itemName = EasyText.empty();
				this.count = 0;
				return;
			}
			this.itemName = ITextComponent.Serializer.fromJson(compound.getString("Name"));
			this.count = compound.getInt("Count");
		}
		
		public CompoundNBT save() {
			CompoundNBT compound = new CompoundNBT();
			if(this.isEmpty)
			{
				compound.putBoolean("Empty", true);
				return compound;
			}
			compound.putString("Name", ITextComponent.Serializer.toJson(this.itemName));
			compound.putInt("Count", this.count);
			return compound;
		}
		
		public ITextComponent format() { return EasyText.translatable("log.shoplog.item.itemformat", this.count, this.itemName); }
		
		public ITextComponent formatWith(ITextComponent other) { return EasyText.translatable("log.shoplog.and", this.format(), other); }
		
		public ITextComponent formatWith(ItemData other) { return EasyText.translatable("log.shoplog.and", this.format(), other.format()); }
		
	}
	
}