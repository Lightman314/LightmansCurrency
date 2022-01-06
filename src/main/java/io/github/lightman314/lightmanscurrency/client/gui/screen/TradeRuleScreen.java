package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.rules.*;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;

public class TradeRuleScreen extends Screen{
	
	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/traderules.png");
	private static final List<Supplier<TradeRule>> REGISTERED_RULES = new ArrayList<>();
	
	public static void RegisterTradeRule(Supplier<TradeRule> rule)
	{
		REGISTERED_RULES.forEach(registeredRule ->
		{
			if(registeredRule.get().type == rule.get().type)
				return;
		});
		REGISTERED_RULES.add(rule);
	}
	
	public final int xSize = 176;
	public final int ySize = 176;
	
	public final int guiLeft() { return (this.width - this.xSize) / 2; }
	public final int guiTop() { return (this.height - this.ySize) / 2; }
	
	final ITradeRuleScreenHandler handler;
	
	int openTab = -1;
	
	Button managerTab;
	
	List<Button> tabButtons = new ArrayList<>();
	
	//Manager tab values
	List<Button> removeRuleButtons = new ArrayList<>();
	List<Button> addRuleButtons = new ArrayList<>();
	
	
	
	List<TradeRule> activeRules() { return this.handler.ruleHandler().getRules(); }
	TradeRule currentRule()
	{
		if(this.openTab >= 0 && this.openTab < activeRules().size())
			return activeRules().get(this.openTab);
		return null;
	}
	TradeRule.GUIHandler currentGUIHandler = null;
	
	List<TradeRule> addableRules()
	{
		List<TradeRule> addableRules = new ArrayList<>();
		REGISTERED_RULES.forEach(rule -> addableRules.add(rule.get()));
		
		this.activeRules().forEach(rule -> {
			for(int i = 0; i < addableRules.size(); i++)
			{
				if(addableRules.get(i).type == rule.type)
					addableRules.remove(i);
			}
		});
		
		return addableRules;
	}
	
	public TradeRuleScreen(ITradeRuleScreenHandler handler)
	{
		super(new TextComponent(""));
		this.handler = handler;
	}
	
	@Override
	public void init()
	{
		
		//Back button
		this.addRenderableWidget(new IconButton(guiLeft() + this.xSize, guiTop(), this::PressBackButton, this.font, IconData.of(GUI_TEXTURE, this.xSize, 0)));
		this.managerTab = this.addRenderableWidget(new IconButton(guiLeft(), guiTop() - 20, this::PressTabButton, this.font, IconData.of(TradeRule.ICON_TEXTURE, 0, 0)));
		
		this.refreshTabs();
		
		this.initManagerTab();
		
	}
	
	private void initManagerTab()
	{
		int y = 0;
		this.removeRuleButtons.clear();
		for(int i = 0; i < this.activeRules().size(); i++)
		{
			this.removeRuleButtons.add(this.addRenderableWidget(new IconButton(this.guiLeft() + 10, this.guiTop() + 10 + 20 * y, this::PressRemoveRuleButton, this.font, IconData.of(GUI_TEXTURE, this.xSize + 32, 0))));
			y++;
		}
		this.addRuleButtons.clear();
		for(int i = 0; i < this.addableRules().size(); i++)
		{
			this.addRuleButtons.add(this.addRenderableWidget(new IconButton(this.guiLeft() + 10, this.guiTop() + 10 + 20 * y, this::PressAddRuleButton, this.font, IconData.of(GUI_TEXTURE, this.xSize + 16, 0))));
			y++;
		}
	}
	
	private void closeManagerTab()
	{
		this.addRuleButtons.forEach(button -> this.removeWidget(button));
		this.addRuleButtons.clear();
		this.removeRuleButtons.forEach(button -> this.removeWidget(button));
		this.removeRuleButtons.clear();
	}
	
