package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.menus.TicketMachineMenu;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

import javax.annotation.Nonnull;

public class TicketMachineScreen extends EasyMenuScreen<TicketMachineMenu> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/ticket_machine.png");

	public static final Sprite SPRITE_ARROW = Sprite.SimpleSprite(GUI_TEXTURE, 176, 0, 24, 16);

	private PlainButton buttonTogglePass;

	private boolean craftPass = false;
	
	public TicketMachineScreen(TicketMachineMenu container, Inventory inventory, Component title)
	{
		super(container, inventory, title);
		this.resize(176,138);
	}
	
	@Override
	protected void renderBG(@Nonnull EasyGuiGraphics gui)
	{

		gui.renderNormalBackground(GUI_TEXTURE, this);

		gui.drawString(this.title, 8, 6, 0x404040);
		gui.drawString(this.playerInventoryTitle, 8, (this.getYSize() - 94), 0x404040);

		if(this.buttonTogglePass.visible)
		{
			int textWidth = gui.font.width(EasyText.translatable("gui.button.lightmanscurrency.craft_pass.option"));
			gui.drawString(EasyText.translatable("gui.button.lightmanscurrency.craft_pass.option"), this.getXSize() - 14 - textWidth, 6, 0x404040);
		}
		
	}
	
	@Override
	protected void initialize(ScreenArea screenArea)
	{
		this.addChild(new PlainButton(screenArea.x + 79, screenArea.y + 21, this::craftTicket, SPRITE_ARROW)
				.withAddons(
						EasyAddonHelper.visibleCheck(() -> this.menu.validInputs() && this.menu.roomForOutput(this.craftPass)),
						EasyAddonHelper.tooltip(this::getArrowTooltip)
				));

		this.buttonTogglePass = this.addChild(IconAndButtonUtil.checkmarkButton(screenArea.x + screenArea.width - 14, screenArea.y + 5, this::togglePassCraft, () -> this.craftPass)
				.withAddons(EasyAddonHelper.visibleCheck(this.menu::hasMasterTicket)
				));

	}

	private Component getArrowTooltip()
	{
		if(this.menu.hasMasterTicket())
			return this.craftPass ? EasyText.translatable("gui.button.lightmanscurrency.craft_pass") : EasyText.translatable("gui.button.lightmanscurrency.craft_ticket");
		else
			return EasyText.translatable("gui.button.lightmanscurrency.craft_master_ticket");
	}

	private void togglePassCraft(EasyButton button) { this.craftPass = !this.craftPass; }

	private void craftTicket(EasyButton button) { this.menu.SendCraftTicketsMessage(Screen.hasShiftDown(), this.craftPass); }
	
}
