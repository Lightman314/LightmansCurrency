package io.github.lightman314.lightmanscurrency.api.traders.rules.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class TradeLimit extends TradeRule implements ICopySupportingRule, IPersistentRule {

    public static final int MAX_LIMIT = 1000000;

	public static final TradeRuleType<TradeLimit> TYPE = new Type();

    private static final MapCodec<TradeLimit> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("limit").forGetter(TradeLimit::getLimit),
            Codec.INT.fieldOf("count").forGetter(r -> r.count),
            baseFields()
    ).apply(builder,TradeLimit::new));

	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = MathUtil.clamp(newLimit,1,MAX_LIMIT); }
	
	int count = 0;
	public void resetCount() { this.count = 0; }
	
	private TradeLimit() { }
    private TradeLimit(int limit, int count, boolean active) {
        super(active);
        this.limit = limit;
        this.count = count;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    protected void encodeInternal(Supplier<LazyPacketData.Builder> source,LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setInt("limit",this.limit);
        builder.setInt("count",this.count);
    }

    @Override
    protected void decodeInternal(LazyPacketData data) {
        this.limit = data.getInt("limit");
        this.count = data.getInt("count");
    }

    @Override
	public IconData getIcon() { return IconUtil.ICON_COUNT; }

	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		if(this.count >= this.limit)
		{
			event.addDenial(LCText.TRADE_RULE_TRADE_LIMIT_DENIAL.get(this.count));
			event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_LIMIT.get(this.limit));
		}
		else
			event.addHelpful(LCText.TRADE_RULE_TRADE_LIMIT_INFO.get(this.count, this.limit));
	}

	@Override
	public boolean afterTrade(PostTradeEvent event) {
		this.count++;
        return true;
	}

    @Nullable
    @Override
    public JsonObject writePersistentData(DataContext<JsonElement> context) {
        JsonObject json = new JsonObject();
        json.addProperty("limit",this.limit);
        return json;
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("Limit"))
            this.limit = GsonHelper.getAsInt(json,"Limit");
        else
            this.limit = GsonHelper.getAsInt(json,"limit");
        if(this.limit <= 0)
            throw new JsonSyntaxException("Trade Limit cannot be less than 1!");
    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) {
        CompoundTag tag = new CompoundTag();
        tag.putInt("count",this.count);
        return tag;
    }

    @Override
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) {
        if(tag.contains("Count"))
            this.count = tag.getInt("Count");
        else
            this.count = tag.getInt("count");
    }

	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		if(compound.contains("Limit", Tag.TAG_INT))
			this.limit = compound.getInt("Limit");
		if(compound.contains("Count", Tag.TAG_INT))
			this.count = compound.getInt("Count");
		
	}

	@Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("limit",this.limit);
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.limit = node.getIntValue("limit");
	}

	@Override
	public void resetToDefaultState() {
		this.limit = 1;
		this.count = 0;
	}
	
	@Override
	public void handleUpdateMessage(Player player, LazyPacketData updateInfo)
	{
		if(updateInfo.contains("Limit"))
			this.limit = updateInfo.getInt("Limit");
		else if(updateInfo.contains("ClearMemory"))
			this.count = 0;
	}

    private static class Type extends TradeRuleType<TradeLimit>
    {
        @Override
        public TradeLimit create() { return new TradeLimit(); }
        @Override
        public MapCodec<TradeLimit> mapCodec() { return MAP_CODEC; }
    }
	
}
