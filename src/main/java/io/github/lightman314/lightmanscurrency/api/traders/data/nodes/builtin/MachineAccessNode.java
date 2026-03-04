package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.google.common.collect.ImmutableMap;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;

import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class MachineAccessNode extends SyncedTraderNode {

    private static final MapCodec<MachineAccessNode> MAP_CODEC = Codec.unboundedMap(Codec.STRING,EnumUtil.buildCodec(AccessLevel.class,"Access Level"))
            .fieldOf("access_rules")
            .xmap(MachineAccessNode::new,MachineAccessNode::getAccessLevels);

    public static final TraderNodeType<MachineAccessNode> TYPE = TraderNodeType.simple(MachineAccessNode::new,MAP_CODEC);

    public static final String EDIT_AUTHORIZATION_PERMISSION = "allowExternalAuthorization";

    private static final Codec<AccessLevel> ACCESS_LEVEL_CODEC = EnumUtil.buildCodec(AccessLevel.class,"Access Level");

    public enum AccessLevel { NONE, ALLY, ADMIN }

    private final Map<String,AccessLevel> accessLevels = new HashMap<>();
    public final Map<String,AccessLevel> getAccessLevels() { return ImmutableMap.copyOf(this.accessLevels); }

    private void setChanged()
    {
        this.setChanged(builder ->
                this.accessLevels.forEach(
                        (key,level) -> builder.setInt(key,level.ordinal())));
    }

    private MachineAccessNode() { super(); }
    private MachineAccessNode(Map<String,AccessLevel> map) { this.accessLevels.putAll(map); }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    //Only needed on the logical server unless the player is editing the data
    @Override
    public boolean isStorageOnly() { return true; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        this.accessLevels.forEach((key,l) -> builder.setInt(key,l.ordinal()));
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        this.accessLevels.clear();
        for(String key : data.keySet())
            this.accessLevels.put(key,EnumUtil.enumFromOrdinal(data.getInt(key),AccessLevel.values(),AccessLevel.NONE));
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        if(tag.contains("lightmanscurrency:external_authorization"))
        {
            this.accessLevels.clear();
            for(String key : tag.getAllKeys())
                this.accessLevels.put(key,EnumUtil.enumFromString(tag.getString(key),AccessLevel.values(),AccessLevel.NONE));
        }
    }

    public void flagAttemptedAccess(String id) {
        if(this.accessLevels.containsKey(id))
            return;
        this.accessLevels.put(id,AccessLevel.NONE);
        this.setChanged();
    }

    public List<String> getAttemptedAccessors() { return new ArrayList<>(this.accessLevels.keySet().stream().sorted(String::compareTo).toList()); }

    public AccessLevel getAccessLevel(@Nullable String id) {
        if(id == null)
            return AccessLevel.NONE;
        return this.accessLevels.getOrDefault(id,AccessLevel.NONE);
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(EDIT_AUTHORIZATION_PERMISSION,0);
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains(this.getType() + "-ChangeAuthorization"))
        {
            if(!this.trader.hasPermission(player,EDIT_AUTHORIZATION_PERMISSION))
                return;
            String entry = message.getString(this.getType() + "-ChangeAuthorization");
            AccessLevel newLevel = EnumUtil.enumFromOrdinal(message.getInt(this.getType() + "-NewLevel"),AccessLevel.values(),AccessLevel.NONE);
            if(this.getAccessLevel(entry) != newLevel)
            {
                this.accessLevels.put(entry,newLevel);
                this.setChanged();
            }
        }
    }

}
