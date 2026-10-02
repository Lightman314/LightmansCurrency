package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.ConfigWidgetHelper;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.widgets.builtin.SectionLabel;
import io.github.lightman314.lightmanscurrency.api.config.options.ConfigOption;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ConfigFileScreen extends ConfigScreen {

    public static final int SPACING = 5;

    private final List<Pair<String,Object>> changes = new ArrayList<>();
    private final ConfigFileOption.DefaultConfigOption option;
    private final ConfigFile file;

    public ConfigFileScreen(Screen parentScreen, ConfigFileOption.DefaultConfigOption config) {
        super(parentScreen);
        this.option = config;
        this.file = config.file;
    }

    @Override
    protected void initialize(ScreenArea area, IPositionalWidgetHolder scrollingArea) {
        //Collect entries
        this.initializeEntries(area,scrollingArea);
        //Add buttons to the bottom
        //Undo Button
        this.addChild(TextButton.builder()
                .atPos(area.centerX() - 210,area.height - BOTTOM_BUTTON_OFFSET)
                .ofWidth(100)
                .withText(LCText.Config.CONFIG_UNDO)
                .onPress(this::undoLastChange)
                .active(this::canUndo)
                .build());
        //Undo All Button
        this.addChild(TextButton.builder()
                .atPos(area.centerX() - 105,area.height - BOTTOM_BUTTON_OFFSET)
                .ofWidth(100)
                .withText(LCText.Config.CONFIG_UNDO_ALL)
                .onPress(this::undoAllChanges)
                .active(this::canUndo)
                .build());
        //Reset to Default Button
        this.addChild(TextButton.builder()
                .atPos(area.centerX() + 5,area.height - BOTTOM_BUTTON_OFFSET)
                .ofWidth(100)
                .withText(LCText.Config.CONFIG_RESET_DEFAULT)
                .onPress(this::resetToDefaults)
                .active(this::canResetToDefault)
                .build());
        //Back Button
        this.addChild(TextButton.builder()
                .atPos(area.centerX() + 110,area.height - BOTTOM_BUTTON_OFFSET)
                .ofWidth(100)
                .withText(CommonComponents.GUI_BACK)
                .onPress(this::onClose)
                .build());

    }

    private void initializeEntries(ScreenArea area,IPositionalWidgetHolder scrollingArea) {
        this.addSectionEntries(area,scrollingArea,this.file.getRoot(),false,SPACING);
    }

    private int addSectionEntries(ScreenArea area,IPositionalWidgetHolder scrollingArea,ConfigFile.ConfigSection section,boolean sectionLabel,int yPos) {
        if(sectionLabel) {
            int height = scrollingArea.addChild(new SectionLabel(section,this.option),yPos);
            yPos += height + SPACING;
        }
        for(var pair : section.getOptionsInOrder()) {
            ConfigOption<?> option = pair.getSecond();
            int height = scrollingArea.addChild(ConfigWidgetHelper.getBuilderForOption(this,this.option,option, o -> this.changeValue(option,o),this::canEdit),yPos);
            yPos += height + SPACING;
        }
        for(ConfigFile.ConfigSection childSection : section.getSectionsInOrder())
            yPos = this.addSectionEntries(area,scrollingArea,childSection,true,yPos);
        return yPos;
    }

    @Override
    protected List<Component> getTitleSections() { return List.of(this.option.name()); }

    @Override
    protected void clientTick() {
        if(!this.option.canAccess(this.minecraft))
            this.onClose();
    }

    private boolean canEdit() { return this.option.canEdit(this.minecraft); }
    private boolean canUndo() { return !this.changes.isEmpty(); }
    private boolean canResetToDefault() {
        for(ConfigOption<?> option : this.file.getAllOptions().values()) {
            if(!this.sameValue(option,option.get(),option.getDefaultValue()))
                return true;
        }
        return false;
    }

    private void changeValue(ConfigOption<?> option,Object newValue) {
        Object oldValue = option.get();
        if(this.sameValue(option,newValue,oldValue))
            return;
        this.changes.add(Pair.of(option.getFullName(),oldValue));
        this.option.changeValue(this.minecraft,option,newValue);
    }

    private boolean sameValue(ConfigOption<?> option,Object newValue,Object oldValue) {
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

}
