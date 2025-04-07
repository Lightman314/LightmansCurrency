package io.github.lightman314.lightmanscurrency.common.traders.permissions;

import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.core.PermissionsTab;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.client.util.text_inputs.TextInputUtil;
import net.minecraft.client.gui.components.EditBox;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class IntegerPermission extends PermissionOption {

	public final int maxValue;
	private EditBox inputBox;
	
	protected IntegerPermission(String permission, int maxValue) {
		super(permission);
		this.maxValue = Math.abs(maxValue);
	}

	@Override
	protected void createWidget(int x, int y, @Nonnull Consumer<Object> addWidgets) {
		this.inputBox = TextInputUtil.intBuilder()
				.position(x,y)
				.size(20,PermissionsTab.ROW_HEIGHT)
				.startingValue(this.permissionValue())
				.apply(IntParser.builder()
						.min(0)
						.max(this.maxValue)
						.consumer())
				.handler(this::setValue)
				.build();
		addWidgets.accept(this.inputBox);
	}

	@Override
	public void updateWidgetPosition(int x, int y) {
		if(this.inputBox != null)
			this.inputBox.setPosition(x,y);
	}

	@Override
	public int widgetWidth() { return 22; }

	@Override
	public void tick() {
		if(this.inputBox != null)
			this.inputBox.visible = this.isVisible();
	}

	public static IntegerPermission of(String permission, int maxValue) { return new IntegerPermission(permission, maxValue); }
	
}
