package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.events.TradeEvent;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.ITradeListener;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;

public class InteractionNode extends SyncedTraderNode implements ITradeListener, IPersistentNode {

    private static final MapCodec<InteractionNode> MAP_CODEC = Codec.LONG.fieldOf("lastInteraction")
            .xmap(InteractionNode::new,InteractionNode::getLastInteractionTime);

    public static final TraderNodeType<InteractionNode> TYPE = TraderNodeType.simple(InteractionNode::new,MAP_CODEC);

    private long lastInteraction = 0;
    public long getLastInteractionTime() { return this.lastInteraction; }
    public void setLastInteractionTime(long newValue) {
        this.lastInteraction = newValue;
        this.setChanged(builder -> builder.setLong("interaction_time",this.lastInteraction));
    }

    private InteractionNode() {}
    private InteractionNode(long lastInteraction) { this.lastInteraction = lastInteraction; }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setLong("interaction_time",this.lastInteraction);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("interaction_time"))
            this.lastInteraction = data.getLong("interaction_time");
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("LastInteraction"))
            this.lastInteraction = tag.getLong("LastInteraction");
    }

    @Override
    public void afterTrade(TradeEvent.PostTradeEvent event) {
        this.lastInteraction = TimeUtil.getCurrentTime();
        this.setChanged(builder -> builder.setLong("last_interaction",this.lastInteraction));
    }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) { }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException { }

    @Nullable
    @Override
    public CompoundTag writePersistentTag(DataContext<Tag> context) {
        if(this.lastInteraction > 0)
        {
            CompoundTag tag = new CompoundTag();
            tag.putLong("LastInteraction",this.lastInteraction);
            return tag;
        }
        return null;
    }

    @Override
    public void readPersistentTag(CompoundTag tag, DataContext<Tag> context) {
        this.lastInteraction = tag.getLong("LastInteraction");
    }

}
