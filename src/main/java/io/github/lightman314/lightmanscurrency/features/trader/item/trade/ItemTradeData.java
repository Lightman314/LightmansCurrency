package io.github.lightman314.lightmanscurrency.features.trader.item.trade;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.Products;
import com.mojang.datafixers.util.Function3;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.ExtractionResults;
import io.github.lightman314.lightmanscurrency.api.helpers.ResourceHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDataType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDataWithRules;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.resources.BuiltInResourceTypes;
import io.github.lightman314.lightmanscurrency.api.trader.trade.settings.SettingsIOTrade;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.features.trader.item.TradeItem;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IItemStorageFilter;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ItemTradeData extends TradeDataWithRules implements IItemStorageFilter, SettingsIOTrade {

    private static final MapCodec<ItemTradeData> MAP_CODEC = buildCodec(ItemTradeData::new);
    public static final Codec<ItemTradeData> CODEC = MAP_CODEC.codec();

    public static final TradeDataType<ItemTradeData> TYPE = new TradeDataType<>(MAP_CODEC);

    public static <T extends ItemTradeData> Products.P3<RecordCodecBuilder.Mu<T>,TradePrice,List<TradeItem>,Map<TradeRuleType<?>,TradeRule>> baseFields(RecordCodecBuilder.Instance<T> builder) {
        return builder.group(TradePrice.CODEC.fieldOf("price").forGetter(ItemTradeData::getInternalPrice),
                TradeItem.CODEC.listOf().fieldOf("items").forGetter(t -> t.sellItems),
                tradeRuleArg());
    }
    public static <T extends ItemTradeData> MapCodec<T> buildCodec(Function3<TradePrice,List<TradeItem>,Map<TradeRuleType<?>,TradeRule>,T> factory) { return RecordCodecBuilder.mapCodec(builder -> baseFields(builder).apply(builder,factory)); }

    @Override
    public TradeDataType<?> getType() { return TYPE; }

    public ItemTradeData() { super(); }
    protected ItemTradeData(TradePrice price, List<TradeItem> items,Map<TradeRuleType<?>, TradeRule> rules) {
        super(price,rules);
        TradeItem.loadList(this.sellItems,items);
    }

    @Override
    public boolean isCustomerReady() {
        if (!this.getInternalPrice().isValid(this.getTrader(),this))
            return false;
        return this.sellItems.stream().anyMatch(TradeItem::isValid);
    }

    private TradeDirection type = TradeDirection.SALE;
    @Override
    public TradeDirection getDirection() { return this.type; }
    public boolean setType(TradeDirection type) {
        if(type.isOther() || type == this.type)
            return false;
        this.type = type;
        this.setChanged(builder -> builder.setEnum("trade_type",this.type));
        return true;
    }

    final ImmutableList<TradeItem> sellItems = TradeItem.createList(2,this::mutateSellItem);
    private boolean invalidSlot(int slot) { return slot < 0 || slot >= 2; }
    public TradeItem getItem(int slot) {
        if(this.invalidSlot(slot))
            return TradeItem.create();
        return this.sellItems.get(slot);
    }
    public List<TradeItem> getCombinedItems() {
        return TradeItem.combineMatching(this.sellItems);
    }
    public void setItem(int slot,ItemStack item) {
        if(this.invalidSlot(slot))
            return;
       this.sellItems.get(slot).setStack(item);
        this.setItemChanged(slot);
    }
    public void growItem(int slot) {
        TradeItem item = this.getItem(slot);
        if(item.grow(1))
            this.setItemChanged(slot);
    }

    public boolean alwayStrictItem(int slot) { return false; }

    @Nullable
    public Identifier getBackgroundOverride() { return null; }
    @Nullable
    public Identifier getItemSelectionFilter() { return null; }

    protected ItemResource mutateSellItem(ItemResource resource) {
        return resource;
    }

    public final void setItemChanged(int slot) {
        this.setChanged(builder -> builder.set("item_" + slot,LCFancyPacketTypes.TRADE_ITEM,this.getItem(slot)));
    }

    @Override
    public void getAdditionalFullPacket(FancyPacketMap.Mutable packet, ISyncingContext context) {
        packet.setEnum("trade_type",this.type);
        packet.set("item_0",LCFancyPacketTypes.TRADE_ITEM,this.getItem(0));
        packet.set("item_1",LCFancyPacketTypes.TRADE_ITEM,this.getItem(1));
    }

    @Override
    public void handleAdditionalPacket(FancyPacketMap packet) {
        if(packet.contains("trade_type"))
            this.type = packet.getEnum("trade_type",TradeDirection.class);
        if(packet.contains("item_0"))
            this.getItem(0).copyFrom(packet.get("item_0",LCFancyPacketTypes.TRADE_ITEM));
        if(packet.contains("item_1"))
            this.getItem(1).copyFrom(packet.get("item_1",LCFancyPacketTypes.TRADE_ITEM));
    }


    @Override
    public final long getStock(TradeContext context) {
        if(this.isPurchase())
            return this.getPrice(context).getAvailableStock(context);
        //Calculate the number of items that can be obtained from the trade
        try(Transaction tx = Transaction.open(context.getTransaction())) {
            return this.calculateTradeStock(context.getTraderResource(BuiltInResourceTypes.ITEM),tx);
        }
    }

    @Override
    public boolean showOutOfSpaceWarning(TradeContext context) {
        List<TradeItem> items = this.getCombinedItems();
        if(items.isEmpty())
            return false;
        try(Transaction tx = Transaction.open(context.getTransaction())) {
            ResourceHandler<ItemResource> customerItems = context.getCustomerResource(BuiltInResourceTypes.ITEM);
            List<ResourceStack<ItemResource>> queryItems = new ArrayList<>();
            for(TradeItem item : items) {
                ExtractionResults<ItemResource> results = ResourceHelper.extractFirstToTarget(customerItems,item,item.getCount(),tx);
                if(results.totalCount() != item.getCount()) {
                    //Extract the results manually
                    queryItems.add(item.getDummyStack());
                }
                else
                    queryItems.addAll(results.extracted());
            }
            queryItems = ResourceHelper.mergeResources(queryItems);
            ResourceHandler<ItemResource> traderItems = context.getTraderResource(BuiltInResourceTypes.ITEM);
            for(ResourceStack<ItemResource> stack : queryItems) {
                int inserted = traderItems.insert(stack.resource(),stack.amount(),tx);
                if(inserted != stack.amount())
                    return true;
            }
        }
        return false;
    }

    protected long calculateTradeStock(ResourceHandler<ItemResource> storage, Transaction transaction) {
        List<TradeItem> items = this.getCombinedItems();
        if(items.isEmpty())
            return 0;
        long minStock = Long.MAX_VALUE;
        for(TradeItem item : items) {
            long stock = ResourceHelper.getResourceCount(storage,item,transaction) / item.getCount();
            minStock = Math.min(minStock,stock);
        }
        return minStock;
    }

    @Override
    public boolean processTradeClick(Player player,TradeSlot slot,int button,ItemStack heldItem,TradeEditContext context) {
        if(slot.isPriceSlot(this.type))
            return this.getInternalPrice().onClickInteraction(player,this,slot,button,heldItem,context);
        //Simply open the advanced edit tab if clicking outside the input/output slot
        if(!slot.isInputOrOutput()) {
            if(context.handler().isSimpleEdit()) {
                //Open with the default price slot if clicking outside the expected area
                context.handler().openAdvancedEdit(this,TradeSlot.defaultPriceSlot(this.type));
                return true;
            }
            return false;
        }
        int itemSlot = slot.slot();
        if(this.invalidSlot(itemSlot))
            return false;

        TradeItem item = this.getItem(itemSlot);
        //Selection interactions (if shift is held, or both the held and actual item are empty
        if(context.hasShiftDown() || (item.isEmpty() && heldItem.isEmpty()))
        {
            if(context.handler().isSimpleEdit())
            {
                //Open the advanced edit menu
                context.handler().openAdvancedEdit(this,slot);
            }
            if(context.handler().isAdvancedEdit() && !context.handler().isSelected(slot))
            {
                //If this item slot isn't currently selected, change to the selection
                context.handler().changeSelection(slot);
            }
            return true;
        }
        //Normal item interactions
        if(item.isEmpty() || !ItemStack.isSameItemSameComponents(item.getStack(),heldItem))
        {
            //Define the item
            ItemStack newStack = heldItem.copy();
            if(button == 1) //Right-click -> only place one
                newStack.setCount(1);
            item.setStack(newStack);
        }
        else
        {
            //Completely override the count if it's a left-click (unless the count is the same)
            if(button == 0 && item.getCount() != heldItem.getCount())
                item.setCount(heldItem.getCount());
            else if(!item.grow(1)) //Otherwise grow by one (and abort the packet if we cannot do so)
                return false;
        }
        this.setItemChanged(itemSlot);
        return true;
    }

    @Override
    public boolean processTradeScroll(Player player,TradeSlot slot,float deltaY,ItemStack heldItem,TradeEditContext context) {
        if(slot.isPriceSlot(this.type))
            return this.getInternalPrice().onScrollInteraction(player,this,slot,deltaY,heldItem,context);
        else if(context.handler().isAdvancedEdit())
        {
            int itemSlot = slot.slot();
            TradeItem stack = this.getItem(itemSlot);
            if(!stack.isEmpty())
            {
                if(deltaY > 0 && stack.grow(1))
                {
                    this.setItemChanged(itemSlot);
                    return true;
                }
                if(deltaY < 0 && stack.shrink(1))
                {
                    this.setItemChanged(itemSlot);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean itemAllowedInStorage(ItemResource resource) { return TradeItem.allowedInStorage(this.sellItems,resource); }
    @Override
    public boolean denyItemExtraction(ItemResource resource) {
        //Deny extraction of sale items
        if(this.isSale())
            return !TradeItem.allowedInStorage(this.sellItems,resource);
        return true;
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.store("price",TradePrice.CODEC,this.getInternalPrice());
        output.store("direction",TradeDirection.CODEC,this.type);
        output.store("item1",TradeItem.CODEC,this.getItem(0));
        output.store("item2",TradeItem.CODEC,this.getItem(2));
    }

    @Override
    public void decodeSettings(ValueInput data) {
        this.setPrice(data.read("price",TradePrice.CODEC).orElse(MoneyPrice.empty()));
        this.setType(data.read("direction",TradeDirection.CODEC).orElse(TradeDirection.SALE));
        this.getItem(0).copyFrom(data.read("item1",TradeItem.CODEC).orElse(TradeItem.create()));
        this.setItemChanged(0);
        this.getItem(1).copyFrom(data.read("item2",TradeItem.CODEC).orElse(TradeItem.create()));
        this.setItemChanged(1);
    }

}
