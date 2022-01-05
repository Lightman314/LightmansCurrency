package io.github.lightman314.lightmanscurrency.client.gui.settings.item;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.ItemTraderSettings;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class ItemInputTab extends SettingsTab{

	public static final ItemInputTab INSTANCE = new ItemInputTab();
	
	private ItemInputTab() { }
	
	PlainButton buttonToggleInputLimit;
	PlainButton buttonToggleOuputLimit;
	
	DirectionalSettingsWidget inputWidget;
	DirectionalSettingsWidget outputWidget;
	
	private final int textColor = 0xD0D0D0;
	
	@Override
	public int getColor() { return 0x00BF00; }

	@Override
	public IconData getIcon() { return IconData.of(Items.HOPPER); }

	@Override
	public ITextComponent getTooltip() { return new TranslationTextComponent("tooltip.lightmanscurrency.settings.iteminput"); }

	@Override
	public ImmutableList<String> requiredPermissions() { return ImmutableList.of(Permissions.ItemTrader.EXTERNAL_INPUTS); }

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.inputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 20, screen.guiTop() + 25, () -> this.getSetting(ItemTraderSettings.class).getInputSides(), this::ToggleInputSide, screen::addRenderableTabWidget);
		this.outputWidget = new DirectionalSettingsWidget(screen.guiLeft() + 110, screen.guiTop() + 25, () -> this.getSetting(ItemTraderSettings.class).getOutputSides(), this::ToggleOutputSide, screen::addRenderableTabWidget);
		
		this.buttonToggleInputLimit = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 5, screen.guiTop() + 100, 10, 10, this::ToggleInputLimit, TraderSettingsScreen.GUI_TEXTURE, 10, 200));
		this.buttonToggleOuputLimit = screen.addRenderableTabWidget(new PlainButton(screen.guiLeft() + 95, screen.guiTop() + 100, 10, 10, this::ToggleOutputLimit, TraderSettingsScreen.GUI_TEXTURE, 10, 200));
		
	}

	@Override
	public void preRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		
		
		TraderSettingsScreen screen = this.getScreen();
		ItemTraderSettings settings = this.getSetting(ItemTraderSettings.class);
		
		//Side Widget Labels
		this.getFont().drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.settings.iteminput.side").getString(), screen.guiLeft() + 20, screen.guiTop() + 7, textColor);
		this.getFont().drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.settings.itemoutput.side").getString(), screen.guiLeft() + 110, screen.guiTop() + 7, textColor);
		
		//Limit Toggle Labels
		//Input
		this.getFont().drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.settings.iteminput.limit").getString(), screen.guiLeft() + 15, screen.guiTop() + 100, textColor);
		this.getFont().drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.settings.iteminput.limit." + (settings.limitInputsToSales() ? "limited" : "any")).getString(), screen.guiLeft() + 15, screen.guiTop() + 110, textColor);
		
		//Output
		this.getFont().drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.settings.itemoutput.limit").getString(), screen.guiLeft() + 105, screen.guiTop() + 100, textColor);
		this.getFont().drawString(matrix, new TranslationTextComponent("gui.lightmanscurrency.settings.itemoutput.limit." + (settings.limitOutputsToPurchases() ? "limited" : "any")).getString(), screen.guiLeft() + 105, screen.guiTop() + 110, textColor);
		
		
	}

	@Override
	public void postRender(MatrixStack matrix, int mouseX, int mouseY, float partialTicks) {
		
		//Render side tooltips
		this.inputWidget.renderTooltips(matrix, mouseX, mouseY, this.getScreen());
		this.outputWidget.renderTooltips(matrix, mouseX, mouseY, this.getScreen());
		
	}

	@Override
	public void tick() {
		
		ItemTraderSettings settings = this.getSetting(ItemTraderSettings.class);
		
		this.buttonToggleInputLimit.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, settings.limitInputsToSales() ? 200 : 220);
		this.buttonToggleOuputLimit.setResource(TraderSettingsScreen.GUI_TEXTURE, 10, settings.limitOutputsToPurchases() ? 200 : 220);
		this.inputWidget.tick();
		this.outputWidget.tick();
		
	}

	@Override
	public void closeTab() {
		
	}
	
	private void ToggleInputSide(Direction side)
	{
		LightmansCurrency.LogInfo("Toggling input side " + side.toString());
		ItemTraderSettings settings = this.getSetting(ItemTraderSettings.class);
		CompoundNBT updateInfo = settings.toggleInputSide(this.getPlayer(), side);
		settings.sendToServer(updateInfo);
	}
	
	private void ToggleOutputSide(Direction side)
	{
		LightmansCurrency.LogInfo("Toggling output side " + side.toString());
		ItemTraderSettings settings = this.getSetting(ItemTraderSettings.class);
		CompoundNBT updateInfo = settings.toggleOutputSide(this.getPlayer(), side);
		settings.sendToServer(updateInfo);
	}
	
	private void ToggleInputLimit(Button button)
	{
		ItemTraderSettings settings = this.getSetting(ItemTraderSettings.class);
		CompoundNBT updateInfo = settings.toggleInputLimit(this.getPlayer());
		settings.sendToServer(updateInfo);
	}
	
	private void ToggleOutputLimit(Button button)
	{
		ItemTraderSettings settings = this.getSetting(ItemTraderSettings.class);
		CompoundNBT updateInfo = settings.toggleOutputLimit(this.getPlayer());
		settings.sendToServer(updateInfo);
	}

}
