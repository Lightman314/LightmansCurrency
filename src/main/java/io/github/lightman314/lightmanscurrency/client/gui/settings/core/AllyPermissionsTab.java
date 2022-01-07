package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.client.gui.settings.PermissionsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.permissions.PermissionsList;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class AllyPermissionsTab extends PermissionsTab{

	public static final AllyPermissionsTab INSTANCE = new AllyPermissionsTab();
	
	private AllyPermissionsTab() {}
	
	@Override
	protected int startHeight() {
		return 5;
	}

	@Override
	protected PermissionsList getPermissionsList() {
		return this.getSetting(CoreTraderSettings.class).getAllyPermissions();
	}

	@Override
	public int getColor() { return 0xFFFFFF; }

	@Override
	public IconData getIcon() { return IconData.of(Items.BOOKSHELF); }

	@Override
	public ITextComponent getTooltip() {
		return new TranslationTextComponent("tooltip.lightmanscurrency.settings.allyperms");
	}

	@Override
	public ImmutableList<String> requiredPermissions() {
		return ImmutableList.of(Permissions.EDIT_PERMISSIONS);
	}

}
