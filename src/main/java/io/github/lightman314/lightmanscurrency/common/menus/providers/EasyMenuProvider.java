package io.github.lightman314.lightmanscurrency.common.menus.providers;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;

import javax.annotation.Nonnull;

public interface EasyMenuProvider extends MenuProvider {
    @Nonnull
    @Override
    default Component getDisplayName() { return EasyText.empty(); }
}
