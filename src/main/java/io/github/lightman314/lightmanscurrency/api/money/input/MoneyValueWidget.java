package io.github.lightman314.lightmanscurrency.api.money.input;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.WidgetStateSprite;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.client.ClientMoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeData;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoneyValueWidget extends EasyWidgetWithChildren {


    public static final int HEIGHT = 69;
    public static final int WIDTH = 176;

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/coinvalueinput.png");
    public static final FixedSizeSprite SPRITE_FREE_TOGGLE = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_freetoggle"),10,10);
    public static final FixedSizeSprite SPRITE_UP_ARROW = SpriteUtil.BUTTON_BIGARROW_UP;
    public static final FixedSizeSprite SPRITE_DOWN_ARROW = SpriteUtil.BUTTON_BIGARROW_DOWN;
    public static final FixedSizeSprite SPRITE_LEFT_ARROW = SpriteUtil.BUTTON_BIGARROW_LEFT;
    public static final FixedSizeSprite SPRITE_RIGHT_ARROW = SpriteUtil.BUTTON_BIGARROW_RIGHT;

    private static String lastSelectedHandler = MoneyAPI.MODID + ":coins!main";

    private final boolean drawBG;
    public boolean allowFreeInput = true;
    private final Supplier<Boolean> allowHandlerChange;
    public boolean canChangeHandlers() { return this.allowHandlerChange.get(); }
    private boolean locked = false;
    public boolean isLocked() { return this.locked; }
    public void lock() { this.locked = true; }
    public void unlock() { this.locked = false; }

    private final Map<String,MoneyInputHandler> availableHandlers;
    private final List<String> handlerKeys = new ArrayList<>();
    private MoneyInputHandler currentHandler = null;
    
    public String getCurrentHandlerType() { return this.currentHandler == null ? "" : this.currentHandler.getUniqueName(); }
    @Nullable
    public MoneyInputHandler getCurrentHandler() { return this.currentHandler; }
    public void tryMatchHandler(MoneyValue value)
    {
        if(this.getCurrentHandler().isForValue(value))
            return;
        if(this.availableHandlers.containsKey(value.getUniqueName()))
            this.setHandler(this.availableHandlers.get(value.getUniqueName()));
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

    private MoneyValue currentValue;
    
    public final MoneyValue getCurrentValue() { return this.currentValue; }
    private final Consumer<MoneyValue> changeHandler;
    private final Consumer<MoneyValueWidget> handlerChangeConsumer;

    private final MoneyValueWidget oldWidget;

    public final int textColor;
    public final int fancyTextColor;

    private MoneyValueWidget(Builder builder)
    {
        super(builder);
        this.changeHandler = builder.handler;
        this.handlerChangeConsumer = builder.typeChangeHandler;
        this.currentValue = builder.oldWidget != null ? builder.oldWidget.currentValue : builder.startingValue;
        this.availableHandlers = this.setupHandlers();
        this.oldWidget = builder.oldWidget;
        this.drawBG = builder.drawBG;
        this.allowFreeInput = builder.allowFree;
        this.allowHandlerChange = builder.allowHandlerChange;
        this.textColor = builder.textColor;
        this.fancyTextColor = builder.fancyTextColor;
    }

    private Map<String,MoneyInputHandler> setupHandlers()
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Map<String,MoneyInputHandler> handlers = new HashMap<>();
        for(MoneyInputHandler handler : ClientMoneyAPI.getApi().GetMoneyInputs(player))
        {
            handlers.put(handler.getUniqueName(), handler);
            handler.setup(this, this::addChild, this::removeChild, this::onHandlerChangeValue);
            this.handlerKeys.add(handler.getUniqueName());
        }
        if(handlers.isEmpty())
            throw new RuntimeException("No valid MoneyInputHandlers are included in the registered CurrencyTypes!");
        return handlers;
    }

    private MoneyInputHandler findDefaultHandler()
    {
        if(this.oldWidget != null && this.oldWidget.currentHandler != null && this.availableHandlers.containsKey(this.oldWidget.currentHandler.getUniqueName()))
            return this.availableHandlers.get(this.oldWidget.currentHandler.getUniqueName());
        MoneyValue value = this.currentValue;

        if(value.isEmpty() || value.isFree())
        {
            //Return last selected
            if(this.availableHandlers.containsKey(lastSelectedHandler))
                return this.availableHandlers.get(lastSelectedHandler);
        }
        else
        {
            //Get type from the money type
            String id = value.getUniqueName();
            if(this.availableHandlers.containsKey(id))
                return this.availableHandlers.get(id);
            else
            {
                for(MoneyInputHandler handler : this.availableHandlers.values())
                {
                    if(handler.isForValue(value))
                        return handler;
                }
            }
        }
        //Could not find a valid handler. Just return the first one.
        return this.availableHandlers.values().stream().toList().get(0);
    }

    @Override
    public void addChildren(ScreenArea area) {

        this.setHandler(this.findDefaultHandler());

        this.addChild(PlainButton.builder()
                .position(area.pos.offset(area.width - 14,4))
                .pressAction(this::toggleFree)
                .sprite(SPRITE_FREE_TOGGLE)
                .addon(EasyAddonHelper.visibleCheck(() -> this.visible && this.allowFreeInput))
                .build());

        this.addChild(DropdownWidget.builder()
                .position(area.pos.offset(10,4))
                .width(100)
                .selected(this.handlerKeys.indexOf(this.currentHandler.getUniqueName()))
                .selectAction(this::selectHandler)
                .options(this.handlerNames())
                .addon(EasyAddonHelper.visibleCheck(() -> this.visible && this.availableHandlers.size() > 1 && this.allowHandlerChange.get()))
                .build());

    }

    private void checkHandler()
    {
        if(this.currentValue.isFree() || this.currentValue.isEmpty() || (this.currentHandler != null && this.currentHandler.isForValue(this.currentValue)))
            return;
        if(this.availableHandlers.containsKey(this.currentValue.getUniqueName()))
            this.setHandler(this.availableHandlers.get(this.currentValue.getUniqueName()));
        else
        {
            for(MoneyInputHandler handler : this.availableHandlers.values())
            {
                if(handler.isForValue(this.currentValue))
                {
                    this.setHandler(handler);
                    return;
                }
            }
        }
    }

    private void setHandler(MoneyInputHandler handler)
    {
        if(this.currentHandler == handler)
            return;
        if(this.currentHandler != null)
        {
            this.removeChild(this.currentHandler);
            this.currentHandler.close();
        }
        this.currentHandler = handler;
        this.addChild(this.currentHandler);
        this.currentHandler.initialize(this.getArea());
        lastSelectedHandler = this.currentHandler.getUniqueName();

        this.markHandlerChanged();
    }

    public void markHandlerChanged()
    {
        this.handlerChangeConsumer.accept(this);
    }

    private List<Component> handlerNames()
    {
        List<Component> names = new ArrayList<>();
        for(String key : this.handlerKeys)
            names.add(this.availableHandlers.get(key).inputName());
        return names;
    }

    private void selectHandler(int handlerIndex)
    {
        if(handlerIndex < 0 || handlerIndex >= this.handlerKeys.size())
            return;
        MoneyInputHandler handler = this.availableHandlers.get(this.handlerKeys.get(handlerIndex));
        if(handler != null)
            this.setHandler(handler);
    }

    @Override
    protected void renderTick() {
        if(this.currentHandler != null)
            this.currentHandler.renderTick();
    }

    @Override
    protected void renderWidget(EasyGuiGraphics gui) {

        //Render the Background
        if(this.drawBG)
            SpriteUtil.GENERIC_BACKGROUND.render(gui,0,0,WIDTH,HEIGHT);

        //Draw widget
        if(this.currentHandler != null)
            this.currentHandler.renderBG(gui);

        //Render the current price in the top-right corner
        int priceWidth = gui.font.width(this.currentValue.getString());
        int freeButtonOffset = this.allowFreeInput ? 15 : 5;
        gui.drawString(this.currentValue.getText(), this.width - freeButtonOffset - priceWidth, 5, this.textColor);

    }

    private void toggleFree()
    {
        if(this.currentValue.isFree())
            this.changeValue(MoneyValue.empty());
        else if(this.allowFreeInput)
            this.changeValue(MoneyValue.free());
    }

    private void onHandlerChangeValue(MoneyValue newValue)
    {
        if(newValue == null)
            newValue = MoneyValue.empty();
        if(newValue.isFree() && !this.allowFreeInput)
            newValue = MoneyValue.empty();
        this.currentValue = newValue;
        this.changeHandler.accept(newValue);
    }

    public void changeValue(MoneyValue newValue)
    {
        if(newValue.isFree() && !this.allowFreeInput)
            newValue = MoneyValue.empty();
        this.currentValue = newValue;
        this.checkHandler();
        this.changeHandler.accept(this.currentValue);
        if(this.currentHandler != null)
            this.currentHandler.onValueChanged(newValue);
    }

    @Override
    public boolean hideFromMouse() { return true; }

    public static Builder builder() { return new Builder(); }

    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {
        private Builder() { super(WIDTH,HEIGHT); }
        @Override
        protected Builder getSelf() { return this; }

        @Nullable
        private MoneyValueWidget oldWidget = null;
        private Consumer<MoneyValue> handler = v -> {};
        private Consumer<MoneyValueWidget> typeChangeHandler = w -> {};
        private MoneyValue startingValue = MoneyValue.empty();
        private boolean drawBG = false;
        private boolean allowFree = true;
        private int textColor = 0x404040;
        private int fancyTextColor = 0xFFFFFF;
        private Supplier<Boolean> allowHandlerChange = () -> true;

        public Builder old(@Nullable MoneyValueWidget widget) { this.oldWidget = widget; return this; }
        public Builder oldIfNotFirst(boolean firstOpen, @Nullable MoneyValueWidget widget) { if(firstOpen) return this; return this.old(widget); }
        public Builder valueHandler(Runnable handler) { this.handler = v -> handler.run(); return this; }
        public Builder valueHandler(Consumer<MoneyValue> handler) { this.handler = handler; return this; }
        public Builder typeChangeListener(Consumer<MoneyValueWidget> handler) { this.typeChangeHandler = handler; return this; }
        public Builder typeChangeListener(Runnable handler) { this.typeChangeHandler = w -> handler.run(); return this; }
        public Builder startingValue(MoneyValue value) { this.startingValue = value; return this; }
        public Builder startingValue(@Nullable TradeData trade) { if(trade == null) return this; return this.startingValue(trade.getCost()); }
        public Builder drawBG() { return this.drawBG(true); }
        public Builder drawBG(boolean drawBG) { this.drawBG = drawBG; return this; }
        public Builder allowFreeInputs(boolean allowFree) { this.allowFree = allowFree; return this; }
        public Builder blockFreeInputs() { this.allowFree = false; return this; }
        public Builder allowHandlerChange(boolean allowHandlerChange) { this.allowHandlerChange = () -> allowHandlerChange; return this; }
        public Builder allowHandlerChange(Supplier<Boolean> allowHandlerChange) { this.allowHandlerChange = allowHandlerChange; return this; }

        public Builder textColor(int textColor) { return this.textColors(textColor,textColor); }
        public Builder textColors(int textColor, int fancyTextColor) { this.textColor = textColor; this.fancyTextColor = fancyTextColor; return this; }

        public MoneyValueWidget build() { return new MoneyValueWidget(this); }

    }

}