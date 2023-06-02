package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.CoinChestScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.core.ModBlocks;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
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
    public void init() {

        //this.screen.getMenu().SendMessageToServer(LazyPacketData.builder().setBoolean("CollectIntoWallet", true));

    }

    @Override
    public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {

    }

    @Override
    public void postRender(PoseStack pose, int mouseX, int mouseY) {

    }

    @Override
    public void tick() {

    }

    @Override
    public void onClose() {

    }

}
