package io.github.lightman314.lightmanscurrency.api.client.gui.widget.price;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextSettings;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyPriceInputWrapper;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyValueWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.trader.client.trade.price.ClientTradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.customer.TraderCustomerScreen;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.TradeData;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.edit.ITradeInteractionHandler;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.builtin.MoneyPrice;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

public final class TradePriceWidget extends AbstractMultiWidget.LateChildren {

    private final Supplier<TradeData> trade;
    private final ITradeInteractionHandler handler;
    private final Consumer<TradePriceType<?>> typeChangeHandler;
    private final Predicate<TradePriceType<?>> typeFilter;
    private final Predicate<TradePriceType<?>> displayFilter;

    private final TextSettings textSettings;
    public TextSettings getTextSettings() { return this.textSettings; }

    @Nullable
    private TradePriceWidget oldWidget;

    private final Map<DualKey,PriceInputHandler> availableHandlers;
    private PriceInputHandler currentHandler = null;
    private final List<DualKey> handlerKeys = new ArrayList<>();

    private int lastKnownSlot = 0;
    public int getSlot() { return this.lastKnownSlot; }
    public void refactorSlot(int slot) { this.lastKnownSlot = slot; }

    private DropdownWidget selectionDropdown;

    public TradePrice getCurrentPrice() {
        TradeData trade = this.trade.get();
        if(trade != null)
            return trade.getInternalPrice();
        return MoneyPrice.empty();
    }

    public TradePriceWidget(Builder builder) {
        super(builder);
        this.trade = builder.trade;
        this.typeFilter = builder.filter;
        this.displayFilter = builder.displayFilter;
        this.handler = builder.handler;
        this.typeChangeHandler = builder.typeChangeHandler;
        this.textSettings = builder.textSettings;
        this.oldWidget = builder.oldWidget;
        this.availableHandlers = this.setupHandlers();
    }

    private void sendPriceChangeMessage(FancyPacketMap message) {
        TradeData trade = this.trade.get();
        if(trade != null)
            this.handler.handlePriceEditPacket(trade,message);
    }

