package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import org.anti_ad.mc.ipn.api.IPNIgnore;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.common.menus.MintMenu;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;

import javax.annotation.Nonnull;

@IPNIgnore
public class MintScreen extends EasyMenuScreen<MintMenu> {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/coinmint.png");

	public static final Sprite ARROW_SPRITE = Sprite.SimpleSprite(GUI_TEXTURE, 176, 0, 24, 16);
	
	public MintScreen(MintMenu container, Inventory inventory, Component title)
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
	}
	
	@Override
	protected void initialize(ScreenArea screenArea)
	{
		this.addChild(new PlainButton(screenArea.pos.offset(79, 21), this::mintCoin, ARROW_SPRITE)
				.withAddons(
						EasyAddonHelper.tooltip(this::getMintTooltip),
						EasyAddonHelper.visibleCheck(() -> this.menu.blockEntity.validMintInput())
				));
	}

	private Component getMintTooltip()
	{
		if(this.menu.isMeltInput())
			return EasyText.translatable("gui.button.lightmanscurrency.melt");
		else
			return EasyText.translatable("gui.button.lightmanscurrency.mint");
	}
	
	private void mintCoin(EasyButton button) { this.menu.SendMintCoinsMessage(Screen.hasShiftDown()); }
	
}
