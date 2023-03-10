package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.options.PermissionOption.OptionWidgets;
import net.minecraft.item.Items;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class PermissionsTab extends SettingsTab{

	public static PermissionsTab INSTANCE = new PermissionsTab();
	
	private PermissionsTab() {}
	
	List<OptionWidgets> widgets = Lists.newArrayList();
	List<PermissionOption> options;
	
	protected int startHeight() { return 5; }
	private int calculateStartHeight()
	{
		return this.getScreen().guiTop() + this.startHeight();
	}
	
	@Override
	public int getColor() { return 0xFFFFFF; }

	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(Items.BOOKSHELF); }

	@Override
	public ITextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.allyperms"); }

	@Override
	public boolean canOpen() { return this.hasPermissions(Permissions.EDIT_PERMISSIONS); }

	@Override
	public void initTab() {
		this.options = new ArrayList<>();
		this.options.addAll(this.getScreen().getTrader().getPermissionOptions());
		int startHeight = this.calculateStartHeight();
		for(int i = 0; i < this.options.size(); ++i)
		{
			int xPos = this.getXPos(i);
			int yPos = this.getYPosOffset(i) + startHeight;
			PermissionOption option = this.options.get(i);
			OptionWidgets optionWidgets = option.initWidgets(this.getScreen(), xPos, yPos);
			optionWidgets.getRenderableWidgets().forEach(widget -> this.getScreen().addRenderableTabWidget(widget));
			optionWidgets.getListeners().forEach(listener -> this.getScreen().addTabListener(listener));
			this.widgets.add(optionWidgets);
		}
	}
	
	private int getYPosOffset(int index)
	{
		int yIndex = index / 2;
		return 20 * yIndex;
	}
	
	private int getXPos(int index)
	{
		return this.getScreen().guiLeft() + (index % 2 == 0 ? 5 : 105);
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		int startHeight = this.calculateStartHeight();
		for(int i = 0; i < this.options.size(); ++i)
		{
			PermissionOption option = this.options.get(i);
			int xPos = this.getXPos(i) + option.widgetWidth();
			int yPos = this.getYPosOffset(i) + startHeight;
			int textWidth = 90 - option.widgetWidth();
			int textHeight = this.getFont().wordWrapHeight(option.widgetName().getString(), textWidth);
			int yStart = ((20 - textHeight) / 2) + yPos;
			this.getFont().drawWordWrap(option.widgetName(), xPos, yStart, textWidth, 0xFFFFFF);
		}
	}

	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) { }

	@Override
	public void tick() {
		for (PermissionOption option : this.options) {
			option.tick();
		}
	}

	@Override
	public void closeTab() {
		
	}

}