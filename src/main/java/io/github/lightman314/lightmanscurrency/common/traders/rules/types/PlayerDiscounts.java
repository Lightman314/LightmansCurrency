package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import java.util.ArrayList;
import java.util.List;

import com.google.common.base.Supplier;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.TradeRuleScreen;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollTextDisplay;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.client.util.TextInputUtil;
import io.github.lightman314.lightmanscurrency.common.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PlayerDiscounts extends TradeRule {

	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "discount_list");
	
	List<PlayerReference> playerList = new ArrayList<>();
	int discount = 10;
	private double getDiscountMult() { return 1d - ((double)discount/100d); }
	private double getIncreaseMult() { return 1d + ((double)discount/100d); }
	
	public PlayerDiscounts() { super(TYPE); }
	
	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.isOnList(event.getPlayerReference()))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE ->
						event.addHelpful(Component.translatable("traderule.lightmanscurrency.discount_list.info.sale", this.discount));
				case PURCHASE ->
						event.addHelpful(Component.translatable("traderule.lightmanscurrency.discount_list.info.purchase", this.discount));
				default -> {
				} //Nothing by default
			}
		}
	}
	
	@Override
	public void tradeCost(TradeCostEvent event)
	{
		if(this.isOnList(event.getPlayerReference()))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE -> event.applyCostMultiplier(this.getDiscountMult());
				case PURCHASE -> event.applyCostMultiplier(this.getIncreaseMult());
				default -> {} //Nothing by default
			}
		}
	}
	
	public boolean isOnList(PlayerReference player)
	{
		for (PlayerReference playerReference : this.playerList) {
			if (playerReference.is(player))
				return true;
		}
		return false;
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound) {
		//Save player names
		ListTag playerNameList = new ListTag();
		for (PlayerReference playerReference : playerList)
			playerNameList.add(playerReference.save());
		compound.put("Players", playerNameList);
		//Save discount
		compound.putInt("discount", this.discount);
	}

	@Override
	public JsonObject saveToJson(JsonObject json) {
		JsonArray playerList = new JsonArray();
		for (PlayerReference playerReference : this.playerList)
			playerList.add(playerReference.saveAsJson());
		json.add("Players", playerList);
		json.addProperty("discounrd", this.discount);
		return json;
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound) {
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
					reference = PlayerReference.of(false, thisCompound.getString("name"));
					if(reference != null && !this.isOnList(reference))
						this.playerList.add(reference);
				}
			}
		}
		//Load discount
		if(compound.contains("discount", Tag.TAG_INT))
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
	protected void handleUpdateMessage(CompoundTag updateInfo)
	{
		if(updateInfo.contains("Discount"))
		{
			this.discount = updateInfo.getInt("Discount");
		}
		else
		{
			boolean add = updateInfo.getBoolean("Add");
			String name = updateInfo.getString("Name");
			PlayerReference player = PlayerReference.of(false, name);
			if(add && !this.isOnList(player))
			{
				this.playerList.add(player);
			}
			else if(!add && this.isOnList(player))
			{
				PlayerReference.removeFromList(this.playerList, player);
			}
		}
	}
	
	@Override
	public CompoundTag savePersistentData() { return null; }
	@Override
	public void loadPersistentData(CompoundTag data) { }
	
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_DISCOUNT_LIST; }

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

			this.nameInput = this.addCustomRenderable(new EditBox(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 34, screen.xSize - 20, 20, Component.empty()));
			
			this.buttonAddPlayer = this.addCustomRenderable(Button.builder(Component.translatable("gui.button.lightmanscurrency.discount.add"), this::PressAddButton).pos(screen.guiLeft() + 10, screen.guiTop() + 55).size(78, 20).build());
			this.buttonRemovePlayer = this.addCustomRenderable(Button.builder(Component.translatable("gui.button.lightmanscurrency.discount.remove"), this::PressForgetButton).pos(screen.guiLeft() + screen.xSize - 88, screen.guiTop() + 55).size(78, 20).build());
			
			this.discountInput = this.addCustomRenderable(new EditBox(screen.getFont(), screen.guiLeft() + 10, screen.guiTop() + 9, 20, 20, Component.empty()));
			this.discountInput.setMaxLength(2);
			PlayerDiscounts rule = this.getRule();
			if(rule != null)
				this.discountInput.setValue(Integer.toString(rule.discount));
			this.buttonSetDiscount = this.addCustomRenderable(Button.builder(Component.translatable("gui.button.lightmanscurrency.discount.set"), this::PressSetDiscountButton).pos(screen.guiLeft() + 110, screen.guiTop() + 10).size(50, 20).build());
			
			this.playerList = this.addCustomRenderable(new ScrollTextDisplay(screen.guiLeft() + 7, screen.guiTop() + 78, screen.xSize - 14, 91, this.screen.getFont(), this::getPlayerList));
			this.playerList.setColumnCount(2);	
			
		}
				
		private List<Component> getPlayerList()
		{
			List<Component> playerList = Lists.newArrayList();
			if(getRule() == null)
				return playerList;
			for(PlayerReference player : getRule().playerList)
				playerList.add(player.getNameComponent(true));
			return playerList;
		}
				
		@Override
		public void renderTab(PoseStack poseStack, int mouseX, int mouseY, float partialTicks) {
			
			if(getRule() == null)
				return;
			
			Screen.fill(poseStack, this.screen.guiLeft() + 7, this.screen.guiTop() + 78, this.screen.guiLeft() + this.screen.width - 7, this.screen.guiTop() + 78 + 91, 0x000000FF);
			
			this.screen.getFont().draw(poseStack, Component.translatable("gui.lightmanscurrency.discount.tooltip").getString(), this.discountInput.getX() + this.discountInput.getWidth() + 4, this.discountInput.getY() + 3, 0xFFFFFF);
			
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
			if(!name.isBlank())
			{
				nameInput.setValue("");
				CompoundTag updateInfo = new CompoundTag();
				updateInfo.putBoolean("Add", true);
				updateInfo.putString("Name", name);
				this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
			}
		}
		
		void PressForgetButton(Button button)
		{
			String name = nameInput.getValue();
			if(!name.isBlank())
			{
				nameInput.setValue("");
				CompoundTag updateInfo = new CompoundTag();
				updateInfo.putBoolean("Add", false);
				updateInfo.putString("Name", name);
				this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
			}
			
		}
		
		void PressSetDiscountButton(Button button)
		{
			int discount = TextInputUtil.getIntegerValue(this.discountInput, 1);
			PlayerDiscounts rule = this.getRule();
			if(rule != null)
				rule.discount = MathUtil.clamp(discount, 1, 100);
			CompoundTag updateInfo = new CompoundTag();
			updateInfo.putInt("Discount", discount);
			this.screen.sendUpdateMessage(this.getRuleRaw(), updateInfo);
		}
		
	}
	
	
}
