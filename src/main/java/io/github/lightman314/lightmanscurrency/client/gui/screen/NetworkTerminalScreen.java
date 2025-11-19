package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FlexibleSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NineSliceSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.WidgetStateSprite;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.SortTypeKey;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.sorting.TerminalSortType;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.dropdown.DropdownWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.NetworkTraderButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.menus.TerminalMenu;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.terminal.TerminalSorter;
import io.github.lightman314.lightmanscurrency.network.message.trader.CPacketOpenTrades;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class NetworkTerminalScreen extends EasyMenuScreen<TerminalMenu> implements IScrollable {

    private static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/network_terminal.png");

    public static final FlexibleSizeSprite BACKGROUND_SPRITE = new NineSliceSprite(new SpriteSource(GUI_TEXTURE,0,0,100,100),25);
    public static final FlexibleSizeSprite BUTTON_BACKGROUND_SPRITE = new NineSliceSprite(new SpriteSource(GUI_TEXTURE,0,100,100,100),25);

    public static final FixedSizeSprite BUTTON_SAVE = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_save"),12,12);

    private TextBoxWrapper<String> searchField;
    private int searchWidth = 118;
    private static int scroll = 0;
    private static String lastSearch = "";
    private DropdownWidget sortSelection;
    private List<TerminalSortType> sortTypeCache;

    ScrollBarWidget scrollBar;

    private int columns;
    private int rows;

    List<NetworkTraderButton> traderButtons;

    private List<TraderData> traderList(){
        List<TraderData> traderList = TraderAPI.getApi().GetAllNetworkTraders(true);
        //No longer need to remove the auction house, as the 'showInTerminal' function now confirms the auction houses enabled/visible status.
        traderList.sort(TerminalSorter.getDefaultSorter(getLatestSorter()));
        return traderList;
    }
    private List<TraderData> filteredTraderList = new ArrayList<>();
    private static TerminalSortType latestSorter = null;
    private static TerminalSortType getLatestSorter()
    {
        if(latestSorter == null)
        {
            //Utilize the default sort option
            latestSorter = getConfiguredSortType();
            //If the default option doesn't exist, use the highest priority option instead
            if(latestSorter == null)
                latestSorter = TraderAPI.getApi().GetAllSortTypes().get(0);
        }
        return latestSorter;
    }
    @Nullable
    private static TerminalSortType getConfiguredSortType()
    {
        SortTypeKey key = SortTypeKey.parse(LCConfig.CLIENT.terminalDefaultSorting.get());
        return TraderAPI.getApi().GetSortType(key);
    }

    public NetworkTerminalScreen(TerminalMenu menu, Inventory inventory, Component ignored)
    {
        super(menu, inventory, LCText.GUI_NETWORK_TERMINAL_TITLE.get());
    }

    private ScreenArea calculateSize()
    {
        if(this.minecraft == null)
            return this.getArea();
        this.columns = 1;
        int columnLimit = LCConfig.CLIENT.terminalColumnLimit.get();
        int availableWidth = this.minecraft.getWindow().getGuiScaledWidth() - NetworkTraderButton.WIDTH - 30;
        while(availableWidth >= NetworkTraderButton.WIDTH && this.columns < columnLimit)
        {
            availableWidth -= NetworkTraderButton.WIDTH;
            this.columns++;
        }
        int availableHeight = this.minecraft.getWindow().getGuiScaledHeight() - NetworkTraderButton.HEIGHT - 45;
        this.rows = 1;
        int rowLimit = LCConfig.CLIENT.terminalRowLimit.get();
        while(availableHeight >= NetworkTraderButton.HEIGHT && this.rows < rowLimit)
        {
            availableHeight -= NetworkTraderButton.HEIGHT;
            this.rows++;
        }
        this.resize((this.columns * NetworkTraderButton.WIDTH) + 30, (this.rows * NetworkTraderButton.HEIGHT) + 45);
        return this.getArea();
    }

    @Override
    protected void initialize(ScreenArea screenArea)
    {

        screenArea = this.calculateSize();

        this.searchWidth = (this.columns - 1) * NetworkTraderButton.WIDTH;

        this.searchField = this.addChild(TextInputUtil.stringBuilder()
                .position(screenArea.pos.offset(28,10))
                .size(this.searchWidth - 17,9)
                .noBorder()
                .maxLength(256)
                .startingValue(lastSearch)
                .textColor(0xFFFFFF)
                .handler(this::onSearchChanged)
                .wrap().build());

        //Sort Type Selection
        this.sortTypeCache = TraderAPI.getApi().GetAllSortTypes();
        this.addChild(DropdownWidget.builder()
                .position(screenArea.pos.offset(screenArea.width - NetworkTraderButton.WIDTH + 18,8))
                .width(NetworkTraderButton.WIDTH - 44)
                .options(this.sortTypeCache.stream().map(TerminalSortType::getName).toList())
                .selected(this.getStartingSortIndex())
                .selectAction(this::changeSortType)
                .build());

        this.addChild(PlainButton.builder()
                .position(screenArea.pos.offset(screenArea.width - NetworkTraderButton.WIDTH,8))
                .sprite(BUTTON_SAVE)
                .addon(EasyAddonHelper.visibleCheck(this::canSaveSortType))
                .pressAction(this::saveSortType)
                .build());

        this.addChild(IconButton.builder()
                .position(screenArea.pos.offset(screenArea.width - 24,4))
                .pressAction(this::OpenAllTraders)
                .icon(ItemIcon.ofItem(ModBlocks.ITEM_NETWORK_TRADER_4))
                .addon(EasyAddonHelper.tooltip(LCText.TOOLTIP_NETWORK_TERMINAL_OPEN_ALL))
                .build());

        this.scrollBar = this.addChild(ScrollBarWidget.builder()
                .position(screenArea.pos.offset(16 + (NetworkTraderButton.WIDTH * this.columns),25))
                .height((NetworkTraderButton.HEIGHT * this.rows) + 2)
                .scrollable(this)
                .build());

        this.initTraderButtons(screenArea);

        this.updateTraderList();

        this.validateScroll();

    }

    private void initTraderButtons(ScreenArea screenArea)
    {
        this.traderButtons = new ArrayList<>();
        for(int y = 0; y < this.rows; y++)
        {
            for(int x = 0; x < this.columns; ++x)
            {
                NetworkTraderButton newButton = this.addChild(NetworkTraderButton.builder()
                        .position(screenArea.pos.offset(15 + (x * NetworkTraderButton.WIDTH),26 + (y * NetworkTraderButton.HEIGHT)))
                        .pressAction(this::OpenTrader)
                        .build());
                this.traderButtons.add(newButton);
            }
        }
    }

    @Override
    public void renderBG(EasyGuiGraphics gui)
    {

        //Render the background
        BACKGROUND_SPRITE.render(gui,0,0,this.imageWidth,this.imageHeight);
        //Render the search icon
        SpriteUtil.SEARCH_ICON.render(gui,14,7);
        //Render search input background
        SpriteUtil.SEARCH_FIELD.render(gui,25,8,this.searchWidth);
        //Render the button background
        BUTTON_BACKGROUND_SPRITE.render(gui,14,25,this.imageWidth - 28, this.imageHeight - 43);

    }

    protected void onSearchChanged(String newSearch)
    {
        if(newSearch.equals(lastSearch))
            return;
        lastSearch = newSearch;
        this.updateTraderList();
    }

    @Override
    public boolean keyPressed(int key, int scanCode, int mods)
    {
        EditBox b = this.searchField.getWrappedWidget();
        String s = b.getValue();
        if(b.keyPressed(key, scanCode, mods))
        {
            if(!Objects.equals(s, b.getValue()))
            {
                this.updateTraderList();
            }
            return true;
        }
        return b.isFocused() && b.isVisible() && key != GLFW_KEY_ESCAPE || super.keyPressed(key, scanCode, mods);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if(this.handleScrollWheel(delta))
            return true;
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    private void OpenTrader(EasyButton button)
    {
        int index = getTraderIndex(button);
        if(index >= 0 && index < this.filteredTraderList.size())
            new CPacketOpenTrades(this.filteredTraderList.get(index).getID()).send();
    }

    private int getTraderIndex(EasyButton button)
    {
        if(button instanceof NetworkTraderButton && this.traderButtons.contains(button))
            return this.traderButtons.indexOf(button) + (scroll * this.columns);
        return -1;
    }

    private void updateTraderList()
    {
        //Filtering of results moved to the TradingOffice.filterTraders
        StringBuilder fullSearch = new StringBuilder();
        String extra = LCConfig.CLIENT.terminalBonusFilters.get();
        if(!extra.isBlank())
        {
            fullSearch = fullSearch.append(extra);
        }
        if(!this.searchField.getValue().isBlank())
        {
            if(!fullSearch.isEmpty())
                fullSearch.append(" ");
            fullSearch.append(this.searchField.getValue());
        }
        this.filteredTraderList = TraderAPI.getApi().FilterTraders(this.traderList(), fullSearch.toString());
        //Validate the scroll
        this.validateScroll();
        //Update the trader buttons
        this.updateTraderButtons();
    }

    private void updateTraderButtons()
    {
        int startIndex = scroll * this.columns;
        for(int i = 0; i < this.traderButtons.size(); i++)
        {
            if(startIndex + i < this.filteredTraderList.size())
                this.traderButtons.get(i).SetData(this.filteredTraderList.get(startIndex + i));
            else
                this.traderButtons.get(i).SetData(null);
        }
    }

    @Override
    public int currentScroll() { return scroll; }

    @Override
    public void setScroll(int newScroll) {
        scroll = newScroll;
        this.updateTraderButtons();
    }

    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.columns * this.rows, this.columns, this.filteredTraderList.size()); }

    private int getStartingSortIndex() { return Math.max(0,this.sortTypeCache.indexOf(getLatestSorter())); }

    private boolean canSaveSortType() { return getConfiguredSortType() != latestSorter; }

    private void changeSortType(int newIndex)
    {
        if(this.sortTypeCache != null && newIndex >= 0 && newIndex < this.sortTypeCache.size())
        {
            latestSorter = this.sortTypeCache.get(newIndex);
            this.updateTraderList();
        }
    }

    private void saveSortType() { LCConfig.CLIENT.terminalDefaultSorting.set(getLatestSorter().getKey().toString()); }

    private void OpenAllTraders(EasyButton button) { new CPacketOpenTrades(-1).send(); }

}