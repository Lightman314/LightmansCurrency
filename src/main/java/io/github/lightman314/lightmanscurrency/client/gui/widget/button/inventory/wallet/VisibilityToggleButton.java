package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.wallet;

import io.github.lightman314.lightmanscurrency.client.ClientEvents;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory.InventoryButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.capability.IWalletHandler;
import io.github.lightman314.lightmanscurrency.common.capability.WalletCapability;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.player.Player;

import java.util.function.Consumer;

public class VisibilityToggleButton extends InventoryButton {

	public static final int SIZE = 6;

	public static final Sprite SPRITE_VISIBLE = Sprite.SimpleSprite(ClientEvents.WALLET_SLOT_TEXTURE, 28 + SIZE, 0, SIZE, SIZE);
	public static final Sprite SPRITE_INVISIBLE = Sprite.SimpleSprite(ClientEvents.WALLET_SLOT_TEXTURE, 28, 0, SIZE, SIZE);

	public VisibilityToggleButton(AbstractContainerScreen<?> inventoryScreen, Consumer<EasyButton> pressable) { super(inventoryScreen, pressable, VisibilityToggleButton::getSprite); }
	
	private static boolean isWalletVisible() {
		Minecraft mc = Minecraft.getInstance();
		Player player = mc.player;
		assert player != null;
		IWalletHandler walletHandler = WalletCapability.lazyGetWalletHandler(player);
		return walletHandler != null && walletHandler.visible();
	}

	private static Sprite getSprite() { return isWalletVisible() ? SPRITE_VISIBLE : SPRITE_INVISIBLE; }

	@Override
	protected ScreenPosition getPositionOffset(boolean isCreativeScreen) { return ClientEvents.getWalletSlotPosition(isCreativeScreen); }

}
