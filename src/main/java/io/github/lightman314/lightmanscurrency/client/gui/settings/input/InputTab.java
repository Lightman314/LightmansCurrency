package io.github.lightman314.lightmanscurrency.client.gui.settings.input;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.traders.InputTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

public class InputTab extends SettingsTab{

	public static final InputTab INSTANCE = new InputTab();
	
	private InputTab() { }
	
	DirectionalSettingsWidget inputWidget;
	DirectionalSettingsWidget outputWidget;
	
	
	
	protected InputTraderData getInputTrader() {
		TraderData trader = this.getTrader();
		if(trader instanceof InputTraderData)
			return (InputTraderData)trader;
		return null;
	}
	
	protected boolean getInputSideValue(Direction side) {
		InputTraderData trader = this.getInputTrader();
		if(trader != null)
			return trader.allowInputSide(side);
		return false;
	}
	
	protected boolean getOutputSideValue(Direction side) {
		InputTraderData trader = this.getInputTrader();
		if(trader != null)
			return trader.allowOutputSide(side);
		return false;
	}
	
	protected ImmutableList<Direction> getIgnoreList() {
		InputTraderData trader = this.getInputTrader();
		if(trader != null)
			return trader.ignoreSides;
		return ImmutableList.of(Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP, Direction.DOWN);
	}
	
	public int getTextColor() {
		InputTraderData trader = this.getInputTrader();
		if(trader != null)
			return trader.inputSettingsTextColor();
		return 0xD0D0D0;
	}
	
	@Override
	public int getColor()
	{
		InputTraderData trader = this.getInputTrader();
		if(trader != null)
			return trader.inputSettingsTabColor();
		return 0xFFFFFF;
	}

	@Override
	public IconData getIcon() {
		InputTraderData trader = this.getInputTrader();
		if(trader != null)
			return trader.inputSettingsTabIcon();
		return IconData.of(Items.HOPPER);
	}

	@Override
	public MutableComponent getTooltip() {
		InputTraderData trader = this.getInputTrader();
		if(trader != null)
			return trader.inputSettingsTabTooltip();
		return Component.translatable("tooltip.lightmanscurrency.settings.iteminput");
	}
	
	public List<InputTabAddon> getAddons() {
		InputTraderData trader = this.getInputTrader();
		if(trader != null)
			return trader.inputSettingsAddons();
		return ImmutableList.of();
	}

	@Override
	public boolean canOpen() { return this.hasPermissions(Permissions.InputTrader.EXTERNAL_INPUTS); }

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.inputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 20, screen.guiTop() + 25, this::getInputSideValue, this.getIgnoreList(), this::ToggleInputSide, screen::addRenderableTabWidget);
		this.outputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 110, screen.guiTop() + 25, this::getOutputSideValue, this.getIgnoreList(), this::ToggleOutputSide, screen::addRenderableTabWidget);
		
		this.getAddons().forEach(a -> a.onInit(screen));
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		
		TraderSettingsScreen screen = this.getScreen();
		//ItemTraderSettings settings = this.getSetting(ItemTraderSettings.class);
		
		//Side Widget Labels
		this.getFont().draw(pose, Component.translatable("gui.lightmanscurrency.settings.iteminput.side"), screen.guiLeft() + 20, screen.guiTop() + 7, this.getTextColor());
		this.getFont().draw(pose, Component.translatable("gui.lightmanscurrency.settings.itemoutput.side"), screen.guiLeft() + 110, screen.guiTop() + 7, this.getTextColor());

		this.getAddons().forEach(a -> a.preRender(screen, pose, mouseX, mouseY, partialTicks));
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		TraderSettingsScreen screen = this.getScreen();
		
		//Render side tooltips
		this.inputWidget.renderTooltips(pose, mouseX, mouseY, this.getScreen());
		this.outputWidget.renderTooltips(pose, mouseX, mouseY, this.getScreen());
		
		this.getAddons().forEach(a -> a.postRender(screen, pose, mouseX, mouseY, partialTicks));
		
	}

	@Override
	public void tick() {
		
		TraderSettingsScreen screen = this.getScreen();
		this.inputWidget.tick();
		this.outputWidget.tick();
		
		this.getAddons().forEach(a -> a.tick(screen));
		
	}

	@Override
	public void closeTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.getAddons().forEach(a -> a.onClose(screen));
		
	}
	
	private void ToggleInputSide(Direction side)
	{
		CompoundTag message = new CompoundTag();
		message.putBoolean("SetInputSide", !this.getInputSideValue(side));
		message.putInt("Side", side.get3DDataValue());
		this.sendNetworkMessage(message);
	}
	
	private void ToggleOutputSide(Direction side)
	{
		CompoundTag message = new CompoundTag();
		message.putBoolean("SetOuputSide", !this.getInputSideValue(side));
		message.putInt("Side", side.get3DDataValue());
		this.sendNetworkMessage(message);
	}

}