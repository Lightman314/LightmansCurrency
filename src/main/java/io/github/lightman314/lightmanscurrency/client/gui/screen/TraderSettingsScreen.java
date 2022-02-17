package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.List;
import java.util.function.Consumer;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.TabButton;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.settings.Settings;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class TraderSettingsScreen extends Screen{

	public static final ResourceLocation GUI_TEXTURE =  new ResourceLocation(LightmansCurrency.MODID, "textures/gui/tradersettings.png");
	
	public PlayerEntity getPlayer() { return this.minecraft.player; }
	private final Supplier<ITrader> trader;
	public ITrader getTrader() { return this.trader.get(); }
	private final Consumer<PlayerEntity> openStorage;
	
	public FontRenderer getFont() { return this.font; }
	
	public final int guiLeft() { return (this.width - this.xSize) / 2; }
	public final int guiTop() { return (this.height - this.ySize) / 2; }
	public final int xSize = 200;
	public final int ySize = 200;
	
	List<Settings> settings;
	public List<Settings> getSettings() { return this.settings; }
	
	List<Widget> tabWidgets = Lists.newArrayList();
	List<IGuiEventListener> tabListeners = Lists.newArrayList();
	
	List<TabButton> tabButtons = Lists.newArrayList();
	
	List<SettingsTab> tabs;
	int currentTabIndex = 0;
	public SettingsTab currentTab()
	{
		return this.tabs.get(MathUtil.clamp(currentTabIndex, 0, this.tabs.size() - 1));
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Settings> T getSetting(Class<T> type)
	{
		for(int i = 0; i < this.settings.size(); ++i)
		{
			if(this.settings.get(i).getClass() == type)
				return (T)this.settings.get(i);
		}
		return null;
	}
	
	public TraderSettingsScreen(Supplier<ITrader> trader, Consumer<PlayerEntity> openStorage)
	{
		super(new StringTextComponent(""));
		
		this.trader = trader;
		this.openStorage = openStorage;
		//Initialize the settings list
		this.settings = Lists.newArrayList(this.getTrader().getCoreSettings());
		this.settings.addAll(this.getTrader().getAdditionalSettings());
		
		//Collect the Settings Tabs
		this.tabs = Lists.newArrayList();
		//Get normal tabs
		this.settings.forEach(setting -> this.tabs.addAll(setting.getSettingsTabs()));
		//Get back end tabs so that they appear last
		this.settings.forEach(setting -> this.tabs.addAll(setting.getBackEndSettingsTabs()));
		
		this.tabs.forEach(tab -> tab.setScreen(this));
		
	}
	
	@Override
	public void init()
	{
		//Initialize the back button
		this.addButton(new IconButton(this.guiLeft(), this.guiTop() - 20, this::OpenStorage, IconAndButtonUtil.ICON_BACK));
		//Initialize the tab buttons
		for(int i = 0; i < this.tabs.size(); ++i)
		{
			TabButton button = this.addButton(new TabButton(this::clickedOnTab, this.font, this.tabs.get(i)));
			button.active = i != this.currentTabIndex;
			button.visible = this.hasPermissions(this.tabs.get(i).requiredPermissions());
			this.tabButtons.add(button);
		}
		this.positionTabButtons();
		
		//Initialize the starting tab
		this.currentTab().initTab();
		
	}
	
	private int getTabPosX(int index)
	{
		if(index < 7)
			return this.guiLeft() + 20 + 25 * index;
		if(index < 15)
			return this.guiLeft() + this.xSize;
		if(index < 23)
			return this.guiLeft() + this.xSize - 25 * (index - 15);
		return this.guiLeft() - 25;
	}
	
	private int getTabPosY(int index)
	{
		if(index < 7)
			return this.guiTop() - 25;
		if(index < 15)
			return this.guiTop() + 25 * (index - 10);
		if(index < 23)
			return this.guiTop() + this.ySize;
		return this.guiTop() + this.ySize - 25 * (index - 23);
	}
	
	private int getTabRotation(int index)
	{
		if(index < 7)
			return 0;
		if(index < 15)
			return 1;
		if(index < 23)
			return 2;
		return 3;
	}
	
	private void positionTabButtons()
	{
		int index = 0;
		for(int i = 0; i < this.tabButtons.size(); ++i)
		{
			TabButton thisButton = this.tabButtons.get(i);
			if(thisButton.visible)
			{
				thisButton.reposition(this.getTabPosX(index), this.getTabPosY(index), this.getTabRotation(index));
				index++;
			}
		}
	}
	
	@Override
	public void render(MatrixStack matrix, int mouseX, int mouseY, float partialTicks)
	{
		
		this.renderBackground(matrix);
		//Render the background
		Minecraft.getInstance().getTextureManager().bindTexture(GUI_TEXTURE);
		this.setColor(this.currentTab().getColor());
		this.blit(matrix, this.guiLeft(), this.guiTop(), 0, 0, this.xSize, this.ySize);
		//Render the tab buttons
		super.render(matrix, mouseX, mouseY, partialTicks);
		//Pre-render the tab
		this.currentTab().preRender(matrix, mouseX, mouseY, partialTicks);
		//Render the renderables
		this.tabWidgets.forEach(widget -> widget.render(matrix, mouseX, mouseY, partialTicks));
		//Post-render the tab
		this.currentTab().postRender(matrix, mouseX, mouseY, partialTicks);
		
		//Render the tab button tooltips
		for(int i = 0; i < this.tabButtons.size(); ++i)
		{
			if(this.tabButtons.get(i).isMouseOver(mouseX, mouseY))
				this.renderTooltip(matrix, this.tabButtons.get(i).tab.getTooltip(), mouseX, mouseY);
		}
		
	}
	
	@SuppressWarnings("deprecation")
	public void setColor(int color)
	{
		float r = (float)(color >> 16 & 255) / 255f;
        float g = (float)(color >> 8 & 255) / 255f;
        float b = (float)(color & 255) / 255f;
        RenderSystem.color3f(r, g, b);
	}
	
	@Override
	public void tick()
	{
		if(this.getTrader() == null)
		{
			this.minecraft.displayGuiScreen(null);
			return;
		}
			
		//Update the tabs visibility
		boolean updateTabs = false;
		for(int i = 0; i < this.tabs.size(); ++i)
		{
			boolean visible = hasPermissions(this.tabs.get(i).requiredPermissions());
			if(visible != this.tabButtons.get(i).visible)
			{
				updateTabs = true;
				this.tabButtons.get(i).visible = visible;
			}
		}
		if(updateTabs)
			this.positionTabButtons();
		
		//Tick the current tab
		this.currentTab().tick();
	}
	
	public boolean hasPermission(String permission)
	{
		if(this.trader.get() != null)
			return this.trader.get().getCoreSettings().hasPermission(this.getPlayer(), permission);
		return false;
	}
	
	public int getPermissionLevel(String permission)
	{
		if(this.trader.get() != null)
			return this.trader.get().getCoreSettings().getPermissionLevel(this.getPlayer(), permission);
		return 0;
	}
	
	public boolean hasPermissions(List<String> permissions)
	{
		for(int i = 0; i < permissions.size(); ++i)
		{
			if(!this.hasPermission(permissions.get(i)))
				return false;
		}
		return true;
	}
	
	public <T extends Widget> T addRenderableTabWidget(T widget)
	{
		this.tabWidgets.add(widget);
		return widget;
	}
	
	public void removeRenderableTabWidget(Widget widget)
	{
		if(this.tabWidgets.contains(widget))
			this.tabWidgets.remove(widget);
	}
	
	public <T extends IGuiEventListener> T addTabListener(T listener)
	{
		this.tabListeners.add(listener);
		return listener;
	}
	
	public void removeTabListener(IGuiEventListener listener)
	{
		if(this.tabListeners.contains(listener))
			this.tabListeners.remove(listener);
	}
	
	private void clickedOnTab(Button tab)
	{
		int tabIndex = this.tabButtons.indexOf(tab);
		if(tabIndex < 0)
			return;
		if(tabIndex != this.currentTabIndex)
		{
			//Close the old tab
			this.currentTab().closeTab();
			this.tabButtons.get(this.currentTabIndex).active = true;
			this.currentTabIndex = tabIndex;
			this.tabButtons.get(this.currentTabIndex).active = false;
			
			//Clear the previous tabs widgets
			this.tabWidgets.clear();
			this.tabListeners.clear();
			
			//Initialize the new tab
			this.currentTab().initTab();
		}
	}
	
	private void OpenStorage(Button button)
	{
		this.openStorage.accept(this.getPlayer());
	}
	
	@Override
	public List<? extends IGuiEventListener> getEventListeners()
	{
		List<? extends IGuiEventListener> coreListeners = super.getEventListeners();
		List<IGuiEventListener> listeners = Lists.newArrayList();
		for(int i = 0; i < coreListeners.size(); ++i)
			listeners.add(coreListeners.get(i));
		listeners.addAll(this.tabWidgets);
		listeners.addAll(this.tabListeners);
		return listeners;
	}
	
	@Override
	public boolean isPauseScreen() { return false; }
	
}
