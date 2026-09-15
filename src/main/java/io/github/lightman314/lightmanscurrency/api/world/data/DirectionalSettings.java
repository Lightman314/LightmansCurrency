package io.github.lightman314.lightmanscurrency.api.world.data;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.debug.DebugHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class DirectionalSettings {

    public static final Codec<DirectionalSettings> CODEC = Codec.unboundedMap(Direction.CODEC,DirectionalSettingsState.CODEC)
            .xmap(DirectionalSettings::new,DirectionalSettings::getNonDefaultData);
    public static final StreamCodec<ByteBuf,DirectionalSettings> STREAM_CODEC = ByteBufCodecs.map(HashMap::new,Direction.STREAM_CODEC,DirectionalSettingsState.STREAM_CODEC)
            .map(DirectionalSettings::new,s -> s.data);

    private IDirectionalSettingsHolder parent = IDirectionalSettingsHolder.DEFAULT;
    public DirectionalSettings withParent(IDirectionalSettingsHolder parent) {
        this.parent = parent;
        this.populateMap();
        return this;
    }

    private Consumer<Consumer<FancyPacketMap.Mutable>> listener = m -> {};
    public DirectionalSettings withListener(Runnable listener) { return this.withListener(m -> listener.run()); }
    public DirectionalSettings withListener(Consumer<Consumer<FancyPacketMap.Mutable>> listener) {
        this.listener = listener;
        return this;
    }
    public FancyPacketMap asPacket() {
        FancyPacketMap.Mutable packet = FancyPacketMap.map();
        for(Direction side : this.data.keySet())
            packet.setEnum(side.getSerializedName(),this.getState(side));
        return packet;
    }
    public void handlePacket(FancyPacketMap packet) {
        for(String key : packet.keySet()) {
            Direction side = EnumHelper.enumFromString(key,Direction.values(),null);
            if(side != null && !this.parent.getIgnoredSides().contains(side))
                this.data.put(side,packet.getEnum(key,DirectionalSettingsState.class,DirectionalSettingsState.NONE));
        }
    }

    private void populateMap() {
        List<Direction> ignored = this.parent.getIgnoredSides();
        for(Direction side : Direction.values()) {
            if(ignored.contains(side))
                this.data.remove(side);
            else
                this.data.putIfAbsent(side,DirectionalSettingsState.NONE);
        }
    }

    private final HashMap<Direction,DirectionalSettingsState> data;
    private Map<Direction,DirectionalSettingsState> getNonDefaultData() {
        Map<Direction,DirectionalSettingsState> result = new HashMap<>();
        for(Direction side : this.data.keySet()) {
            DirectionalSettingsState state = this.data.get(side);
            if(state != DirectionalSettingsState.NONE)
                result.put(side,state);
        }
        return result;
    }

    public DirectionalSettings() { this(new HashMap<>()); }
    private DirectionalSettings(Map<Direction,DirectionalSettingsState> data) { this.data = new HashMap<>(data); }

    public DirectionalSettingsState getState(Direction side) { return this.data.getOrDefault(side,DirectionalSettingsState.NONE); }
    public void setState(Direction side,DirectionalSettingsState state) {
        if(this.parent.getIgnoredSides().contains(side))
            return;
        this.data.put(side,state);
        this.listener.accept(m -> m.setEnum(side.getSerializedName(),state));
    }

    public boolean allowInputs(Direction side) { return this.getState(side).allowsInputs(); }
    public boolean allowOutputs(Direction side) { return this.getState(side).allowsOutputs(); }

    public boolean hasInput() { return Direction.stream().anyMatch(this::allowInputs); }
    public boolean hasOutput() { return Direction.stream().anyMatch(this::allowOutputs); }

    @Override
    public String toString() { return "DirectionalSettings[" + DebugHelper.debugMap(this.getNonDefaultData(),",",":") + "]"; }

}