package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.TraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.TraderMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class TraderClientTab extends EasyTab {
	
	protected final TraderScreen screen;
	protected final TraderMenu menu;
	protected final Font font;

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.BLANK; }

	@Override
	public final int getColor() { return 0xFFFFFF; }

	@Nullable
	@Override
	public final Component getTooltip() { return EasyText.empty(); }

	protected TraderClientTab(TraderScreen screen) {
		super(screen);
		this.screen = screen;
		this.menu = this.screen.getMenu();
		this.font = this.screen.getFont();
	}
	
}
