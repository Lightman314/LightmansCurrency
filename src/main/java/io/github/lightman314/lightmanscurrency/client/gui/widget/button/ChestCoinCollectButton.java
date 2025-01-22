package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.money.coins.CoinAPI;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketChestQuickCollect;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class ChestCoinCollectButton extends IconButton {

    private static ChestCoinCollectButton lastButton;

    private final ContainerScreen screen;

    private ChestCoinCollectButton(@Nonnull Builder builder) {
        super(IconButton.builder().pressAction(CPacketChestQuickCollect::sendToServer).icon(ChestCoinCollectButton::getIcon));
        this.screen = builder.screen;
        lastButton = this;
        //Position in the top-right corner
        this.setPosition(this.screen.getGuiLeft() + this.screen.getXSize() - this.width, this.screen.getGuiTop() - this.height);
    }

    private static IconData getIcon() {
        return IconData.of(CoinAPI.API.getEquippedWallet(Minecraft.getInstance().player));
    }

    private boolean shouldBeVisible()
    {
        if(!LCConfig.CLIENT.chestButtonVisible.get())
            return false;
        Minecraft mc = Minecraft.getInstance();
        if(mc != null)
        {
            ItemStack wallet = CoinAPI.API.getEquippedWallet(mc.player);
            if(WalletItem.isWallet(wallet))
            {
                final boolean allowSideChains = LCConfig.CLIENT.chestButtonAllowSideChains.get();
                //Check menu inventory for coins
                Container container = this.screen.getMenu().getContainer();
                for(int i = 0; i < container.getContainerSize(); ++i)
                {
                    ItemStack item = container.getItem(i);
                    if(CoinAPI.API.IsAllowedInCoinContainer(item, allowSideChains))
                        return true;
                }
            }
        }
        return false;
    }

    @Override
    protected void renderTick() { super.renderTick(); this.visible = this.shouldBeVisible(); }

    public static void tryRenderTooltip(EasyGuiGraphics gui, int mouseX, int mouseY) {
        if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
            gui.renderTooltip(LCText.TOOLTIP_CHEST_COIN_COLLECTION_BUTTON.get());
    }

    public static Builder chestBuilder() { return new Builder(); }

    @MethodsReturnNonnullByDefault
    @FieldsAreNonnullByDefault
    public static class Builder extends EasyBuilder<Builder>
    {

        private Builder() {}
        @Override
        protected Builder getSelf() { return this; }

        private ContainerScreen screen = null;

        public Builder screen(@Nonnull ContainerScreen screen) { this.screen = screen; return this; }

        public ChestCoinCollectButton build() { return new ChestCoinCollectButton(this); }

    }

}
