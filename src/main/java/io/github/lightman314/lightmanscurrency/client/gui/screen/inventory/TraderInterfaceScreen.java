package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import com.mojang.blaze3d.matrix.MatrixStack;
import io.github.lightman314.lightmanscurrency.client.util.RenderUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.util.Constants;
import org.anti_ad.mc.ipn.api.IPNIgnore;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TraderInterfaceBlockEntity.ActiveMode;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.menus.TraderInterfaceMenu;
import io.github.lightman314.lightmanscurrency.common.menus.TraderStorageMenu.IClientMessage;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceClientTab;
import io.github.lightman314.lightmanscurrency.common.menus.traderinterface.TraderInterfaceTab;

import javax.annotation.Nonnull;

@IPNIgnore
public class TraderInterfaceScreen extends ContainerScreen<TraderInterfaceMenu> implements IClientMessage {

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/container/trader_interface.png");
	
	public static final int WIDTH = 206;
	public static final int HEIGHT = 236;
	
	Map<Integer,TraderInterfaceClientTab<?>> availableTabs = new HashMap<>();
	public TraderInterfaceClientTab<?> currentTab() { return this.availableTabs.get(this.menu.getCurrentTabIndex()); }
	
	Map<Integer,TabButton> tabButtons = new HashMap<>();
	
	List<Widget> tabRenderables = new ArrayList<>();
	List<IGuiEventListener> tabListeners = new ArrayList<>();
	
	IconButton modeToggle;
	
	IconButton onlineModeToggle;
	
	public TraderInterfaceScreen(TraderInterfaceMenu menu, PlayerInventory inventory, ITextComponent title) {
		super(menu, inventory, title);
		this.menu.getAllTabs().forEach((key,tab) -> this.availableTabs.put(key, tab.createClientTab(this)));
		this.imageWidth = WIDTH;
		this.imageHeight = HEIGHT;
	}
	
	@Override
	public void init() {
		
		super.init();
		
		this.tabRenderables.clear();
		this.tabListeners.clear();
		
		//Create the tab buttons
		this.tabButtons.clear();
		this.availableTabs.forEach((key,tab) ->{
			TabButton newButton = this.addButton(new TabButton(button -> this.changeTab(key), this.font, tab));
			if(key == this.menu.getCurrentTabIndex())
				newButton.active = false;
			this.tabButtons.put(key, newButton);
		});
		
		this.modeToggle = this.addButton(new IconButton(this.leftPos + this.imageWidth, this.topPos, this::ToggleMode, () -> IconAndButtonUtil.GetIcon(this.menu.getBE().getMode()), new IconAndButtonUtil.SuppliedTooltip(() -> this.getMode().getDisplayText())));
		
		this.onlineModeToggle = this.addButton(new IconButton(this.leftPos + this.imageWidth, this.topPos + 20, this::ToggleOnlineMode, () -> this.menu.getBE().isOnlineMode() ? IconAndButtonUtil.ICON_ONLINEMODE_TRUE : IconAndButtonUtil.ICON_ONLINEMODE_FALSE, new IconAndButtonUtil.SuppliedTooltip(() -> EasyText.translatable("gui.lightmanscurrency.interface.onlinemode." + this.menu.getBE().isOnlineMode()))));
		
		//Initialize the current tab
		this.currentTab().onOpen();
		
		this.tick();
		
	}

