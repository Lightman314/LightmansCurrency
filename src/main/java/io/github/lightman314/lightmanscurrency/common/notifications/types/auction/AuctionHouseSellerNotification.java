package io.github.lightman314.lightmanscurrency.common.notifications.types.auction;

import java.util.ArrayList;
import java.util.List;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.AuctionHouseCategory;
import io.github.lightman314.lightmanscurrency.common.notifications.data.ItemData;
import io.github.lightman314.lightmanscurrency.common.traders.auction.tradedata.AuctionTradeData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class AuctionHouseSellerNotification extends Notification {

    public static final NotificationType<AuctionHouseSellerNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("auction_house_seller"),AuctionHouseSellerNotification::new);

    List<ItemData> items;
    MoneyValue highestBid = MoneyValue.empty();
    MoneyValue payment = MoneyValue.empty();
    MoneyValue fee = MoneyValue.empty();

    String customer;

    private AuctionHouseSellerNotification() { }

    public AuctionHouseSellerNotification(AuctionTradeData trade, MoneyValue payment, MoneyValue fee) {

        this.highestBid = trade.getLastBidAmount();
        this.payment = payment;
        this.fee = fee;

        this.customer = trade.getLastBidPlayer().getName(false);

        this.items = new ArrayList<>();
        for(int i = 0; i < trade.getAuctionItems().size(); ++i)
            this.items.add(new ItemData(trade.getAuctionItems().get(i)));

    }

    @Override
    protected NotificationType<AuctionHouseSellerNotification> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return AuctionHouseCategory.INSTANCE; }

    @Override
    public List<Component> getMessageLines() {
        Component itemText = ItemData.getItemNames(this.items);

        Component cost = this.highestBid.getText("0");

        //Create log from stored data
        Component line1 = LCText.NOTIFICATION_AUCTION_SELLER.get(this.customer, itemText, cost);

        if(this.fee.isEmpty())
            return List.of(line1);

        Component line2 = LCText.NOTIFICATION_AUCTION_SELLER_FEE.get(this.payment.getText("0"),this.fee.getText("0"));
        return List.of(line1,line2);

    }

    @Override
    protected void saveAdditional(CompoundTag compound) {

        ListTag itemList = new ListTag();
        for(ItemData item : this.items)
            itemList.add(item.save());
        compound.put("Items", itemList);
        compound.put("Price", this.highestBid.save());
        if(!this.fee.isEmpty())
        {
            compound.put("Payment",this.payment.save());
            compound.put("Fee",this.fee.save());
        }
        compound.putString("Customer", this.customer);

    }

    @Override
    protected void loadAdditional(CompoundTag compound) {

        ListTag itemList = compound.getList("Items", Tag.TAG_COMPOUND);
        this.items = new ArrayList<>();
        for(int i = 0; i < itemList.size(); ++i)
            this.items.add(ItemData.load(itemList.getCompound(i)));
        this.highestBid = MoneyValue.safeLoad(compound, "Price");
        if(compound.contains("Payment") && compound.contains("Fee"))
        {
            this.payment = MoneyValue.load(compound.getCompound("Payment"));
            this.fee = MoneyValue.load(compound.getCompound("Fee"));
        }
        this.customer = compound.getString("Customer");

    }

    @Override
    protected boolean canMerge(Notification other) { return false; }

}