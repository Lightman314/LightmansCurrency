package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyMenuScreen;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.easy.rendering.Sprite;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;

import io.github.lightman314.lightmanscurrency.common.menus.MintMenu;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import javax.annotation.Nonnull;

public class MintScreen extends EasyMenuScreen<MintMenu> {

	public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/coinmint.png");

	public static final Sprite ARROW_SPRITE = Sprite.LockedSprite(GUI_TEXTURE, 176, 0, 22, 16);
	
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

		gui.blitSpriteFadeHoriz(ARROW_SPRITE, ScreenPosition.of(80, 21), this.menu.blockEntity.getMintProgress());

	}
	
	@Override
	protected void initialize(ScreenArea screenArea) { }
	
}
