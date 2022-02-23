package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.trader.tradedata.TradeRule;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public class PlayerDiscounts extends TradeRule {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "discount_list");
	
	List<PlayerReference> playerList = new ArrayList<>();
	int discount = 10;
	public int getDiscountPercent() { return this.discount; }
	public void setDiscountPercent(int percent) { this.discount = MathUtil.clamp(percent, 0, 100); }
	private double getDiscountMult() { return 1d - ((double)discount/100d); }
	private double getIncreaseMult() { return 1d + ((double)discount/100d); }
	
	public PlayerDiscounts() { super(TYPE); }
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		if(this.isOnList(event.getPlayerReference()))
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
	
	public boolean isOnList(PlayerReference player)
	{
		for(int i = 0; i < this.playerList.size(); ++i)
		{
			if(this.playerList.get(i).is(player))
				return true;
		}
		return false;
	}
	
	@Override
	protected CompoundNBT write(CompoundNBT compound) {
		//Save player names
		ListNBT playerNameList = new ListNBT();
		for(int i = 0; i < this.playerList.size(); i++)
		{
			playerNameList.add(this.playerList.get(i).save());
		}
		compound.put("Players", playerNameList);
		//Save discount
		compound.putInt("discount", this.discount);
		
		return compound;
	}
	
	@Override
	public JsonObject saveToJson(JsonObject json) {
		JsonArray playerList = new JsonArray();
		for(int i = 0; i < this.playerList.size(); ++i)
		{
			playerList.add(this.playerList.get(i).saveAsJson());
		}
		json.add("Players", playerList);
		json.addProperty("discounrd", this.discount);
		return json;
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
				PlayerReference reference = PlayerReference.load(thisCompound);
				if(reference != null)
					this.playerList.add(reference);
				else if(thisCompound.contains("name", Constants.NBT.TAG_STRING))
				{
					reference = PlayerReference.of(thisCompound.getString("name"));
					if(reference != null && !this.isOnList(reference))
						this.playerList.add(reference);
				}
			}
		}
		//Load discount
		if(compound.contains("discount", Constants.NBT.TAG_INT))
			this.discount = compound.getInt("discount");
		
	}
	
	@Override
	public void loadFromJson(JsonObject json) {
		if(json.has("Players"))
		{
			this.playerList.clear();
			JsonArray playerList = json.get("Players").getAsJsonArray();
			for(int i = 0; i < playerList.size(); ++i) {
				PlayerReference reference = PlayerReference.load(playerList.get(i));
				if(reference != null && !this.isOnList(reference))
					this.playerList.add(reference);
			}
		}
		if(json.has("discount"))
			this.discount = json.get("discount").getAsInt();
	}
	
	@Override
	public CompoundNBT savePersistentData() { return null; }
	@Override
	public void loadPersistentData(CompoundNBT data) { }
	
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
		
		ScrollTextDisplay playerList;
		
		@Override
		public void initTab() {
			
			this.nameInput = screen.addCustomListener(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 34, screen.xSize - 20, 20, new StringTextComponent("")));
			
			this.buttonAddPlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + 10, screen.guiTop() + 55, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.discount.add"), this::PressAddButton));
			this.buttonRemovePlayer = this.screen.addCustomButton(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 55, 78, 20, new TranslationTextComponent("gui.button.lightmanscurrency.discount.remove"), this::PressForgetButton));
			
			
			this.discountInput = this.addListener(new TextFieldWidget(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, new StringTextComponent("")));
			this.discountInput.setMaxStringLength(2);
			this.discountInput.setText(Integer.toString(this.getRule().discount));
			this.buttonSetDiscount = this.addButton(new Button(screen.guiLeft() + 110, screen.guiTop() + 10, 50, 20, new TranslationTextComponent("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton));
			
			this.playerList = this.addListener(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 78, screen.xSize - 14, 91, this.screen.getFont(), this::getPlayerList));
			this.playerList.setColumnCount(2);
			
		}
		
		private List<ITextComponent> getPlayerList()
		{
			List<ITextComponent> playerList = Lists.newArrayList();
			if(getRule() == null)
				return playerList;
			for(PlayerReference player : getRule().playerList)
				playerList.add(new StringTextComponent(player.lastKnownName()));
			return playerList;
		}
		
		@Override
		public void renderTab(MatrixStack matrixStack, int mouseX, int mouseY, float partialTicks) {
			
			if(getRule() == null)
				return;
			
			this.nameInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.discountInput.render(matrixStack, mouseX, mouseY, partialTicks);
			this.playerList.render(matrixStack, mouseX, mouseY, partialTicks);
			this.screen.getFont().drawString(matrixStack, new TranslationTextComponent("gui.lightmanscurrency.discount.tooltip").getString(), this.discountInput.x + this.discountInput.getWidth() + 4, this.discountInput.y + 3, 0xFFFFFF);
			
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
			screen.removeListener(this.playerList);
			
		}
		
		void PressAddButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				nameInput.setText("");
				PlayerReference reference = PlayerReference.of(name);
				if(reference != null)
				{
					if(!getRule().isOnList(reference))
					{
						getRule().playerList.add(reference);
						screen.markRulesDirty();
					}
				}
			}
		}
		
		void PressForgetButton(Button button)
		{
			String name = nameInput.getText();
			if(name != "")
			{
				nameInput.setText("");
				PlayerReference reference = PlayerReference.of(name);
				if(reference != null)
				{
					if(getRule().isOnList(reference))
					{
						boolean notFound = true;
						for(int i = 0; notFound && i < getRule().playerList.size(); ++i)
						{
							if(getRule().playerList.get(i).is(reference))
							{
								notFound = false;
								getRule().playerList.remove(i);
							}
						}
						screen.markRulesDirty();
					}
				}
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
