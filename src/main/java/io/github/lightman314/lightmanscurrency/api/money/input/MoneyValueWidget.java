package io.github.lightman314.lightmanscurrency.api.money.input;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.CurrencyType;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.client.gui.easy.WidgetAddon;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class MoneyValueWidget extends EasyWidgetWithChildren {


    public static final int HEIGHT = 69;
    public static final int WIDTH = 176;

    public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID,"textures/gui/coinvalueinput.png");
    public static final Sprite SPRITE_FREE_TOGGLE = Sprite.SimpleSprite(GUI_TEXTURE, 40, HEIGHT, 10, 10);
    public static final Sprite SPRITE_UP_ARROW = Sprite.SimpleSprite(GUI_TEXTURE, 0, HEIGHT, 20, 10);
    public static final Sprite SPRITE_DOWN_ARROW = Sprite.SimpleSprite(GUI_TEXTURE, 20, HEIGHT, 20, 10);
    public static final Sprite SPRITE_LEFT_ARROW = Sprite.SimpleSprite(GUI_TEXTURE, 50, HEIGHT, 10, 20);
    public static final Sprite SPRITE_RIGHT_ARROW = Sprite.SimpleSprite(GUI_TEXTURE, 60, HEIGHT, 10, 20);

    public static final Consumer<MoneyValue> EMPTY_CONSUMER = v -> {};

    private static String lastSelectedHandler = MoneyAPI.MODID + ":coins!main";

    public boolean drawBG = true;
    public boolean allowFreeInput = true;
    private boolean locked = false;
    public boolean isLocked() { return this.locked; }
    public void lock() { this.locked = true; }
    public void unlock() { this.locked = false; }

    private final Font font = Minecraft.getInstance().font;
    public final Font getFont() { return this.font; }

    private final Map<String,MoneyInputHandler> availableHandlers;
    private final List<String> handlerKeys = new ArrayList<>();
    private MoneyInputHandler currentHandler = null;
    @Nonnull
    public String getCurrentHandlerType() { return this.currentHandler == null ? "" : this.currentHandler.getUniqueName(); }
    @Nullable
    public MoneyInputHandler getCurrentHandler() { return this.currentHandler; }

    private MoneyValue currentValue;
    @Nonnull
    public final MoneyValue getCurrentValue() { return this.currentValue; }
    private final Consumer<MoneyValue> changeHandler;
    private Consumer<MoneyValueWidget> handlerChangeConsumer = w -> {};
    public void setHandlerChangeListener(@Nonnull Consumer<MoneyValueWidget> consumer) { this.handlerChangeConsumer = consumer; }

    private final MoneyValueWidget oldWidget;

    private DropdownWidget dropdown = null;
    private EasyButton freeToggle = null;

    public MoneyValueWidget(int x, int y, @Nullable MoneyValueWidget oldWidget, @Nonnull MoneyValue startingValue, @Nonnull Consumer<MoneyValue> changeHandler) { this(ScreenPosition.of(x,y), oldWidget, startingValue, changeHandler); }
    public MoneyValueWidget(@Nonnull ScreenPosition pos, @Nullable MoneyValueWidget oldWidget, @Nonnull MoneyValue startingValue, @Nonnull Consumer<MoneyValue> changeHandler) {
        super(pos, WIDTH, HEIGHT);
        this.changeHandler = changeHandler;
        this.currentValue = oldWidget != null ? oldWidget.currentValue : startingValue;
        this.availableHandlers = this.setupHandlers();
        this.oldWidget = oldWidget;
    }

    private Map<String,MoneyInputHandler> setupHandlers()
    {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        Map<String,MoneyInputHandler> handlers = new HashMap<>();
        for(CurrencyType type : MoneyAPI.API.AllCurrencyTypes())
        {
            for(Object h : type.getInputHandlers(player))
            {
                if(h instanceof MoneyInputHandler handler)
                {
                    handlers.put(handler.getUniqueName(), handler);
                    handler.setup(this, this::addChild, this::removeChild, this::onHandlerChangeValue);
                    this.handlerKeys.add(handler.getUniqueName());
                }
            }
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
        }
        //Could not find a valid handler. Just return the first one.
        return this.availableHandlers.values().stream().toList().getFirst();
    }

    @Override
    public MoneyValueWidget withAddons(WidgetAddon... addons) {
        this.withAddonsInternal(addons);
        return this;
    }

    @Override
    public void addChildren() {

        this.setHandler(this.findDefaultHandler());

        this.freeToggle = this.addChild(new PlainButton(this.getX() + this.width - 14, this.getY() + 4, this::toggleFree, SPRITE_FREE_TOGGLE));

        this.dropdown = this.addChild(new DropdownWidget(this.getX() + 10, this.getY() + 4, 64, this.handlerKeys.indexOf(this.currentHandler.getUniqueName()), this::selectHandler, this.handlerNames()));

    }

    private void checkHandler()
    {
        if(this.currentValue.isFree() || this.currentValue.isEmpty() || this.currentValue.getUniqueName().equals(this.currentHandler.getUniqueName()))
            return;
        if(this.availableHandlers.containsKey(this.currentValue.getUniqueName()))
            this.setHandler(this.availableHandlers.get(this.currentValue.getUniqueName()));
    }

    private void setHandler(@Nonnull MoneyInputHandler handler)
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
        this.freeToggle.visible = this.allowFreeInput && this.isVisible();
        this.dropdown.visible = this.isVisible() && this.availableHandlers.size() > 1;
        if(this.currentHandler != null)
            this.currentHandler.renderTick();
    }

    @Override
    protected void renderWidget(@Nonnull EasyGuiGraphics gui) {

        //Render the Background
        if(this.drawBG)
            gui.blit(GUI_TEXTURE, 0, 0, 0, 0, WIDTH, HEIGHT);

        //Draw widget
        if(this.currentHandler != null)
            this.currentHandler.renderBG(gui);

        //Render the current price in the top-right corner
        int priceWidth = gui.font.width(this.currentValue.getString());
        int freeButtonOffset = this.allowFreeInput ? 15 : 5;
        gui.drawString(this.currentValue.getText(), this.width - freeButtonOffset - priceWidth, 5, 0x404040);

    }

    private void toggleFree(EasyButton button)
    {
        if(this.allowFreeInput && this.currentValue.isFree())
            this.onHandlerChangeValue(MoneyValue.empty());
        else if(this.allowFreeInput)
            this.onHandlerChangeValue(MoneyValue.free());
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

    public void changeValue(@Nonnull MoneyValue newValue)
    {
        if(newValue.isFree() && !this.allowFreeInput)
            newValue = MoneyValue.empty();
        this.currentValue = newValue;
        this.checkHandler();
        if(this.currentHandler != null)
            this.currentHandler.onValueChanged(newValue);
    }

    @Override
    public boolean hideFromMouse() { return true; }

}
