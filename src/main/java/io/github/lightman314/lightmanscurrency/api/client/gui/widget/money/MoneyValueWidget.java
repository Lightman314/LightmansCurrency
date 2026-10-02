package io.github.lightman314.lightmanscurrency.api.client.gui.widget.money;

import com.google.common.base.Predicates;
import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.AbstractMultiWidget;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextSettings;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyValueType;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Predicate;

public final class MoneyValueWidget extends AbstractMultiWidget.LateChildren {

    public static final int HEIGHT = 69;
    public static final int WIDTH = 176;
    public static final int HALF_WIDTH = WIDTH / 2;

    public static final SizedSprite.Builder SPRITE_FREE_TOGGLE = LCSprites.FREE_TOGGLE;

    static DualKey lastSelectedHandler = DualKey.create(LCApi.id("coins"),"main");
    public static DualKey getLastSelectedHandler() { return lastSelectedHandler; }

    @Nullable
    private MoneyValueWidget oldWidget;
    private final Consumer<MoneyValue> handler;
    private final Consumer<MoneyValueWidget> typeChangeListener;
    private final Predicate<MoneyValue> filter;
    private final boolean allowFree;
    private final BooleanSupplier allowHandlerChange;
    public boolean canChangeHandlers() { return !this.locked && this.allowHandlerChange.getAsBoolean(); }
    private final TextSettings textSettings;
    public TextSettings getTextSettings() { return this.textSettings; }

    private boolean locked = false;
    public boolean isLocked() { return this.locked; }
    public void lock() { this.locked = true; }
    public void unlock() { this.locked = false; }

    private MoneyValue currentValue;
    public MoneyValue getCurrentValue() { return this.currentValue; }

    private MoneyValueWidget(Builder builder) {
        super(builder);
        this.oldWidget = builder.oldWidget;
        this.handler = builder.handler;
        this.typeChangeListener = builder.typeChangeListener;
        this.filter = builder.filter;
        this.allowFree = builder.allowFree;
        this.allowHandlerChange = builder.allowHandlerChange;
        this.textSettings = builder.textSettings;

        this.currentValue = this.oldWidget == null ? builder.startingValue : this.oldWidget.currentValue;
        this.availableHandlers = this.setupHandlers();
    }

    private final Map<DualKey,MoneyInputHandler> availableHandlers;
    private final List<DualKey> handlerKeys = new ArrayList<>();
    @Nullable
    private MoneyInputHandler currentHandler = null;

    public DualKey getCurrentHandlerType() { return this.currentHandler == null ? DualKey.create(Identifier.fromNamespaceAndPath("",""),"") : this.currentHandler.getKey(); }
    @Nullable
    public MoneyInputHandler getCurrentHandler() { return this.currentHandler; }
    public void tryMatchHandler(MoneyValue value) {
        this.tryMatchValue(value,true);
    }

    private Map<DualKey,MoneyInputHandler> setupHandlers() {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        ImmutableMap.Builder<DualKey,MoneyInputHandler> builder = ImmutableMap.builder();
        Map<DualKey,MoneyInputHandler> oldHandlers = this.oldWidget != null ? this.oldWidget.availableHandlers : Map.of();
        Consumer<MoneyInputHandler> b = h -> {
            DualKey key = h.getKey();
            builder.put(key,h);
            h.setup(this,this::widgetChangedValue,oldHandlers.get(key));
            this.handlerKeys.add(key);
        };
        for(ClientMoneyValueType moneyType : ClientMoneyValueType.REGISTRY)
            moneyType.collectMoneyInputs(player,b);
        return builder.build();
    }

    @Nullable
    private MoneyInputHandler findDefaultHandler() {
        if(this.oldWidget != null && this.oldWidget.currentHandler != null)
        {
            DualKey type = this.oldWidget.currentHandler.getKey();
            if(this.availableHandlers.containsKey(type))
            {
                this.oldWidget = null;
                MoneyInputHandler handler = this.availableHandlers.get(type);
                if(handler.isForValue(this.currentValue))
                    return handler;
            }
        }
        this.oldWidget = null;
        MoneyValue value = this.currentValue;
        //Get last selected
        if(value.isEmpty() || value.isFree()) {
            if(this.availableHandlers.containsKey(lastSelectedHandler))
                return this.availableHandlers.get(lastSelectedHandler);
        }
        //Get type from the money type
        else {
            DualKey type = value.getKey();
            if(this.availableHandlers.containsKey(type))
                return this.availableHandlers.get(type);
            else
            {
                for(MoneyInputHandler handler : this.availableHandlers.values()) {
                    if(handler.isForValue(value))
                        return handler;
                }
            }
        }
        if(this.availableHandlers.isEmpty())
            return null;
        //Could not find a valid handler, just return the first one
        return this.availableHandlers.values().stream().toList().getFirst();
    }

    @Override
    protected void addLateChildren(ScreenArea area) {

        this.setHandler(this.findDefaultHandler());

        this.addChild(SpriteButton.builder()
                .atPos(area.pos.offset(area.width - 14,4))
                .withSprite(SPRITE_FREE_TOGGLE)
                .onPress(this::toggleFree)
                .visible(v -> this.isVisible() && this.allowFree)
                .build());

        this.addChild(DropdownWidget.builder()
                .atPos(area.pos.offset(10,4))
                .ofWidth(100)
                .withCurrentlySelected(this.handlerKeys.indexOf(this.currentHandler.getKey()))
                .withHandler(this::selectHandler)
                .withOptions(this.handlerOptions())
                .visible(() -> this.isVisible() && !this.availableHandlers.isEmpty() && this.canChangeHandlers())
                .build());

    }

