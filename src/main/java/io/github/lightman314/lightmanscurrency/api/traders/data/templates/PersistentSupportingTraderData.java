package io.github.lightman314.lightmanscurrency.api.traders.data.templates;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderState;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.NodeCollector;
import io.github.lightman314.lightmanscurrency.api.traders.data.interfaces.IPersistentTrader;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin.PersistentDataNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Extension of {@link NetworkSupportingTraderData} that also extends the {@link IPersistentTrader} interface and implements all of its methods<br>
 * Write and read more data
 */
public abstract class PersistentSupportingTraderData extends NetworkSupportingTraderData implements IPersistentTrader {

    //Creation from Type
    protected PersistentSupportingTraderData() { }
    protected PersistentSupportingTraderData(Map<TraderNodeType<?>,Object> args) { super(args); }

    //Creation from Block Entity
    protected PersistentSupportingTraderData(boolean alwaysNetwork,Level level, BlockPos pos) { this(new HashMap<>(),alwaysNetwork,level,pos); }
    protected PersistentSupportingTraderData(Map<TraderNodeType<?>,Object> args, boolean alwaysNetwork,Level level, BlockPos pos) { super(args,alwaysNetwork,level,pos); }

    //Loading from Codec
    protected PersistentSupportingTraderData(long id, Map<TraderNodeType<?>, TraderNode> nodes) { this(new HashMap<>(),id,nodes); }
    protected PersistentSupportingTraderData(Map<TraderNodeType<?>,Object> args, long id, Map<TraderNodeType<?>, TraderNode> nodes) { super(args,id, nodes); }

    @Override
    public void addDefaultNodes(NodeCollector collector) {
        super.addDefaultNodes(collector);
        collector.addNode(PersistentDataNode.TYPE);
    }

    @Override
    public boolean shouldValidateRules() { return !this.isPersistent(); }

    @Override
    public boolean isPersistent() { return this.findNodeValue(PersistentDataNode.TYPE,PersistentDataNode::isPersistent,false); }

    @Override
    public String getPersistentID() { return this.findNodeValue(PersistentDataNode.TYPE,PersistentDataNode::getPersistentID,""); }

    @Override
    public void makePersistent(long id, String persistentID) {
        this.setID(id);
        PersistentDataNode node = this.getNode(PersistentDataNode.TYPE);
        if(node == null)
            throw new IllegalStateException("Cannot make the trader persistent as it is missing its 'PerisistentDataNode'!");
        node.setPersistentID(persistentID);
        //Make Creative
        //Don't need to make creative, as the PersistentDataNode will now flag the trader as having infinite stock
        this.setState(TraderState.PERSISTENT);
    }

    @Override
    public JsonObject writePersistentJson(String id, String name, DataContext<JsonElement> context) {
        JsonObject json = new JsonObject();
        for(TraderNode node : this.getNodeIterable())
        {
            if(node instanceof IPersistentNode pn)
                pn.writePersistentData(json,context,id,name);
        }
        //Define the Type and ID last
        json.addProperty("Type",LCRegistries.TRADER_TYPES.getKey(this.getType()).toString());
        return json;
    }

    @Override
    public void readPersistentJson(JsonObject json, DataContext<JsonElement> context) {
        for(TraderNode node : this.getNodeIterable())
        {
            if(node instanceof IPersistentNode pn)
                pn.loadPersistentData(json,context);
        }
    }

    @Override
    public CompoundTag savePersistentData(DataContext<Tag> context) {
        CompoundTag tag = new CompoundTag();
        for(TraderNode node : this.getNodeIterable())
        {
            if(node instanceof IPersistentNode pn)
            {
                CompoundTag entry = pn.writePersistentTag(context);
                if(entry != null)
                    tag.put(LCRegistries.TRADER_NODE.getKey(node.getType()).toString(),entry);
            }
        }
        //Version Flag to tell future me that this is 2.4.0.0+ data
        tag.putInt("version",1);
        return tag;
    }

    @Override
    public void loadPersistentData(CompoundTag compound, DataContext<Tag> context) {
        int version = compound.getInt("version");
        for(TraderNode node : this.getNodeIterable())
        {
            if(node instanceof IPersistentNode pn)
            {
                CompoundTag entry = null;
                String key = LCRegistries.TRADER_NODE.getKey(node.getType()).toString();
                if(compound.contains(key))
                    entry = compound.getCompound(key);
                //If loading from an older version, provide the base tag instead of the tag exclusive for that node
                if(version < 1 && entry == null)
                    entry = compound;
                if(entry != null)
                {
                    try {
                        pn.readPersistentTag(entry,context);
                    } catch (IllegalStateException e) {
                        LightmansCurrency.LogError("Error loading persistent tag: ",e);
                    }
                }
            }
        }
    }

}
