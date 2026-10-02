package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.TextButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.IPositionalWidgetHolder;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import java.util.ArrayList;
import java.util.List;

public final class ConfigSelectionScreen extends ConfigScreen {

    private final List<ConfigFileOption> configFiles;
    private final String modName;

    private boolean firstOpen = true;

    private ConfigSelectionScreen(Screen parentScreen,String modName,List<ConfigFileOption> configFiles)
    {
        super(parentScreen);
        this.modName = modName;
        this.configFiles = configFiles;
    }

    @Override
    protected boolean centerWidgets() { return true; }

    @Override
    protected void initialize(ScreenArea area,IPositionalWidgetHolder holder) {
        if(this.firstOpen)
        {
            this.firstOpen = false;
            for(ConfigFileOption option : this.configFiles)
                option.onSelectionScreenOpened(this.minecraft);
        }
        int centerX = area.centerX();
        int yPos = 0;
        for(ConfigFileOption option : this.configFiles)
        {
            //Add Option Buttons to the scrollable area holder
            holder.addChild(
                    ScreenPosition.of(centerX - 100,yPos),
                    TextButton.builder()
                    .ofWidth(200)
                    .withText(option.name())
                    .onPress(() -> this.editConfig(option))
                    .tooltip(TooltipSource.deferredList(option::buttonTooltip))
                    .active(() -> option.canAccess(this.minecraft))
                    .build());
            yPos += 30;
        }

        //Back Button
        this.addChild(TextButton.builder()
                .atPos(centerX - 100,area.height - BOTTOM_BUTTON_OFFSET)
                .ofWidth(200)
                .withText(CommonComponents.GUI_BACK)
                .onPress(this::onClose)
                .build());

    }

    @Override
    protected void afterClose() {
        for(ConfigFileOption option : this.configFiles)
            option.onSelectionScreenClosed(this.minecraft);
    }

    @Override
    protected List<Component> getTitleSections() { return List.of(LCText.Config.CONFIG_TITLE_FILES.get(this.modName)); }

    public static IConfigScreenFactory createFactory(ConfigFile... configFiles)
    {
        List<ConfigFileOption> entries = new ArrayList<>();
        for(ConfigFile file : configFiles)
            entries.add(ConfigFileOption.create(file));
        return createFactory(entries);
    }
    public static IConfigScreenFactory createFactory(ConfigFileOption... configFiles) { return createFactory(ImmutableList.copyOf(configFiles)); }
    public static IConfigScreenFactory createFactory(List<ConfigFileOption> configFiles) { return (c,s) -> createScreen(c,s,configFiles); }

    public static IConfigScreenFactory mixedFactory(Object... configOptions)
    {
        List<ConfigFileOption> entries = new ArrayList<>();
        for(Object file : configOptions)
        {
            if(file instanceof ConfigFile f)
                entries.add(ConfigFileOption.create(f));
            else if(file instanceof ConfigFileOption option)
                entries.add(option);
            else
                LightmansCurrency.LogError(file.getClass().getName() + " was passed to a mixedFactory constructor, but it is not a supported config file/option!",new Throwable());
        }
        return createFactory(entries);
    }

    private static Screen createScreen(ModContainer container,Screen parentScreen,List<ConfigFileOption> options)
    {
        if(options.isEmpty())
            return parentScreen;
        if(options.size() == 1) //If only one config file, open that one directly
            return options.getFirst().openScreen(parentScreen);
        return new ConfigSelectionScreen(parentScreen,container.getModInfo().getDisplayName(),ImmutableList.copyOf(options));
    }

    private void editConfig(ConfigFileOption entry) {
        if(entry.canAccess(this.minecraft))
            this.minecraft.setScreen(entry.openScreen(this));
    }

}