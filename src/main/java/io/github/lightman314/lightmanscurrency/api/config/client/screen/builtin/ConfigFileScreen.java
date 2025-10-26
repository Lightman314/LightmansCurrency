package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.ConfigWidgetHelper;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.OptionWidget;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.SectionLabel;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollListener;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.TextRenderUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ConfigFileScreen extends ConfigScreen implements IScrollable {

    List<Pair<String,Object>> changes = new ArrayList<>();
    private final ConfigFileOption.DefaultConfigOption option;
    private final ConfigFile file;
    public ConfigFileScreen(Screen parentScreen, ConfigFileOption.DefaultConfigOption config) {
        super(parentScreen);
        this.option = config;
        this.file = config.file;
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

        //Collect entries
        this.initializeEntries();

        //Add buttons to the bottom
        EasyTextButton button = this.addChild(EasyTextButton.builder()
                .position(screenArea.centerX() - 210,screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(100)
                .text(LCText.CONFIG_UNDO)
                .pressAction(this::undoLastChange)
                .addon(EasyAddonHelper.activeCheck(this::canUndo))
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.centerX() - 105, screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(100)
                .text(LCText.CONFIG_UNDO_ALL)
                .pressAction(this::undoAllChanges)
                .addon(EasyAddonHelper.activeCheck(this::canUndo))
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.centerX() + 5, screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(100)
                .text(LCText.CONFIG_RESET_DEFAULT)
                .pressAction(this::resetToDefaults)
                .addon(EasyAddonHelper.activeCheck(this::canResetToDefault))
                .build());

        this.addChild(EasyTextButton.builder()
                .position(screenArea.centerX() + 110, screenArea.height - BOTTOM_BUTTON_OFFSET)
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

    private void initializeEntries()
    {
        this.displayEntries.clear();
        this.addSectionEntries(this.file.getRoot(),false);
        this.onScrollChanged();
    }

    private void addSectionEntries(ConfigFile.ConfigSection section, boolean sectionLabel)
    {
        if(sectionLabel)
            this.addEntry(SectionLabel.create(this.file,section));
        for(var pair : section.getOptionsInOrder())
        {
            ConfigOption<?> option = pair.getSecond();
            this.addEntry(ConfigWidgetHelper.buildWidgetForOption(this,this.option,option,o -> this.changeValue(option,o),this::canEdit));
        }
        for(ConfigFile.ConfigSection childSection : section.getSectionsInOrder())
            this.addSectionEntries(childSection,true);
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
    protected List<Component> getTitleSections() { return List.of(this.option.name()); }

    @Override
    protected void screenTick() {
        //Close the screen if we lost edit permissions
        if(!this.option.canAccess(this.minecraft))
            this.onClose();
    }

    private boolean canEdit() { return this.option.canEdit(this.minecraft); }

    private boolean canUndo() { return !this.changes.isEmpty(); }

    private boolean canResetToDefault()
    {
        for(ConfigOption<?> option : this.file.getAllOptions().values())
        {
            if(!Objects.equals(option.writeUnsafe(option.get()),option.writeUnsafe(option.getDefaultValue())))
                return true;
        }
        return false;
    }

    private void changeValue(ConfigOption<?> option, Object newValue)
    {
        Object oldValue = option.get();
        if(this.sameValue(option,newValue,oldValue))
            return;
        this.changes.add(Pair.of(option.getFullName(),oldValue));
        this.option.changeValue(this.minecraft,option,newValue);
    }

    private boolean sameValue(ConfigOption<?> option, Object newValue, Object oldValue)
    {
        try { return Objects.equals(option.writeUnsafe(newValue),option.writeUnsafe(oldValue));
        } catch (ClassCastException ignored) { return true; }
    }

    private void undoLastChange() {
        if(this.changes.isEmpty())
            return;
        Pair<String,Object> lastChange = this.changes.removeLast();
        ConfigOption<?> option = this.file.getAllOptions().get(lastChange.getFirst());
        if(option != null)
            this.option.changeValue(this.minecraft,option,lastChange.getSecond());
    }

    private void undoAllChanges() {
        while(!this.changes.isEmpty())
            this.undoLastChange();
    }

    private void resetToDefaults() {
        this.file.getAllOptions().forEach((key,option) -> this.changeValue(option,option.getDefaultValue()));
    }

    private void back() { this.onClose(); }

}
