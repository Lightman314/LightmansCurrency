package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
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
	protected ScreenPosition getPositionOffset(boolean isCreativeScreen) { return isCreativeScreen ? Config.CLIENT.notificationAndTeamButtonCreativePosition.get().offset(OFFSET) : Config.CLIENT.notificationAndTeamButtonPosition.get().offset(OFFSET); }

	@Override
	protected boolean canShow() { return EjectionSaveData.GetValidEjectionData(true, this.getPlayer()).size() > 0; }

	public static void tryRenderTooltip(EasyGuiGraphics gui, int mouseX, int mouseY) {
		if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
			gui.renderTooltip(EasyText.translatable("tooltip.button.team_manager"), mouseX, mouseY);
	}

}