package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.message.wallet.CPacketChestQuickCollect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

public class ChestCoinCollectButton extends IconButton {

    private static ChestCoinCollectButton lastButton;

    private final ContainerScreen screen;

    public ChestCoinCollectButton(ContainerScreen screen) {
        super(0,0, b -> CPacketChestQuickCollect.sendToServer(), ChestCoinCollectButton::getIcon);
        this.screen = screen;
        lastButton = this;
        //Position in the top-right corner
        this.setPosition(this.screen.getGuiLeft() + this.screen.getXSize() - this.width, this.screen.getGuiTop() - this.height);
    }

    private static IconData getIcon() {
        Minecraft mc = Minecraft.getInstance();
        if(mc != null)
            return IconData.of(LightmansCurrency.getWalletStack(mc.player));
        return IconData.BLANK;
    }

    private boolean shouldBeVisible()
    {
        if(!Config.CLIENT.chestButtonVisible.get())
            return false;
        Minecraft mc = Minecraft.getInstance();
        if(mc != null)
        {
            ItemStack wallet = LightmansCurrency.getWalletStack(mc.player);
            if(WalletItem.isWallet(wallet))
            {
                final boolean allowHidden = Config.CLIENT.chestButtonAllowHidden.get();
                //Check menu inventory for coins
                Container container = this.screen.getMenu().getContainer();
                for(int i = 0; i < container.getContainerSize(); ++i)
                {
                    if(MoneyUtil.isCoin(container.getItem(i), allowHidden))
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
            gui.renderTooltip(EasyText.translatable("tooltip.button.chest.coin_collection"), mouseX, mouseY);
    }

}
