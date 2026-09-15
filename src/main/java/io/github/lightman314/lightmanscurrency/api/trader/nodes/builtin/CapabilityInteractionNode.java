package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.world.data.IDirectionalSettingsObject;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class CapabilityInteractionNode extends SimpleSyncedNode implements IDirectionalSettingsObject, ISettingsMessageListener {

    private static final MapCodec<CapabilityInteractionNode> MAP_CODEC = DirectionalSettings.CODEC.fieldOf("settings")
            .xmap(CapabilityInteractionNode::new,n -> n.settings);

    public static final TraderNodeType<CapabilityInteractionNode> TYPE = TraderNodeType.simple(CapabilityInteractionNode::new,MAP_CODEC);

    private InputNodeData data = InputNodeData.DEFAULT;

    private final DirectionalSettings settings;

    private CapabilityInteractionNode() { this(new DirectionalSettings()); }
    private CapabilityInteractionNode(DirectionalSettings settings) {
        this.settings = settings.withParent(this)
                .withListener(m -> this.setChanged(m));
    }

    public static Supplier<DirectionalSettingsState> getSidedState(INodeAccess trader,Direction relativeSide) { return () -> trader.getNodeValue(CapabilityInteractionNode.TYPE,n -> n.getSidedState(relativeSide),DirectionalSettingsState.NONE); }

    @Override
    public void updateArgument(TraderArguments arguments) {
        arguments.get(TYPE).ifPresent(arg -> {
            if(arg instanceof InputNodeData d)
                this.data = d;
        });
    }

    @Override
    public boolean isStorageOnly() { return true; }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.merge(this.settings.asPacket());
    }

    @Override
    public void onDataSync(FancyPacketMap data) {
        this.settings.handlePacket(data);
    }

    @Nullable
    @Override
    public Block getDisplayBlock() { return this.getNodeValue(WorldNode.TYPE,WorldNode::getBlockOrNull); }
    @Override
    public DirectionalSettingsState getSidedState(Direction side) { return this.settings.getState(side); }

    @Override
    public void handleSettingsChange(Player player, FancyPacketMap message) {

    }

    public record InputNodeData(boolean allowInputs, boolean allowOutputs,List<Direction> ignoreSides) {
        public static InputNodeData DEFAULT = new InputNodeData(true,true,List.of());
        public InputNodeData allowInputs(boolean newState) { return new InputNodeData(newState,this.allowOutputs,this.ignoreSides); }
        public InputNodeData allowOutputs(boolean newState) { return new InputNodeData(this.allowInputs,newState,this.ignoreSides); }

        public InputNodeData ignoreSides(Direction... sides) { return this.ignoreSides(List.of(sides)); }
        public InputNodeData ignoreSides(List<Direction> sides) { return new InputNodeData(this.allowInputs,this.allowOutputs,addToList(this.ignoreSides,sides)); }

        public InputNodeData allowSides(Direction... sides) { return this.allowSides(List.of(sides)); }
        public InputNodeData allowSides(List<Direction> sides) { return new InputNodeData(this.allowInputs,this.allowOutputs,removeFromList(this.ignoreSides,sides)); }

        public boolean ignored(Direction side) { return this.ignoreSides.contains(side); }

        private static List<Direction> addToList(List<Direction> sides,List<Direction> newSides) {
            List<Direction> newList = new ArrayList<>(sides);
            boolean changed = false;
            for(Direction newSide : newSides) {
                if(!newList.contains(newSide)) {
                    changed = true;
                    newList.add(newSide);
                }
            }
            return changed ? List.copyOf(newList) : sides;
        }

        private static List<Direction> removeFromList(List<Direction> sides,List<Direction> removeSides) {
            List<Direction> newList = new ArrayList<>(sides);
            boolean changed = false;
            for(Direction remove : removeSides)
                changed = changed || newList.remove(remove);
            return changed ? List.copyOf(newList) : sides;
        }

    }

}
