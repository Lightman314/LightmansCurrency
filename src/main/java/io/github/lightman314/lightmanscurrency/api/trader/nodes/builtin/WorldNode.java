package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderState;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.world.data.WorldPosition;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Optional;

public class WorldNode extends SimpleSyncedNode {

    private static final MapCodec<WorldNode> MAP_CODEC = RecordCodecBuilder.mapCodec(buider -> buider.group(
            TraderState.CODEC.fieldOf("state").forGetter(WorldNode::getState),
            WorldPosition.CODEC.fieldOf("pos").forGetter(WorldNode::getPosition),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block").forGetter(n -> n.block)
    ).apply(buider,WorldNode::new));
    public static final TraderNodeType<WorldNode> TYPE = TraderNodeType.simple(WorldNode::new,MAP_CODEC);

    private TraderState state = TraderState.NORMAL;
    public TraderState getState() { return this.state; }
    public void setState(TraderState state) {
        if(this.state == state)
            return;
        this.state = state;
        this.setChanged(builder -> builder.setEnum("state",this.state));
    }
    public void setNormalState() {
        //If the current world position is null/void, then flag the state as "moved by machine" as we have no idea where it is
        //Otherwise it's just the "normal" state
        this.setState(this.position.isVoid() ? TraderState.MOVED_BY_MACHINE : TraderState.NORMAL);
    }

    private WorldPosition position = WorldPosition.VOID;
    public WorldPosition getPosition() { return this.position; }
    public void setPosition(WorldPosition position) {
        if(this.position.equals(position))
            return;
        this.position = position;
        this.setChangedNoPacket();
    }
    private Optional<Block> block = Optional.empty();
    public Block getBlock() { return this.block.orElse(Blocks.AIR); }

    private WorldNode() {}
    private WorldNode(TraderState state, WorldPosition position, Optional<Block> block)
    {
        this.position = position;
        this.block = block;
    }

    @Override
    public void updateArgument(Optional<Object> argument) {
        if(argument.isPresent())
        {
            Object arg = argument.get();
            if(arg instanceof TraderBlockEntity be)
            {
                this.position = be.getPosition();
                this.block = Optional.of(be.getBlockState().getBlock());
            }
            else if(arg instanceof BlockEntity be)
            {
                this.position = WorldPosition.ofBE(be);
                this.block = Optional.of(be.getBlockState().getBlock());
            }
        }
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.setEnum("state",this.state)
                .set("position",LCFancyPacketTypes.WORLD_POSITION,this.position);
        if(this.block.isPresent())
            builder.setRegistryEntry("block",BuiltInRegistries.BLOCK,this.block.get());
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        if(data.contains("state"))
            this.state = data.getEnum("state",TraderState.class,this.state);
        if(data.contains("position"))
            this.position = data.get("position",LCFancyPacketTypes.WORLD_POSITION,this.position);
        if(data.contains("block"))
            this.block = Optional.of(data.getRegistryEntry("block",BuiltInRegistries.BLOCK));
    }

}
