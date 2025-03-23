package io.github.lightman314.lightmanscurrency.common.traders.permissions;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class IntegerPermission extends PermissionOption {

	public final int maxValue;
	
	protected IntegerPermission(String permission, int maxValue) {
		super(permission);
		this.maxValue = Math.abs(maxValue);
	}

	@Override
	protected void createWidget(int x, int y, @Nonnull Consumer<Object> addWidgets) {
		LightmansCurrency.LogInfo("Integer Permission Widget is not yet built.");
	}

	@Override
	public void updateWidgetPosition(int x, int y) {

	}

	@Override
	public int widgetWidth() {
		return 0;
	}

	public static IntegerPermission of(String permission, int maxValue) { return new IntegerPermission(permission, maxValue); }
	
}
