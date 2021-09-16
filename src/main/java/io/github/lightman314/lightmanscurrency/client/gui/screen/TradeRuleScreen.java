package io.github.lightman314.lightmanscurrency.client.gui.screen;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.IconButton;
import io.github.lightman314.lightmanscurrency.tradedata.rules.*;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;

public class TradeRuleScreen extends Screen{
	
	private static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/traderules.png");
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
	int selectedAddition = 0;
	
	boolean firstTick = true;
	
	Button managerTab;
	
	List<Button> tabButtons = new ArrayList<>();
	
	List<TradeRule> activeRules() { return this.handler.ruleHandler().getRules(); }
	TradeRule currentRule()
	{
		if(this.openTab >= 0 && this.openTab < activeRules().size())
			return activeRules().get(this.openTab);
		return null;
	}
	
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
		
		if(handler.ruleHandler() == null)
			LightmansCurrency.LogError("Handler supplier returned null at init.");
		else
			LightmansCurrency.LogInfo("Handler supplier worked at init.");
		
		//Back button
		this.addButton(new IconButton(guiLeft() + this.xSize, guiTop(), this::PressBackButton, GUI_TEXTURE, this.xSize, 0));
		this.managerTab = this.addButton(new IconButton(guiLeft(), guiTop() - 20, this::PressTabButton, TradeRule.ICON_TEXTURE, 0, 0));
		
		this.refreshTabs();
		
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void render(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks)
	{
		this.renderBackground(matrixStack);
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.minecraft.getTextureManager().bindTexture(GUI_TEXTURE);
		this.blit(matrixStack, guiLeft(), guiTop(), 0, 0, this.xSize, this.ySize);
		
		//Render the background
		super.render(matrixStack, mouseX, mouseY, partialTicks);
		
		//Render the current rule
		if(this.currentRule() != null)
		{
			this.currentRule().renderTab(this, matrixStack, mouseX, mouseY, partialTicks);
		}
		
	}
	
	public void tick()
	{
		if(firstTick)
		{
			firstTick = false;
			if(handler.ruleHandler() == null)
				LightmansCurrency.LogError("Handler supplier returned null at first tick.");
			else
				LightmansCurrency.LogInfo("Handler supplier worked at first tick.");
		}
		
		if(this.currentRule() != null)
		{
			this.currentRule().onScreenTick(this);
		}
	}
	
	public void PressBackButton(Button button)
	{
		this.handler.reopenLastScreen();
	}
	
	public void PressTabButton(Button button)
	{
		if(tabButtons.contains(button))
		{
			if(this.currentRule() != null)
				this.currentRule().onTabClose(this);
			this.openTab = tabButtons.indexOf(button);
			if(this.currentRule() != null)
				this.currentRule().initTab(this);
		}
		else if(button == this.managerTab)
		{
			if(this.currentRule() != null)
				this.currentRule().onTabClose(this);
			this.openTab = -1;
		}
			
	}
	
	public void refreshTabs()
	{
		
		this.tabButtons.forEach(button -> {
			this.removeButton(button);
		});
		this.tabButtons.clear();
		
		int startX = (this.width - this.xSize) / 2;
		int startY = (this.height - this.ySize) / 2;
		
		List<TradeRule> activeRules = this.activeRules();
		for(int i = 0; i < activeRules.size(); i++)
		{
			TradeRule thisRule = activeRules.get(i);
			this.tabButtons.add(this.addButton(new IconButton(startX + 16 + 16 * i, startY - 20, this::PressTabButton, thisRule.getButtonGUI(), thisRule.getGUIX(), thisRule.getGUIY())));
		}
		
	}
	
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
	
}
