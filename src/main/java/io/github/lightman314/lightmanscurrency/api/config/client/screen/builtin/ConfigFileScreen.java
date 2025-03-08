package io.github.lightman314.lightmanscurrency.api.config.client.screen.builtin;

import io.github.lightman314.lightmanscurrency.api.config.client.screen.ConfigScreen;
import io.github.lightman314.lightmanscurrency.api.config.client.screen.options.ConfigFileOption;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.Nonnull;

public final class ConfigFileScreen extends ConfigScreen {

    private final ConfigFileOption.DefaultConfigOption config;
    public ConfigFileScreen(Screen parentScreen, ConfigFileOption.DefaultConfigOption config) { super(parentScreen); this.config = config; }

    @Override
    protected void initialize(ScreenArea screenArea) {

    }

    @Override
    protected void renderBG(@Nonnull EasyGuiGraphics gui) {

    }

}
