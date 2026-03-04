package io.github.lightman314.lightmanscurrency.api.traders.permissions.client;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.PermissionsTab;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.AlliesNode;
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
		if(this.tab == null)
			return 0;
        AlliesNode node = this.tab.menu.getTraderNode(AlliesNode.TYPE);
        return node != null ? node.getAllyPermissionsMap().getOrDefault(this.permission,0) : 0;
	}

	protected final boolean isVisible() {
		if(this.tab == null)
			return false;
		return this.tab.isOptionVisible(this);
	}
	
	public final void setValue(boolean newValue) { setValue(newValue ? 1 : 0); }
	
	public final void setValue(int newValue)
	{
        if(this.tab == null)
            return;
        TraderData trader = this.tab.menu.getTrader();
        if(trader == null)
            return;
        AlliesNode node = this.tab.menu.getTraderNode(AlliesNode.TYPE);
        if(node != null && trader.hasPermission(this.tab.menu.getPlayer(),Permissions.EDIT_PERMISSIONS))
            node.setAllyPermission(PlayerReference.of(this.tab.menu.getPlayer()),this.permission,newValue);
        //Send the request to the server regardless, just in case the server has the correct trader state
		this.tab.sendMessage(tab.builder()
				.setString("ChangeAllyPermissions", this.permission)
				.setInt("NewLevel", newValue));
	}
	
	public final void initWidgets(PermissionsTab tab, int x, int y, Consumer<Object> addWidgets)
	{
		this.tab = tab;
		this.createWidget(x, y, addWidgets);
	}
	
	protected abstract void createWidget(int x, int y, Consumer<Object> addWidgets);

	public abstract void updateWidgetPosition(int x,int y);

	public void tick() {}
	
	public void render(EasyGuiGraphics gui) { }
	
	public abstract int widgetWidth();
	
}