    private void checkHandler() {
        this.tryMatchValue(this.currentValue,false);
    }

    private void tryMatchValue(MoneyValue value,boolean updateWidget) {
        if(this.currentValue.isFree() || this.currentValue.isEmpty())
            return;
        if(this.currentHandler != null && this.currentHandler.isForValue(value))
        {
            if(updateWidget)
                this.currentHandler.tryMatchValue(value);
            return;
        }
        if(this.availableHandlers.containsKey(value.getKey()))
            this.setHandler(this.availableHandlers.get(value.getKey()));
        else
        {
            for(MoneyInputHandler handler : this.availableHandlers.values())
            {
                if(handler.isForValue(value))
                {
                    this.setHandler(handler);
                    return;
                }
            }
        }
    }

    private void setHandler(MoneyInputHandler handler) {
        if(this.currentHandler == handler)
            return;
        if(this.currentHandler != null)
        {
            this.removeChild(this.currentHandler);
            this.currentHandler.close();
        }
        this.currentHandler = handler;
        if(this.currentHandler != null) {
            this.addChild(this.currentHandler);
            this.currentHandler.initialize(this.getArea());
            lastSelectedHandler = this.currentHandler.getKey();
        }

        this.markHandlerChanged();
    }

    public void markHandlerChanged() {
        this.typeChangeListener.accept(this);
    }

    private List<DropdownOption> handlerOptions() {
        List<DropdownOption> options = new ArrayList<>();
        for(DualKey key : this.handlerKeys)
            options.add(this.availableHandlers.get(key).inputOption());
        return options;
    }

    private void selectHandler(int handlerIndex) {
        if(handlerIndex < 0 || handlerIndex >= this.handlerKeys.size())
            return;
        MoneyInputHandler handler = this.availableHandlers.get(this.handlerKeys.get(handlerIndex));
        if(handler != null)
            this.setHandler(handler);
    }

    @Override
    protected void renderTickInternal(ScreenPosition mousePos) {
        if(this.currentHandler != null)
            this.currentHandler.renderTick(mousePos);
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {

        //Render the widget
        if(this.currentHandler != null)
            this.currentHandler.extractBG(gui,area);

        //Render the current price in the top-right corner
        Component text = this.currentValue.getText();
        int width = gui.getFont().width(text);
        int freeButtonOffset = this.allowFree ? 15 : 5;
        gui.text(text,this.width - freeButtonOffset - width,5,this.textSettings.textColor(),false);

    }

    private void toggleFree() {
        MoneyValue newValue = this.currentValue.isFree() ? MoneyValue.empty() : MoneyValue.free();
        this.changeValue(newValue,true,true);
    }

    public void changeValue(MoneyValue value) { this.changeValue(value,true,false); }

    private void widgetChangedValue(MoneyValue value) { this.changeValue(value,false,true); }

    private void changeValue(MoneyValue value,boolean sendToWidget,boolean sendToListener) {
        if(!this.filter.test(value))
            return;
        this.currentValue = value;
        this.checkHandler();
        if(this.currentHandler != null && sendToWidget)
            this.currentHandler.onValueChanged(value);
        if(this.handler != null && sendToListener)
            this.handler.accept(value);
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    public static Builder builder() { return new Builder(); }

    public static class Builder extends AbstractBuilder<Builder,MoneyValueWidget> {

        @Nullable
        private MoneyValueWidget oldWidget;
        private Consumer<MoneyValue> handler = v -> {};
        private Consumer<MoneyValueWidget> typeChangeListener = w -> {};
        private Predicate<MoneyValue> filter = Predicates.alwaysTrue();
        private MoneyValue startingValue = MoneyValue.empty();
        private boolean allowFree = true;
        private BooleanSupplier allowHandlerChange = () -> true;
        private TextSettings textSettings = TextSettings.DEFAULT;

        private Builder() { super(WIDTH,HEIGHT); }

        @Override
        protected Builder getSelf() { return this; }

        public Builder oldWidget(@Nullable MoneyValueWidget widget) { this.oldWidget = widget; return this; }

        public Builder handler(Runnable handler) { return this.handler(v -> handler.run()); }
        public Builder handler(Consumer<MoneyValue> handler) { this.handler = handler; return this; }
        public Builder typeChangeListener(Runnable listener) { return this.typeChangeListener(w -> listener.run()); }
        public Builder typeChangeListener(Consumer<MoneyValueWidget> listener) { this.typeChangeListener = listener; return this; }
        public Builder filterValue(Predicate<MoneyValue> filter) { this.filter = filter; return this; }

        public Builder startingValue(MoneyValue value) { this.startingValue = value; return this; }

        public Builder disallowFreeInput() { return this.allowFreeInput(false); }
        public Builder allowFreeInput(boolean allowFree) { this.allowFree = allowFree; return this; }
        public Builder allowHandlerChange(boolean allowHandlerChange) { return this.allowHandlerChange(() -> allowHandlerChange); }
        public Builder allowHandlerChange(BooleanSupplier allowHandlerChange) { this.allowHandlerChange = allowHandlerChange; return this; }

        public Builder textColor(int textColor) { return this.textColors(textColor,textColor); }
        public Builder textColors(int textColor,int fancyTextColor) { this.textSettings = new TextSettings(textColor,fancyTextColor); return this; }

        @Override
        public MoneyValueWidget build() { return new MoneyValueWidget(this); }
    }

}