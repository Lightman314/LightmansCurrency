package io.github.lightman314.lightmanscurrency.trader.permissions.options;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.trader.permissions.PermissionsList;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class PermissionOption {
	
	public final String permission;
	
	protected PermissionOption(String permission) { this.permission = permission; }
	
	public Component widgetName() { return new TranslatableComponent("permission." + permission); }
	
	protected TraderSettingsScreen screen;
	private Supplier<PermissionsList> permissionList;
	
	protected final boolean hasPermission() { return this.permissionValue() > 0; }
	
	protected final int permissionValue()
	{
		if(this.permissionList == null || this.permissionList.get() == null)
			return 0;
		return this.permissionList.get().getLevel(this.permission);
	}
	
	public final void setValue(boolean newValue) { setValue(newValue ? 1 : 0); }
	
	public final void setValue(int newValue)
	{
		if(this.permissionList == null || permissionList.get() == null)
			return;
		CompoundTag updateInfo = this.permissionList.get().changeLevel(this.screen.getPlayer(), permission, newValue);
		this.screen.getSetting(CoreTraderSettings.class).sendToServer(updateInfo);
	}
	
	public final OptionWidgets initWidgets(TraderSettingsScreen screen, Supplier<PermissionsList> permissionList, int x, int y)
	{
		this.screen = screen;
		this.permissionList = permissionList;
		OptionWidgets widgets = new OptionWidgets();
		this.createWidget(x, y, widgets);
		return widgets;
	}
	
	protected abstract void createWidget(int x, int y, OptionWidgets widgets);
	
	public abstract void tick();
	
	public void render(PoseStack pose, int mouseX, int mouseY) { }
	
	public abstract int widgetWidth();
	
	public static class OptionWidgets
	{
		List<AbstractWidget> buttons = Lists.newArrayList();
		List<GuiEventListener> listeners = Lists.newArrayList();
		
		public List<AbstractWidget> getRenderableWidgets() { return this.buttons; }
		public List<GuiEventListener> getListeners() { return this.listeners; }
		
		public <T extends AbstractWidget> T addRenderableWidget(T widget)
		{
			this.buttons.add(widget);
			return widget;
		}
		
		public <T extends GuiEventListener> T addListener(T listener)
		{
			this.listeners.add(listener);
			return listener;
		}
		
	}
	
}
