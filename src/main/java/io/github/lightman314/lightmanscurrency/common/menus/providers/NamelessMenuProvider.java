package io.github.lightman314.lightmanscurrency.common.menus.providers;

import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public abstract class NamelessMenuProvider implements INamedContainerProvider {


    @Nonnull
    @Override
    public final ITextComponent getDisplayName() { return EasyText.empty(); }

}
