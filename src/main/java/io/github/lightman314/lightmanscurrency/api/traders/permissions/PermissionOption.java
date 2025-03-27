package io.github.lightman314.lightmanscurrency.api.traders.permissions;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.PermissionsTab;
import net.minecraft.network.chat.MutableComponent;

import javax.annotation.Nullable;

public abstract class PermissionOption {
	
	public final String permission;
	
	protected PermissionOption(String permission) { this.permission = permission; }
	
	public MutableComponent widgetName() { return EasyText.translatable("permission.lightmanscurrency." + this.permission); }
	@Nullable
	public MutableComponent widgetTooltip() { return EasyText.translatableOrNull("permission.lightmanscurrency." + this.permission + ".tooltip"); }

	protected PermissionsTab tab;
	
	protected final boolean hasPermission() { return this.permissionValue() > 0; }
	
	protected final int permissionValue()
	{
		if(this.tab == null || this.tab.menu.getTrader() == null)
			return 0;
		return this.tab.menu.getTrader().getAllyPermissionLevel(this.permission);
	}

	protected final boolean isVisible() {
		if(this.tab == null)
			return false;
		return this.tab.isOptionVisible(this);
	}
	
	public final void setValue(boolean newValue) { setValue(newValue ? 1 : 0); }
	
	public final void setValue(int newValue)
	{
		if(this.tab.menu.getTrader() == null)
			return;
		this.tab.menu.getTrader().setAllyPermissionLevel(this.tab.menu.getPlayer(), this.permission, newValue);
		this.tab.sendMessage(LazyPacketData.builder()
				.setString("ChangeAllyPermissions", this.permission)
				.setInt("NewLevel", newValue));
	}
	
	public final void initWidgets(PermissionsTab tab, int x, int y, Consumer<Object> addWidgets)
	{
		this.tab = tab;
		this.createWidget(x, y, addWidgets);
	}
	
	protected abstract void createWidget(int x, int y, Consumer<Object> addWidgets);

	public abstract void updateWidgetPosition(int x, int y);

	public void tick() {}
	
	public void render(EasyGuiGraphics gui) { }
	
	public abstract int widgetWidth();
	
}
