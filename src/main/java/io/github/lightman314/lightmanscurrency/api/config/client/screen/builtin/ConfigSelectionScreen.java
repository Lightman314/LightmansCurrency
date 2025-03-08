package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class ConfigSelectionScreen extends ConfigScreen {

    private final List<ConfigFileOption> configFiles;

    private ConfigSelectionScreen(Screen parentScreen, List<ConfigFileOption> configFiles)
    {
        super(parentScreen);
        this.configFiles = configFiles;
    }

    @Override
    protected void initialize(ScreenArea screenArea) {

    }

    @Override
    protected void renderBG(EasyGuiGraphics gui) {

    }

    public static IConfigScreenFactory createFactory(ConfigFile... configFiles) {
        List<ConfigFileOption> entries = new ArrayList<>();
        for(ConfigFile file : configFiles)
            entries.add(ConfigFileOption.create(file));
        return createFactory(entries);
    }
    public static IConfigScreenFactory createFactory(ConfigFileOption... configFiles) { return createFactory(ImmutableList.copyOf(configFiles)); }
    public static IConfigScreenFactory createFactory(List<ConfigFileOption> configFiles) {
        return (c,s) -> new ConfigSelectionScreen(s,ImmutableList.copyOf(configFiles));
    }

    private void editConfig(ConfigFileOption entry) {
        if(entry.canAccess(this.minecraft))
            this.minecraft.setScreen(entry.openScreen(this));
    }

}
