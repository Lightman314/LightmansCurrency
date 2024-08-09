package io.github.lightman314.lightmanscurrency.api.traders.permissions;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class PermissionOption {
	
	public final String permission;
	
	protected PermissionOption(String permission) { this.permission = permission; }
	
	public MutableComponent widgetName() { return Component.translatable("permission.lightmanscurrency." + this.permission); }
	
	protected SettingsSubTab tab;
	
	protected final boolean hasPermission() { return this.permissionValue() > 0; }
	
	protected final int permissionValue()
	{
		if(this.tab.menu.getTrader() == null)
			return 0;
		return this.tab.menu.getTrader().getAllyPermissionLevel(this.permission);
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
	
	public final void initWidgets(SettingsSubTab tab, int x, int y, Consumer<Object> addWidgets)
	{
		this.tab = tab;
		this.createWidget(x, y, addWidgets);
	}
	
	protected abstract void createWidget(int x, int y, Consumer<Object> addWidgets);
	
	public void tick() {}
	
	public void render(EasyGuiGraphics gui) { }
	
	public abstract int widgetWidth();
	
}
