package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Items;

public class AllyTab extends SettingsTab {

	public static final AllyTab INSTANCE = new AllyTab();
	
	private AllyTab() { }
	
	@Override
	public int getColor() {
		return 0xFFFFFFFF;
	}
	
	EditBox nameInput;
	Button buttonAddAlly;
	Button buttonRemoveAlly;
	
	ScrollTextDisplay display;
	
	int scroll = 0;
	
	@Override
	public IconData getIcon() { return IconData.of(Items.PLAYER_HEAD); }

	@Override
	public MutableComponent getTooltip() { return Component.translatable("tooltip.lightmanscurrency.settings.ally"); }
	
	@Override
	public boolean canOpen() { return this.hasPermissions(Permissions.ADD_REMOVE_ALLIES); }

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.nameInput = screen.addRenderableTabWidget(new EditBox(screen.getFont(), screen.guiLeft() + 20, screen.guiTop() + 10, 160, 20, Component.empty()));
		this.nameInput.setMaxLength(16);
		
		this.buttonAddAlly = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 35, 74, 20, Component.translatable("gui.button.lightmanscurrency.allies.add"), this::AddAlly));
		this.buttonRemoveAlly = screen.addRenderableTabWidget(new Button(screen.guiLeft() + screen.xSize - 93, screen.guiTop() + 35, 74, 20, Component.translatable("gui.button.lightmanscurrency.allies.remove"), this::RemoveAlly));
		
		this.display = screen.addRenderableTabWidget(new ScrollTextDisplay(screen.guiLeft() + 5, screen.guiTop() + 60, 190, 135, screen.getFont(), this::getAllyList));
		this.display.setColumnCount(2);
		
	}

	@Override
	public void preRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}
	
	@Override
	public void postRender(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
	}

	private List<Component> getAllyList()
	{
		List<Component> list = Lists.newArrayList();
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
		CompoundTag message = new CompoundTag();
		message.putString("AddAlly", allyName);
		this.sendNetworkMessage(message);
		this.nameInput.setValue("");
	}
	
	private void RemoveAlly(Button button)
	{
		String allyName = this.nameInput.getValue();
		CompoundTag message = new CompoundTag();
		message.putString("RemoveAlly", allyName);
		this.sendNetworkMessage(message);
		this.nameInput.setValue("");
	}
	
}
