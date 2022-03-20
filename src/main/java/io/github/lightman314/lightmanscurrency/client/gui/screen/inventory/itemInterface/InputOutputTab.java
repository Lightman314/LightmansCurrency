package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.itemInterface;

import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.blockentity.UniversalTraderInterfaceBlockEntity.InteractionType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.ItemInterfaceScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.DirectionalSettingsWidget;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.gui.widget.lockableslot.LockableSlotInterface;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.item.Items;

public class InputOutputTab extends ItemInterfaceTab {

	public InputOutputTab(ItemInterfaceScreen screen) {
		super(screen, false);
	}

	LockableSlotInterface lockInterface;
	
	DirectionalSettingsWidget inputSettings;
	DirectionalSettingsWidget outputSettings;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.CHEST); }

	@Override
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.interface.item.input"); }

	@Override
	public boolean valid(InteractionType interaction) { return true; }
	
	@Override
	public void init() {
		
		this.lockInterface = new LockableSlotInterface(this.screen, this.screen.getMenu(), this.screen::addRenderableTabWidget);
		
		this.inputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 12, () -> this.screen.getMenu().blockEntity.getItemHandler().getInputSides(), this::ToggleInputSide, this.screen::addRenderableTabWidget);
		this.outputSettings = new DirectionalSettingsWidget(this.screen.getGuiLeft() + 93, this.screen.getGuiTop() + 12, () -> this.screen.getMenu().blockEntity.getItemHandler().getOutputSides(), this::ToggleOutputSide, this.screen::addRenderableTabWidget);
		this.inputSettings.visible = this.screen.getMenu().blockEntity.allowAnyInput();
		this.inputSettings.tick();
		this.outputSettings.visible = this.screen.getMenu().blockEntity.allowAnyOutput();
		this.outputSettings.tick();
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		if(this.screen.getMenu().blockEntity.allowAnyInput())
			this.screen.getFont().draw(pose, new TranslatableComponent("gui.lightmanscurrency.settings.iteminput.side"), this.screen.getGuiLeft() + 10, this.screen.getGuiTop() + 5, 0x404040);
		if(this.screen.getMenu().blockEntity.allowAnyOutput())
			this.screen.getFont().draw(pose, new TranslatableComponent("gui.lightmanscurrency.settings.itemoutput.side"), this.screen.getGuiLeft() + 93, this.screen.getGuiTop() + 5, 0x404040);
		
		this.screen.getFont().draw(pose, new TranslatableComponent("gui.lightmanscurrency.lockableslot.title"), this.screen.getGuiLeft() + 7, this.screen.getGuiTop() + 70, 0x404040);
		
	}

	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY) {
		
		this.lockInterface.renderTooltips(this.screen, pose, mouseX, mouseY);
		
		this.inputSettings.renderTooltips(pose, mouseX, mouseY, this.screen);
		this.outputSettings.renderTooltips(pose, mouseX, mouseY, this.screen);
		
	}

	@Override
	public void tick() {
		
		this.inputSettings.visible = this.screen.getMenu().blockEntity.allowAnyInput();
		this.outputSettings.visible = this.screen.getMenu().blockEntity.allowAnyOutput();
		
		this.inputSettings.tick();
		this.outputSettings.tick();
		
	}

	@Override
	public void onClose() {
		
	}
	
	private void ToggleInputSide(Direction side) {
		this.screen.getMenu().blockEntity.getItemHandler().toggleInputSide(side);
	}
	
	private void ToggleOutputSide(Direction side) {
		this.screen.getMenu().blockEntity.getItemHandler().toggleOutputSide(side);
	}

}
