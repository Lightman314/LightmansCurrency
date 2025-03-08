package io.github.lightman314.lightmanscurrency.api.config.client.screen;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreen;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public abstract class ConfigScreen extends EasyScreen {

    private final Screen parentScreen;

    public ConfigScreen(Screen parentScreen) {
        super(EasyText.empty());
        this.parentScreen = parentScreen;
    }

    @Override
    public final void onClose() { this.minecraft.setScreen(this.parentScreen); }

}
