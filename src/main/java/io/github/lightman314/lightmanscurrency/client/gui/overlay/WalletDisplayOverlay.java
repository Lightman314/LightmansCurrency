package io.github.lightman314.lightmanscurrency.client.gui.overlay;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.util.ScreenCorner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.gui.overlay.ForgeGui;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;

import java.util.ArrayList;
import java.util.List;

public class WalletDisplayOverlay implements IGuiOverlay {

    public static final WalletDisplayOverlay INSTANCE = new WalletDisplayOverlay();

    private boolean sendError = true;

    public enum DisplayType { ITEMS_WIDE, ITEMS_NARROW, TEXT }

    private WalletDisplayOverlay() {}

    @Override
    public void render(ForgeGui fgui, GuiGraphics mcgui, float partialTick, int screenWidth, int screenHeight) {
        if(!LCConfig.CLIENT.walletOverlayEnabled.get())
            return;

        try {
            EasyGuiGraphics gui = EasyGuiGraphics.create(mcgui, fgui.getFont(), 0, 0, partialTick);

            ScreenCorner corner = LCConfig.CLIENT.walletOverlayCorner.get();
            ScreenPosition offset = LCConfig.CLIENT.walletOverlayPosition.get();

            ScreenPosition currentPosition = corner.getCorner(screenWidth, screenHeight).offset(offset);
            if(corner.isRightSide)
                currentPosition = currentPosition.offset(ScreenPosition.of(-16,0));
            if(corner.isBottomSide)
                currentPosition = currentPosition.offset(ScreenPosition.of(0, -16));

            //Draw the wallet
            IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(fgui.getMinecraft().player);
            if(walletHandler == null)
                return;
            ItemStack wallet = walletHandler.getWallet();
            if(WalletItem.isWallet(wallet))
            {
                //Draw the wallet
                gui.renderItem(wallet, currentPosition.x, currentPosition.y);
                if(corner.isRightSide)
                    currentPosition = currentPosition.offset(ScreenPosition.of(-17,0));
                else
                    currentPosition = currentPosition.offset(ScreenPosition.of(17,0));

                MoneyView contents = walletHandler.getStoredMoney();

                //Draw the stored money
                switch(LCConfig.CLIENT.walletOverlayType.get())
                {
                    case ITEMS_NARROW,ITEMS_WIDE -> {
                        int offsetAmount = LCConfig.CLIENT.walletOverlayType.get() == DisplayType.ITEMS_WIDE ? 17 : 9;
                        List<ItemStack> walletContents;
                        MoneyValue randomValue = contents.getRandomValue();
                        if(randomValue instanceof IItemBasedValue itemValue)
                            walletContents = itemValue.getAsItemList();
                        else
                            walletContents = new ArrayList<>();
                        for(ItemStack coin : walletContents)
                        {
                            gui.renderItem(coin, currentPosition.x, currentPosition.y);
                            if(corner.isRightSide)
                                currentPosition = currentPosition.offset(ScreenPosition.of(-offsetAmount,0));
                            else
                                currentPosition = currentPosition.offset(ScreenPosition.of(offsetAmount,0));
                        }
                    }
                    case TEXT -> {
                        Component walletText = contents.getRandomValueText();
                        if(corner.isRightSide)
                            gui.drawString(walletText, currentPosition.offset(-1 * gui.font.width(walletText), 3), 0xFFFFFF);
                        else
                            gui.drawString(walletText, currentPosition.offset(0,3), 0xFFFFFF);
                    }
                }

            }
        } catch (Throwable t) {
            if(this.sendError)
            {
                this.sendError = false;
                LightmansCurrency.LogError("Error occurred while rendering wallet overlay!", t);
            }
        }
    }

}
