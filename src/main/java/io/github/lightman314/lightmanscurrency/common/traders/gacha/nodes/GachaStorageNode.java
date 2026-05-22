package io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.menu.storage.ITraderStorageMenu;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.UpgradesNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IContentProvider;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IUpgradeHandler;
import io.github.lightman314.lightmanscurrency.api.upgrades.UpgradeType;
import io.github.lightman314.lightmanscurrency.common.blockentity.handler.GachaItemHandler;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.tabs.GachaStorageTab;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.upgrades.Upgrades;
import io.github.lightman314.lightmanscurrency.api.upgrades.types.CapacityUpgrade;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public class GachaStorageNode extends SyncedTraderNode implements IUpgradeHandler, IPersistentNode, IContentProvider {

    private static final MapCodec<GachaStorageNode> MAP_CODEC = CodecHelper.UNLIMITED_ITEM_LIST.fieldOf("storage")
            .xmap(GachaStorageNode::new,n -> n.getStorage().getContents());

    public static final TraderNodeType<GachaStorageNode> TYPE = TraderNodeType.simple(GachaStorageNode::new,MAP_CODEC);

    private final GachaStorage storage = new GachaStorage(this::getStorageCapacity).withListener(this::setStorageChanged);
    public GachaStorage getStorage() { return this.storage; }
    public IItemHandler getStorageWrapper() { return GachaItemHandler.getFullyAuthorizedHandler(this); }

    private GachaStorageNode() {}
    private GachaStorageNode(List<ItemStack> storage) {
        this.storage.load(storage);
    }

    public void setStorageChanged()
    {
        this.setChanged(builder -> builder.setList("storage",this.storage.getContents(),ModLazyPackets.ITEM_STACK));
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
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
        if(tag.contains("Storage"))
            this.storage.loadOldData(tag.getList("Storage", Tag.TAG_COMPOUND),lookup);
    }

    public int getStorageCapacity()
    {
        int limit = ItemTraderData.DEFAULT_STACK_LIMIT;
        UpgradesNode node = this.trader.getNode(UpgradesNode.TYPE);
        if(node == null)
            return limit;
        for(var entry : node.getContainer())
        {
            if(entry.getFirst() == Upgrades.ITEM_CAPACITY)
                limit += entry.getSecond().getIntValue(CapacityUpgrade.CAPACITY);
        }
        return limit;
    }

    @Override
    public boolean allowUpgrade(UpgradeType upgrade) { return upgrade == Upgrades.ITEM_CAPACITY; }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        json.add("Items", CodecHelper.UNLIMITED_ITEM_LIST.encodeStart(context.ops(),this.storage.getContents()).getOrThrow());
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        //Force the data to exist
        GsonHelper.getAsJsonArray(json,"Items");
        this.storage.load(CodecHelper.UNLIMITED_ITEM_LIST.decode(context.ops(),json.get("Items")).getOrThrow(JsonSyntaxException::new).getFirst());
    }

    @Override
    public List<ItemStack> getContents() { return this.storage.getSplitContents(); }

    @Override
    public void applyLateStorageTabs(ITraderStorageMenu menu) {
        //Add storage tab late so that it overrides the "Normal" basic trade tab
        menu.addTab(new GachaStorageTab(menu));
    }
}
