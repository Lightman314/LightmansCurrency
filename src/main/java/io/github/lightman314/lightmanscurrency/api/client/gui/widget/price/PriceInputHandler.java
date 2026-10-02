package io.github.lightman314.lightmanscurrency.api.client.gui.widget.price;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.interfaces.IWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextSettings;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePriceType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public abstract class PriceInputHandler implements IWidgetHolder {

    public abstract DropdownOption inputOption();
    public abstract TradePriceType<?> getPriceType();
    public DualKey getKey() { return DualKey.create(this.getPriceType().getKey()); }
    public abstract boolean isForValue(TradePrice price);

    protected Font getFont() { return Minecraft.getInstance().font; }

    public int getCurrentSlot() { return this.parent == null ? 0 : this.parent.getSlot(); }

    private TradePriceWidget parent;
    private Consumer<FancyPacketMap> messageSender;
    protected boolean isVisible() { return this.parent != null && this.parent.isVisible(); }

    protected TradePrice getCurrentValue() { return this.parent.getCurrentPrice(); }

    public final void setup(TradePriceWidget parent, Consumer<FancyPacketMap> messageSender, @Nullable PriceInputHandler lastHandler) {
        this.parent = parent;
        this.messageSender = messageSender;
        if(lastHandler != null)
            this.copyHandlerState(lastHandler);
    }

    protected void copyHandlerState(PriceInputHandler lastHandler) { }

    private final List<Object> children = new ArrayList<>();

    @Override
    public <T> T addChild(T child) {
        if(this.parent != null)
            this.parent.addChild(child);
        this.children.add(child);
        return child;
    }

    @Override
    public void removeChild(Object child) {
        if(this.parent != null)
            this.parent.removeChild(child);
        this.children.remove(child);
    }

    @Override
    public void removeAllChildren() {
        for(Object child : new ArrayList<>(this.children))
            this.removeChild(child);
        this.children.clear();
    }

    public abstract void initialize(ScreenArea area);
    public void renderTick(ScreenPosition mousePos) {}

    public final void extractBG(FancyGuiExtractor gui,ScreenArea area) {
        if(this.parent != null)
            this.extractBG(gui,area,this.parent.getTextSettings());
    }

    protected abstract void extractBG(FancyGuiExtractor gui,ScreenArea area,TextSettings settings);

    protected final void sendPriceChange(FancyPacketMap message) { this.messageSender.accept(message); }

    public final void close() {
        this.removeAllChildren();
        this.onClose();
    }

    protected void onClose() {}

}
