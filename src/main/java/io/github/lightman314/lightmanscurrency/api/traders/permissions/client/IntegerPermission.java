package io.github.lightman314.lightmanscurrency.api.traders.permissions.client;

import io.github.lightman314.lightmanscurrency.api.client.widgets.text_inputs.TextBoxWrapper;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.client.builtin.settings.builtin.PermissionsTab;
import io.github.lightman314.lightmanscurrency.api.client.widgets.text_inputs.IntParser;
import io.github.lightman314.lightmanscurrency.api.client.widgets.text_inputs.TextInputUtil;
import io.github.lightman314.lightmanscurrency.api.client.widgets.easy.EasyAddonHelper;

import java.util.function.Consumer;

/**
 * Never used in the base mod, but functional if you intend on adding permission options that can have various levels<br>
 * For example, a hypothetical "access" permission could allow a player to "view" certain info at level 1, and "edit" the info at level 2 (with level 0 being no permission at all)
 */
public class IntegerPermission extends PermissionOption {

	public final int maxValue;
	private TextBoxWrapper<Integer> inputBox;
	
	protected IntegerPermission(String permission, int maxValue) {
		super(permission);
		this.maxValue = Math.abs(maxValue);
	}

	@Override
	protected void createWidget(int x, int y, Consumer<Object> addWidgets) {
		this.inputBox = TextInputUtil.intBuilder()
				.position(x,y)
				.size(20,PermissionsTab.ROW_HEIGHT)
				.startingValue(this.permissionValue())
				.apply(IntParser.builder()
						.min(0)
						.max(this.maxValue)
						.consumer())
				.handler(this::setValue)
                .wrap()
                .addon(EasyAddonHelper.visibleCheck(this::isVisible))
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

	public static IntegerPermission of(String permission, int maxValue) { return new IntegerPermission(permission, maxValue); }
	
}
