package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyTextButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraftforge.client.ConfigScreenHandler;
import net.minecraftforge.fml.ModContainer;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ConfigSelectionScreen extends ConfigScreen {

    private final List<ConfigFileOption> configFiles;
    private final String modName;

    private boolean firstOpen = true;

    private ConfigSelectionScreen(Screen parentScreen, String modName, List<ConfigFileOption> configFiles)
    {
        super(parentScreen);
        this.modName = modName;
        this.configFiles = configFiles;

    }

    @Override
    protected void initialize(ScreenArea screenArea) {
        if(this.firstOpen)
        {
            this.firstOpen = false;
            for(ConfigFileOption option : this.configFiles)
                option.onSelectionScreenOpened(this.minecraft);
        }
        int centerX = screenArea.centerX();
        int centerY = screenArea.centerY();
        int yPos = Math.round(((float)this.configFiles.size() / 2f) * -30) + 5 + centerY;
        for(ConfigFileOption option : this.configFiles)
        {
            this.addChild(EasyTextButton.builder()
                    .position(centerX - 100, yPos)
                    .width(200)
                    .text(option.name())
                    .pressAction(() -> this.editConfig(option))
                    .addon(EasyAddonHelper.tooltips(option.buttonTooltip()))
                    .addon(EasyAddonHelper.activeCheck(() -> option.canAccess(this.minecraft)))
                    .build());

            yPos += 30;
        }

        //Back Button
        this.addChild(EasyTextButton.builder()
                .position(centerX - 100,screenArea.height - BOTTOM_BUTTON_OFFSET)
                .width(200)
                .text(LCText.CONFIG_BACK)
                .pressAction(this::onClose)
                .build());

    }

    @Override
    protected void afterClose() {
        for(ConfigFileOption option : this.configFiles)
            option.onSelectionScreenClosed(this.minecraft);
    }

    @Override
    protected List<Component> getTitleSections() { return List.of(LCText.CONFIG_TITLE_FILES.get(this.modName)); }

    public static ConfigScreenHandler.ConfigScreenFactory createFactory(ModContainer container,ConfigFile... configFiles)
    {
        List<ConfigFileOption> entries = new ArrayList<>();
        for(ConfigFile file : configFiles)
            entries.add(ConfigFileOption.create(file));
        return createFactory(container,entries);
    }
    public static ConfigScreenHandler.ConfigScreenFactory createFactory(ModContainer container,ConfigFileOption... configFiles) { return createFactory(container,ImmutableList.copyOf(configFiles)); }
    public static ConfigScreenHandler.ConfigScreenFactory createFactory(ModContainer container,List<ConfigFileOption> configFiles) { return new ConfigScreenHandler.ConfigScreenFactory((c,s) -> createScreen(container,s,configFiles)); }

    public static ConfigScreenHandler.ConfigScreenFactory mixedFactory(ModContainer container,Object... configOptions)
    {
        List<ConfigFileOption> entries = new ArrayList<>();
        for(Object file : configOptions)
        {
            if(file instanceof ConfigFile f)
                entries.add(ConfigFileOption.create(f));
            else if(file instanceof ConfigFileOption option)
                entries.add(option);
            else
                LightmansCurrency.LogError(file.getClass().getName() + " was passed by a mixedFactory construct, but it is not a supported config file/option!",new Throwable());
        }
        return createFactory(container,entries);
    }

    private void editConfig(ConfigFileOption entry) {
        if(entry.canAccess(this.minecraft))
            this.minecraft.setScreen(entry.openScreen(this));
    }

    private static Screen createScreen(ModContainer container,Screen parentScreen, List<ConfigFileOption> options)
    {
        if(options.isEmpty())
            return parentScreen;
        if(options.size() == 1) //If only one config file, open that one directly
            return options.get(0).openScreen(parentScreen);
        return new ConfigSelectionScreen(parentScreen,container.getModInfo().getDisplayName(),ImmutableList.copyOf(options));
    }

}