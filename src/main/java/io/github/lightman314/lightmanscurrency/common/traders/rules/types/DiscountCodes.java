package io.github.lightman314.lightmanscurrency.common.traders.rules.types;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.misc.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.data.SavedSettingData;
import io.github.lightman314.lightmanscurrency.api.traders.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.rules.ICopySupportingRule;
import io.github.lightman314.lightmanscurrency.api.traders.rules.TradeRuleType;
import io.github.lightman314.lightmanscurrency.api.traders.rules.data.PlayerMemory;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.traders.rules.PriceTweakingTradeRule;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DiscountCodes extends PriceTweakingTradeRule implements ICopySupportingRule {

    public static TradeRuleType<DiscountCodes> TYPE = new TradeRuleType<>(VersionUtil.lcResource("discount_code"), DiscountCodes::new);

    private DiscountCodes() { super(TYPE); }

    private final Map<String,DiscountRules> rules = new HashMap<>();
    public Map<String,DiscountRules> getRules() { return this.rules; }

    @Override
    public IconData getIcon() { return ItemIcon.ofItem(ModItems.COUPON); }

    @Override
    public void beforeTrade(TradeEvent.PreTradeEvent event) {
        TradeContext context = event.getContext();
        this.rules.forEach((code,rule) -> {
            if(context.hasDiscountCode(code))
            {
                //Limit Info
                if(rule.limit > 0)
                {
                    event.addNeutral(LCText.TRADE_RULE_DISCOUNT_CODES_INFO_LIMIT.get(rule.memory.getCount(event,rule.timeLimit),rule.limit));
                    if(rule.timeLimit > 0)
                        event.addNeutral(LCText.TRADE_RULE_DISCOUNT_CODES_INFO_TIMED.get(new TimeUtil.TimeData(rule.timeLimit).getString()));
                }
                if(rule.validLimit(event))
                {
                    //Give discount
                    switch (event.getTrade().getTradeDirection()) {
                        case SALE -> event.addHelpful(LCText.TRADE_RULE_DISCOUNT_CODES_INFO_SALE.get(rule.discount));
                        case PURCHASE -> event.addHelpful(LCText.TRADE_RULE_DISCOUNT_CODES_INFO_PURCHASE.get(rule.discount));
                        default -> {} //Nothing by default
                    }
                }
            }
        });
    }

    @Override
    public void tradeCost(TradeEvent.TradeCostEvent event) {
        TradeContext context = event.getContext();
        this.rules.forEach((code,rule) -> {
            if(context.hasDiscountCode(code))
            {
                if(rule.validLimit(event))
                {
                    //Give discount
                    switch (event.getTrade().getTradeDirection()) {
                        case SALE -> event.giveDiscount(rule.discount);
                        case PURCHASE -> event.hikePrice(rule.discount);
                        default -> {} //Nothing by default
                    }
                }
            }
        });
    }

    @Override
    public void afterTrade(TradeEvent.PostTradeEvent event) {
        TradeContext context = event.getContext();
        this.rules.forEach((code,rule) -> {
            if(context.hasDiscountCode(code))
            {
                context.consumeDiscountCode(code);
                if(rule.limit > 0)
                {
                    rule.memory.addEntry(context.getPlayerReference().id);
                    event.markDirty();
                }
            }
            if(rule.memory.clearExpiredData(rule.timeLimit))
                event.markDirty();
        });
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        ListTag list = new ListTag();
        this.rules.forEach((code,rule) -> list.add(rule.save(code)));
        compound.put("Rules",list);
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        ListTag list = compound.getList("Rules",Tag.TAG_COMPOUND);
        this.rules.clear();
        for(int i = 0; i < list.size(); ++i)
        {
            Pair<String,DiscountRules> entry = DiscountRules.load(list.getCompound(i));
            this.rules.put(entry.getFirst(),entry.getSecond());
        }
    }

    @Override
    public JsonObject saveToJson(JsonObject json, HolderLookup.Provider lookup) {
        JsonArray list = new JsonArray();
        this.rules.forEach((code,rules) -> list.add(rules.write(code)));
        json.add("Rules",list);
        return json;
    }

    @Override
    public void loadFromJson(JsonObject json, HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
        JsonArray list = GsonHelper.getAsJsonArray(json,"Rules");
        if(list.isEmpty())
            throw new JsonSyntaxException("Rules cannot be empty!");
        this.rules.clear();
        for(int i = 0; i < list.size(); ++i)
        {
            Pair<String,DiscountRules> entry = DiscountRules.read(GsonHelper.convertToJsonObject(list.get(i),"Rules[" + i + "]"));
            this.rules.put(entry.getFirst(),entry.getSecond());
        }
    }

    @Override
    public void resetToDefaultState() { this.rules.clear(); }

    @Override
    public void writeSettings(SavedSettingData.MutableNodeAccess node) {
        AtomicInteger index = new AtomicInteger(0);
        this.rules.forEach((code,rules) -> {
            SavedSettingData.MutableNodeAccess entryNode = node.forSubNode("rule_" + index.getAndIncrement());
            rules.saveSettings(entryNode,code);
        });
    }

    @Override
    public void loadSettings(SavedSettingData.NodeAccess node) {
        this.rules.clear();
        for(int i = 0; !node.forSubNode("rule_" + i).isEmpty(); ++i)
        {
            SavedSettingData.NodeAccess entryNode = node.forSubNode("rule_" + i);
            Pair<String,DiscountRules> entry = DiscountRules.loadSettings(entryNode);
            this.rules.put(entry.getFirst(),entry.getSecond());
        }
    }

    @Override
    public CompoundTag savePersistentData(HolderLookup.Provider provider) {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        this.rules.forEach((code,rule) -> {
            if(rule.limit > 0)
            {
                CompoundTag entry = new CompoundTag();
                entry.putString("Code",code);
                rule.memory.save(entry);
                list.add(entry);
            }
        });
        if(!list.isEmpty())
        {
            tag.put("Memory",list);
            return tag;
        }
        return null;
    }

    @Override
    public void loadPersistentData(CompoundTag data, HolderLookup.Provider lookup) {
        ListTag list = data.getList("Memory",Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundTag entry = list.getCompound(i);
            String code = entry.getString("Code");
            if(this.rules.containsKey(code))
                this.rules.get(code).memory.load(entry);
        }
    }

    @Override
    protected void handleUpdateMessage(Player player, LazyPacketData updateInfo) {

        if(updateInfo.contains("Edit"))
        {
            String code = updateInfo.getString("Edit");
            DiscountRules entry = this.rules.get(code);
            if(entry == null)
            {
                LightmansCurrency.LogWarning("Could not find '" +code + "' entry on the server.");
                return;
            }
            if(updateInfo.contains("Discount"))
                entry.discount = MathUtil.clamp(updateInfo.getInt("Discount"),1,100);
            if(updateInfo.contains("Limit"))
                entry.limit = Math.max(0,updateInfo.getInt("Limit"));
            if(updateInfo.contains("Timer"))
                entry.timeLimit = Math.max(0,updateInfo.getLong("Timer"));
            if(updateInfo.contains("Rename"))
            {
                this.rules.remove(code);
                this.rules.put(updateInfo.getString("Rename"),entry);
            }
        }
        if(updateInfo.contains("Create"))
        {
            String newCode = updateInfo.getString("Create");
            if(!this.rules.containsKey(newCode))
                this.rules.put(newCode,new DiscountRules());
        }
        if(updateInfo.contains("Remove"))
            this.rules.remove(updateInfo.getString("Remove"));
    }

    public static class DiscountRules
    {
        public int discount = 10;
        public int limit = 0;
        public long timeLimit = 0;
        public final PlayerMemory memory = new PlayerMemory();
        public CompoundTag save(String code)
        {
            CompoundTag tag = new CompoundTag();
            tag.putString("Code",code);
            tag.putInt("Discount",this.discount);
            tag.putInt("Limit",this.limit);
            tag.putLong("Timer",this.timeLimit);
            this.memory.save(tag);
            return tag;
        }

        public boolean validLimit(TradeEvent event) { return this.limit <= 0 || this.memory.getCount(event,this.timeLimit) < this.limit; }

        public static Pair<String,DiscountRules> load(CompoundTag tag)
        {
            String code = tag.getString("Code");
            DiscountRules rules = new DiscountRules();
            rules.discount = MathUtil.clamp(tag.getInt("Discount"),1,100);
            rules.limit = Math.max(0,tag.getInt("Limit"));
            rules.timeLimit = Math.max(0,tag.getLong("Timer"));
            rules.memory.load(tag);
            return Pair.of(code,rules);
        }

        public JsonObject write(String code)
        {
            JsonObject json = new JsonObject();
            json.addProperty("Code",code);
            json.addProperty("Discount",this.discount);
            json.addProperty("Limit",this.limit);
            json.addProperty("Timer",this.timeLimit);
            return json;
        }

        public static Pair<String,DiscountRules> read(JsonObject json) throws JsonSyntaxException
        {
            String code = GsonHelper.getAsString(json,"Code");
            DiscountRules rules = new DiscountRules();
            rules.discount = GsonHelper.getAsInt(json,"Discount");
            rules.limit = GsonHelper.getAsInt(json,"Limit");
            rules.timeLimit = GsonHelper.getAsInt(json,"Timer");
            return Pair.of(code,rules);
        }

        public void saveSettings(SavedSettingData.MutableNodeAccess node,String code)
        {
            node.setStringValue("code",code);
            node.setIntValue("discount",this.discount);
            node.setIntValue("limit",this.limit);
            node.setLongValue("timer",this.timeLimit);
        }

        public static Pair<String,DiscountRules> loadSettings(SavedSettingData.NodeAccess node)
        {
            String code = node.getStringValue("code");
            DiscountRules rules = new DiscountRules();
            rules.discount = node.getIntValue("discount");
            rules.limit = node.getIntValue("limit");
            rules.timeLimit = node.getLongValue("timer");
            return Pair.of(code,rules);
        }


    }

}
