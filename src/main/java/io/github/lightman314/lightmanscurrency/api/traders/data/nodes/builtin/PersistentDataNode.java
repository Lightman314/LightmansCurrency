package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IBaseRuleModifier;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.INetworkController;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.interfaces.IPersistentNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

public class PersistentDataNode extends SyncedTraderNode implements IPersistentNode, INetworkController, IBaseRuleModifier {

    private static final MapCodec<PersistentDataNode> MAP_CODEC = Codec.STRING.optionalFieldOf("persistentID").xmap(PersistentDataNode::new,n -> n.persistentID);

    public static final TraderNodeType<PersistentDataNode> TYPE = TraderNodeType.simple(PersistentDataNode::new,MAP_CODEC);

    private PersistentDataNode() { }
    private PersistentDataNode(Optional<String> persistentID) { this.persistentID = persistentID; }

    private Optional<String> persistentID = Optional.empty();

    public boolean isPersistent() { return this.persistentID.isPresent(); }

    @Nullable
    public String getPersistentID() { return this.persistentID.orElse(null); }
    public void setPersistentID(@Nullable String id) { this.persistentID = Optional.ofNullable(id); this.setChanged(builder -> this.getPacket(builder,true)); }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder, Player player) {
        this.getPacket(builder,false);
    }

    private void getPacket(LazyPacketData.Builder builder,boolean change)
    {
        if(change || this.isPersistent())
            builder.setBoolean("persistent",this.isPersistent());
        if(this.isPersistent())
            builder.setString("persistentID",this.persistentID.orElse(""));
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("persistentID"))
        {
            if(data.getBoolean("persistentID"))
                this.persistentID = Optional.of("?");
            else
                this.persistentID = Optional.empty();
        }
    }

    //Nothing to load from old data
    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) { }

    @Override
    public void writePersistentData(JsonObject json, DataContext<JsonElement> context, String id, String ownerName) {
        json.addProperty("ID",id);
    }

    @Override
    public void loadPersistentData(JsonObject json, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
        this.persistentID = Optional.of(GsonHelper.getAsString(json,"ID"));
    }

    @Override
    public boolean visibleToNetwork() { return this.isPersistent(); }

    @Override
    public void hasInfiniteStock(AtomicBoolean result) {
        if(this.isPersistent())
            result.set(true);
    }

    @Override
    public void shouldStoreMoney(AtomicBoolean result) {
        if(this.isPersistent())
            result.set(false);
    }

    //Deny all permissions except for storage access if persistent
    @Override
    public boolean blockPermission(String permission) { return this.isPersistent() && !Objects.equals(permission,Permissions.OPEN_STORAGE); }

}
