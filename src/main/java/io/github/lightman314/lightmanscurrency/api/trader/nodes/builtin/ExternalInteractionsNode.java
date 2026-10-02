package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.INodeAccess;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.notifications.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsStorageIONode;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsDisplayOutput;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsLoadContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.TrackingLevel;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.world.data.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ExternalInteractionsNode extends SimpleSyncedNode implements IDirectionalSettingsObject, IPermissionUser, ISettingsMessageListener, ISettingsStorageIONode {

    private static final MapCodec<ExternalInteractionsNode> MAP_CODEC = DirectionalSettings.CODEC.fieldOf("settings")
            .xmap(ExternalInteractionsNode::new, n -> n.settings);

    public static final TraderNodeType<ExternalInteractionsNode> TYPE = TraderNodeType.simple(ExternalInteractionsNode::new,MAP_CODEC);

    private InputNodeData data = InputNodeData.DEFAULT;

    private final DirectionalSettings settings;

    public static final TextEntry NAME = TextEntry.traderNode(TYPE);
    public static final TextEntry VALUE_SETTINGS_DUMB = TextEntry.traderNodeValue(TYPE,"directional_settings.dumb");
    public static final TextEntry VALUE_SETTINGS = TextEntry.traderNodeValue(TYPE,"directional_settings");

    public static final TextEntry TOOLTIP_SETTINGS_DEFAULT = TextEntry.tooltip(LCApi.MODID,"trader.settings.input");
    public static final TextEntry GUI_SETTINGS_LABEL = TextEntry.gui(LCApi.MODID,"trader.settings.input");

    private ExternalInteractionsNode() { this(new DirectionalSettings()); }
    private ExternalInteractionsNode(DirectionalSettings settings) {
        this.settings = settings.withParent(this)
                .withListener(m -> this.setChanged(m));
    }

    public final void setSidedState(@Nullable PlayerReference admin, Direction side, DirectionalSettingsState state) {
        if(this.settings.setState(side,state) && admin != null)
            this.postInternalNotification(ChangeSettingNotification.simple(admin, Component.empty().append(VALUE_SETTINGS.get()).append(DirectionalSettings.GUI_INPUT_SIDES.getComponent(side)),state.getText()));
    }

    public static Supplier<DirectionalSettingsState> getSidedState(INodeAccess trader,Direction relativeSide) { return () -> trader.getNodeValue(ExternalInteractionsNode.TYPE, n -> n.getSidedState(relativeSide),DirectionalSettingsState.NONE); }

    @Override
    public void updateArgument(TraderArguments arguments) {
        arguments.get(TYPE).ifPresent(arg -> {
            if(arg instanceof InputNodeData d)
                this.data = d;
        });
    }

    @Override
    public TrackingLevel requiredTrackingLevel() { return TrackingLevel.STORAGE; }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }

    @Override
    public void createSyncPacket(FancyPacketMap.Mutable builder, ISyncingContext context) {
        builder.merge(this.settings.asPacket());
    }

    @Override
    public void onDataSync(FancyPacketMap data) { this.settings.handlePacket(data); }

    @Nullable
    @Override
    public Block getDisplayBlock() { return this.getNodeValue(WorldNode.TYPE,WorldNode::getBlockOrNull); }
    @Override
    public DirectionalSettingsState getSidedState(Direction side) { return this.settings.getState(side); }

    @Override
    public void handleSettingsChange(Player player, FancyPacketMap message) {
        if(message.contains("setSidedState") && this.getPermission(player,LCPermissions.EXTERNAL_ACCESS_SETTINGS)) {
            FancyPacketMap entry = message.getMap("setSidedState");
            DirectionalSettingsState state = entry.getEnum("state",DirectionalSettingsState.class);
            Direction side = entry.getEnum("side",Direction.class);
            this.setSidedState(PlayerReference.of(player),side,state);
        }
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(LCPermissions.EXTERNAL_ACCESS_SETTINGS);
    }

    @Override
    public void encodeSettings(ValueOutput output) {
        output.store("settings",DirectionalSettings.CODEC,this.settings);
    }

    @Override
    public void decodeSettings(ValueInput data,SettingsLoadContext context) {
        if(context.getPermission(LCPermissions.EXTERNAL_ACCESS_SETTINGS))
            this.settings.copyFrom(data.read("settings",DirectionalSettings.CODEC).orElse(DirectionalSettings.EMPTY));
    }

    @Override
    public void appendDisplay(ValueInput data,SettingsDisplayOutput output) {
        output.acceptTitle(NAME);
        output.acceptLine(VALUE_SETTINGS_DUMB);
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
