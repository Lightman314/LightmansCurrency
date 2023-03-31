package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.TradeRulesClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.trade_rules.rule_tabs.TradeLimitTab;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.icon.IconData;
import io.github.lightman314.lightmanscurrency.client.util.IconAndButtonUtil;
import io.github.lightman314.lightmanscurrency.common.easy.EasyText;
import io.github.lightman314.lightmanscurrency.common.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PreTradeEvent;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class TradeLimit extends TradeRule{

	public static final ResourceLocation OLD_TYPE = new ResourceLocation(LightmansCurrency.MODID, "tradelimit2");
	public static final ResourceLocation TYPE = new ResourceLocation(LightmansCurrency.MODID, "trade_limit");

	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = newLimit; }

	int count = 0;
	public void resetCount() { this.count = 0; }

	public TradeLimit() { super(TYPE); }

	@Override
	public void beforeTrade(PreTradeEvent event) {

		if(this.count >= this.limit)
		{
			event.addDenial(EasyText.translatable("traderule.lightmanscurrency.tradelimit2.denial", this.count));
			event.addDenial(EasyText.translatable("traderule.lightmanscurrency.tradelimit.denial.limit", this.limit));
		}
		else
			event.addHelpful(EasyText.translatable("traderule.lightmanscurrency.tradelimit2.info", this.count, this.limit));
	}

	@Override
	public void afterTrade(PostTradeEvent event) {

		this.count++;

		event.markDirty();

	}

	@Override
	protected void saveAdditional(CompoundTag compound) {

		compound.putInt("Limit", this.limit);
		compound.putInt("Count", this.count);

	}

	@Override
	public JsonObject saveToJson(JsonObject json) {
		json.addProperty("Limit", this.limit);
		return json;
	}

	@Override
	protected void loadAdditional(CompoundTag compound) {

		if(compound.contains("Limit", Tag.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("Count", Tag.TAG_INT))
			this.count = compound.getInt("Count");

	}

	@Override
	public void loadFromJson(JsonObject json) {
		if(json.has("Limit"))
			this.limit = json.get("Limit").getAsInt();
	}

	@Override
	public void handleUpdateMessage(CompoundTag updateInfo)
	{
		if(updateInfo.contains("Limit"))
		{
			this.limit = updateInfo.getInt("Limit");
		}
		else if(updateInfo.contains("ClearMemory"))
		{
			this.count = 0;
		}
	}

	@Override
	public CompoundTag savePersistentData() {
		CompoundTag data = new CompoundTag();
		data.putInt("Count", this.count);
		return data;
	}
	@Override
	public void loadPersistentData(CompoundTag data) {
		if(data.contains("Count", Tag.TAG_INT))
			this.count = data.getInt("Count");
	}

	@Override
	public IconData getButtonIcon() { return IconAndButtonUtil.ICON_COUNT; }

	@Nonnull
	@Override
	@OnlyIn(Dist.CLIENT)
	public TradeRulesClientSubTab createTab(TradeRulesClientTab<?> parent) { return new TradeLimitTab(parent); }

}