	@Override
	public void render(PoseStack poseStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(poseStack);
		
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		
		this.blit(poseStack, guiLeft(), guiTop(), 0, 0, this.xSize, this.ySize);
		
		//Render the current rule
		if(this.currentGUIHandler!= null)
			this.currentGUIHandler.renderTab(poseStack, mouseX, mouseY, partialTicks);
		else
		{
			
			//If current rule is null, but open tab is not -1, reset to the manager tab
			if(this.openTab >= 0)
				this.openTab = -1;
			
			//Render the manager tab
			int y = 0;
			for(int i = 0; i < this.activeRules().size(); i++)
			{
				this.font.draw(poseStack, this.activeRules().get(i).getName().getString(), guiLeft() + 34, guiTop() + 16 + y * 20, 0xFFFFFF);
				y++;
			}
			for(int i = 0; i < this.addableRules().size(); i++)
			{
				this.font.draw(poseStack, this.addableRules().get(i).getName().getString(), guiLeft() + 34, guiTop() + 16 + y * 20, 0xFFFFFF);
				y++;
			}
			
		}
		
		//Render the buttons, etc
		super.render(poseStack, mouseX, mouseY, partialTicks);
		
		if(this.managerTab.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(poseStack, new TranslatableComponent("gui.button.lightmanscurrency.mananger"), mouseX, mouseY);
		}
		else
		{
			boolean hoverButton = true;
			for(int i = 0; i < this.tabButtons.size() && i < this.activeRules().size() && hoverButton; i++)
			{
				Button thisTab = this.tabButtons.get(i);
				if(thisTab.isMouseOver(mouseX, mouseY))
				{
					this.renderTooltip(poseStack, this.activeRules().get(i).getName(), mouseX, mouseY);
					hoverButton = false;
				}
			}
			for(int i = 0; i < this.removeRuleButtons.size() && i < this.activeRules().size() && hoverButton; i++)
			{
				if(this.removeRuleButtons.get(i).isMouseOver(mouseX, mouseY))
				{
					this.renderTooltip(poseStack, new TranslatableComponent("gui.button.lightmanscurrency.removerule", this.activeRules().get(i).getName()), mouseX, mouseY);
					hoverButton = false;
				}
			}
			for(int i = 0; i < this.addRuleButtons.size() && i < this.addableRules().size() && hoverButton; i++)
			{
				if(this.addRuleButtons.get(i).isMouseOver(mouseX, mouseY))
				{
					this.renderTooltip(poseStack, new TranslatableComponent("gui.button.lightmanscurrency.addrule", this.addableRules().get(i).getName()), mouseX, mouseY);
					hoverButton = false;
				}
			}
		}
	}
	
	public void tick()
	{
		if(currentGUIHandler != null)
		{
			this.currentGUIHandler.onScreenTick();
		}
		else
		{
			//Manager screen tick
		}
	}
	
	void PressBackButton(Button button)
	{
		this.handler.reopenLastScreen();
	}
	
	void PressTabButton(Button button)
	{
		if(tabButtons.contains(button))
		{
			
			if(this.openTab == tabButtons.indexOf(button))
				return;
			
			if(this.currentGUIHandler != null)
			{
				this.currentGUIHandler.onTabClose();
				this.currentGUIHandler = null;
			}
			else
				this.closeManagerTab();
			
			this.openTab = tabButtons.indexOf(button);
			
			if(this.currentRule() != null)
			{
				this.currentGUIHandler = this.currentRule().createHandler(this, () -> this.currentRule());
				this.currentGUIHandler.initTab();
			}
		}
		else if(button == this.managerTab)
		{
			if(this.openTab < 0)
				return;
			
			if(this.currentGUIHandler != null)
			{
				this.currentGUIHandler.onTabClose();
				this.currentGUIHandler = null;
			}
				
			this.openTab = -1;
			this.initManagerTab();
		}
			
	}
	
	void PressAddRuleButton(Button button)
	{
		if(this.addRuleButtons.contains(button))
		{
			int addIndex = this.addRuleButtons.indexOf(button);
			if(addIndex >= 0 && addIndex < this.addableRules().size())
			{
				TradeRule newRule = this.addableRules().get(addIndex);
				this.handler.ruleHandler().addRule(newRule);
				LightmansCurrency.LogInfo("Adding rule type " + newRule.getName().getString());
				this.markRulesDirty();
				this.closeManagerTab();
				this.refreshTabs();
				this.initManagerTab();
			}
		}
	}
	
	void PressRemoveRuleButton(Button button)
	{
		if(this.removeRuleButtons.contains(button))
		{
			int removeIndex = this.removeRuleButtons.indexOf(button);
			if(removeIndex >= 0 && removeIndex < this.activeRules().size())
			{
				TradeRule removedRule = this.activeRules().get(removeIndex);
				this.handler.ruleHandler().removeRule(removedRule);
				LightmansCurrency.LogInfo("Removing rule type " + removedRule.getName().getString());
				this.markRulesDirty();
				this.closeManagerTab();
				this.refreshTabs();
				this.initManagerTab();
			}
		}
	}
	
	
	public void refreshTabs()
	{
		
		this.tabButtons.forEach(button -> this.removeWidget(button) );
		this.tabButtons.clear();
		
		List<TradeRule> activeRules = this.activeRules();
		for(int i = 0; i < activeRules.size(); i++)
		{
			TradeRule thisRule = activeRules.get(i);
			this.tabButtons.add(this.addRenderableWidget(new IconButton(guiLeft() + 20 + 20 * i, guiTop() - 20, this::PressTabButton, this.font, thisRule.getButtonIcon())));
				
		}
		
	}
	
	//Public functions for easy traderule renderer access
	public Font getFont() { return this.font; }
	
	public <T extends GuiEventListener & Widget & NarratableEntry> T addCustomRenderable(T widget)
	{
		if(widget != null)
			this.addRenderableWidget(widget);
		return widget;
	}
	
	public <T extends GuiEventListener & NarratableEntry> T addCustomWidget(T widget)
	{
		if(widget != null)
			this.addWidget(widget);
		return widget;
	}
	
	public <T extends GuiEventListener> void removeCustomWidget(T widget)
	{
		this.removeWidget(widget);
	}
	
	public void markRulesDirty()
	{
		this.handler.updateServer(this.activeRules());
	}
	
}
