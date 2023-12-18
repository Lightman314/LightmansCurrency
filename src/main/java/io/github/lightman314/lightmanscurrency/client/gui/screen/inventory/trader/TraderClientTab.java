package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader;

import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderMenu;
import io.github.lightman314.lightmanscurrency.api.traders.menu.customer.ITraderScreen;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public abstract class TraderClientTab extends EasyTab {
	
	protected final ITraderScreen screen;
	protected final ITraderMenu menu;
	protected final Font font;

	@Nonnull
	@Override
	public IconData getIcon() { return IconData.BLANK; }

	@Override
	public final int getColor() { return 0xFFFFFF; }

	@Nullable
	@Override
	public final Component getTooltip() { return EasyText.empty(); }

	protected TraderClientTab(@Nonnull ITraderScreen screen) {
		super(screen);
		this.screen = screen;
		this.menu = this.screen.getMenu();
		this.font = this.screen.getFont();
	}
	
}
