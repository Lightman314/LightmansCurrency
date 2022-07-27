package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;

public class TeamManagerButton extends PlainButton {
	
	private static TeamManagerButton lastButton = null;
	
	public static final int SIZE = 9;
	
	public static final int OFFSET = 0;
	
	private final AbstractContainerScreen<?> screen;
	
	public TeamManagerButton(AbstractContainerScreen<?> screen) {
		super(getXPosition(screen), getYPosition(screen), SIZE, SIZE, button -> LightmansCurrency.PROXY.openTeamManager(), TeamManagerScreen.GUI_TEXTURE, 200, 0);
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
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.x = getXPosition(this.screen);
		this.y = getYPosition(this.screen);
		
		//Change visibility based on whether the correct tab is open
		if(this.screen instanceof CreativeModeInventoryScreen)
			this.visible = ((CreativeModeInventoryScreen)this.screen).getSelectedTab() == CreativeModeTab.TAB_INVENTORY.getId();
		super.render(pose, mouseX, mouseY, partialTicks);
	}
	
	public static void tryRenderTooltip(PoseStack pose, int mouseX, int mouseY) {
		if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
			lastButton.screen.renderTooltip(pose, Component.translatable("tooltip.button.team_manager"), mouseX, mouseY);
	}
	
}