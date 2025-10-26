package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.wallet;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.WidgetStateSprite;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.InventoryButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

@MethodsReturnNonnullByDefault
public class WalletButton extends InventoryButton {

	public static final FixedSizeSprite SPRITE = WidgetStateSprite.lazyHoverable(VersionUtil.lcResource("common/widgets/button_open_wallet"),10,10);

	public WalletButton(AbstractContainerScreen<?> inventoryScreen, Runnable pressable) {
		super(inventoryScreen, pressable, SPRITE);
	}

	@Override
	protected ScreenPosition getPositionOffset(boolean isCreativeScreen) { return ClientEvents.getWalletSlotPosition(isCreativeScreen).offset(LCConfig.CLIENT.walletButtonOffset.get()); }

	@Override
	protected boolean canShow() {
		WalletHandler walletHandler = WalletHandler.get(Minecraft.getInstance().player);
		return walletHandler != null && !walletHandler.getWallet().isEmpty();
	}

}
