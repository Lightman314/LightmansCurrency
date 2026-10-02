package io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.builtin;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.TextSettings;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.dropdown.DropdownOption;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.money.MoneyInputHandler;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.coins.value.CoinValue;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.keys.DualKey;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.BooleanSupplier;

public class CoinValueInput extends MoneyInputHandler implements IScrollable {

    public static final int MAX_BUTTON_COUNT = 6;
    private static final int SEGMENT_WIDTH = 20;
    private static final int SEGMENT_SPACING = 5;
    private static final int SEGMENT_TOTAL = SEGMENT_WIDTH + SEGMENT_SPACING;

    private final ChainData chain;
    private final DualKey key;
    private final List<CoinEntry> coinData;

    private int scroll = 0;

    public CoinValueInput(ChainData chain) {
        this.chain = chain;
        this.key = DualKey.create(CoinValue.TYPE,this.chain.chain);
        this.coinData = this.chain.getAllEntries(false,ChainData.SORT_HIGHEST_VALUE_FIRST);
        //Default to fully scrolled
        this.scroll = this.getMaxScroll();
    }

    @Override
    public DropdownOption inputOption() { return new DropdownOption(this.chain.getDisplayName(),this.chain.getDisplaySprite()); }

    @Override
    public DualKey getKey() { return this.key; }

    @Override
    protected void copyHandlerState(MoneyInputHandler oldHandler) {
        //Copy the scroll status from the old handler to be a tad more consistent
        if(oldHandler instanceof CoinValueInput cvi && cvi.chain == this.chain)
            this.scroll = cvi.scroll;
    }

    @Override
    public void initialize(ScreenArea area) {
        int buttonCount = this.coinData.size();
        if(buttonCount > MAX_BUTTON_COUNT)
        {
            buttonCount = MAX_BUTTON_COUNT;
            this.addChild(ScrollArea.builder()
                    .ofArea(area)
                    .forScrollable(this)
                    .build());
            this.addChild(SpriteButton.builder()
                    .atPos(area.pos.offset(4,33))
                    .onPress(this::incrementScroll)
                    .withSprite(LCSprites.BUTTON_BIG_ARROW_LEFT)
                    .active(() -> this.scroll < this.getMaxScroll())
                    .visible(this::isVisible)
                    .build());
            this.addChild(SpriteButton.builder()
                    .atPos(area.pos.offset(area.width - 14,33))
                    .onPress(this::decrementScroll)
                    .withSprite(LCSprites.BUTTON_BIG_ARROW_RIGHT)
                    .active(() -> this.scroll > 0)
                    .visible(this::isVisible)
                    .build());
        }
        int startX = this.getStartX(area,buttonCount);
        for(int x = 0; x < buttonCount; ++x)
        {
            int xPos = startX + (x * SEGMENT_TOTAL);
            final int index = x;
            this.addChild(SpriteButton.builder()
                    .atPos(area.pos.offset(xPos,19))
                    .onPress((b,e) -> this.increaseButtonHit(index,e))
                    .withSprite(LCSprites.BUTTON_BIG_ARROW_UP)
                    .visible(this::isVisible)
                    .active(this::isIncrementActive)
                    .build());
            this.addChild(SpriteButton.builder()
                    .atPos(area.pos.offset(xPos,57))
                    .onPress((b,e) -> this.decreaseButtonHit(index,e))
                    .withSprite(LCSprites.BUTTON_BIG_ARROW_DOWN)
                    .visible(this::isVisible)
                    .active(this.isDecrementActive(x))
                    .build());
        }

    }

    private int getStartX(ScreenArea area,int buttonCount) {
        int space = area.width - (buttonCount * SEGMENT_TOTAL) + SEGMENT_SPACING;
        return space / 2;
    }

    @Override
    protected void extractBG(FancyGuiExtractor gui, ScreenArea area, TextSettings settings) {
        this.validateScroll();

        int buttonCount = Math.min(this.coinData.size(),MAX_BUTTON_COUNT);

        //Draw the coins initial and texture
        int startX = this.getStartX(area,buttonCount);
        for(int x = 0; x < buttonCount; ++x) {
            CoinEntry coin = this.coinData.get(x + this.scroll);
            int xPos = startX + (x * SEGMENT_TOTAL);
            //Draw sprite
            gui.item(new ItemStack(coin.getCoin()),xPos+ 2,30);
            //Draw string
            gui.centeredText(String.valueOf(this.getQuantityOfCoin(coin)),xPos + 10,47,settings.textColor(),false);
        }
    }

    private boolean isIncrementActive() { return !this.isLocked() && !this.isFree(); }
    private BooleanSupplier isDecrementActive(int index) {
        return () -> {
            if(this.isLocked() || this.isFree())
                return false;
            int i = index + this.scroll;
            if(i < 0 || i >= this.coinData.size())
                return false;
            return this.getQuantityOfCoin(this.coinData.get(i)) > 0;
        };
    }

    private long getQuantityOfCoin(CoinEntry coin) {
        MoneyValue currentValue = this.currentValue();
        if(currentValue instanceof CoinValue coinValue)
            return coinValue.getEntry(coin.getCoin());
        return 0;
    }

    @Override
    public void onValueChanged(MoneyValue newValue) { }

    @Override
    public int getScroll() { return this.scroll; }

    @Override
    public void setScroll(int scroll) { this.scroll = scroll; }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.coinData.size(),MAX_BUTTON_COUNT); }

    private void increaseButtonHit(int index,MouseButtonEvent event) {
        final int coinIndex = index + this.scroll;

        if(coinIndex >= 0 && coinIndex < this.coinData.size())
        {
            CoinEntry coin = this.coinData.get(coinIndex);
            int addAmount = 1;
            if(event.hasShiftDown())
                addAmount = this.getLargeIncreaseAmount(coin);
            if(event.hasControlDown())
                addAmount *= 10;

            MoneyValue currentValue = this.currentValue();
            long value = 0;
            if(currentValue instanceof CoinValue cv && cv.isChain(this.chain))
                value = currentValue.getInternalValue();
            this.changeValue(CoinValue.fromNumber(this.chain,value + (coin.getInternalValue() * addAmount)));
        }
    }

    private void decreaseButtonHit(int index,MouseButtonEvent event) {
        final int coinIndex = index + this.scroll;

        if(coinIndex >= 0 && coinIndex < this.coinData.size())
        {
            CoinEntry coin = this.coinData.get(coinIndex);
            int removeAmount = 1;
            if(event.hasShiftDown())
                removeAmount = this.getLargeIncreaseAmount(coin);
            if(event.hasControlDown())
                removeAmount *= 10;
            MoneyValue currentValue = this.currentValue();
            long value = 0;
            if(currentValue instanceof CoinValue cv && cv.isChain(this.chain))
                value = currentValue.getInternalValue();
            this.changeValue(CoinValue.fromNumber(this.chain,value - (coin.getInternalValue() * removeAmount)));
        }
    }

    private int getLargeIncreaseAmount(CoinEntry entry) {
        Pair<CoinEntry,Integer> exchange = entry.getUpperExchange();
        if(exchange == null)
            exchange = entry.getLowerExchange();
        return exchange == null ? 10 : this.getLargeAmount(exchange);
    }

    private int getLargeAmount(Pair<CoinEntry,Integer> exchange) {
        if(exchange.getSecond() >= 64)
            return 16;
        if(exchange.getSecond() > 10)
            return 10;
        if(exchange.getSecond() > 5)
            return 5;
        return 2;
    }

}