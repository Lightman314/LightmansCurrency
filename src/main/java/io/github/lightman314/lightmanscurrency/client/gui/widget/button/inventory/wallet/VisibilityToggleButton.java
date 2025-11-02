package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.wallet;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.InventoryButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.wallet.WalletCapability;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@MethodsReturnNonnullByDefault
public class VisibilityToggleButton extends InventoryButton {

    public static final int SIZE = 6;

    public static final FixedSizeSprite SPRITE_VISIBLE = new NormalSprite(SpriteSource.createBottom(VersionUtil.lcResource("common/widgets/button_wallet_viisibility"),6,6));
    public static final FixedSizeSprite SPRITE_INVISIBLE = new NormalSprite(SpriteSource.createTop(VersionUtil.lcResource("common/widgets/button_wallet_viisibility"),6,6));

    public VisibilityToggleButton(AbstractContainerScreen<?> inventoryScreen, Runnable pressable) { super(inventoryScreen, pressable, VisibilityToggleButton::getSprite); }

    private static boolean isWalletVisible() {
        IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(Minecraft.getInstance().player);
        return walletHandler.visible();
    }

    private static FixedSizeSprite getSprite() { return isWalletVisible() ? SPRITE_VISIBLE : SPRITE_INVISIBLE; }

    @Override
    protected ScreenPosition getPositionOffset(boolean isCreativeScreen) { return ClientEvents.getWalletSlotPosition(isCreativeScreen); }

}