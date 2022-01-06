package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

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
		super(new StringTextComponent(""));
		this.handler = handler;
	}
	
	@Override
	public void init()
	{
		
		//Back button
		this.addButton(new IconButton(guiLeft() + this.xSize, guiTop(), this::PressBackButton, this.font, IconData.of(GUI_TEXTURE, this.xSize, 0)));
		this.managerTab = this.addButton(new IconButton(guiLeft(), guiTop() - 20, this::PressTabButton, this.font, IconData.of(TradeRule.ICON_TEXTURE, 0, 0)));
		
		this.refreshTabs();
		
		this.initManagerTab();
		
	}
	
	private void initManagerTab()
	{
		int y = 0;
		this.removeRuleButtons.clear();
		for(int i = 0; i < this.activeRules().size(); i++)
		{
			this.removeRuleButtons.add(this.addButton(new IconButton(this.guiLeft() + 10, this.guiTop() + 10 + 20 * y, this::PressRemoveRuleButton, this.font, IconData.of(GUI_TEXTURE, this.xSize + 32, 0))));
			y++;
		}
		this.addRuleButtons.clear();
		for(int i = 0; i < this.addableRules().size(); i++)
		{
			this.addRuleButtons.add(this.addButton(new IconButton(this.guiLeft() + 10, this.guiTop() + 10 + 20 * y, this::PressAddRuleButton, this.font, IconData.of(GUI_TEXTURE, this.xSize + 16, 0))));
			y++;
		}
	}
	
	private void closeManagerTab()
	{
		this.addRuleButtons.forEach(button -> this.removeButton(button));
		this.addRuleButtons.clear();
		this.removeRuleButtons.forEach(button -> this.removeButton(button));
		this.removeRuleButtons.clear();
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		this.blit(matrixStack, guiLeft(), guiTop(), 0, 0, this.xSize, this.ySize);
		
		//Render the current rule
		if(this.currentGUIHandler!= null)
			this.currentGUIHandler.renderTab(matrixStack, mouseX, mouseY, partialTicks);
		else
		{
			
			//If current rule is null, but open tab is not -1, reset to the manager tab
			if(this.openTab >= 0)
				this.openTab = -1;
			
			//Render the manager tab
			int y = 0;
			for(int i = 0; i < this.activeRules().size(); i++)
			{
				this.font.drawString(matrixStack, this.activeRules().get(i).getName().getString(), guiLeft() + 34, guiTop() + 16 + y * 20, 0xFFFFFF);
				y++;
			}
			for(int i = 0; i < this.addableRules().size(); i++)
			{
				this.font.drawString(matrixStack, this.addableRules().get(i).getName().getString(), guiLeft() + 34, guiTop() + 16 + y * 20, 0xFFFFFF);
				y++;
			}
			
		}
		
		//Render the buttons, etc
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		if(this.managerTab.isMouseOver(mouseX, mouseY))
		{
			this.renderTooltip(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.mananger"), mouseX, mouseY);
		}
		else
		{
			boolean hoverButton = true;
			for(int i = 0; i < this.tabButtons.size() && i < this.activeRules().size() && hoverButton; i++)
			{
				Button thisTab = this.tabButtons.get(i);
				if(thisTab.isMouseOver(mouseX, mouseY))
				{
					this.renderTooltip(matrixStack, this.activeRules().get(i).getName(), mouseX, mouseY);
					hoverButton = false;
				}
			}
			for(int i = 0; i < this.removeRuleButtons.size() && i < this.activeRules().size() && hoverButton; i++)
			{
				if(this.removeRuleButtons.get(i).isMouseOver(mouseX, mouseY))
				{
					this.renderTooltip(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.removerule", this.activeRules().get(i).getName()), mouseX, mouseY);
					hoverButton = false;
				}
			}
			for(int i = 0; i < this.addRuleButtons.size() && i < this.addableRules().size() && hoverButton; i++)
			{
				if(this.addRuleButtons.get(i).isMouseOver(mouseX, mouseY))
				{
					this.renderTooltip(matrixStack, new TranslationTextComponent("gui.button.lightmanscurrency.addrule", this.addableRules().get(i).getName()), mouseX, mouseY);
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
		
		this.tabButtons.forEach(button -> this.removeButton(button) );
		this.tabButtons.clear();
		
		List<TradeRule> activeRules = this.activeRules();
		for(int i = 0; i < activeRules.size(); i++)
		{
			TradeRule thisRule = activeRules.get(i);
			this.tabButtons.add(this.addButton(new IconButton(guiLeft() + 20 + 20 * i, guiTop() - 20, this::PressTabButton, this.font, thisRule.getButtonIcon())));
		}
		
	}
	
	//Public functions for easy traderule renderer access
	public FontRenderer getFont() { return this.font; }
	
	public <T extends Button> T addCustomButton(T button)
	{
		if(button != null)
			this.addButton(button);
		return button;
	}
	
	public <T extends IGuiEventListener> T addCustomListener(T listener)
	{
		if(listener != null)
			this.addListener(listener);
		return listener;
	}
	
	public void removeButton(Button button)
	{
		if(this.buttons.contains(button))
			this.buttons.remove(button);
		if(this.children.contains(button))
			this.children.remove(button);
	}
	
	public void removeListener(IGuiEventListener listener)
	{
		if(this.children.contains(listener))
			this.children.remove(listener);
	}
	
	public void markRulesDirty()
	{
		this.handler.updateServer(this.activeRules());
	}
	
}
