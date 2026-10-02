package io.github.lightman314.lightmanscurrency.client.features.wallet.gui;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenCorner;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.money.MoneyDisplayHelper;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.money.values.interfaces.IItemBasedValue;
import io.github.lightman314.lightmanscurrency.core.neoforge.LCDataAttachments;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletAttachment;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletItem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.GuiLayer;

import java.util.List;

public class WalletGuiLayer implements GuiLayer {

    public static final WalletGuiLayer INSTANCE = new WalletGuiLayer();

    private boolean sendError = true;

    protected WalletGuiLayer() {}

    @Override
    public void render(GuiGraphicsExtractor guiGraphics,DeltaTracker deltaTracker) {
        if(!LCConfig.CLIENT.walletOverlayEnabled.get())
            return;

        try {
            FancyGuiExtractor gui = new FancyGuiExtractor(guiGraphics,0f);
            Minecraft mc = Minecraft.getInstance();

            ScreenCorner corner = LCConfig.CLIENT.walletOverlayCorner.get();
            ScreenPosition offset = LCConfig.CLIENT.walletOverlayPosition.get();

            ScreenPosition currentPosition = corner.getCorner(mc.getWindow().getGuiScaledWidth(),mc.getWindow().getGuiScaledHeight());
            if(corner.isRight())
                currentPosition = currentPosition.offset(-16,0);
            if(corner.isBottom())
                currentPosition = currentPosition.offset(0,-16);
            currentPosition = currentPosition.offset(offset);

            //Draw the wallet
            WalletAttachment walletHandler = mc.player.getData(LCDataAttachments.WALLET);
            ItemStack wallet = walletHandler.getWallet();
            if(WalletItem.isWallet(wallet)) {
                //Draw the wallet
                gui.item(wallet,currentPosition);
                currentPosition = currentPosition.offset(17 * corner.getHorizontalMult(),0);
            }

            //Draw the stored money
            MoneyResourceHandler money = LCApi.getMoneyAPI().getPlayersMoneyHandler(mc.player);
            //Don't draw anything if no money is present
            if(money.isEmpty())
                return;

            WalletDisplayType type = LCConfig.CLIENT.walletOverlayType.get();
            boolean textFlag = false;
            if(type.isItem()) {
                int offsetAmount = type.getItemOffset() * corner.getHorizontalMult();
                List<MoneyValue> randomValue = MoneyDisplayHelper.getCyclingLikeValues(money);
                if(randomValue.isEmpty())
                    return;
                //Confirm at least the first value is an item based value
                //If a money helper is grouping non-item-based money with item-based money that's their issue at this point
                if(randomValue.getFirst() instanceof IItemBasedValue) {
                    for(MoneyValue value : randomValue) {
                        if(value instanceof IItemBasedValue iv) {
                            for(ItemStack coin : iv.getAsItemList()) {
                                gui.item(coin,currentPosition);
                                currentPosition = currentPosition.offset(offsetAmount,0);
                            }
                        }
                    }
                }
                else //If it's not an item-base value, forcibly use the text display
                    type = WalletDisplayType.TEXT;
            }
            if(type.isText()) {
                Component text = MoneyDisplayHelper.getCyclingValueLine(money,"");
                if(corner.isRight())
                    currentPosition = currentPosition.offset(gui.getFont().width(text) * -1,0);
                gui.text(text,currentPosition.x,currentPosition.y + 3,-1,false);
            }

        } catch (Throwable error) {
            if(this.sendError) {
                this.sendError = false;
                LightmansCurrency.LogError("Error occurred while rendering the wallet overlay!",error);
            }
        }
    }

}