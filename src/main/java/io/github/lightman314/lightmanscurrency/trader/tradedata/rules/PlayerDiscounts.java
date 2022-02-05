package io.github.lightman314.lightmanscurrency.trader.tradedata.rules;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.trader.settings.PlayerReference;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	protected CompoundTag write(CompoundTag compound) {
		//Save player names
		ListTag playerNameList = new ListTag();
		for(int i = 0; i < playerList.size(); i++)
		{
			playerNameList.add(this.playerList.get(i).save());
		}
		compound.put("Players", playerNameList);
		//Save discount
		compound.putInt("discount", this.discount);
		
		return compound;
	}

	@Override
	public void readNBT(CompoundTag compound) {
		//Load player names
		if(compound.contains("Players", Tag.TAG_LIST))
		{
			this.playerList.clear();
			ListTag playerNameList = compound.getList("Players", Tag.TAG_COMPOUND);
			for(int i = 0; i < playerNameList.size(); i++)
			{
				CompoundTag thisCompound = playerNameList.getCompound(i);
				PlayerReference reference = PlayerReference.load(thisCompound);
				if(reference != null)
					this.playerList.add(reference);
				//Load old method
				else if(thisCompound.contains("name", Tag.TAG_STRING))
				{
					reference = PlayerReference.of(thisCompound.getString("name"));
					if(reference != null && !this.isOnList(reference))
						this.playerList.add(reference);
				}
			}
		}
		//Load discount
		if(compound.contains("discount", Tag.TAG_INT))
			this.discount = compound.getInt("discount");
		
	}
	
	public IconData getButtonIcon() { return IconData.of(new TranslatableComponent("gui.button.lightmanscurrency.discount_list")); }

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
		
		EditBox nameInput;
		EditBox discountInput;
		
		Button buttonAddPlayer;
		Button buttonRemovePlayer;
		
		Button buttonSetDiscount;
		
		ScrollTextDisplay playerList;
		
		@Override
		public void initTab() {
			
			this.nameInput = this.addCustomRenderable(new EditBox(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 34, screen.xSize - 20, 20, new TextComponent("")));
			
			this.buttonAddPlayer = this.addCustomRenderable(new Button(screen.guiLeft() + 10, screen.guiTop() + 55, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.discount.add"), this::PressAddButton));
			this.buttonRemovePlayer = this.addCustomRenderable(new Button(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 55, 78, 20, new TranslatableComponent("gui.button.lightmanscurrency.discount.remove"), this::PressForgetButton));
			
			this.discountInput = this.addCustomRenderable(new EditBox(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, new TextComponent("")));
			this.discountInput.setMaxLength(2);
			this.discountInput.setValue(Integer.toString(this.getRule().discount));
			this.buttonSetDiscount = this.addCustomRenderable(new Button(screen.guiLeft() + 110, screen.guiTop() + 10, 50, 20, new TranslatableComponent("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton));
			
			this.playerList = this.addCustomRenderable(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 78, screen.xSize - 14, 91, this.screen.getFont(), this::getPlayerList));
			this.playerList.setColumnCount(2);	
			
		}
				
		private List<Component> getPlayerList()
		{
			List<Component> playerList = Lists.newArrayList();
			if(getRule() == null)
				return playerList;
			for(PlayerReference player : getRule().playerList)
				playerList.add(new TextComponent(player.lastKnownName()));
			return playerList;
		}
				
		@Override
		public void renderTab(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
			
			if(getRule() == null)
				return;
			
			this.screen.blit(poseStack, this.screen.guiLeft(), this.screen.guiTop() + 78, 0, this.screen.ySize, this.screen.xSize, 80);
			this.screen.blit(poseStack, this.screen.guiLeft(), this.screen.guiTop() + 78 + 80, 0, this.screen.ySize, this.screen.xSize, 11);
			
			this.screen.getFont().draw(poseStack, new TranslatableComponent("gui.lightmanscurrency.discount.tooltip").getString(), this.discountInput.x + this.discountInput.getWidth() + 4, this.discountInput.y + 3, 0xFFFFFF);
			
		}
		
		@Override
		public void onScreenTick()
		{
			TextInputUtil.whitelistInteger(this.discountInput, 0, 99);
		}
		
		
		@Override
		public void onTabClose() {
			
			this.removeCustomWidget(this.nameInput);
			this.removeCustomWidget(this.buttonAddPlayer);
			this.removeCustomWidget(this.buttonRemovePlayer);
			this.removeCustomWidget(this.discountInput);
			this.removeCustomWidget(this.buttonSetDiscount);
			this.removeCustomWidget(this.playerList);
			
		}
		
		void PressAddButton(Button button)
		{
			String name = nameInput.getValue();
			if(name != "")
			{
				nameInput.setValue("");
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
			String name = nameInput.getValue();
			if(name != "")
			{
				nameInput.setValue("");
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
