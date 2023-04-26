package io.github.lightman314.lightmanscurrency.client.gui.widget.button;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.common.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.wallet.MessageChestQuickCollect;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public class ChestCoinCollectButton extends IconButton {

    private static ChestCoinCollectButton lastButton;

    private final ContainerScreen screen;

    public ChestCoinCollectButton(ContainerScreen screen) {
        super(0,0, b -> LightmansCurrencyPacketHandler.instance.sendToServer(new MessageChestQuickCollect(Config.CLIENT.chestButtonAllowHidden.get())), ChestCoinCollectButton::getIcon);
        this.screen = screen;
        lastButton = this;
        //Position in the top-right corner
        this.x = this.screen.getGuiLeft() + this.screen.getXSize() - this.width;
        this.y = this.screen.getGuiTop() - this.height;
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
    public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {
        //Update visibility
        this.visible = this.shouldBeVisible();
        super.render(pose, mouseX, mouseY, partialTicks);
    }

    public static void tryRenderTooltip(PoseStack pose, int mouseX, int mouseY) {
        if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
            lastButton.screen.renderTooltip(pose, EasyText.translatable("tooltip.button.chest.coin_collection"), mouseX, mouseY);
    }

}