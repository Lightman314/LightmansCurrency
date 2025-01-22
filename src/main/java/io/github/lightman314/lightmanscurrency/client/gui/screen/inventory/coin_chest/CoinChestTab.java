package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyTab;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.IEasyScreen;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.CoinChestScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.tab.TabButton;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

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
			this.icon = IconData.of(data.getItem());
			this.tooltip = data.getItem().getName(new ItemStack(data.getItem()));
		}

		@Nonnull
		@Override
		public IconData getIcon() { return this.icon; }
		@Override
		public Component getTooltip() { return this.tooltip; }
		//Use red sprite for inactive buttons
		@Nullable
		@Override
		public Pair<ResourceLocation, ScreenPosition> getSprite() {
			CoinChestUpgradeData data = this.getUpgradeData();
			if(data == null)
				return null;
			return data.isActive() ? TabButton.NORMAL : TabButton.RED;
		}

		@Nullable
		protected final CoinChestUpgradeData getUpgradeData() { return this.screen.be.getChestUpgradeOfType(this.type); }

	}

}
