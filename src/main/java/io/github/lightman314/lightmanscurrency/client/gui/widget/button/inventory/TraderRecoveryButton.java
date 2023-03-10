package io.github.lightman314.lightmanscurrency.client.gui.widget.button.inventory;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.Config;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.emergency_ejection.EjectionSaveData;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.emergencyejection.CPacketOpenTraderRecovery;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.screen.inventory.CreativeScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemGroup;
import net.minecraft.util.ResourceLocation;

import javax.annotation.Nonnull;

public class TraderRecoveryButton extends PlainButton {
	
	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/misc.png");
	
	private static TraderRecoveryButton lastButton = null;
	
	public static final int SIZE = 9;
	
	public static final int OFFSET = -10;
	
	private final ContainerScreen<?> screen;
	private PlayerEntity getPlayer() {
			Minecraft mc = this.screen.getMinecraft();
			if(mc != null)
				return mc.player;
			return null;
	}
	
	public TraderRecoveryButton(ContainerScreen<?> screen) {
		super(getXPosition(screen), getYPosition(screen), SIZE, SIZE, b -> openTraderRecoveryMenu(), GUI_TEXTURE, 0, 0);
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
		
		if(EjectionSaveData.GetValidEjectionData(true, this.getPlayer()).size() > 0)
		{
			this.visible = true;
			//Change visibility based on whether the correct tab is open
			if(this.screen instanceof CreativeScreen)
				this.visible = ((CreativeScreen)this.screen).getSelectedTab() == ItemGroup.TAB_INVENTORY.getId();
			super.render(pose, mouseX, mouseY, partialTicks);
		}
		else
			this.visible = false;
		
	}
	
	public static void tryRenderTooltip(MatrixStack pose, int mouseX, int mouseY) {
		if(lastButton != null && lastButton.isMouseOver(mouseX, mouseY))
			lastButton.screen.renderTooltip(pose, EasyText.translatable("tooltip.button.team_manager"), mouseX, mouseY);
	}
	
	private static void openTraderRecoveryMenu() {
		LightmansCurrencyPacketHandler.instance.sendToServer(new CPacketOpenTraderRecovery());
	}
	
}