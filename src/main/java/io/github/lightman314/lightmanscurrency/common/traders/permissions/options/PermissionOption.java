package io.github.lightman314.lightmanscurrency.common.traders.permissions.options;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;

public abstract class PermissionOption {

	public final String permission;

	protected PermissionOption(String permission) { this.permission = permission; }

	public MutableComponent widgetName() { return EasyText.translatable("permission." + permission); }

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

	public final OptionWidgets initWidgets(SettingsSubTab tab, int x, int y)
	{
		this.tab = tab;
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