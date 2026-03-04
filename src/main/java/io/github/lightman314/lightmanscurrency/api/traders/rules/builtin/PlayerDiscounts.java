package io.github.lightman314.lightmanscurrency.api.traders.rules.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
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
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.IPersistentRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.PreTradeEvent;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent.TradeCostEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconUtil;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class PlayerDiscounts extends PriceTweakingTradeRule implements ICopySupportingRule, IPersistentRule {

	public static final TradeRuleType<PlayerDiscounts> TYPE = new Type();

    private static final MapCodec<PlayerDiscounts> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            PlayerReference.LIST_CODEC.fieldOf("players").forGetter(PlayerDiscounts::getPlayerList),
            Codec.INT.fieldOf("discount").forGetter(PlayerDiscounts::getDiscount),
            baseFields()
    ).apply(builder,PlayerDiscounts::new));
	
	private List<PlayerReference> playerList = new ArrayList<>();
	public ImmutableList<PlayerReference> getPlayerList() { return ImmutableList.copyOf(this.playerList); }
	int discount = 10;
	public int getDiscount() { return this.discount; }
	public void setDiscount(int discount) { this.discount = MathUtil.clamp(discount, 1, 100); }
	
	private PlayerDiscounts() { }
    private PlayerDiscounts(List<PlayerReference> playerList,int discount,boolean active) {
        super(active);
        this.playerList = new ArrayList<>(this.playerList);
        this.discount = discount;
    }

    @Override
    public TradeRuleType<?> getType() { return TYPE; }

    @Override
    protected void encodeInternal(Supplier<LazyPacketData.Builder> source,LazyPacketData.Builder builder,Player player) {
        builder.setInt("discount",this.discount)
                .setList("players",this.playerList,ModLazyPackets.PLAYER_REFERENCE);
    }

    @Override
    protected void decodeInternal(LazyPacketData data) {
        this.discount = data.getInt("discount");
        this.playerList = data.getList("players",ModLazyPackets.PLAYER_REFERENCE);
    }

    @Override
	public IconData getIcon() { return IconUtil.ICON_DISCOUNT_LIST; }

	@Override
	public void beforeTrade(PreTradeEvent event)
	{
		if(this.isOnList(event.getPlayerReference()))
		{
			switch (event.getTrade().getTradeDirection()) {
				case SALE ->
						event.addHelpful(LCText.TRADE_RULE_PLAYER_DISCOUNTS_INFO_SALE.get(this.discount));
				case PURCHASE ->
						event.addHelpful(LCText.TRADE_RULE_PLAYER_DISCOUNTS_INFO_PURCHASE.get(this.discount));
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
				case SALE -> event.giveDiscount(this.discount);
				case PURCHASE -> event.hikePrice(this.discount);
				default -> {} //Nothing by default
			}
		}
	}

	public boolean isOnList(PlayerReference player)  { return PlayerReference.isInList(this.playerList, player); }
	
	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		//Load player names
		this.playerList = PlayerReference.loadList(compound, "Players");
		//Load discount
		if(compound.contains("discount", Tag.TAG_INT))
			this.discount = compound.getInt("discount");
	}

    @Nullable
    @Override
    public JsonObject writePersistentData(DataContext<JsonElement> context) {
        if(this.playerList.isEmpty())
            return null;
        JsonObject json = new JsonObject();
        json.add("players",PlayerReference.LIST_CODEC.encodeStart(context.ops(),this.playerList).getOrThrow());
        json.addProperty("discount",this.discount);
        return json;
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("players"))
            this.playerList = PlayerReference.LIST_CODEC.decode(context.ops(),json.get("players")).getOrThrow(JsonSyntaxException::new).getFirst();
        else if(json.has("Players"))
        {
            this.playerList.clear();
            JsonArray playerList = json.get("Players").getAsJsonArray();
            for(int i = 0; i < playerList.size(); ++i) {
                PlayerReference reference = PlayerReference.load(playerList.get(i));
                if(reference != null && !this.isOnList(reference))
                    this.playerList.add(reference);
            }
        }
        this.discount = GsonHelper.getAsInt(json,"discount");
    }

	@Override
	public void writeSettings(SavedSettingData.MutableNodeAccess node) {
		node.setIntValue("discount",this.discount);
		for(int i = 0; i < this.playerList.size(); ++i)
			node.setCompoundValue("player_" + i,this.playerList.get(i).save());
	}

	@Override
	public void loadSettings(SavedSettingData.NodeAccess node) {
		this.discount = node.getIntValue("discount");
		List<PlayerReference> temp = new ArrayList<>();
		for(int i = 0; node.hasCompoundValue("player_" + i); ++i)
			temp.add(PlayerReference.load(node.getCompoundValue("player_" + i)));
		this.playerList = temp;
	}

	@Override
	public void resetToDefaultState() {
		this.discount = 10;
		this.playerList = new ArrayList<>();
	}

	@Override
	protected void handleUpdateMessage(Player player, LazyPacketData updateInfo)
	{
		if(updateInfo.contains("Discount"))
			this.discount = updateInfo.getInt("Discount");
		else if(updateInfo.contains("AddPlayer"))
		{
			PlayerReference added = PlayerReference.load(updateInfo.getTag("AddPlayer"));
			if(added == null || this.isOnList(added))
				return;
			this.playerList.add(added);
		}
		else if(updateInfo.contains("RemovePlayer"))
		{
			PlayerReference removed = PlayerReference.load(updateInfo.getTag("RemovePlayer"));
			if(removed == null || !this.isOnList(removed))
				return;
			PlayerReference.removeFromList(this.playerList,removed);
		}
	}

    private static class Type extends TradeRuleType<PlayerDiscounts>
    {
        @Override
        public PlayerDiscounts create() { return new PlayerDiscounts(); }
        @Override
        public MapCodec<PlayerDiscounts> mapCodec() { return MAP_CODEC;}
    }

}
