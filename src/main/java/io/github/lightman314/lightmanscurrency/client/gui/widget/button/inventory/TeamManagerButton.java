package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TeamManagerScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.item.ItemGroup;

import javax.annotation.Nonnull;

public class TeamManagerButton extends PlainButton {
	
	private static TeamManagerButton lastButton = null;
	
	public static final int SIZE = 9;
	
	public static final int OFFSET = 0;
	
	private final ContainerScreen<?> screen;
	
	public TeamManagerButton(ContainerScreen<?> screen) {
		super(getXPosition(screen), getYPosition(screen), SIZE, SIZE, button -> LightmansCurrency.PROXY.openTeamManager(), TeamManagerScreen.GUI_TEXTURE, 200, 0);
		this.screen = screen;
		lastButton = this;
	}
	
	private static int getXPosition(ContainerScreen<?> screen) {
		return (screen instanceof CreativeScreen ? Config.CLIENT.notificationAndTeamButtonXCreative.get() : Config.CLIENT.notificationAndTeamButtonX.get()) + OFFSET + screen.getGuiLeft();
	}
	
	private static int getYPosition(ContainerScreen<?> screen) {
		return (screen instanceof CreativeScreen ? Config.CLIENT.notificationAndTeamButtonYCreative.get() : Config.CLIENT.notificationAndTeamButtonY.get()) + screen.getGuiTop();
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.x = getXPosition(this.screen);
		this.y = getYPosition(this.screen);
		
		//Change visibility based on whether the correct tab is open
		if(this.screen instanceof CreativeScreen)
			this.visible = ((CreativeScreen)this.screen).getSelectedTab() == ItemGroup.TAB_INVENTORY.getId();
		super.render(pose, mouseX, mouseY, partialTicks);
	}
	
	public static void tryRenderTooltip(MatrixStack pose, int mouseX, int mouseY) {
		if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
			lastButton.screen.renderTooltip(pose, EasyText.translatable("tooltip.button.team_manager"), mouseX, mouseY);
	}
	
}