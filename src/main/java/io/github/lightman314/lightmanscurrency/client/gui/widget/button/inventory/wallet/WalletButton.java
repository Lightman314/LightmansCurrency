package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.wallet;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.InventoryButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.attachments.WalletHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;

public class WalletButton extends InventoryButton {

	public static final Sprite SPRITE = Sprite.SimpleSprite(ClientEvents.WALLET_SLOT_TEXTURE, 18, 0, 10, 10);
	
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
