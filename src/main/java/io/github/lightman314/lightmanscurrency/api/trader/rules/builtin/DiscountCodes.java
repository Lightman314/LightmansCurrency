package io.github.lightman314.lightmanscurrency.api.trader.rules.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.event.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.trader.persistent.PersistentDataException;
import io.github.lightman314.lightmanscurrency.api.trader.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.PriceModifyingTradeRule;
import io.github.lightman314.lightmanscurrency.api.trader.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.trader.rules.data.InteractionMemory;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.core.LCItems;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.features.tickets.TicketTradeResources;
import io.github.lightman314.lightmanscurrency.features.tickets.resources.coupon.CouponResourceHandler;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class DiscountCodes extends PriceModifyingTradeRule implements IPersistentRule {

    private static final MapCodec<DiscountCodes> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            activeField(),
            Codec.unboundedMap(Codec.STRING,DiscountRules.CODEC).fieldOf("codes").forGetter(r -> r.discounts)
    ).apply(builder,DiscountCodes::new));
    public static TradeRuleType<DiscountCodes> TYPE = new TradeRuleType<>(MAP_CODEC,DiscountCodes::new);

    private final Map<String,DiscountRules> discounts = new HashMap<>();
    private DiscountCodes() {}
    private DiscountCodes(boolean active,Map<String,DiscountRules> data) {
        super(active);
        data.forEach((code,rule) -> this.discounts.put(code,rule.initialize(this,code)));
    }

    private void setEntryChanged(String code,Consumer<FancyPacketMap.Mutable> writer) {
        this.setChanged(p -> p.modifyMap("changed",d -> d.modifyMap(code,writer)));
    }
    private void setMemoryChanged(String code,Consumer<FancyPacketMap.Mutable> writer,Predicate<ISyncingContext> target) {
        this.setChanged(p -> p.modifyMap("changed",d -> d.modifyMap(code,writer)),target);
    }
    private void setRulesDeleted(String code) {
        this.setChanged(p -> p.addToList("deleted",LCFancyPacketTypes.STRING,code));
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    public IconData getIcon() { return ItemIcon.of(LCItems.COUPON); }

    @Override
    protected boolean isAdditionalDefaultValues() { return this.discounts.isEmpty(); }

    @Override
    protected void createSyncPacket(FancyPacketMap.Mutable builder,ISyncingContext context) {
        FancyPacketMap.Mutable dataMap = FancyPacketMap.map();
        this.discounts.forEach((code,rules) ->
                dataMap.setMap(code,rules.writeToPacket(context)));
        builder.setMap("changed",dataMap);
    }

    @Override
    protected void handlePacket(FancyPacketMap message) {
        for(String deleted : message.getList("deleted",LCFancyPacketTypes.STRING))
            this.discounts.remove(deleted);
        FancyPacketMap data = message.getMap("changed");
        for(String code : data.keySet()) {
            DiscountRules rule;
            if(this.discounts.containsKey(code))
                rule = this.discounts.get(code);
            else {
                rule = new DiscountRules().initialize(this,code);
                this.discounts.put(code,rule);
            }
            rule.handleUpdatePacket(data.getMap(code));
        }
    }

    @Override
    protected void beforeTrade(TradeEvent.Pre event) {
        TradeContext context = event.getContext();
        TradeData trade = event.getTrade();
        if(!trade.isSale() || !trade.isPurchase())
            return;
        CouponResourceHandler handler = context.getCustomerResource(TicketTradeResources.COUPON_HANDLER);
        this.discounts.forEach((code,rule) -> {
            if(handler.hasCode(code)) {
                //Limit info
                if(rule.limit > 0) {
                    event.addNeutral(Component.literal("temp1"));
                    if(rule.timeLimit > 0)
                        event.addNeutral(Component.literal("temp3"));
                }
                if(rule.validLimit(event)) {
                    //Discount info
                    switch (trade.getDirection()) {
                        case SALE -> event.addHelpful(Component.literal("temp"));
                        case PURCHASE -> event.addHelpful(Component.literal("temp2"));
                    }
                }
            }
        });
    }

    @Override
    protected void tradeCost(TradeEvent.Cost event) {
        TradeContext context = event.getContext();
        TradeData trade = event.getTrade();
        if(!trade.isSale() || !trade.isPurchase())
            return;
        CouponResourceHandler handler = context.getCustomerResource(TicketTradeResources.COUPON_HANDLER);
        this.discounts.forEach((code,rule) -> {
            if(rule.validLimit(event) && handler.hasCode(code)) {
                context.addFlag(TYPE.getKey(),rule.getDiscountKey());
                //Give discount
                switch (trade.getDirection()) {
                    case SALE -> event.giveDiscount(rule.discount);
                    case PURCHASE -> event.hikePrice(rule.discount);
                }
            }
        });
    }

    @Override
    protected void extraCost(TradeEvent.AfterPriceExchange event) {
        TradeContext context = event.getContext();
        TradeData trade = event.getTrade();
        if(!trade.isSale() || !trade.isPurchase())
            return;
        CouponResourceHandler handler = context.getCustomerResource(TicketTradeResources.COUPON_HANDLER);
        this.discounts.forEach((code,rule) -> {
            if(rule.validLimit(event) && context.consumeFlag(TYPE.getKey(),rule.getDiscountKey())) {
                int taken = handler.collectCoupon(code,1,context.getTransaction());
                if(taken != 1) //Abort the trade if the players coupon was taken as part of the trades price
                    event.setFailedToTake();
                else //Flag the trade as successfully consuming the coupon for memory purposes
                    context.addFlag(TYPE.getKey(),rule.getConsumedKey());
            }
        });
    }

    @Override
    protected void afterTrade(TradeEvent.Post event) {
        TradeContext context = event.getContext();
        this.discounts.forEach((code,rule) -> {
            //Add the interaction to memory if relevant
            if(context.consumeFlag(TYPE.getKey(),rule.getConsumedKey())) {
                if(rule.limit > 0)
                    rule.memory.addEntry(event);
            }
            rule.memory.clearExpiredData(rule.timeLimit);
        });
    }

    @Override
    protected void handlePlayerInteraction(FancyPacketMap request) {
        //TODO
    }

    @Override
    protected void resetAdditionalToDefault() {
        this.discounts.clear();
    }

    @Override
    public void writePersistentData(ValueOutput output) throws PersistentDataException {
        this.encodeSettings(output);
    }

    @Override
    public void loadPersistentData(ValueInput input) throws PersistentDataException {
        this.decodeSettings(input);
        if(this.discounts.isEmpty())
            throw new PersistentDataException("Must have at least one discount code!");
    }

    @Override
    public void writePersistentSaveData(ValueOutput ouput) {
        this.discounts.forEach((code,rules) ->
                ouput.store(code,InteractionMemory.CODEC,rules.memory));
    }

    @Override
    public void readPersisentSaveData(ValueInput input) {
        this.discounts.forEach((code,rules) ->
                input.read(code,InteractionMemory.CODEC).ifPresent(rules.memory::copyFrom));
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        this.discounts.forEach((code,rule) -> {
            ValueOutput child = output.child(code);
            rule.saveSettings(output);
        });
    }

    @Override
    public void decodeSettings(ValueInput input) {
        this.discounts.clear();
        for(String code : input.keySet()) {
            input.child(code).ifPresent(child -> {
                DiscountRules rules = new DiscountRules();
                rules.loadSettings(child);
                this.discounts.put(code,rules.initialize(this,code));
            });
        }
    }

    public static final class DiscountRules implements ISidedContext {

        public static final Codec<DiscountRules> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("discount").forGetter(n -> n.discount),
                Codec.INT.fieldOf("limit").forGetter(n -> n.limit),
                Codec.LONG.fieldOf("timer").forGetter(n -> n.timeLimit),
                InteractionMemory.CODEC.fieldOf("memory").forGetter(n -> n.memory)
        ).apply(builder,DiscountRules::new));
        private static final Codec<DiscountRules> PARTIAL = RecordCodecBuilder.create(builder -> builder.group(
                Codec.INT.fieldOf("discount").forGetter(n -> n.discount),
                Codec.INT.fieldOf("limit").forGetter(n -> n.limit),
                Codec.LONG.fieldOf("timer").forGetter(n -> n.timeLimit)
        ).apply(builder,DiscountRules::new));

        private String getDiscountKey() { return this.code + ";" + this.discount; }
        private String getConsumedKey() { return this.code + ";" + this.discount + "!"; }

        private ISidedContext context = ISidedContext.LOGICAL_CLIENT;
        public DiscountRules initialize(DiscountCodes rule,String code) {
            this.context = rule;
            this.code = code;
            this.memory.withListener((builder,target) -> rule.setMemoryChanged(this.code,builder,target));
            return this;
        }
        @Override
        public boolean isClient() { return this.context.isClient(); }

        private String code = "";
        private void updateCode(String newCode) { this.code = newCode; }

        public int discount;
        public int limit;
        public long timeLimit;
        public final InteractionMemory memory;
        public DiscountRules() { this(10,0,0,new InteractionMemory()); }
        private DiscountRules(int discount,int limit,long timeLimit) { this(discount,limit,timeLimit,new InteractionMemory()); }
        private DiscountRules(int discount,int limit,long timeLimit,InteractionMemory memory) {
            this.discount = discount;
            this.limit = limit;
            this.timeLimit = timeLimit;
            this.memory = memory.setSidedContext(this);
        }

        public boolean validLimit(TradeEvent event) { return this.limit <= 0 || this.memory.getCount(event,this.timeLimit) < this.limit; }

        public FancyPacketMap writeToPacket(ISyncingContext context) {
            return FancyPacketMap.map().setInt("discount",this.discount)
                    .setInt("limit",this.limit)
                    .setLong("timeLimit",this.timeLimit)
                    .setMap("memory",this.memory.createFullSyncPacket(context));
        }
        public void handleUpdatePacket(FancyPacketMap packet) {
            if(packet.contains("discount"))
                this.discount = packet.getInt("discount");
            if(packet.contains("limit"))
                this.limit = packet.getInt("limit");
            if(packet.contains("timeLimit"))
                this.timeLimit = packet.getLong("timeLimit");
            if(packet.contains("memory"))
                this.memory.handlePacket(packet.getMap("memory"));
        }

        public void saveSettings(ValueOutput output) {
            output.putInt("discount",this.discount);
            if(this.limit > 0)
                output.putInt("limit",this.limit);
            if(this.timeLimit > 0)
                output.putLong("timer",this.timeLimit);
        }

        public void loadSettings(ValueInput input) {
            this.discount = input.getIntOr("discount",10);
            this.limit = input.getIntOr("limit",0);
            this.timeLimit = input.getLongOr("timer",0);
        }

    }

}
