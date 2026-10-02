package io.github.lightman314.lightmanscurrency.api.config.client.screen;

import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.screen.generic.FancyScreen;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.VerticalScrollBar;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.WidgetScrollingArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;

import java.util.List;

public abstract class ConfigScreen extends FancyScreen {

    //Copied from AbstractSelectionList
    private static final Identifier MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/menu_list_background.png");
    private static final Identifier INWORLD_MENU_LIST_BACKGROUND = Identifier.withDefaultNamespace("textures/gui/inworld_menu_list_background.png");

    public static final int BOTTOM_BUTTON_OFFSET = 25;

    private final Screen parentScreen;

    private WidgetScrollingArea widgetArea;

    public ConfigScreen(Screen parentScreen) {
        super(Component.empty());
        this.parentScreen = parentScreen;
    }

    @Override
    public final void onClose() {
        this.minecraft.setScreen(this.parentScreen);
        this.afterClose();
    }
    protected void afterClose() {}

    protected int headerSize() { return 33; }
    protected int footerSize() { return 33; }
    protected final int headerAndFooterSize() { return this.headerSize() + this.footerSize(); }

    @Override
    protected final void initialize(ScreenArea area) {
        this.widgetArea = this.addChild(WidgetScrollingArea.builder()
                .atPos(area.pos.offset(0,this.headerSize()))
                .ofSize(area.width,area.height - this.headerAndFooterSize())
                .verticallyCentered(this.centerWidgets())
                .withBottomPadding(this.getBottomPadding())
                .build());
        this.addChild(VerticalScrollBar.builder(this.widgetArea)
                .atPos(area.pos.offset(area.halfWidth() + 160,this.headerSize()))
                .ofHeight(area.height - this.headerAndFooterSize())
                .build());
        this.initialize(area,this.widgetArea);
    }

    protected boolean centerWidgets() { return false; }
    protected int getBottomPadding() { return 10; }

    protected abstract void initialize(ScreenArea area,IPositionalWidgetHolder scrollingArea);

    @Override
    protected final void extractBackground(FancyGuiExtractor gui, ScreenArea area) {
        //Render the header
        Identifier header = this.minecraft.level != null ? Screen.INWORLD_HEADER_SEPARATOR : Screen.HEADER_SEPARATOR;
        gui.blit(header,0,this.headerSize() - 2,0,0,area.width,2,32,2);
        //Background
        Identifier background = this.minecraft.level != null ? INWORLD_MENU_LIST_BACKGROUND : MENU_LIST_BACKGROUND;
        gui.blit(background,0,this.headerSize(),0,this.widgetArea.getScroll(),area.width,area.height - this.headerAndFooterSize(),32,32);
        //Footer
        Identifier footer = this.minecraft.level != null ? Screen.INWORLD_FOOTER_SEPARATOR : Screen.FOOTER_SEPARATOR;
        gui.blit(footer,0,area.height - this.footerSize(),0,0,area.width,2,32,2);

        //Render the title
        gui.scrollingText(this.getTitle(),0,0,area.width,this.headerSize(),-1,true);

        //Render any custom BG widgets
        this.extractAdditionalBG(gui,area);
    }

    protected void extractAdditionalBG(FancyGuiExtractor gui,ScreenArea area) {}

    @Override
    public Component getTitle() {
        List<Component> titleSections = this.getTitleSections();
        if(titleSections.isEmpty())
            return Component.empty();
        if(titleSections.size() == 1)
            return titleSections.getFirst();
        MutableComponent titleBuilder = Component.empty();
        for(int i = 0; i < titleSections.size(); ++i) {
            if(i > 0)
                titleBuilder.append(LCText.Config.CONFIG_TITLE_SEPERATOR.get());
            titleBuilder.append(titleSections.get(i));
        }
        return titleBuilder;
    }

    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double scrollY) {
        //Pass scrolling to the scrollable area first, then pass along to other widgets
        return this.widgetArea.handleScrolling(scrollY) || super.mouseScrolled(x,y,scrollX,scrollY);
    }

    protected abstract List<Component> getTitleSections();

}
