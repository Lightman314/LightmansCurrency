package io.github.lightman314.lightmanscurrency.trader.permissions.options;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;

public class IntegerPermission extends PermissionOption{

	public final int maxValue;
	
	protected IntegerPermission(String permission, int maxValue) {
		super(permission);
		this.maxValue = Math.abs(maxValue);
	}

	@Override
	protected void createWidget(int x, int y, OptionWidgets widgets) {
		LightmansCurrency.LogInfo("Integer Permission Widget is not yet built.");
	}

	@Override
	public void tick() {
		
		
	}

	@Override
	public int widgetWidth() {
		return 0;
	}

	public static IntegerPermission of(String permission, int maxValue) { return new IntegerPermission(permission, maxValue); }
	
}
