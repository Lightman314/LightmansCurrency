package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.coin_chest;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.CoinChestScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton.ITab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.menus.CoinChestMenu;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgrade;
import io.github.lightman314.lightmanscurrency.common.upgrades.types.coin_chest.CoinChestUpgradeData;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public abstract class CoinChestTab implements ITab
{

	protected final CoinChestScreen screen;
	protected final CoinChestMenu menu;
	public final Font font;

	protected CoinChestTab(Object screen) { this.screen = (CoinChestScreen)screen; this.menu = this.screen.getMenu(); this.font = this.screen.getFont(); }

	public boolean isVisible() { return true; }

	public abstract void init();
	
	public abstract void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks);
	
	public abstract void postRender(PoseStack pose, int mouseX, int mouseY);
	
	public abstract void tick();
	
	public abstract void onClose();
	
	public boolean blockInventoryClosing() { return false; }
	
	public final int getColor() { return 0xFFFFFF; }

	public final boolean upgradeSlotsVisible() { return this.getClass() == DefaultTab.class; }
	public boolean coinSlotsVisible() { return true; }
	public boolean inventoryVisible() { return true; }
	public boolean titleVisible() { return true; }

	public static abstract class Upgrade extends CoinChestTab
	{

		private final CoinChestUpgrade type;
		private final IconData icon;
		private final MutableComponent tooltip;

		protected Upgrade(CoinChestUpgradeData data, Object screen) {
			super(screen);
			this.type = data.upgrade;
			this.icon = IconData.of(data.getItem());
			this.tooltip = EasyText.makeMutable(data.getItem().getName(new ItemStack(data.getItem())));
		}

		@Nonnull
		@Override
		public final IconData getIcon() { return this.icon; }
		@Override
		public final MutableComponent getTooltip() { return this.tooltip; }

		@Nullable
		protected final CoinChestUpgradeData getUpgradeData() { return this.screen.be.getChestUpgradeOfType(this.type); }

	}

}
