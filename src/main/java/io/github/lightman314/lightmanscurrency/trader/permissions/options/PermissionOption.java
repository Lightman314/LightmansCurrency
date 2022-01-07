package io.github.lightman314.lightmanscurrency.trader.permissions.options;

import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.trader.permissions.PermissionsList;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class PermissionOption {
	
	public final String permission;
	
	protected PermissionOption(String permission) { this.permission = permission; }
	
	public ITextComponent widgetName() { return new TranslationTextComponent("permission." + permission); }
	
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
		CompoundNBT updateInfo = this.permissionList.get().changeLevel(this.screen.getPlayer(), permission, newValue);
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
	
	public void render(MatrixStack matrix, int mouseX, int mouseY) { }
	
	public abstract int widgetWidth();
	
	public static class OptionWidgets
	{
		List<Widget> buttons = Lists.newArrayList();
		List<IGuiEventListener> listeners = Lists.newArrayList();
		
		public List<Widget> getRenderableWidgets() { return this.buttons; }
		public List<IGuiEventListener> getListeners() { return this.listeners; }
		
		public <T extends Widget> T addRenderableWidget(T widget)
		{
			this.buttons.add(widget);
			return widget;
		}
		
		public <T extends IGuiEventListener> T addListener(T listener)
		{
			this.listeners.add(listener);
			return listener;
		}
		
	}
	
}
