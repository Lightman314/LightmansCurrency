package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.TraderClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.trader.common.TraderInteractionTab;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.menus.TraderMenu;
import io.github.lightman314.lightmanscurrency.money.MoneyUtil;
import io.github.lightman314.lightmanscurrency.network.LightmansCurrencyPacketHandler;
import io.github.lightman314.lightmanscurrency.network.message.trader.MessageCollectCoins;
import io.github.lightman314.lightmanscurrency.trader.permissions.Permissions;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

@IPNIgnore
public class TraderScreen extends AbstractContainerScreen<TraderMenu>{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader.png");
	
	public static final int WIDTH = 206;
	public static final int HEIGHT = 236;
	
	Button buttonOpenStorage;
	Button buttonCollectCoins;
	
	Button buttonOpenTerminal;
	
	List<AbstractWidget> tabRenderables = new ArrayList<>();
	List<GuiEventListener> tabListeners = new ArrayList<>();
	
	TraderClientTab currentTab = new TraderInteractionTab(this);
	public void setTab(@Nonnull TraderClientTab tab) {
		//Close the old tab
		this.currentTab.onClose();
		this.tabRenderables.clear();
		this.tabListeners.clear();
		//Set the new tab
		this.currentTab = tab;
		this.currentTab.onOpen();
	}
	public void closeTab() { this.setTab(new TraderInteractionTab(this)); }
	
	public TraderScreen(TraderMenu menu, Inventory inventory, Component title) {
		super(menu, inventory, title);
		this.imageWidth = WIDTH;
		this.imageHeight = HEIGHT;
	}
	
	@Override
	public void init() {
		
		super.init();
		
		this.tabRenderables.clear();
		this.tabListeners.clear();
		
		this.buttonOpenStorage = this.addRenderableWidget(IconAndButtonUtil.storageButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 118, this::OpenStorage, () -> this.menu.isSingleTrader() && this.menu.getSingleTrader().hasPermission(this.menu.player, Permissions.OPEN_STORAGE)));
		this.buttonCollectCoins = this.addRenderableWidget(IconAndButtonUtil.collectCoinButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + 138, this::CollectCoins, this.menu.player, this.menu::getSingleTrader));
		this.buttonOpenTerminal = this.addRenderableWidget(IconAndButtonUtil.backToTerminalButton(this.leftPos + TraderMenu.SLOT_OFFSET - 20, this.topPos + this.imageHeight - 20, this::OpenTerminal, this.menu::isUniversalTrader));
		
		//Initialize the current tab
		this.currentTab.onOpen();
		
		this.containerTick();
		
	}

	@Override
	protected void renderBg(PoseStack pose, float partialTicks, int mouseX, int mouseY) {
		
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		
		//Main BG
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		//Coin Slots
		for(Slot slot : this.menu.getCoinSlots())
		{
			this.blit(pose, this.leftPos + slot.x - 1, this.topPos + slot.y - 1, this.imageWidth, 0, 18, 18);
		}
		
		//Interaction Slot BG
		if(this.menu.getInteractionSlot().isActive())
			this.blit(pose, this.leftPos + this.menu.getInteractionSlot().x - 1, this.topPos + this.menu.getInteractionSlot().y - 1, this.imageWidth, 0, 18, 18);
		
		try {
			this.currentTab.renderBG(pose, mouseX, mouseY, partialTicks);
			this.tabRenderables.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
		} catch(Throwable t) { LightmansCurrency.LogError("Error rendering trader tab " + this.currentTab.getClass().getName(), t); }
		
	}
	
	@Override
	protected void renderLabels(PoseStack pose, int mouseX, int mouseY) {
		
		this.font.draw(pose, this.playerInventoryTitle, TraderMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);
		
		//Moved to underneath the coin slots
		String valueText = MoneyUtil.getStringOfValue(this.menu.getContext(null).getAvailableFunds());
		font.draw(pose, valueText, TraderMenu.SLOT_OFFSET + 170 - this.font.width(valueText), this.imageHeight - 94, 0x404040);
		
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
		try {
			this.currentTab.renderTooltips(pose, mouseX, mouseY);
		} catch (Throwable t) { LightmansCurrency.LogError("Error rendering trader tab tooltips " + this.currentTab.getClass().getName(), t); }
		
		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, this.renderables);
		
	}
	
	@Override
	public void containerTick() {
		this.currentTab.tick();
	}
	
	private void OpenStorage(Button button) {
		if(this.menu.isSingleTrader())
			this.menu.getSingleTrader().sendOpenStorageMessage();
	}
	
	private void CollectCoins(Button button) {
		if(this.menu.isSingleTrader())
			LightmansCurrencyPacketHandler.instance.sendToServer(new MessageCollectCoins());
	}
	
	private void OpenTerminal(Button button) {
		if(this.menu.isUniversalTrader())
		{
			this.menu.player.closeContainer();
			LightmansCurrency.PROXY.openTerminalScreen();
		}
	}
	
	@Override
	public boolean keyPressed(int p_97765_, int p_97766_, int p_97767_) {
	      InputConstants.Key mouseKey = InputConstants.getKey(p_97765_, p_97766_);
	      //Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
	      if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.currentTab.blockInventoryClosing()) {
	    	  return true;
	      }
	      return super.keyPressed(p_97765_, p_97766_, p_97767_);
	}
	
	public <T extends AbstractWidget> T addRenderableTabWidget(T widget) {
		this.tabRenderables.add(widget);
		return widget;
	}
	
	public <T extends Widget> void removeRenderableTabWidget(T widget) {
		this.tabRenderables.remove(widget);
	}
	
	public <T extends GuiEventListener> T addTabListener(T listener) {
		this.tabListeners.add(listener);
		return listener;
	}
	
	public <T extends GuiEventListener> void removeTabListener(T listener) {
		this.tabListeners.remove(listener);
	}
	
	@Override
	public List<? extends GuiEventListener> children()
	{
		List<? extends GuiEventListener> coreListeners = super.children();
		List<GuiEventListener> listeners = Lists.newArrayList();
		for(int i = 0; i < coreListeners.size(); ++i)
			listeners.add(coreListeners.get(i));
		listeners.addAll(this.tabRenderables);
		listeners.addAll(this.tabListeners);
		return listeners;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		try {
			if(this.currentTab.mouseClicked(mouseX, mouseY, button))
				return true;
		} catch(Throwable t) {}
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		try {
			if(this.currentTab.mouseReleased(mouseX, mouseY, button))
				return true;
		} catch(Throwable t) {}
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
}
