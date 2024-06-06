package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.CPacketOpenEjectionMenu;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class EjectionMenuButton extends InventoryButton {
	
	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/misc.png");
	
	private static EjectionMenuButton lastButton = null;
	
	public static final int SIZE = 9;

	public static final Sprite SPRITE = Sprite.SimpleSprite(GUI_TEXTURE, 0, 0, SIZE, SIZE);

	public static final ScreenPosition OFFSET = ScreenPosition.of(-10, 0);

	private Player getPlayer() { return this.inventoryScreen.getMinecraft().player; }
	
	public EjectionMenuButton(AbstractContainerScreen<?> inventoryScreen) {
		super(inventoryScreen, b -> CPacketOpenEjectionMenu.sendToServer(), SPRITE);
		lastButton = this;
	}

	@Override
	protected ScreenPosition getPositionOffset(boolean isCreativeScreen) { return isCreativeScreen ? LCConfig.CLIENT.notificationAndTeamButtonCreativePosition.get().offset(OFFSET) : LCConfig.CLIENT.notificationAndTeamButtonPosition.get().offset(OFFSET); }

	@Override
	protected boolean canShow() { return !EjectionSaveData.GetValidEjectionData(true, this.getPlayer()).isEmpty(); }

	public static void tryRenderTooltip(EasyGuiGraphics gui) {
		if(lastButton != null && lastButton.isMouseOver(gui.mousePos.x, gui.mousePos.y))
			gui.renderTooltip(LCText.TOOLTIP_EJECTION_BUTTON.get());
	}

}