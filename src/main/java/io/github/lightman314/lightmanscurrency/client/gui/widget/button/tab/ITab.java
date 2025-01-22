package io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface ITab {
    @Nonnull
    IconData getIcon();
    @Nullable
    default Pair<ResourceLocation, ScreenPosition> getSprite() { return null; }
    @Nullable
    Component getTooltip();
}