	@Override
	protected void renderBg(@Nonnull MatrixStack pose, float partialTicks, int mouseX, int mouseY) {

		RenderUtil.bindTexture(GUI_TEXTURE);
		RenderUtil.color4f(1f, 1f, 1f, 1f);
		
		//Main BG
		this.blit(pose, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
		
		//Current tab
		try {
			this.currentTab().renderBG(pose, mouseX, mouseY, partialTicks);
			this.tabRenderables.forEach(widget -> widget.render(pose, mouseX, mouseY, partialTicks));
		} catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab " + this.currentTab().getClass().getName(), e); }
		
		
	}
	
	@Override
	protected void renderLabels(@Nonnull MatrixStack pose, int mouseX, int mouseY) {
		
		this.font.draw(pose, this.inventory.getName(), TraderInterfaceMenu.SLOT_OFFSET + 8, this.imageHeight - 94, 0x404040);
		
	}
	
	@Override
	public void render(@Nonnull MatrixStack pose, int mouseX, int mouseY, float partialTicks) {
		
		this.renderBackground(pose);
		super.render(pose, mouseX, mouseY, partialTicks);
		this.renderTooltip(pose, mouseX, mouseY);
		
		try {
			this.currentTab().renderTooltips(pose, mouseX, mouseY);
		} catch(Exception e) { LightmansCurrency.LogError("Error rendering trader storage tab tooltips " + this.currentTab().getClass().getName(), e); }

		IconAndButtonUtil.renderButtonTooltips(pose, mouseX, mouseY, this.buttons);
		
		this.tabButtons.forEach((key, button) -> {
			if(button.isMouseOver(mouseX, mouseY))
				this.renderTooltip(pose, button.tab.getTooltip(), mouseX, mouseY);
		});
		
	}
	
	@Override
	public void tick()
	{
		super.tick();

		if(!this.currentTab().commonTab.canOpen(this.menu.player))
			this.changeTab(TraderInterfaceTab.TAB_INFO);
		
		this.updateTabs();
		
		this.currentTab().tick();
		
	}
	
	private ActiveMode getMode() {
		if(this.menu.getBE() != null)
			return this.menu.getBE().getMode();
		return ActiveMode.DISABLED;
	}
	
	private void ToggleMode(Button button) { this.menu.changeMode(this.getMode().getNext()); }
	
	private void ToggleOnlineMode(Button button) { this.menu.setOnlineMode(!this.menu.getBE().isOnlineMode()); }
	
	private void updateTabs() {
		//Position the tab buttons
		int yPos = this.topPos - TabButton.SIZE;
		AtomicInteger index = new AtomicInteger(0);
		this.tabButtons.forEach((key,button) -> {
			TraderInterfaceClientTab<?> tab = this.availableTabs.get(key);
			button.visible = tab.tabButtonVisible();
			if(button.visible)
			{
				int xPos = this.leftPos + TabButton.SIZE * index.get();
				button.reposition(xPos, yPos, 0);
				index.set(index.get() + 1);
			}
		});
	}
	
	@Override
	public boolean keyPressed(int key, int scanCode, int mods) {
		if(this.currentTab().keyPressed(key, scanCode, mods))
			return true;
		InputMappings.Input mouseKey = InputMappings.getKey(key, scanCode);
		//Manually block closing by inventory key, to allow usage of all letters while typing player names, etc.
		if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey) && this.currentTab().blockInventoryClosing()) {
			return true;
		}
		return super.keyPressed(key, scanCode, mods);
	}
	
	private TabButton getTabButton(int key) {
		if(this.tabButtons.containsKey(key))
			return this.tabButtons.get(key);
		return null;
	}
	
	public void changeTab(int newTab) { this.changeTab(newTab, true, null); }
	
	public void changeTab(int newTab, boolean sendMessage, CompoundNBT selfMessage) {
		
		if(newTab == this.menu.getCurrentTabIndex())
			return;
		
		//Close the old tab
		int oldTab = this.menu.getCurrentTabIndex();
		this.currentTab().onClose();
		
		//Make the old tabs button active again
		TabButton button = this.getTabButton(this.menu.getCurrentTabIndex());
		if(button != null)
			button.active = true;
		
		//Clear the renderables & listeners
		this.tabRenderables.clear();
		this.tabListeners.clear();
		
		//Change the tab officially
		this.menu.changeTab(newTab);
		
		//Make the tab button for the current tab inactive
		button = this.getTabButton(this.menu.getCurrentTabIndex());
		if(button != null)
			button.active = false;
		
		//Open the new tab
		if(selfMessage != null)
			this.currentTab().receiveSelfMessage(selfMessage);
		this.currentTab().onOpen();
		
		//Inform the server that the tab has been changed
		if(oldTab != this.menu.getCurrentTabIndex() && sendMessage)
			this.menu.sendMessage(this.menu.createTabChangeMessage(newTab, null));
		
	}
	
	@Override
	public void selfMessage(CompoundNBT message) {
		//LightmansCurrency.LogInfo("Received self-message:\n" + message.getAsString());
		if(message.contains("ChangeTab", Constants.NBT.TAG_INT))
			this.changeTab(message.getInt("ChangeTab"), false, message);
		else
			this.currentTab().receiveSelfMessage(message);
	}
	
	public <T extends Widget> T addRenderableTabWidget(T widget) {
		this.tabRenderables.add(widget);
		return widget;
	}
	
	public <T extends Widget> void removeRenderableTabWidget(T widget) {
		this.tabRenderables.remove(widget);
	}
	
	public <T extends IGuiEventListener> T addTabListener(T listener) {
		this.tabListeners.add(listener);
		return listener;
	}
	
	public <T extends IGuiEventListener> void removeTabListener(T listener) {
		this.tabListeners.remove(listener);
	}
	
	@Nonnull
	@Override
	public List<? extends IGuiEventListener> children()
	{
		List<? extends IGuiEventListener> coreListeners = super.children();
		List<IGuiEventListener> listeners = Lists.newArrayList();
		listeners.addAll(coreListeners);
		listeners.addAll(this.tabRenderables);
		listeners.addAll(this.tabListeners);
		return listeners;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		if(this.currentTab().mouseClicked(mouseX, mouseY, button))
			return true;
		return super.mouseClicked(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		if(this.currentTab().mouseReleased(mouseX, mouseY, button))
			return true;
		return super.mouseReleased(mouseX, mouseY, button);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaMouseX, double deltaMouseY) {
		if(this.currentTab().mouseDragged(mouseX, mouseY, button, deltaMouseX, deltaMouseY))
			return true;
		return super.mouseDragged(mouseX, mouseY, button, deltaMouseX, deltaMouseY);
	}
	
	@Override
	public boolean charTyped(char c, int code) {
		if(this.currentTab().charTyped(c, code))
			return true;
		return super.charTyped(c, code);
	}
	
}
