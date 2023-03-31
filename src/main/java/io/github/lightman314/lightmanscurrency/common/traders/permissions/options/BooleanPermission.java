package io.github.lightman314.lightmanscurrency.common.traders.permissions.options;

import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import net.minecraft.client.gui.components.Button;

public class BooleanPermission extends PermissionOption{

	protected BooleanPermission(String permission) { super(permission); }

	PlainButton checkmark;
	
	@Override
	protected void createWidget(int x, int y, OptionWidgets widgets) {
		this.checkmark = widgets.addRenderableWidget(new PlainButton(x, y + 5, 10, 10, this::TogglePermission, IconAndButtonUtil.WIDGET_TEXTURE, 10, this.hasPermission() ? 200 : 220));
	}

	@Override
	public void tick() {
		this.checkmark.setResource(IconAndButtonUtil.WIDGET_TEXTURE, 10, this.hasPermission() ? 200 : 220);
	}

	@Override
	public int widgetWidth() {
		return 12;
	}
	
	private void TogglePermission(Button button)
	{
		this.setValue(!this.hasPermission());
	}
	
	public static BooleanPermission of(String permission) { return new BooleanPermission(permission); }

}
