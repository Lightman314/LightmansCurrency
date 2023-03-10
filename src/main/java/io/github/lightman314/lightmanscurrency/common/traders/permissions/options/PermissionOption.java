package io.github.lightman314.lightmanscurrency.common.traders.permissions.options;

import java.util.List;

import com.google.common.collect.Lists;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.IFormattableTextComponent;

public abstract class PermissionOption {
	
	public final String permission;
	
	protected PermissionOption(String permission) { this.permission = permission; }
	
	public IFormattableTextComponent widgetName() { return EasyText.translatable("permission." + permission); }
	
	protected TraderSettingsScreen screen;
	
	protected final boolean hasPermission() { return this.permissionValue() > 0; }
	
	protected final int permissionValue()
	{
		if(this.screen.getTrader() == null)
			return 0;
		return this.screen.getTrader().getAllyPermissionLevel(this.permission);
	}
	
	public final void setValue(boolean newValue) { setValue(newValue ? 1 : 0); }
	
	public final void setValue(int newValue)
	{
		if(this.screen.getTrader() == null)
			return;
		this.screen.getTrader().setAllyPermissionLevel(this.screen.getPlayer(), this.permission, newValue);
		CompoundNBT message = new CompoundNBT();
		message.putString("ChangeAllyPermissions", this.permission);
		message.putInt("NewLevel", newValue);
		this.screen.getTrader().sendNetworkMessage(message);
	}
	
	public final OptionWidgets initWidgets(TraderSettingsScreen screen, int x, int y)
	{
		this.screen = screen;
		OptionWidgets widgets = new OptionWidgets();
		this.createWidget(x, y, widgets);
		return widgets;
	}
	
	protected abstract void createWidget(int x, int y, OptionWidgets widgets);
	
	public abstract void tick();
	
	public void render(MatrixStack pose, int mouseX, int mouseY) { }
	
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