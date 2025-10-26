package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.OptionWidget;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ListOptionScreen extends ConfigScreen implements IScrollable {

    private final ListScreenSettings settings;
    public final ConfigFileOption file;
    private final ConfigOption<?> option;
    public ListOptionScreen(Screen parent, ConfigFileOption file, ConfigOption<?> option, ListScreenSettings settings)
    {
        super(parent);
        this.settings = settings;
        this.settings.setScreen(this);
        this.file = file;
        this.option = option;
        this.option.addListener(this::onOptionChanged);
    }

    private final List<AbstractWidget> displayEntries = new ArrayList<>();
    private int visibleEntries = 1;

    private int scroll = 0;
    @Override
    public int currentScroll() { return this.scroll; }
    @Override
    public void setScroll(int newScroll) {
        this.scroll = newScroll;
        this.onScrollChanged();
    }
    @Override
    public int getMaxScroll() { return IScrollable.calculateMaxScroll(this.visibleEntries,this.displayEntries.size()); }

    @Override
    protected void initialize(ScreenArea screenArea) {

        //Calculate how many entries can be displayed on each "page"
        this.visibleEntries = (screenArea.height - 80) / 30;

        //Intialize Entries (similar to ConfigFileScreen)
        this.setupWidgets();

        //Add Button
        this.addChild(EasyTextButton.builder()
                .position(screenArea.centerX() - 105,screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(100)
                .text(LCText.CONFIG_OPTION_LIST_ADD)
                .pressAction(this::addOption)
                .addon(EasyAddonHelper.activeCheck(this.settings::canAddEntry))
                .build());

        //Back Button
        this.addChild(EasyTextButton.builder()
                .position(screenArea.centerX() + 5, screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(100)
                .text(LCText.CONFIG_BACK)
                .pressAction(this::onClose)
                .build());

        //Scroll Listener and Scroll Bar
        this.addChild(ScrollListener.builder()
                .area(screenArea)
                .listener(this)
                .build());

        this.addChild(ScrollBarWidget.builder()
                .scrollable(this)
                .position(screenArea.centerX() + OptionWidget.HALF_WIDTH + 10,40)
                .height(screenArea.height - 80)
                .build());

    }

    @Override
    protected List<Component> getTitleSections() { return List.of(this.file.name(),this.option.getDisplayName()); }

    private void onOptionChanged(ConfigOption<?> option) { this.revalidateWidgets(); }

    public boolean canEdit() { return this.file.canEdit(this.minecraft); }

    private void addOption()
    {
        //LightmansCurrency.LogDebug("Adding new option to list");
        this.settings.addEntry();
        this.revalidateWidgets();
    }

    private void setupWidgets()
    {
        this.displayEntries.clear();
        this.revalidateWidgets();
    }

    private void revalidateWidgets()
    {
        if(this.displayEntries.size() != this.settings.getListSize())
        {
            //Remove old widgets and create new ones
            for(AbstractWidget widget : this.displayEntries)
                this.removeChild(widget);
            this.displayEntries.clear();
            for(int i = 0; i < this.settings.getListSize(); ++i)
                this.addEntry(this.settings.buildEntry(i));
            this.validateScroll();
            this.onScrollChanged();
        }
    }

    private void addEntry(AbstractWidget widget) { this.displayEntries.add(this.addChild(widget)); }

    private void onScrollChanged()
    {
        ScreenArea area = this.getArea();
        int index = 0;
        int yPos = 40;
        int xPos = area.centerX() - OptionWidget.HALF_WIDTH;
        for(AbstractWidget widget : this.displayEntries)
        {
            widget.visible = index >= this.scroll && index < this.scroll + this.visibleEntries;
            if(widget.visible)
            {
                widget.setPosition(xPos,yPos);
                yPos += 30;
            }
            index++;
        }
    }

    @Override
    protected void afterClose() {
        this.option.removeListener(this::onOptionChanged);
        this.settings.setScreen(null);
    }

}
