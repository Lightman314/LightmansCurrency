package io.github.lightman314.lightmanscurrency.api.traders.permissions;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;

import java.util.function.Consumer;

public class BooleanPermission extends PermissionOption {

	protected BooleanPermission(String permission) { super(permission); }

	PlainButton checkmark;
	
	@Override
	protected void createWidget(int x, int y, Consumer<Object> addWidgets) {
		this.checkmark = PlainButton.builder()
				.position(x,y + 4)
				.pressAction(this::TogglePermission)
				.sprite(IconAndButtonUtil.SPRITE_CHECK(this::hasPermission))
				.build();
		addWidgets.accept(this.checkmark);
	}

    @Override
	public int widgetWidth() { return 12; }
	
	private void TogglePermission()
	{
		this.setValue(!this.hasPermission());
	}
	
	public static BooleanPermission of(String permission) { return new BooleanPermission(permission); }

}
