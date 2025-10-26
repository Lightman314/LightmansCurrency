package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.CoinChestScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.WidgetRotation;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CoinChestTab extends EasyTab
{

	protected final CoinChestScreen screen;
	protected final CoinChestMenu menu;

	protected CoinChestTab(Object screen) { super((IEasyScreen)screen); this.screen = (CoinChestScreen)screen; this.menu = this.screen.getMenu(); }

	public boolean isVisible() { return true; }

	public final boolean upgradeSlotsVisible() { return this.getClass() == DefaultTab.class; }
	public boolean coinSlotsVisible() { return true; }
	public boolean inventoryVisible() { return true; }
	public boolean titleVisible() { return true; }

	public static abstract class Upgrade extends CoinChestTab
	{

		private final CoinChestUpgrade type;
		private final IconData icon;
		private final Component tooltip;

		protected Upgrade(CoinChestUpgradeData data, Object screen) {
			super(screen);
			this.type = data.upgrade;
			this.icon = ItemIcon.ofItem(data.getItem());
			this.tooltip = data.getItem().getName(new ItemStack(data.getItem()));
		}

		@Override
		public IconData getIcon() { return this.icon; }
		@Override
		public Component getTooltip() { return this.tooltip; }
		//Use red texture for inactive buttons
		@Nullable
		@Override
		public Function<WidgetRotation, FixedSizeSprite> getSprite() {
			CoinChestUpgradeData data = this.getUpgradeData();
			if(data == null)
				return null;
			return data.isActive() ? TabButton.NORMAL : TabButton.RED;
		}

		@Nullable
		protected final CoinChestUpgradeData getUpgradeData() { return this.screen.be.getChestUpgradeOfType(this.type); }

	}

}
