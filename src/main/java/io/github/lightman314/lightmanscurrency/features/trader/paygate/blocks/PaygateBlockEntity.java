package io.github.lightman314.lightmanscurrency.features.trader.paygate.blocks;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerClient;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ITickerServer;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.core.LCBlockEntities;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCTraderTypes;
import io.github.lightman314.lightmanscurrency.features.trader.paygate.OutputConflictHandling;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.redstone.Redstone;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class PaygateBlockEntity extends TraderBlockEntity.SingleType implements ITickerServer, ITickerClient {

    public PaygateBlockEntity(BlockPos worldPosition, BlockState blockState) { this(LCBlockEntities.PAYGATE.get(),worldPosition,blockState); }
    protected PaygateBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    private final Map<Direction,SidedData> data = new HashMap<>();

    private final Map<Direction,Integer> simplifiedData = new HashMap<>();

    @Override
    protected TraderType getTraderType() { return LCTraderTypes.PAYGATE.get(); }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ValueOutput dataOutput = output.child("data");
        this.data.forEach((side,data) -> dataOutput.store(side.getSerializedName(),SidedData.CODEC,data));
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.child("data").ifPresent(dataInput -> {
            this.data.clear();
            for(String key : dataInput.keySet()) {
                Direction side = EnumHelper.stringRepresentableFromString(key,Direction.values(),null);
                if(side != null) {
                    dataInput.read(key,SidedData.CODEC)
                            .ifPresent(sd -> this.data.put(side,sd));
                }
            }
        });
    }

    @Override
    public void writeInitialPacket(FancyPacketMap.Mutable packet, Player player) {
        super.writeInitialPacket(packet, player);
        FancyPacketMap.Mutable data = FancyPacketMap.map();
        this.data.forEach((side,entry) ->
            data.setInt(side.getSerializedName(),entry.timer));
        if(!data.isEmpty())
            packet.setMap("data",data);
    }

    @Override
    public void handleSyncPacket(FancyPacketMap packet) {
        super.handleSyncPacket(packet);
        if(packet.contains("data")) {
            FancyPacketMap data = packet.getMap("data");
            for(String key : data.keySet()) {
                Direction side = EnumHelper.stringRepresentableFromString(key,Direction.values(),null);
                if(side != null)
                    this.simplifiedData.put(side,data.getInt(key));
            }
        }
    }

    public boolean isActive() { return this.isClient() ? !this.simplifiedData.isEmpty() : !this.data.isEmpty(); }
    public Set<Direction> getActiveSides() { return this.isClient() ? Set.copyOf(this.simplifiedData.keySet()) : Set.copyOf(this.data.keySet()); }
    public int getTimeRemaining(DirectionalSettings sides) {
        AtomicInteger time = new AtomicInteger(0);
        if(this.isClient()) {
            this.simplifiedData.forEach((side,timer) -> {
                if(sides.allowOutputs(side))
                    time.set(Math.max(time.get(),timer));
            });
        }
        else {
            this.data.forEach((side,data) -> {
                if(sides.allowOutputs(side))
                    time.set(Math.max(time.get(),data.timer));
            });
        }
        return time.get();
    }

    public int getPowerLevel(Direction relativeSide) {
        if(this.data.containsKey(relativeSide))
            return this.data.get(relativeSide).power;
        return Redstone.SIGNAL_NONE;
    }

    public void activate(int duration,int level,DirectionalSettings outputSides,OutputConflictHandling conflictHandling,String name) {
        FancyPacketMap.Mutable packet = FancyPacketMap.map();
        for(Direction side : Direction.values()) {
            if(outputSides.allowOutputs(side)) {
                int newTime = duration;
                if(this.data.containsKey(side) && conflictHandling == OutputConflictHandling.ADD_TIME)
                    newTime += this.data.get(side).timer;
                this.data.put(side,new SidedData(level,newTime,name));
                packet.setInt(side.getSerializedName(),newTime);
            }
        }
        //Update block state last as we need all data saved to the BE befor enotifying the neighbors about the change
        //Flag of two as we don't want to update neighbors with this change
        this.level.setBlock(this.worldPosition,this.getBlockState().setValue(PaygateBlock.POWERED,true),Block.UPDATE_CLIENTS);
        //Manually update neighbors here so that they'll always be updated even if the "powered" state didn't change
        this.level.updateNeighborsAt(this.worldPosition,this.getBlockState().getBlock());
        //Send update packet
        this.setChanged();
        this.sendPacket(FancyPacketMap.map().setMap("data",packet));
    }

    @Override
    public void clientTick() {
        //Also tick on the client so that we don't need to sync the timer every tick
        for(Direction side : Set.copyOf(this.simplifiedData.keySet())) {
            this.simplifiedData.compute(side,(s,t) -> t - 1);
            if(this.simplifiedData.get(side) <= 0)
                this.simplifiedData.remove(side);
        }
    }

    @Override
    public void serverTick() {
        boolean changed = false;
        boolean removal = false;
        for(Direction side : Set.copyOf(this.data.keySet())) {
            changed = true;
            if(this.data.get(side).tickTimer()) {
                this.data.remove(side);
                removal = true;
            }
        }
        if(changed)
            this.setChanged();
        if(removal) {
            if(this.data.isEmpty()) //Update the block state to unpowered
                this.level.setBlock(this.worldPosition,this.getBlockState().setValue(PaygateBlock.POWERED,false),Block.UPDATE_CLIENTS);
            //Notify neighbors of the output state change
            this.level.updateNeighborsAt(this.worldPosition,this.getBlockState().getBlock());
        }
    }

    private static class SidedData {
        private static final Codec<SidedData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                Codec.intRange(1,Redstone.SIGNAL_MAX).fieldOf("power").forGetter(d -> d.power),
                Codec.INT.fieldOf("timer").forGetter(d -> d.timer),
                Codec.STRING.optionalFieldOf("name").forGetter(d -> d.name)
        ).apply(builder,SidedData::new));
        private final int power;
        private int timer;
        @Nullable
        private final Optional<String> name;
        private SidedData(int power,int timer,String name) { this(power,timer,name.isBlank() ? Optional.empty() : Optional.of(name)); }
        private SidedData(int power,int timer,Optional<String> name) {
            this.power = power;
            this.timer = timer;
            this.name = name;
        }
        boolean tickTimer() { return --this.timer <= 0; }
    }

    public record OutputVisibilityData(List<Direction> sides,Optional<String> name,int power,int timer) { }

    private record OutputDataKey(int power,int timer,String name) {}

}