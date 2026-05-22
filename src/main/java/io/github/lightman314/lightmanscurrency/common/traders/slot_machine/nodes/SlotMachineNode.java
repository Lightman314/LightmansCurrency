package io.github.lightman314.lightmanscurrency.common.traders.slot_machine.nodes;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.icons.IconData;
import io.github.lightman314.lightmanscurrency.api.misc.icons.types.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.value.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;

import io.github.lightman314.lightmanscurrency.api.traders.trade.TradeContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.DummyTradeOfferNode;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.IItemExtractionFilter;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.IItemInsertionFilter;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.tabs.SlotMachineEntryTab;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.tabs.SlotMachinePriceTab;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineDummyTrade;
import io.github.lightman314.lightmanscurrency.common.traders.slot_machine.trade.SlotMachineEntry;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import io.github.lightman314.lightmanscurrency.util.OldDataHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

public class SlotMachineNode extends DummyTradeOfferNode<SlotMachineDummyTrade> implements IItemInsertionFilter, IItemExtractionFilter, IPersistentNode {

    private static final MapCodec<SlotMachineNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            IconData.CODEC.listOf().fieldOf("last_icons").forGetter(SlotMachineNode::getLastIcons),
            MoneyValue.CODEC.fieldOf("price").forGetter(SlotMachineNode::getPrice),
            SlotMachineEntry.CODEC.listOf().fieldOf("entries").forGetter(SlotMachineNode::getAllEntries)
    ).apply(builder,SlotMachineNode::new));

    public static final TraderNodeType<SlotMachineNode> TYPE = TraderNodeType.simple(SlotMachineNode::new,MAP_CODEC);

    private int nextSyncID = 0;
    public int getNextSyncID() { return this.nextSyncID++; }

    private final SlotMachineDummyTrade trade = new SlotMachineDummyTrade();

    private final NonNullList<IconData> lastIcons = SlotMachineEntry.createDefaultIcons();
    public List<IconData> getLastIcons() { return ImmutableList.copyOf(this.lastIcons); }
    public final void setLastIcons(List<IconData> icons) {
        this.lastIcons.clear();
        for(int i = 0; i < this.lastIcons.size() && i < icons.size();i++)
            this.lastIcons.set(i,icons.get(i));
        this.setChanged(builder -> builder.setList("last_icons",this.lastIcons,ModLazyPackets.ICON));
    }

    private MoneyValue price = MoneyValue.empty();
    public final MoneyValue getPrice() { return this.price; }
    public void setPrice(MoneyValue newValue) {
        this.price = newValue;
        this.setChanged(builder -> builder.setMoneyValue("price",this.price));
    }
    public final boolean isPriceValid() { return this.price.isFree() || !this.price.isEmpty(); }

    private final List<SlotMachineEntry> entries;
    public final List<SlotMachineEntry> getAllEntries() { return new ArrayList<>(this.entries); }
    public final List<SlotMachineEntry> getValidEntries() { return this.entries.stream().filter(SlotMachineEntry::isValid).toList(); }
    private boolean entriesChanged = false;
    public boolean areEntriesChanged() { return this.entriesChanged; }
    public void clearEntriesChangedCache() { this.entriesChanged = false; }
    public void addEntry() {
        if(this.entries.size() >= TraderData.GLOBAL_TRADE_LIMIT) return;
        this.entries.add(SlotMachineEntry.create(this));
        this.setEntryChanged(this.entries.getLast());
    }
    public void removeEntry(int entryIndex) {
        if(entryIndex < 0 || entryIndex >= this.entries.size())
            return;
        SlotMachineEntry entry = this.entries.remove(entryIndex);
        this.setEntryRemoved(entry);
    }
    public final double getTotalOdds() {
        double odds = 0;
        for(SlotMachineEntry entry : this.getValidEntries())
            odds += entry.getOdds();
        return odds;
    }
    public final double getFailOdds() { return Math.max(0d,100d - this.getTotalOdds()); }
    public final String getFailOddsText() { return SlotMachineEntry.ODDS_FORMATTER.format(this.getFailOdds()); }
    public final boolean hasValidOdds() {
        double totalOdds = this.getTotalOdds();
        return totalOdds > 0d && totalOdds <= 100d;
    }

    protected void setEntryRemoved(SlotMachineEntry entry)
    {
        final int syncID = entry.getSyncID();
        this.setChanged(builder ->
            builder.remove("edit_entry_" + syncID)
                    .setFlag("remove_entry_" + syncID));
    }

    public void setEntryChanged(SlotMachineEntry entry)
    {
        if(!this.entries.contains(entry))
            return; //Ignore if the entry isn't contained by this node
        this.entriesChanged = true;
        this.setChanged(builder -> builder.setCustom("edit_entry_" + entry.getSyncID(),entry,ModLazyPackets.SLOT_MACHINE_ENTRY));
    }

    public boolean isValidSetup()
    {
        return this.entries.stream().anyMatch(SlotMachineEntry::isValid) && this.isPriceValid() && this.hasValidOdds();
    }

    private SlotMachineNode() {
        this.entries = new ArrayList<>();
        this.entries.add(SlotMachineEntry.create());
    }
    private SlotMachineNode(List<IconData> lastIcons,MoneyValue price,List<SlotMachineEntry> entries)
    {
        for(int i = 0;i < this.lastIcons.size() && i < lastIcons.size(); ++i)
            this.lastIcons.set(i,lastIcons.get(i));
        this.price = price;
        this.entries = new ArrayList<>(entries);
    }

    protected void afterEntryLoad(SlotMachineEntry entry) { entry.initialize(this); }

    private void afterEntryLoad(List<SlotMachineEntry> entries)
    {
        for(SlotMachineEntry entry : entries)
            this.afterEntryLoad(entry);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setList("last_icons",this.lastIcons,ModLazyPackets.ICON)
                .setMoneyValue("price",this.price)
                .setList("entries",this.entries,ModLazyPackets.SLOT_MACHINE_ENTRY);
    }

    @Override
    protected SlotMachineDummyTrade getTrade() { return this.trade; }

    @Override
    public void onDataSync(LazyPacketData data) {
        //Don't need to call super data changed shenanigans
        if(data.contains("last_icons"))
        {
            List<IconData> list = data.getList("last_icons",ModLazyPackets.ICON);
            this.lastIcons.clear();
            for(int i = 0; i < this.lastIcons.size() && i < list.size(); ++i)
                this.lastIcons.set(i,list.get(i));
        }
        if(data.contains("price"))
            this.price = data.getMoneyValue("price");
        if(data.contains("entries")) {
            this.entries.clear();
            this.entries.addAll(data.getList("entries", ModLazyPackets.SLOT_MACHINE_ENTRY));
            this.afterEntryLoad(this.entries);
        }
        for(String key : data.keySet())
        {
            if(key.startsWith("edit_entry_"))
            {
                int syncID = NumberUtil.GetIntegerValue(key.substring("edit_entry_".length()),-1);
                if(syncID >= 0)
                {
                    boolean found = false;
                    SlotMachineEntry entry = data.getCustom(key,ModLazyPackets.SLOT_MACHINE_ENTRY);
                    for(int i = 0; i < this.entries.size() && !found; ++i)
                    {
                        SlotMachineEntry e = this.entries.get(i);
                        if(e.getSyncID() == syncID)
                        {
                            this.entries.set(i,entry);
                            found = true;
                        }
                    }
                    if(!found)
                        this.entries.add(entry);
                    this.afterEntryLoad(entry);
                }
            }
            if(key.startsWith("remove_entry_"))
            {
                int syncID = NumberUtil.GetIntegerValue(key.substring("remove_entry_".length()),-1);
                if(syncID >= 0)
                {
                    boolean found = false;
                    for(int i = 0; i < this.entries.size() && !found; ++i)
                    {
                        SlotMachineEntry e = this.entries.get(i);
                        if(e.getSyncID() == syncID)
                        {
                            this.entries.remove(i);
                            found = true;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected Supplier<LazyPacketType<SlotMachineDummyTrade>> getPacketType() { return ModLazyPackets.SLOT_MACHINE_DUMMY; }

    @Override
    @SuppressWarnings("deprecation")
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        this.loadOldLastIcons(tag,lookup);
        if(tag.contains("Entries"))
        {
            this.entries.clear();
            List<Integer> deprecatedWeights = new ArrayList<>();
            ListTag list = tag.getList("Entries", Tag.TAG_COMPOUND);
            for(int i = 0; i < list.size(); ++i)
            {
                CompoundTag e = list.getCompound(i);
                this.entries.add(SlotMachineEntry.loadOldData(e,lookup));
                if(e.contains("Weight"))
                    deprecatedWeights.add(e.getInt("Weight"));
            }
            if(!deprecatedWeights.isEmpty() && deprecatedWeights.size() == this.entries.size())
            {
                int totalWeight = 0;
                for(Integer w : deprecatedWeights)
                    totalWeight += w;
                if(totalWeight > 0)
                {
                    for(int i = 0; i < deprecatedWeights.size(); ++i)
                        this.entries.get(i).setOdds(((double)deprecatedWeights.get(i)/(double)totalWeight) * 100d);
                }
            }
            this.entriesChanged = true;
        }
        if(tag.contains("Price"))
            this.price = MoneyValue.safeLoad(tag, "Price");
    }

    @Deprecated
    private void loadOldLastIcons(CompoundTag compound, HolderLookup.Provider lookup)
    {
        if(compound.contains("LastReward"))
        {
            List<ItemStack> lastReward = new ArrayList<>();
            ListTag itemList = compound.getList("LastReward", Tag.TAG_COMPOUND);
            for(int i = 0; i < itemList.size(); ++i)
            {
                ItemStack stack = OldDataHelper.loadItem(itemList.getCompound(i),lookup);
                if(!stack.isEmpty())
                    lastReward.add(stack);
            }
            lastReward = SlotMachineEntry.splitDisplayItems(lastReward);
            for(int i = 0; i < lastReward.size() && i < this.lastIcons.size(); ++i)
                this.lastIcons.set(i, ItemIcon.ofItem(lastReward.get(i)));
        }
        if(compound.contains("LastIcons"))
            TagUtil.readIconList(this.lastIcons,compound.getList("LastIcons",Tag.TAG_COMPOUND),lookup,SlotMachineEntry.DEFAULT_ICON);
    }

    @Nullable
    public final SlotMachineEntry getRandomizedEntry(TradeContext context)
    {
        Level level;
        if(context.hasPlayer())
            level = context.getPlayer().level();
        else
        {
            try{ level = LightmansCurrency.getProxy().safeGetDummyLevel();
            } catch(Throwable t) {
                LightmansCurrency.LogError("Could not get a valid level from the trade's context or the proxy. Will have to use Java randomizer");
                return this.getRandomizedEntry(new Random().nextDouble());
            }
        }
        return this.getRandomizedEntry(level.random.nextDouble());
    }

    @Nullable
    private SlotMachineEntry getRandomizedEntry(double rand)
    {
        //Multiply by 100 to make it a percentage to match the entries "odds" value
        rand = rand * 100d;
        for(SlotMachineEntry entry : this.getValidEntries())
        {
            rand -= entry.getOdds();
            if(rand < 0)
                return entry;
        }
        return null;
    }

    @Override
    public boolean isItemRelevant(ItemStack stack) {
        for(SlotMachineEntry entry : new ArrayList<>(this.entries))
        {
            if(entry.isItemRelevant(stack))
                return true;
        }
        return false;
    }

    @Override
    public boolean allowExtraction(ItemStack stack) { return !this.isItemRelevant(stack); }

    @Override
    public void applyStorageTabs(ITraderStorageMenu menu) {
        menu.addTab(new SlotMachineEntryTab(menu));
        menu.addTab(new SlotMachinePriceTab(menu));
    }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {

    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("Price"))
            this.price = MoneyValue.LENIENT_NON_EMPTY_CODEC.decode(context.ops(),json.get("Price")).getOrThrow(JsonSyntaxException::new).getFirst();
        else
            throw new JsonSyntaxException("Expected a 'Price' entry!");

        this.entries.clear();
        JsonArray entryList = GsonHelper.getAsJsonArray(json, "Entries");
        for(int i = 0; i < entryList.size(); ++i)
        {
            try{
                this.entries.add(SlotMachineEntry.parse(GsonHelper.convertToJsonObject(entryList.get(i), "Entries[" + i + "]"),context));
            } catch(JsonSyntaxException | ResourceLocationException t) { LightmansCurrency.LogError("Error parsing Slot Machine Trader Entry #" + (i + 1), t); }
        }
        if(this.entries.isEmpty())
            throw new JsonSyntaxException("Slot Machine Trader had no valid Entries!");
    }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) {
        CompoundTag tag = new CompoundTag();
        tag.put("last_icons",IconData.CODEC.listOf().encodeStart(context.ops(),this.lastIcons).getOrThrow());
        return tag;
    }

    @Override
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) {
        if(tag.contains("LastIcons"))
            this.loadOldLastIcons(tag,context.registryAccess());
        else if(tag.contains("last_icons"))
            this.setLastIcons(IconData.CODEC.listOf().decode(context.ops(),tag.get("last_icons")).getOrThrow().getFirst());
    }

    public final List<Component> getSlotMachineInfo()
    {
        List<Component> tooltips = new ArrayList<>();
        //Return undefined info if not yet defined
        if(!this.isValidSetup())
        {
            tooltips.add(LCText.TOOLTIP_SLOT_MACHINE_UNDEFINED.get().withStyle(ChatFormatting.RED));
            return tooltips;
        }

        if(!this.trade.hasStock(TradeContext.createStorageMode(this.trader)))
            tooltips.add(LCText.TOOLTIP_OUT_OF_STOCK.get().withStyle(ChatFormatting.RED));

        return tooltips;
    }

}
