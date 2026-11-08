package io.github.lightman314.lightmanscurrency.features.tickets.trader;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IItemStorageFilter;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketHelper;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket.TicketResourceHandler;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketTradeResources;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketID;
import io.github.lightman314.lightmanscurrency.features.tickets.data.TicketUses;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.ticket_stub.TicketStubCollector;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeFailedException;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeResult;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeDirection;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeEditContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.TradeSlot;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Set;

public class TicketPrice extends TradePrice implements IItemStorageFilter {

    private static final MapCodec<TicketPrice> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("master").forGetter(p -> Optional.ofNullable(p.masterTicket)),
            Codec.LONG.optionalFieldOf("ticketID",-1L).forGetter(TicketPrice::getTicketID),
            Codec.INT.fieldOf("ticketColor").forGetter(TicketPrice::getTicketColor),
            Codec.INT.fieldOf("count").forGetter(TicketPrice::getCount),
            Codec.BOOL.fieldOf("storeStubs").forGetter(TicketPrice::storeTicketStubs)
    ).apply(builder,TicketPrice::new));
    private static final StreamCodec<RegistryFriendlyByteBuf,TicketPrice> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.optional(ByteBufCodecs.registry(Registries.ITEM)), p -> Optional.ofNullable(p.masterTicket),
            ByteBufCodecs.LONG,TicketPrice::getTicketID,
            ByteBufCodecs.INT,TicketPrice::getTicketColor,
            ByteBufCodecs.INT,TicketPrice::getCount,
            ByteBufCodecs.BOOL,TicketPrice::storeTicketStubs,
            TicketPrice::new);

    public static final TradePriceType<TicketPrice> TYPE = new TradePriceType<>(MAP_CODEC,STREAM_CODEC,TicketPrice::new);

    public static final TextEntry OPTION_NAME = TextEntry.priceOption(TYPE);
    public static final TextEntry GUI_STORE_TICKET_STUBS = TextEntry.gui(LCApi.MODID,"ticket_price.store_stubs");

    public static final TextEntry TOOLTIP_USES = TextEntry.tooltip(LCApi.MODID,"ticket_price.uses");

    @Nullable
    private Item masterTicket = null;
    public Optional<Item> getConsumableTicket() {
        try {
            if(this.masterTicket != null)
                return Optional.ofNullable(TicketHelper.getConsumableTicketFromMaster(this.masterTicket));
        } catch (IllegalStateException ignored) {}
        return Optional.empty();
    }
    public ItemStack getDisplayStack() {
        Optional<Item> set = this.getConsumableTicket();
        if(set.isPresent()) {
            ItemStack stack = TicketHelper.createTicket(set.get(),this.ticketID,this.ticketColor,TicketUses.UNLIMITED);
            stack.setCount(this.count);
            return stack;
        }
        return ItemStack.EMPTY;
    }
    private long ticketID = -1;
    public long getTicketID() { return this.ticketID; }
    private int ticketColor = -1;
    private int getTicketColor() { return this.ticketColor; }
    private int count = 1;
    public int getCount() { return this.count; }
    private boolean storeStubs = false;
    public boolean storeTicketStubs() { return this.storeStubs; }

    private TicketPrice() {}
    private TicketPrice(Optional<Item> masterTicket,long ticketID,int ticketColor,int count,boolean storeStubs) { this(masterTicket.orElse(null),ticketID,ticketColor,count,storeStubs); }
    private TicketPrice(@Nullable Item masterTicket,long ticketID,int ticketColor,int count,boolean storeStubs) {
        this.masterTicket = masterTicket;
        this.ticketID = ticketID;
        this.ticketColor = ticketColor;
        this.count = count;
        this.storeStubs = storeStubs;
    }

    @Override
    public TradePriceType<?> getType() { return TYPE; }

    @Override
    public TradePrice copy() {
        return new TicketPrice(this.masterTicket,this.ticketID,this.ticketColor,this.count,this.storeStubs);
    }

    @Override
    public Set<TradeDirection> supportedTradeTypes() { return SALE_ONLY_SET; }

    @Override
    public boolean maySupportTrade(TraderData trader,TradeData trade) { return true; }

    @Override
    public boolean isValid() { return this.masterTicket != null && TicketHelper.getConsumableTicketFromMaster(this.masterTicket) != null && this.ticketID >= 0 && this.count > 0; }

    @Override
    protected TransferResult transferFromCustomerToTrader(TradeContext context) throws TradeFailedException {
        if(!this.isValid())
            throw new TradeFailedException(TradeResult.FAIL_INVALID_TRADE);
        TicketResourceHandler ticketHandler = context.getCustomerResource(TicketTradeResources.TICKET_HANDLER);
        TicketStubCollector collector = this.storeStubs ? getTraderStubCollector(context) : ticketHandler;
        int collected = ticketHandler.collectTicket(this.ticketID,this.count,collector,context.getTransaction());
        if(collected != this.count)
            return TransferResult.FAILED_TO_TAKE;
        return TransferResult.success(new TicketPriceReceipt(this.ticketID));
    }

    @Override
    protected TransferResult transferFromTraderToCustomer(TradeContext context) { return TransferResult.FAILED_TO_TAKE; }

    private static TicketStubCollector getTraderStubCollector(TradeContext context) {
        return context.hasTraderResource(TicketTradeResources.STUB_COLLECTOR) ? context.getTraderResource(TicketTradeResources.STUB_COLLECTOR) : context.getTraderResource(TicketTradeResources.TICKET_HANDLER);
    }

    @Override
    public long getAvailableStock(TradeContext context) {
        if(!this.isValid())
            return 0;
        TicketResourceHandler ticketHandler = context.getCustomerResource(TicketTradeResources.TICKET_HANDLER);
        return ticketHandler.getCount(this.ticketID) / this.count;
    }

    @Override
    public boolean showOutOfSpaceWarning(TradeContext context) {
        if(this.storeStubs) {
            Optional<Item> ticketSet = this.getConsumableTicket();
            if(ticketSet.isPresent() && this.ticketID >= 0) {
                TicketStubCollector collector = getTraderStubCollector(context);
                //Try to place fake ticket stubs
                try(Transaction tx = Transaction.open(context.getTransaction())) {
                    int collected = collector.collectTicketStub(new ItemStack(ticketSet.get()),this.count,tx);
                    if(collected < this.count)
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean showCannotAffordWarning(TradeContext context) {
        if(!this.isValid())
            return false;
        try(Transaction tx = Transaction.open(context.getTransaction())) {
            return context.getCustomerResource(TicketTradeResources.TICKET_HANDLER).collectTicket(this.ticketID,this.count,tx) != this.count;
        }
    }

    @Override
    public boolean supportsModification() { return false; }

    @Override
    public TradePrice percentageOfValue(int percentage) { return this; }

    @Override
    public boolean onClickInteraction(Player player,TradeData trade,TradeSlot slot,int button,ItemStack heldItem,TradeEditContext context) {
        boolean clear = heldItem.isEmpty();
        if(context.hasShiftDown() || heldItem.isEmpty() && !this.isValid()) {
            ITradeInteractionHandler handler = context.handler();
            if(handler.isSimpleEdit()) {
                //Open the advanced edit menu
                context.handler().openAdvancedEdit(trade,slot);
            }
            if(handler.isAdvancedEdit() && !handler.isSelected(slot)) {
                //If the ticket slot isn't currently selected, change the selection
                handler.changeSelection(slot);
            }
            return true;
        }
        if(TicketHelper.getConsumableTicketFromMaster(heldItem) != null) {
            TicketID id = heldItem.get(LCDataComponents.TICKET_ID);
            if(id != null) {
                clear = false;
                if(this.masterTicket == heldItem.getItem() && this.ticketID == id.ticketID()) {
                    if(button == 0)
                        this.count = heldItem.getCount();
                    else
                        this.count = Math.min(this.count + 1,TicketUses.DEFAULT_USE_LIMIT);
                    this.setChanged();
                }
                else {
                    this.masterTicket = heldItem.getItem();
                    this.ticketID = id.ticketID();
                    this.ticketColor = heldItem.getOrDefault(DataComponents.DYED_COLOR,new DyedItemColor(0xFFFFFF)).rgb();
                    if(button == 0)
                        this.count = 1;
                    else
                        this.count = heldItem.getCount();
                    this.setChanged();
                }
            }
        }
        if(clear) {
            if(this.masterTicket == null)
                return false;
            this.masterTicket = null;
            this.ticketID = -1;
            this.ticketColor = -1;
            this.setChanged();
        }
        return true;
    }

    @Override
    public boolean onScrollInteraction(Player player, TradeData trade, TradeSlot slot, float scroll, ItemStack heldItem, TradeEditContext context) {
        if(this.isValid()) {
            int newCount = Math.clamp(scroll > 0f ? this.count + 1 : this.count - 1,1,TicketUses.DEFAULT_USE_LIMIT);
            if(newCount != this.count) {
                this.count = newCount;
                this.setChanged();
                return true;
            }
        }
        return false;
    }

    @Override
    public void handleCustomEditMessage(FancyPacketMap packet,TradeSlot slot) {
        if(packet.contains("setStoreStub")) {
            this.storeStubs = packet.getBoolean("setStoreStub");
            this.setChanged();
        }
    }

    //Allow ticket stubs in the traders item storage
    @Override
    public boolean itemAllowedInStorage(ItemResource resource) {
        Optional<Item> ticket = this.getConsumableTicket();
        if(ticket.isPresent())
            return resource.is(TicketHelper.getTicketStub(ticket.get()));
        return false;
    }

}