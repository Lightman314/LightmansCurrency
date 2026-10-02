package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.subscreens.list;

import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.FancyRenderable;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.scrolling.IScrollable;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin.ConfigFileScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ListOptionScreen extends ConfigScreen implements IPositionalWidgetHolder {

    private final ListScreenSettings settings;
    public final ConfigFileOption file;
    private final ConfigOption<?> option;
    private final Consumer<ConfigOption<?>> listener;
    public ListOptionScreen(Screen parentScreen,ConfigFileOption file,ConfigOption<?> option,ListScreenSettings settings) {
        super(parentScreen);
        this.settings = settings;
        this.settings.setScreen(this);
        this.file = file;
        this.option = option;
        this.listener = this::onOptionChanged;
        this.option.addListener(this.listener);
    }

    private IPositionalWidgetHolder scrollingArea;
    private final List<List<Object>> displayEntries = new ArrayList<>();

    private List<Object> indexCache = null;

    @Override
    protected void initialize(ScreenArea area,IPositionalWidgetHolder scrollingArea) {
        this.scrollingArea = scrollingArea;

        //Initialize Entries (similar to ConfigFileScreen)
        this.setupWidgets();

        //Add Button
        this.addChild(TextButton.builder()
                .atPos(area.centerX() - 105,area.height - BOTTOM_BUTTON_OFFSET)
                .ofWidth(100)
                .withText(LCText.Config.CONFIG_OPTION_LIST_ADD)
                .onPress(this::addOption)
                .active(this.settings::canAddEntry)
                .build());
        //Back Button
        this.addChild(TextButton.builder()
                .atPos(area.centerX() + 5,area.height - BOTTOM_BUTTON_OFFSET)
                .ofWidth(100)
                .withText(CommonComponents.GUI_BACK)
                .onPress(this::onClose)
                .build());

    }

    @Override
    protected List<Component> getTitleSections() { return List.of(this.file.name(),this.option.getDisplayName()); }

    private void onOptionChanged(ConfigOption<?> option) { this.revalidateWidgets(); }

    public boolean canEdit() { return this.file.canEdit(this.minecraft); }

    private void addOption() {
        this.settings.addEntry();
        this.revalidateWidgets();
    }

    private void setupWidgets() {
        this.displayEntries.clear();
        this.revalidateWidgets();
    }

    private void revalidateWidgets() {
        int listSize = this.settings.getListSize();
        if(this.displayEntries.size() != listSize) {
            //Remove overflow widgets
            while(this.displayEntries.size() > listSize) {
                for(Object child : this.displayEntries.removeLast())
                    this.scrollingArea.removeChild(child);
            }
            while(this.displayEntries.size() < listSize) {
                int index = this.displayEntries.size();
                this.indexCache = new ArrayList<>();
                this.addChild(this.settings.buildEntry(index),this.scrollingArea.getLowestWidgetPoint() + ConfigFileScreen.SPACING);
                this.displayEntries.add(this.indexCache);
                this.indexCache = null;
            }
            //Validate the current scroll value
            if(this.scrollingArea instanceof IScrollable s)
                s.validateScroll();
        }
    }

    @Override
    public <T extends FancyRenderable> T addChild(ScreenPosition position,T child) {
        if(this.indexCache != null)
            this.indexCache.add(child);
        return this.scrollingArea.addChild(position,child);
    }
    @Override
    public <T> T addUnpositionedChild(T child) {
        if(this.indexCache != null)
            this.indexCache.add(child);
        return this.scrollingArea.addUnpositionedChild(child);
    }
    @Override
    public int getLowestWidgetPoint() { return this.scrollingArea.getLowestWidgetPoint(); }

    @Override
    protected void afterClose() { this.option.removeListener(this.listener); }

}
