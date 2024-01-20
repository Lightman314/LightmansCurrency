package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.CoinChestScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nonnull;

public class DefaultTab extends CoinChestTab {

    public DefaultTab(CoinChestScreen screen) { super(screen); }

    @Nonnull
    @Override
    public IconData getIcon() { return IconData.of(ModBlocks.COIN_CHEST); }

    @Override
    public MutableComponent getTooltip() { return EasyText.empty(); }

    @Override
    public void initialize(ScreenArea screenArea, boolean firstOpen) { }

    @Override
    public void renderBG(@Nonnull EasyGuiGraphics gui) { }

}
