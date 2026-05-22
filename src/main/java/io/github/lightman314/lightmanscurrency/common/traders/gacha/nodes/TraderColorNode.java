package io.github.lightman314.lightmanscurrency.common.traders.gacha.nodes;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IColoredBlock;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.WorldStateNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.BlockItem;

import javax.annotation.Nullable;

public class TraderColorNode extends SyncedTraderNode implements IPersistentNode {

    private static final MapCodec<TraderColorNode> MAP_CODEC = Codec.INT.fieldOf("color")
            .xmap(TraderColorNode::new,TraderColorNode::getColor);
    public static final TraderNodeType<TraderColorNode> TYPE = TraderNodeType.advanced(TraderColorNode::factory,MAP_CODEC);

    private int color;
    public int getColor() {
        if(this.color < 0 && this.trader != null)
        {
            WorldStateNode node = this.trader.getNode(WorldStateNode.TYPE);
            if(node != null && node.getTraderBlock() instanceof BlockItem bi && bi.getBlock() instanceof IColoredBlock cb)
                this.setColor(cb.getBlockColor());
            else
                this.setColor(0xFFFFFF);
        }
        return this.color;

    }
    public void setColor(int color) { this.color = color; this.setChanged(builder -> builder.setInt("color",this.color)); }

    private TraderColorNode(int color) { this.color = color; }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,ISyncingContext context) {
        builder.setInt("color",this.color);
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("color"))
            this.color = data.getInt("color");
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("Color"))
            this.color = tag.getInt("Color");
    }

    private static TraderColorNode factory(@Nullable Object argument) {
        if(argument instanceof Number n)
            return new TraderColorNode(n.intValue());
        return new TraderColorNode(-1);
    }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        json.addProperty("Color",this.color);
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        this.color = GsonHelper.getAsInt(json,"Color",0xFFFFFF);
    }

}
