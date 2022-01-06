package io.github.lightman314.lightmanscurrency.client.gui.settings.core;

import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.client.gui.screen.TraderSettingsScreen;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.trader.settings.CoreTraderSettings;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
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
	public Component getTooltip() { return new TranslatableComponent("tooltip.lightmanscurrency.settings.ally"); }
	
	@Override
	public ImmutableList<String> requiredPermissions() { return ImmutableList.of(Permissions.ADD_REMOVE_ALLIES); }

	@Override
	public void initTab() {
		
		TraderSettingsScreen screen = this.getScreen();
		
		this.nameInput = screen.addRenderableTabWidget(new EditBox(screen.getFont(), screen.guiLeft() + 20, screen.guiTop() + 10, 160, 20, new TextComponent("")));
		this.nameInput.setMaxLength(16);
		
		this.buttonAddAlly = screen.addRenderableTabWidget(new Button(screen.guiLeft() + 20, screen.guiTop() + 35, 74, 20, new TranslatableComponent("gui.button.lightmanscurrency.allies.add"), this::AddAlly));
		this.buttonRemoveAlly = screen.addRenderableTabWidget(new Button(screen.guiLeft() + screen.xSize - 93, screen.guiTop() + 35, 74, 20, new TranslatableComponent("gui.button.lightmanscurrency.allies.remove"), this::RemoveAlly));
		
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
		this.getSetting(CoreTraderSettings.class).getAllies().forEach(ally -> list.add(new TextComponent(ally.lastKnownName())));
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
		CoreTraderSettings settings = this.getSetting(CoreTraderSettings.class);
		String allyName = this.nameInput.getValue();
		CompoundTag updateInfo = settings.addAlly(this.getPlayer(), allyName);
		settings.sendToServer(updateInfo);
		this.nameInput.setValue("");
	}
	
	private void RemoveAlly(Button button)
	{
		CoreTraderSettings settings = this.getSetting(CoreTraderSettings.class);
		String allyName = this.nameInput.getValue();
		CompoundTag updateInfo = settings.removeAlly(this.getPlayer(), allyName);
		settings.sendToServer(updateInfo);
		this.nameInput.setValue("");
	}
	
}
