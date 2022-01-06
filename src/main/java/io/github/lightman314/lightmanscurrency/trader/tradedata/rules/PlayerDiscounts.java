package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class PlayerDiscounts extends TradeRule {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "discount_list");
	
	List<String> playerList = new ArrayList<>();
	int discount = 10;
	public int getDiscountPercent() { return this.discount; }
	public void setDiscountPercent(int percent) { this.discount = MathUtil.clamp(percent, 0, 100); }
	private double getDiscountMult() { return 1d - ((double)discount/100d); }
	private double getIncreaseMult() { return 1d + ((double)discount/100d); }
	
	public PlayerDiscounts() { super(TYPE); }
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		if(this.playerList.contains(event.getPlayer().getDisplayName().getString()))
		{
			switch(event.getTrade().getTradeDirection())
			{
			case SALE:
				event.applyCostMultiplier(this.getDiscountMult());
				break;
			case PURCHASE:
				event.applyCostMultiplier(this.getIncreaseMult());
				break;
				default: //Nothing by default
			}
		}
	}
	
	@Override
	protected CompoundNBT write(CompoundNBT compound) {
		//Save player names
		ListNBT playerNameList = new ListNBT();
		for(int i = 0; i < playerList.size(); i++)
		{
			CompoundNBT thisCompound = new CompoundNBT();
			thisCompound.putString("name", playerList.get(i));
			playerNameList.add(thisCompound);
		}
		compound.put("Players", playerNameList);
		//Save discount
		compound.putInt("discount", this.discount);
		
		return compound;
	}

	@Override
	public void readNBT(CompoundNBT compound) {
		//Load player names
		if(compound.contains("Players", Constants.NBT.TAG_LIST))
		{
			this.playerList.clear();
			ListNBT playerNameList = compound.getList("Players", Constants.NBT.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundNBT thisCompound = playerNameList.getCompound(i);
				if(thisCompound.contains("name", Constants.NBT.TAG_STRING))
					this.playerList.add(thisCompound.getString("name"));
			}
		}
		//Load discount
		if(compound.contains("discount", Constants.NBT.TAG_INT))
			this.discount = compound.getInt("discount");
		
	}
	
	@Override
	public IconData getButtonIcon() { return IconData.of(new TranslationTextComponent("gui.button.lightmanscurrency.discount_list")); }

	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRule.GUIHandler createHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
	{
		return new GUIHandler(screen, rule);
	}
	
	@OnlyIn(Dist.CLIENT)
	private static class GUIHandler extends TradeRule.GUIHandler
	{
		
		protected final PlayerDiscounts getRule()
		{
			if(getRuleRaw() instanceof PlayerDiscounts)
				return (PlayerDiscounts)getRuleRaw();
			return null;
		}
		
		GUIHandler(TradeRuleScreen screen, Supplier<TradeRule> rule)
		{
			super(screen, rule);
		}
		
		TextFieldWidget nameInput;
		TextFieldWidget discountInput;
		
		Button buttonAddPlayer;
		Button buttonRemovePlayer;
		
		Button buttonSetDiscount;
		
		final int namesPerPage = 9;
		
		@Override
		public void initTab() {
			
			this.nameInput = screen.addCustomListener(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 34, screen.xSize - 20, 20, new StringTextComponent("")));
			
			this.buttonAddPlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + 10, screen.guiTop() + 55, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.discount.add"), this::PressAddButton));
			this.buttonRemovePlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 55, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.discount.remove"), this::PressForgetButton));
			
			
			this.discountInput = this.addListener(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, new StringTextComponent("")));
			this.discountInput.setMaxStringLength(2);
			this.discountInput.setText(Integer.toString(this.getRule().discount));
			this.buttonSetDiscount = this.addButton(new Button(screen.guiLeft() + 110, screen.guiTop() + 10, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton));
			
		}
		
		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			if(getRule() == null)
				return;
			
			this.screen.blit(matrixStack, this.screen.guiLeft(), this.screen.guiTop() + 78, 0, this.screen.ySize, this.screen.xSize, 80);
			this.screen.blit(matrixStack, this.screen.guiLeft(), this.screen.guiTop() + 78 + 80, 0, this.screen.ySize, this.screen.xSize, 11);
			
			this.nameInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.discountInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.screen.getFont().drawString(matrixStack, new TranslationTextComponent("gui.lightmanscurrency.discount.tooltip").getString(), this.discountInput.x + this.discountInput.getWidth() + 4, this.discountInput.y + 3, 0xFFFFFF);
			
			int x = 0;
			int y = 0;
			for(int i = 0; i < getRule().playerList.size() && x < 2; i++)
			{
				screen.getFont().drawString(matrixStack, getRule().playerList.get(i), screen.guiLeft() + 10 + 78 * x, screen.guiTop() + 80 + 10 * y, 0xFFFFFF);
				y++;
				if(y >= this.namesPerPage)
				{
					y = 0;
					x++;
				}
			}
			
		}
		
		@Override
		public void onScreenTick()
		{
			this.discountInput.tick();
			this.nameInput.tick();
			TextInputUtil.whitelistInteger(this.discountInput, 0, 99);
		}
		
		
		@Override
		public void onTabClose() {
			
			screen.removeListener(this.nameInput);
			screen.removeButton(this.buttonAddPlayer);
			screen.removeButton(this.buttonRemovePlayer);
			screen.removeListener(this.discountInput);
			screen.removeButton(this.buttonSetDiscount);
			
		}
		
		void PressAddButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				if(!getRule().playerList.contains(name))
				{
					getRule().playerList.add(name);
					screen.markRulesDirty();
				}
				nameInput.setText("");
			}
		}
		
		void PressForgetButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				if(getRule().playerList.contains(name))
				{
					getRule().playerList.remove(name);
					screen.markRulesDirty();
				}
				nameInput.setText("");
			}
			
		}
		
		void PressSetDiscountButton(Button button)
		{
			int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
			this.getRule().discount = discount;
			this.screen.markRulesDirty();
		}
		
	}
	
	
}