    private Map<DualKey,PriceInputHandler> setupHandlers() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        ImmutableMap.Builder<DualKey, PriceInputHandler> builder = ImmutableMap.builder();
        Map<DualKey,PriceInputHandler> oldHandlers = this.oldWidget != null ? this.oldWidget.availableHandlers : Map.of();
        Consumer<PriceInputHandler> b = h -> {
            if(this.typeFilter.test(h.getPriceType())) {
                DualKey key = h.getKey();
                builder.put(key,h);
                h.setup(this,this::sendPriceChangeMessage,oldHandlers.get(key));
                this.handlerKeys.add(key);
            }
        };
        for(ClientTradePrice clientType : ClientTradePrice.REGISTRY)
            clientType.collectPriceInputs(player,b);
        return builder.build();
    }

    private PriceInputHandler findDefaultHandler() {
        if(this.oldWidget != null && this.oldWidget.currentHandler != null) {
            DualKey type = this.oldWidget.currentHandler.getKey();
            if(this.availableHandlers.containsKey(type)) {
                this.oldWidget = null;
                PriceInputHandler handler = this.availableHandlers.get(type);
                if(handler.isForValue(this.getCurrentPrice()))
                    return handler;
            }
        }
        this.oldWidget = null;
        TradePrice price = this.getCurrentPrice();
        //Get last selected if it's a free/empty money type
        if(price instanceof MoneyPrice mp && (mp.getPrice().isFree() || mp.getPrice().isEmpty())) {
            DualKey lastSelected = MoneyValueWidget.getLastSelectedHandler();
            for(PriceInputHandler h : this.availableHandlers.values()) {
                if(h instanceof MoneyPriceInputWrapper wrapper && wrapper.getMoneyKey().equals(lastSelected))
                    return h;
            }
        }
        //Get from price type
        for(PriceInputHandler handler : this.availableHandlers.values()) {
            if(handler.isForValue(price))
                return handler;
        }
        //Could not find a valid handler, just return the first one
        return this.availableHandlers.values().stream().toList().getFirst();
    }

    @Override
    protected void addLateChildren(ScreenArea area) {

        this.setHandler(this.findDefaultHandler());

        int currentlySelected = -1;
        if(this.currentHandler != null)
            currentlySelected = this.handlerKeys.indexOf(this.currentHandler.getKey());

        this.selectionDropdown = this.addChild(DropdownWidget.builder()
                .atPos(area.pos.offset(10,4))
                .ofWidth(100)
                .withCurrentlySelected(currentlySelected)
                .withHandler(this::selectHandler)
                .withOptions(this.handlerOptions())
                .visible(() -> this.isVisible() && !this.availableHandlers.isEmpty())
                .build());

    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        if(this.currentHandler != null)
            this.currentHandler.extractBG(gui,area);
    }

    public void updateHandlerAfterTypeChange() {
        //The "Find Default Handler" method already obtains the current handler correctly
        //And since we clear the "old widget" once we're done with it, we won't accidentally
        //revert to old data
        this.setHandler(this.findDefaultHandler());
        //Update the dropdown so that it knows that it knows the selection has changed
        if(this.selectionDropdown != null) {
            this.selectionDropdown.setCurrentlySelected(this.handlerKeys.indexOf(this.currentHandler.getKey()));
        }
    }

    private void setHandler(@Nullable PriceInputHandler handler) {
        if(this.currentHandler == handler)
            return;
        if(this.currentHandler != null) {
            this.removeChild(this.currentHandler);
            this.currentHandler.close();
        }
        this.currentHandler = handler;
        if(this.currentHandler != null) {
            this.addChild(this.currentHandler);
            this.currentHandler.initialize(this.getArea());
        }
    }

    private List<DropdownOption> handlerOptions() {
        List<DropdownOption> options = new ArrayList<>();
        for(DualKey key : this.handlerKeys) {
            PriceInputHandler handler = this.availableHandlers.get(key);
            options.add(handler.inputOption()
                    .withVisibleCheck(() -> this.displayFilter.test(handler.getPriceType())));
        }
        return options;
    }

    private void selectHandler(int handlerIndex) {
        if(handlerIndex < 0 || handlerIndex >= this.handlerKeys.size())
            return;
        PriceInputHandler handler = this.availableHandlers.get(this.handlerKeys.get(handlerIndex));
        if(handler != null) {
            TradePriceType<?> oldType = this.currentHandler == null ? null : this.currentHandler.getPriceType();
            this.setHandler(handler);
            //Check if the price type has changed, and if so inform the type change handler/listener
            TradePriceType<?> newType = handler.getPriceType();
            if(oldType != newType)
                this.typeChangeHandler.accept(newType);
        }

    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder,TradePriceWidget> {

        private Builder() { super(TraderCustomerScreen.WIDTH,0); }

        private Supplier<TradeData> trade = () -> null;
        private Predicate<TradePriceType<?>> filter = Predicates.alwaysTrue();
        private Predicate<TradePriceType<?>> displayFilter = Predicates.alwaysTrue();
        private ITradeInteractionHandler handler = ITradeInteractionHandler.NULL;
        private Consumer<TradePriceType<?>> typeChangeHandler = t -> {};

        @Nullable
        TradePriceWidget oldWidget = null;

        private TextSettings textSettings = TextSettings.DEFAULT;

        public Builder ofWidth(int width) { this.setWidth(width); return this; }

        public Builder withOldWidget(@Nullable TradePriceWidget oldWidget) { this.oldWidget = oldWidget; return this; }

        public Builder forTrade(Supplier<TradeData> source) { this.trade = source; return this; }
        public Builder withFilter(Predicate<TradePriceType<?>> filter) { this.filter = filter; return this; }
        public Builder withOptionDisplayFilter(Predicate<TradePriceType<?>> filter) { this.displayFilter = filter; return this; }
        public Builder withHandler(ITradeInteractionHandler handler) { this.handler = handler; return this; }
        public Builder withTypeChangeHandler(Consumer<TradePriceType<?>> handler) { this.typeChangeHandler = handler; return this; }

        public Builder textColor(int textColor) { return this.textColor(textColor,textColor); }
        public Builder textColor(int textColor,int fancyTextColor) { this.textSettings = new TextSettings(textColor,fancyTextColor); return this; }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public TradePriceWidget build() { return new TradePriceWidget(this); }
    }

}
