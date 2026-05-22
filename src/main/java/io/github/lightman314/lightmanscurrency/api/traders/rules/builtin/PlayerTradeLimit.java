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
import io.github.lightman314.lightmanscurrency.api.traders.rules.data.PlayerMemory;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PostTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class PlayerTradeLimit extends TradeRule implements ICopySupportingRule, IPersistentRule {

    public static final int MAX_LIMIT = 1000000;

	public static final TradeRuleType<PlayerTradeLimit> TYPE = new Type();

    private static final MapCodec<PlayerTradeLimit> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("limit").forGetter(PlayerTradeLimit::getLimit),
            Codec.LONG.fieldOf("timeLimit").forGetter(PlayerTradeLimit::getTimeLimit),
            PlayerMemory.CODEC.fieldOf("memory").forGetter(r -> r.memory),
            baseFields()
    ).apply(builder,PlayerTradeLimit::new));

	private int limit = 1;
	public int getLimit() { return this.limit; }
	public void setLimit(int newLimit) { this.limit = MathUtil.clamp(newLimit,1,MAX_LIMIT); }
	
	private long timeLimit = 0;
	private boolean enforceTimeLimit() { return this.timeLimit > 0; }
	public long getTimeLimit() { return this.timeLimit; }
	public void setTimeLimit(long timeLimit) { this.timeLimit = timeLimit; }

    private final PlayerMemory memory;
	public void resetMemory() { this.memory.clear(); }
	
	private PlayerTradeLimit() { this.memory = new PlayerMemory(); }
    private PlayerTradeLimit(int limit,long timeLimit,PlayerMemory memory,boolean active) {
        super(active);
        this.limit = limit;
        this.timeLimit = timeLimit;
        this.memory = memory;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    protected void encodeInternal(Supplier<LazyPacketData.Builder> source, LazyPacketData.Builder builder, ISyncingContext context) {
        builder.setInt("limit",this.limit)
                .setLong("timeLimit",this.timeLimit)
                .setList("memory",this.memory.encode(source,context),LazyPacketData.MAP_FACTORY);
    }

    @Override
    protected void decodeInternal(LazyPacketData data) {
        this.limit = data.getInt("limit");
        this.timeLimit = data.getLong("timeLimit");
        this.memory.decode(data,"memory");
    }

    @Override
	public IconData getIcon() { return IconUtil.ICON_COUNT_PLAYER; }

	@Override
	public void beforeTrade(PreTradeEvent event) {
		
		int tradeCount = this.memory.getCount(event,this.timeLimit);
		if(tradeCount >= this.limit)
		{
			if(this.enforceTimeLimit())
            {
                event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_TIMED.get(tradeCount, new TimeUtil.TimeData(this.getTimeLimit()).getString()));
                long timeRemaining = this.memory.getTimeRemaining(event,this.timeLimit);
                if(timeRemaining > 0)
                    event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_TIME_REMAINING.get(new TimeUtil.TimeData(timeRemaining).getString()));
            }
			else
				event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL.get(tradeCount));
			event.addDenial(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_DENIAL_LIMIT.get(this.limit));
		}
		else
		{
			if(this.enforceTimeLimit())
				event.addHelpful(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_INFO_TIMED.get(tradeCount, this.limit, new TimeUtil.TimeData(this.getTimeLimit()).getString()));
			else
				event.addHelpful(LCText.TRADE_RULE_PLAYER_TRADE_LIMIT_INFO.get(tradeCount, this.limit));
		}
	}

	@Override
	public boolean afterTrade(PostTradeEvent event) {
		this.memory.addEntry(event);
        this.memory.clearExpiredData(this.timeLimit);
		return true;
	}

    @Nullable
    @Override
    public JsonObject writePersistentData(DataContext<JsonElement> context) {
        JsonObject json = new JsonObject();
        json.addProperty("limit", this.limit);
        if(this.enforceTimeLimit())
            json.addProperty("forget_time", this.timeLimit);
        return json;
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("Limit"))
            this.limit = GsonHelper.getAsInt(json,"Limit");
        else
            this.limit = GsonHelper.getAsInt(json,"limit");
        if(json.has("ForgetTime"))
            this.timeLimit = GsonHelper.getAsLong(json,"ForgetTime");
        else
            this.timeLimit = GsonHelper.getAsLong(json,"forget_time",0);
    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) {
        CompoundTag tag = new CompoundTag();
        tag.put("memory",PlayerMemory.CODEC.encodeStart(context.ops(),this.memory).getOrThrow());
        return tag;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) {
        if(tag.contains("memory"))
            this.memory.copyFrom(PlayerMemory.CODEC.decode(context.ops(),tag.get("memory")).getOrThrow().getFirst());
        else
            this.memory.loadOldData(tag);
    }

	@Override
    @SuppressWarnings("deprecation")
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		
		if(compound.contains("Limit", Tag.TAG_INT))
			this.limit = compound.getInt("Limit");
        this.memory.loadOldData(compound);
		if(compound.contains("ForgetTime", Tag.TAG_LONG))
			this.timeLimit = compound.getLong("ForgetTime");
	}

	@Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("limit",this.limit);
		node.setLongValue("time_limit",this.timeLimit);
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.limit = Math.max(1,node.getIntValue("limit"));
		this.timeLimit = node.getLongValue("time_limit");
	}

	@Override
	public void resetToDefaultState() {
		this.limit = 1;
		this.timeLimit = 0;
		this.resetMemory();
	}

	@Override
	public void handleUpdateMessage(Player player, LazyPacketData updateInfo)
	{
		if(updateInfo.contains("Limit"))
			this.limit = updateInfo.getInt("Limit");
		else if(updateInfo.contains("TimeLimit"))
			this.timeLimit = updateInfo.getLong("TimeLimit");
		else if(updateInfo.contains("ClearMemory"))
			this.resetMemory();
	}

    private static class Type extends TradeRuleType<PlayerTradeLimit>
    {
        @Override
        public PlayerTradeLimit create() { return new PlayerTradeLimit(); }
        @Override
        public MapCodec<PlayerTradeLimit> mapCodec() { return MAP_CODEC; }
    }
	
}
