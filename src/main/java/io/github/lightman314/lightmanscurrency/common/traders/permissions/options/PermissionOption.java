package io.github.lightman314.lightmanscurrency.common.traders.permissions.options;

import java.util.function.Consumer;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public abstract class PermissionOption {
	
	public final String permission;
	
	protected PermissionOption(String permission) { this.permission = permission; }
	
	public MutableComponent widgetName() { return Component.translatable("permission." + permission); }
	
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
		this.tab.menu.getTrader().setAllyPermissionLevel(this.tab.menu.player, this.permission, newValue);
		CompoundTag message = new CompoundTag();
		message.putString("ChangeAllyPermissions", this.permission);
		message.putInt("NewLevel", newValue);
		this.tab.sendNetworkMessage(message);
	}
	
	public final void initWidgets(SettingsSubTab tab, int x, int y, Consumer<Object> addWidgets)
	{
		this.tab = tab;
		this.createWidget(x, y, addWidgets);
	}
	
	protected abstract void createWidget(int x, int y, Consumer<Object> addWidgets);
	
	public void tick() {}
	
	public void render(PoseStack pose, int mouseX, int mouseY) { }
	
	public abstract int widgetWidth();
	
}
