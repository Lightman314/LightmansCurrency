package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.CPacketOpenTraderRecovery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import org.jetbrains.annotations.NotNull;

public class TraderRecoveryButton extends PlainButton {
	
	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/misc.png");
	
	private static TraderRecoveryButton lastButton = null;
	
	public static final int SIZE = 9;
	
	public static final int OFFSET = -10;
	
	private final AbstractContainerScreen<?> screen;
	private Player getPlayer() {
		Minecraft mc = this.screen.getMinecraft();
		return mc.player;
	}
	
	public TraderRecoveryButton(AbstractContainerScreen<?> screen) {
		super(getXPosition(screen), getYPosition(screen), SIZE, SIZE, b -> openTraderRecoveryMenu(), GUI_TEXTURE, 0, 0);
		this.screen = screen;
		lastButton = this;
	}
	
	private static int getXPosition(AbstractContainerScreen<?> screen) {
		return (screen instanceof CreativeModeInventoryScreen ? Config.CLIENT.notificationAndTeamButtonXCreative.get() : Config.CLIENT.notificationAndTeamButtonX.get()) + OFFSET + screen.getGuiLeft();
	}
	
	private static int getYPosition(AbstractContainerScreen<?> screen) {
		return (screen instanceof CreativeModeInventoryScreen ? Config.CLIENT.notificationAndTeamButtonYCreative.get() : Config.CLIENT.notificationAndTeamButtonY.get()) + screen.getGuiTop();
	}
	
	@Override
	public void render(@NotNull PoseStack pose, int mouseX, int mouseY, float partialTicks) {

		this.x = getXPosition(this.screen);
		this.y = getYPosition(this.screen);

		if(EjectionSaveData.GetValidEjectionData(true, this.getPlayer()).size() > 0)
		{
			this.visible = true;
			//Change visibility based on whether the correct tab is open
			if(this.screen instanceof CreativeModeInventoryScreen cs)
				this.visible = cs.getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId();
			super.render(pose, mouseX, mouseY, partialTicks);
		}
		else
			this.visible = false;
		
	}
	
	public static void tryRenderTooltip(PoseStack pose, int mouseX, int mouseY) {
		if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
			lastButton.screen.renderTooltip(pose, Component.translatable("tooltip.button.team_manager"), mouseX, mouseY);
	}
	
	private static void openTraderRecoveryMenu() {
		LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketOpenTraderRecovery());
	}
	
}