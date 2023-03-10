package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;

import javax.annotation.Nonnull;

public class AllyTab extends SettingsTab {

	public static final AllyTab INSTANCE = new AllyTab();
	
	private AllyTab() { }
	
	@Override
	public int getColor() {
		return 0xFFFFFFFF;
	}
	
	TextFieldWidget nameInput;
	Button buttonAddAlly;
	Button buttonRemoveAlly;
	
	ScrollTextDisplay display;
	
	@Nonnull
    @Override
	public IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

	@Override
	public ITextComponent getTooltip() { return EasyText.translatable("tooltip.lightmanscurrency.settings.ally"); }
	
	@Override
	public boolean canOpen() { return this.hasPermissions(Permissions.ADD_REMOVE_ALLIES); }

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.nameInput = screen.addRenderableTabWidget(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 20, screen.guiTop() + 10, 160, 20, EasyText.empty()));
		this.nameInput.setMaxLength(16);
		
		this.buttonAddAlly = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 35, 74, 20, EasyText.translatable("gui.button.lightmanscurrency.allies.add"), this::AddAlly));
		this.buttonRemoveAlly = screen.addRenderableTabWidget(new Button(screen.guiLeft() + screen.xSize - 93, screen.guiTop() + 35, 74, 20, EasyText.translatable("gui.button.lightmanscurrency.allies.remove"), this::RemoveAlly));
		
		this.display = screen.addRenderableTabWidget(new ScrollTextDisplay(screen.guiLeft() + 5, screen.guiTop() + 60, 190, 135, screen.getFont(), this::getAllyList));
		this.display.setColumnCount(2);
		
	}

	@Override
	public void preRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	@Override
	public void postRender(MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	private List<ITextComponent> getAllyList()
	{
		List<ITextComponent> list = new ArrayList<>();
		this.getScreen().getTrader().getAllies().forEach(ally -> list.add(ally.getNameComponent(true)));
		return list;
	}
	
	@Override
	public void tick()
	{
		this.nameInput.tick();
		this.buttonAddAlly.active = this.buttonRemoveAlly.active = !this.nameInput.getValue().isEmpty();
	}

	@Override
	public void closeTab() { }

	private void AddAlly(Button button)
	{
		String allyName = this.nameInput.getValue();
		CompoundNBT message = new CompoundNBT();
		message.putString("AddAlly", allyName);
		this.sendNetworkMessage(message);
		this.nameInput.setValue("");
	}
	
	private void RemoveAlly(Button button)
	{
		String allyName = this.nameInput.getValue();
		CompoundNBT message = new CompoundNBT();
		message.putString("RemoveAlly", allyName);
		this.sendNetworkMessage(message);
		this.nameInput.setValue("");
	}
	
}