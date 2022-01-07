package io.github.lightman314.lightmanscurrency.client.gui.settings;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.trader.permissions.PermissionsList;
import io.github.lightman314.lightmanscurrency.trader.permissions.options.PermissionOption;
import io.github.lightman314.lightmanscurrency.trader.permissions.options.PermissionOption.OptionWidgets;

public abstract class PermissionsTab extends SettingsTab{

	List<OptionWidgets> widgets = Lists.newArrayList();
	List<PermissionOption> options;
	
	protected abstract int startHeight();
	private int calculateStartHeight()
	{
		return this.getScreen().guiTop() + this.startHeight();
	}
	protected abstract PermissionsList getPermissionsList();

	@Override
	public void initTab() {
		this.options = Lists.newArrayList();
		this.getScreen().getSettings().forEach(setting-> setting.getPermissionOptions().forEach(option ->this.options.add(option)));
		int startHeight = this.calculateStartHeight();
		for(int i = 0; i < this.options.size(); ++i)
		{
			int xPos = this.getXPos(i);
			int yPos = this.getYPosOffset(i) + startHeight;
			PermissionOption option = this.options.get(i);
			OptionWidgets optionWidgets = option.initWidgets(this.getScreen(), this::getPermissionsList, xPos, yPos);
			optionWidgets.getRenderableWidgets().forEach(widget -> this.getScreen().addRenderableTabWidget(widget));
			optionWidgets.getListeners().forEach(listener -> this.getScreen().addTabListener(listener));
			this.widgets.add(optionWidgets);
		}
	}
	
	private final int getYPosOffset(int index)
	{
		int yIndex = index / 2;
		return 20 * yIndex;
	}
	
	private final int getXPos(int index)
	{
		return this.getScreen().guiLeft() + (index % 2 == 0 ? 5 : 105);
	}

	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		int startHeight = this.calculateStartHeight();
		for(int i = 0; i < this.options.size(); ++i)
		{
			PermissionOption option = this.options.get(i);
			int xPos = this.getXPos(i) + option.widgetWidth();
			int yPos = this.getYPosOffset(i) + startHeight;
			int textWidth = 90 - option.widgetWidth();
			int textHeight = this.getFont().getWordWrappedHeight(option.widgetName().getString(), textWidth);
			int yStart = ((20 - textHeight) / 2) + yPos;
			this.getFont().func_238418_a_(option.widgetName(), xPos, yStart, textWidth, 0xFFFFFF);
		}
	}

	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) { }

	@Override
	public void tick() {
		for(int i = 0; i < this.options.size(); ++i)
		{
			this.options.get(i).tick();
		}
	}

	@Override
	public void closeTab() {
		
	}

}
