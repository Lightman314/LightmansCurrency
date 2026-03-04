package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.api.settings.SettingsNode;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.traders.settings.builtin.InputSettingsNode;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InputNode extends SyncedTraderNode implements IDirectionalSettingsObject {

    private static final MapCodec<InputNode> MAP_CODEC = DirectionalSettings.CODEC.fieldOf("settings")
            .xmap(InputNode::new,n -> n.settings);

    public static final TraderNodeType<InputNode> TYPE = TraderNodeType.advanced(InputNode::factory,MAP_CODEC);

    public static Component getFacingName(Direction side) { return LCText.GUI_INPUT_SIDES.get(side).get(); }

    private final InputNodeData data;

    private final DirectionalSettings settings;

    public DirectionalSettingsState getSidedState(Direction side) { return this.settings.getState(side); }

    private InputNode(InputNodeData data) { this.data = data; this.settings = new DirectionalSettings(this); }
    private InputNode(DirectionalSettings settings) {
        this.data = InputNodeData.DEFAULT;
        this.settings = settings.withParent(this);
    }

    public boolean setDirectionalState(@Nullable PlayerReference admin,Direction side, DirectionalSettingsState newState)
    {
        if(this.settings.getState(side) != newState && !this.data.ignores(side))
        {
            this.settings.setState(side,newState);
            //Send sync packet
            this.setChanged(builder -> builder.setCustom(side.getSerializedName(),this.settings.getState(side),ModLazyPackets.DIRECTIONAL_SETTINGS));
            if(admin != null)
                this.pushLocalNotification(ChangeSettingNotification.simple(admin, EasyText.empty().append(LCText.DATA_ENTRY_INPUT_OUTPUT_SIDES.get()).append(getFacingName(side)),newState.getText()));
            return true;
        }
        return false;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public boolean isStorageOnly() { return true; }

    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        for(Direction side : Direction.values())
        {
            if(!this.data.ignores(side))
                builder.setCustom(side.getSerializedName(),this.settings.getState(side),ModLazyPackets.DIRECTIONAL_SETTINGS);
        }
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        for(Direction side : Direction.values())
        {
            if(!this.data.ignoreSides.contains(side) && data.contains(side.getSerializedName()))
                this.settings.setState(side,data.getCustom(side.getSerializedName(),ModLazyPackets.DIRECTIONAL_SETTINGS));
        }
    }

    @Override
    @Nullable
    public Block getDisplayBlock() {
        WorldStateNode node = this.trader.getNode(WorldStateNode.TYPE);
        if(node != null && node.getTraderBlock() instanceof BlockItem block)
            return block.getBlock();
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getVariant() {
        WorldStateNode node = this.trader.getNode(WorldStateNode.TYPE);
        if(node != null)
            return node.getBlockVariant();
        return IDirectionalSettingsObject.super.getVariant();
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        this.settings.loadOldData(tag,"InputOutputState");
    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains("SetDirectionalState") && this.hasPermission(player,Permissions.InputTrader.EXTERNAL_INPUTS))
        {
            DirectionalSettingsState state = DirectionalSettingsState.parse(message.getString("SetDirectionalState"));
            Direction side = Direction.from3DDataValue(message.getInt("Side"));
            this.setDirectionalState(PlayerReference.of(player),side,state);
        }
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.InputTrader.EXTERNAL_INPUTS, 1);
    }

    @Override
    public void registerSettingsNodes(TraderData trader, Consumer<SettingsNode> consumer) {
        consumer.accept(new InputSettingsNode(trader,this));
    }

    private static InputNode factory(@Nullable Object argument) {
        if(argument instanceof InputNodeData data)
            return new InputNode(data);
        return new InputNode(InputNodeData.DEFAULT);
    }

    public record InputNodeData(boolean allowInputs, boolean allowOutputs, List<Direction> ignoreSides)
    {
        public static InputNodeData DEFAULT = new InputNodeData(true,true,ImmutableList.of());
        public InputNodeData allowInputs(boolean newValue) { return new InputNodeData(newValue,this.allowOutputs,this.ignoreSides); }
        public InputNodeData allowOutputs(boolean newValue) { return new InputNodeData(this.allowInputs,newValue,this.ignoreSides); }
        public InputNodeData ignoreSide(Direction side) { return new InputNodeData(this.allowInputs,this.allowOutputs,addToList(this.ignoreSides,side)); }
        public InputNodeData ignoreSides(List<Direction> sides) { return new InputNodeData(this.allowInputs,this.allowOutputs,ImmutableList.copyOf(sides)); }
        public InputNodeData dontIgnoreSide(Direction side) { return new InputNodeData(this.allowInputs,this.allowOutputs,removeFromList(this.ignoreSides,side)); }

        public boolean ignores(Direction side) { return this.ignoreSides.contains(side); }

        private static List<Direction> addToList(List<Direction> sides,Direction newSide)
        {
            if(sides.contains(newSide))
                return sides;
            List<Direction> list = new ArrayList<>(sides);
            list.add(newSide);
            return ImmutableList.copyOf(list);
        }

        private static List<Direction> removeFromList(List<Direction> sides, Direction newSide)
        {
            if(!sides.contains(newSide))
                return sides;
            List<Direction> list = new ArrayList<>(sides);
            list.remove(newSide);
            return ImmutableList.copyOf(list);
        }

    }

}
