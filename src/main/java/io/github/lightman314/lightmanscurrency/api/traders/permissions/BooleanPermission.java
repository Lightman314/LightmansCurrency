package io.github.lightman314.lightmanscurrency.api.traders.permissions;

import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteUtil;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import net.minecraft.MethodsReturnNonnullByDefault;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BooleanPermission extends PermissionOption {

	protected BooleanPermission(String permission) { super(permission); }

	PlainButton checkmark;

	@Override
	protected void createWidget(int x, int y, Consumer<Object> addWidgets) {
		this.checkmark = PlainButton.builder()
				.position(x,y + 2)
				.pressAction(this::TogglePermission)
				.sprite(SpriteUtil.createCheckbox(this::hasPermission))
				.addon(EasyAddonHelper.visibleCheck(this::isVisible))
				.build();
		addWidgets.accept(this.checkmark);
	}

	@Override
	public void updateWidgetPosition(int x, int y) {
		if(this.checkmark != null)
			this.checkmark.setPosition(x,y + 2);
	}

	@Override
	public int widgetWidth() { return 12; }

	private void TogglePermission()
	{
		this.setValue(!this.hasPermission());
	}

	public static BooleanPermission of(String permission) { return new BooleanPermission(permission); }

}