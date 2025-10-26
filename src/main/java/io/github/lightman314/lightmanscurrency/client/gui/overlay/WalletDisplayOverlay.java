package io.github.lightman314.lightmanscurrency.client.gui.overlay;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.money.MoneyAPI;
import io.github.lightman314.lightmanscurrency.api.money.types.IPlayerMoneyHandler;
import io.github.lightman314.lightmanscurrency.api.money.value.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyView;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.money.value.holder.IMoneyHolder;
import io.github.lightman314.lightmanscurrency.client.util.ScreenCorner;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.LayeredDraw;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class WalletDisplayOverlay implements LayeredDraw.Layer {

    public static final WalletDisplayOverlay INSTANCE = new WalletDisplayOverlay();

    private boolean sendError = true;

    public enum DisplayType { ITEMS_WIDE, ITEMS_NARROW, TEXT }

    private WalletDisplayOverlay() {}

    @Override
    public void render(@Nonnull GuiGraphics mcgui, @Nonnull DeltaTracker tracker) {
        if(!LCConfig.CLIENT.walletOverlayEnabled.get())
            return;

        try {
            EasyGuiGraphics gui = EasyGuiGraphics.create(mcgui, Minecraft.getInstance().font, 0, 0, 0f);

            ScreenCorner corner = LCConfig.CLIENT.walletOverlayCorner.get();
            ScreenPosition offset = LCConfig.CLIENT.walletOverlayPosition.get();

            ScreenPosition currentPosition = corner.getCorner(gui.guiWidth(), gui.guiHeight()).offset(offset);
            if(corner.isRightSide)
                currentPosition = currentPosition.offset(ScreenPosition.of(-16,0));
            if(corner.isBottomSide)
                currentPosition = currentPosition.offset(ScreenPosition.of(0, -16));

            //Draw the wallet
            WalletHandler walletHandler = WalletHandler.get(Minecraft.getInstance().player);
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
            }

            //Draw the stored money
            IMoneyHolder money = MoneyAPI.getApi().GetPlayersMoneyHandler(Minecraft.getInstance().player);
            MoneyView contents = money.getStoredMoney();
            //Don't draw anything if no money is present
            if(contents.isEmpty())
                return;

            DisplayType type = LCConfig.CLIENT.walletOverlayType.get();
            if(type == DisplayType.ITEMS_NARROW || type == DisplayType.ITEMS_WIDE)
            {
                int offsetAmount = LCConfig.CLIENT.walletOverlayType.get() == DisplayType.ITEMS_WIDE ? 17 : 9;
                MoneyValue randomValue = contents.getRandomValue();
                if(randomValue instanceof IItemBasedValue itemValue)
                {
                    for(ItemStack coin : itemValue.getAsItemList())
                    {
                        gui.renderItem(coin, currentPosition.x, currentPosition.y);
                        if(corner.isRightSide)
                            currentPosition = currentPosition.offset(ScreenPosition.of(-offsetAmount,0));
                        else
                            currentPosition = currentPosition.offset(ScreenPosition.of(offsetAmount,0));
                    }
                }
                else
                    type = DisplayType.TEXT;
            }
            if(type == DisplayType.TEXT)
            {
                Component walletText = contents.getRandomValueText(EasyText.empty());
                if(corner.isRightSide)
                    gui.drawString(walletText, currentPosition.offset(-1 * gui.font.width(walletText), 3), 0xFFFFFF);
                else
                    gui.drawString(walletText, currentPosition.offset(0,3), 0xFFFFFF);
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
