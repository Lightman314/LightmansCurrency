package io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.terminal.builtin;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.LCSprites;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.NetworkTraderButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IRenderTick;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.ScrollArea;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.text.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.terminal.TradingTerminalClientTab;
import io.github.lightman314.lightmanscurrency.api.trader.client.world.menu.terminal.TradingTerminalScreen;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalMenu;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalTab;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.builtin.NetworkTraderSelectionTab;
import io.github.lightman314.lightmanscurrency.core.LCBlocks;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class NetworkTraderSelectionClientTab extends TradingTerminalClientTab<NetworkTraderSelectionTab> implements IRenderTick, IScrollable {

    public static final TabBuilder<TradingTerminalMenu,NetworkTraderSelectionTab,TradingTerminalTab,TradingTerminalScreen> BUILDER = NetworkTraderSelectionClientTab::new;

    //Stored statically now since this is no longer a part of the base screen
    private static int columns = 1;
    private static int rows = 1;

    private int searchWidth = 118;
    private static int scroll = 0;
    private static String lastSearch = "";

    private List<TraderData> allTraderCache = new ArrayList<>();
    private List<TraderData> filteredTraderCache = new ArrayList<>();

    protected NetworkTraderSelectionClientTab(TradingTerminalMenu menu,NetworkTraderSelectionTab commonTab,TradingTerminalScreen screen) { super(menu, commonTab, screen); }

    public static Pair<Integer,Integer> calculateSize(Minecraft mc) {
        columns = 1;
        int columnLimit = LCConfig.CLIENT.terminalColumnLimit.get();
        //Leave 30 px of space for the edges, and then an additional 20 on each side to leave room for the tab buttons
        int scaledWidth = mc.getWindow().getGuiScaledWidth();
        int availableWidth = scaledWidth - NetworkTraderButton.WIDTH - 70;
        while(availableWidth >= NetworkTraderButton.WIDTH && columns < columnLimit) {
            availableWidth -= NetworkTraderButton.WIDTH;
            columns++;
        }
        int scaledHeight = mc.getWindow().getGuiScaledHeight();
        int availableHeight = scaledHeight - NetworkTraderButton.HEIGHT - 45;
        rows = 1;
        int rowLimit = LCConfig.CLIENT.terminalRowLimit.get();
        while(availableHeight >= NetworkTraderButton.HEIGHT && rows < rowLimit) {
            availableHeight -= NetworkTraderButton.HEIGHT;
            rows++;
        }
        return Pair.of((columns * NetworkTraderButton.WIDTH) + 30,(rows * NetworkTraderButton.HEIGHT) + 45);
    }

    @Override
    public void renderTick(ScreenPosition mousePos) {
        this.allTraderCache = this.getNetworkTraders();
        StringBuilder fullSearch = new StringBuilder();
        String extra = LCConfig.CLIENT.terminalBonusFilters.get();
        if(!extra.isBlank())
            fullSearch = fullSearch.append(extra);
        if(!lastSearch.isBlank()) {
            if(!fullSearch.isEmpty())
                fullSearch.append(" ");
            fullSearch.append(lastSearch);
        }
        this.filteredTraderCache = LCApi.getTraderAPI().filterTraders(this.allTraderCache,fullSearch.toString());
        //Validate the scroll
        this.validateScroll();
    }

    @Override
    public IconData getIcon() { return ItemIcon.of(LCBlocks.TRADING_TERMINAL); }

    @Override
    public Component getName() { return NetworkTraderSelectionTab.NAME.get(); }

    @Override
    protected void initialize(ScreenArea area, FancyPacketMap message) {

        //Search Box
        this.searchWidth = (columns - 1) * NetworkTraderButton.WIDTH;
        this.addChild(TextBoxWrapper.stringBuilder()
                .atPos(area.pos.offset(28,10))
                .ofSize(this.searchWidth - 17,9)
                .noBorder()
                .withMaxLength(256)
                .withStartingString(lastSearch)
                .withTextColor(0xFFFFFFFF)
                .withHandler(this::onSearchChanged)
                .build());

        //Sort Type Selection
        //TODO sort types

        //Access All Network Traders button
        this.addChild(IconButton.builder()
                .atPos(area.pos.offset(area.width - 25,4))
                .withIcon(ItemIcon.of(Items.ENDER_PEARL))
                .onPress(this.getCommonTab()::openAllTraders)
                .active(this::hasAnyNetworkTraders)
                .tooltip(TooltipSource.simple(NetworkTraderSelectionTab.TOOLTIP_OPEN_ALL_TRADERS))
                .build());

        //Scroll Bar
        this.addChild(VerticalScrollBar.builder(this)
                .atPos(area.pos.offset(area.width - 14,25))
                .ofHeight(area.height - 43)
                .build());
        //Just go ahead and add a global scroll listener
        this.addChild(ScrollArea.builder()
                .ofArea(area)
                .withListener(this.buildScrollListener()));

        //Add Trader Buttons
        int buttonIndex = 0;
        for(int y = 0; y < rows; ++y) {
            for(int x = 0; x < columns; ++x) {
                final int i = buttonIndex++;
                this.addChild(NetworkTraderButton.builder()
                        .atPos(area.pos.offset(15 + (x * NetworkTraderButton.WIDTH),26 + (y * NetworkTraderButton.HEIGHT)))
                        .forPlayer(this.getPlayer())
                        .forTrader(() -> this.getTrader(i))
                        .onPress(() -> this.openTrader(i))
                        .build());
            }
        }

    }

    @Override
    public void extractBackground(FancyGuiExtractor gui, ScreenArea area) {

        //Search Icon
        gui.blitSprite(LCSprites.SEARCH_ICON,14,7);
        //Search Field
        gui.blitSprite(LCSprites.SEARCH_FIELD,25,8,this.searchWidth);
        //Button Background
        gui.blitSprite(TradingTerminalScreen.BUTTON_BACKGROUND,14,25,area.width - 28,area.height - 43);

    }

    @Override
    public int getScroll() { return scroll; }
    @Override
    public void setScroll(int s) { scroll = s; }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.getNetworkTraders().size(),columns,columns * rows); }

    private void onSearchChanged(String newSearch) {
        if(newSearch.equals(lastSearch))
            return;
        lastSearch = newSearch;
    }

    private boolean hasAnyNetworkTraders() { return !this.allTraderCache.isEmpty(); }

    @Nullable
    private TraderData getTrader(int buttonIndex) {
        int index = (scroll * columns) + buttonIndex;
        return ListHelper.getOrNull(this.filteredTraderCache,index);
    }

    private void openTrader(int buttonIndex) {
        TraderData trader = this.getTrader(buttonIndex);
        if(trader != null)
            this.getCommonTab().openTrader(trader.getID());
    }

}
