package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management.tabs;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_management.CoinManagementScreen;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import net.minecraft.network.chat.Component;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class SelectChainTab extends CoinManagementTab {

    public SelectChainTab(@Nonnull CoinManagementScreen screen) { super(screen); }

    @Nonnull
    @Override
    public IconData getIcon() {
        return null;
    }

    @Nullable
    @Override
    public Component getTooltip() {
        return null;
    }

    @Override
    protected void initialize(ScreenArea screenArea, boolean firstOpen) {

    }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) {

    }
}
