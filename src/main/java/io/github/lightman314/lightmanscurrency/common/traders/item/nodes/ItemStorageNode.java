package io.github.lightman314.lightmanscurrency.common.traders.item.nodes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.UpgradesNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IContentProvider;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IUpgradeHandler;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.IItemInsertionFilter;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.api.upgrades.types.CapacityUpgrade;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class ItemStorageNode extends SyncedTraderNode implements IUpgradeHandler, IPersistentNode, IContentProvider {

    private static final MapCodec<ItemStorageNode> MAP_CODEC = TraderItemStorage.CODEC.fieldOf("storage")
            .xmap(ItemStorageNode::new,ItemStorageNode::getStorage);

    public static final TraderNodeType<ItemStorageNode> TYPE = TraderNodeType.simple(ItemStorageNode::new,MAP_CODEC);

    private TraderItemStorage storage;
    public TraderItemStorage getStorage() { return this.storage; }

    private ItemStorageNode() { this(new TraderItemStorage()); }
    private ItemStorageNode(TraderItemStorage storage)
    {
        this.storage = storage.withFilter(this::allowItem)
                .withStorageLimit(this::getStorageCapacity)
                .withListener(this::setStorageChanged);
    }

    public void setStorageChanged()
    {
        this.setChanged(builder -> builder.setList("storage",this.storage.getContents(),ModLazyPackets.ITEM_STACK));
    }

    protected boolean allowItem(ItemStack item)
    {
        for(TraderNode node : this.trader.getNodeIterable())
        {
            if(node instanceof IItemInsertionFilter filter && filter.isItemRelevant(item))
                return true;
        }
        return false;
    }

    public int getStorageCapacity()
    {
        int limit = ItemTraderData.DEFAULT_STACK_LIMIT;
        UpgradesNode node = this.trader.getNode(UpgradesNode.TYPE);
        if(node == null)
            return limit;
        return limit + CapacityUpgrade.getBonusCapacity(node.getContainer(),Upgrades.ITEM_CAPACITY);
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder, Player player) {
        builder.setList("storage",this.storage.getContents(),ModLazyPackets.ITEM_STACK);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("storage"))
            this.storage.load(data.getList("storage",ModLazyPackets.ITEM_STACK));
    }

    @Override
    @SuppressWarnings("deprecation")
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        DataContext<Tag> context = DataContext.createNBT(lookup);
        //Item Traders called it "Item Storage"
        this.storage.load(tag,"ItemStorage",context);
        //Slot Machine simply called it "Storage"
        this.storage.load(tag,"Storage",context);
    }

    @Override
    public boolean allowUpgrade(UpgradeType upgrade) { return upgrade == Upgrades.ITEM_CAPACITY; }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        ItemTradeNode tradeNode = this.trader.getNode(ItemTradeNode.TYPE);
        if(tradeNode == null)
            return;
        //Save relevant storage (for sales that have randomized items to output)
        List<ItemStack> itemsToWrite = new ArrayList<>();
        List<ItemTradeData> trades = tradeNode.getAllTrades();
        for(ItemStack item : this.storage.getContents())
        {
            boolean shouldWrite = false;
            for(int i = 0; i < trades.size() && !shouldWrite; ++i)
            {
                ItemTradeData trade = trades.get(i);
                if(trade.isValid() && trade.shouldStorageItemBeSaved(item))
                    shouldWrite = true;
            }
            if(shouldWrite)
                itemsToWrite.add(item);
        }
        if(!itemsToWrite.isEmpty())
            json.add("RelevantStorage", CodecHelper.UNLIMITED_ITEM_LIST.encodeStart(context.ops(),itemsToWrite).getOrThrow());
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        if(json.has("RelevantStorage"))
            this.storage = new TraderItemStorage.LockedTraderStorage(CodecHelper.UNLIMITED_ITEM_LIST.decode(context.ops(),json.get("RelevantStorage")).getOrThrow(JsonSyntaxException::new).getFirst());
    }

    @Override
    public List<ItemStack> getContents() {
        return this.storage.getSplitContents();
    }

}
