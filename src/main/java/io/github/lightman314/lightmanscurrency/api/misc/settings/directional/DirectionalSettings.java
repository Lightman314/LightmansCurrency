package io.github.lightman314.lightmanscurrency.api.misc.settings.directional;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.Map;
import java.util.function.IntFunction;

public class DirectionalSettings {

    public static final Codec<DirectionalSettings> CODEC = Codec.unboundedMap(Direction.CODEC,DirectionalSettingsState.CODEC)
            .xmap(DirectionalSettings::new,s -> s.data);
    public static final StreamCodec<ByteBuf,DirectionalSettings> STREAM_CODEC = ByteBufCodecs.map((IntFunction<Map<Direction,DirectionalSettingsState>>)HashMap::new,Direction.STREAM_CODEC,DirectionalSettingsState.STREAM_CODEC)
            .map(DirectionalSettings::new,s -> s.data);

    private IDirectionalSettingsHolder parent;
    private final Map<Direction,DirectionalSettingsState> data = new HashMap<>();

    private Runnable listener = () -> {};

    public DirectionalSettings() {}
    public DirectionalSettings(IDirectionalSettingsHolder parent) { this.parent = parent; }
    private DirectionalSettings(Map<Direction,DirectionalSettingsState> data) { this.data.putAll(data); }

    public DirectionalSettings withParent(IDirectionalSettingsHolder parent) {
        this.parent = parent;
        for(Direction ignored : this.parent.getIgnoredSides())
            this.data.remove(ignored);
        return this;
    }

    public DirectionalSettings withListener(Runnable listener) { this.listener = listener; return this; }

    public DirectionalSettingsState getState(Direction side) { return this.parent.getIgnoredSides().contains(side) ? DirectionalSettingsState.NONE : this.data.getOrDefault(side,DirectionalSettingsState.NONE); }
    public void setState(Direction side,DirectionalSettingsState state) {
        if(this.parent.getIgnoredSides().contains(side))
            return;
        this.data.put(side,state);
        this.listener.run();
    }

    public boolean allowInputs(Direction side) { return this.getState(side).allowsInputs(); }
    public boolean allowOutputs(Direction side) { return this.getState(side).allowsOutputs(); }

    public boolean hasInput() {
        for(Direction side : Direction.values())
        {
            if(this.allowInputs(side))
                return true;
        }
        return false;
    }

    public void save(CompoundTag compound, String tag) {
        compound.put(tag,CODEC.encodeStart(NbtOps.INSTANCE,this).getOrThrow());
    }

    @Deprecated(forRemoval = true)
    public void loadOldData(CompoundTag compound, String tag) {
        if(!compound.contains(tag))
            return;
        this.data.clear();
        ListTag list = compound.getList(tag, Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); ++i)
        {
            CompoundTag entry = list.getCompound(i);
            Direction side = EnumUtil.enumFromString(entry.getString("Side"),Direction.values(),null);
            DirectionalSettingsState state = DirectionalSettingsState.parse(entry.getString("State"));
            if(side != null && state != DirectionalSettingsState.NONE)
                this.data.put(side,state);
            else if(side == null)
                LightmansCurrency.LogWarning("Could not properly parse '" + entry.getString("Side") + "' as a valid side!");
        }
    }

    public void copy(DirectionalSettings other) {
        for(Direction side : Direction.values())
        {
            if(this.parent.getIgnoredSides().contains(side))
                continue;
            this.setState(side,other.getState(side));
        }
    }

    public void clear() { this.data.clear(); }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder("DirectionalSettings[");
        boolean notFirst = false;
        for(Direction side : Direction.values())
        {
            if(this.parent.getIgnoredSides().contains(side))
                continue;
            DirectionalSettingsState state = this.getState(side);
            if(state != DirectionalSettingsState.NONE)
            {
                if(notFirst)
                    builder.append(",");
                else
                    notFirst = true;
                builder.append(side).append(':').append(state);
            }
        }
        return builder.append("]").toString();
    }
}
