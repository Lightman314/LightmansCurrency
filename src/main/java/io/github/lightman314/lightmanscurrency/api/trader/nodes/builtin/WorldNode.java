package io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.text.MultiLineTextEntry;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderState;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IAdminInfoProvider;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IPermissionUser;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ISettingsMessageListener;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.templates.SimpleSyncedNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.ISyncingContext;
import io.github.lightman314.lightmanscurrency.api.trader.world.block_entity.TraderBlockEntity;
import io.github.lightman314.lightmanscurrency.api.world.data.WorldPosition;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCFancyPacketTypes;
import io.github.lightman314.lightmanscurrency.core.lightmanscurrency.LCPermissions;
import io.github.lightman314.lightmanscurrency.features.commands.LCAdminCommand;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

public class WorldNode extends SimpleSyncedNode implements IDisplayNode, ISettingsMessageListener, IPermissionUser, IAdminInfoProvider {

    private static final MapCodec<WorldNode> MAP_CODEC = RecordCodecBuilder.mapCodec(buider -> buider.group(
            TraderState.CODEC.fieldOf("state").forGetter(WorldNode::getState),
            WorldPosition.CODEC.fieldOf("pos").forGetter(WorldNode::getPosition),
            BuiltInRegistries.BLOCK.byNameCodec().optionalFieldOf("block").forGetter(n -> n.block)
    ).apply(buider,WorldNode::new));
    public static final TraderNodeType<WorldNode> TYPE = TraderNodeType.simple(WorldNode::new,MAP_CODEC);

    public static final TextEntry BUTTON_TRADER_SETTINGS_DESTROY_TRADER = TextEntry.button(LCApi.MODID,"trader.settings.delete_trader");
    public static final MultiLineTextEntry TOOLTIP_TRADER_SETTINGS_DESTROY_TRADER = MultiLineTextEntry.tooltip(LCApi.MODID,"trader.settings.delete_trader");

    public static final TextEntry COMMAND_LIST_STATE = LCAdminCommand.traderListText(TYPE,"state");
    public static final TextEntry COMMAND_LIST_DIMENSION = LCAdminCommand.traderListText(TYPE,"dimension");
    public static final TextEntry COMMAND_LIST_POSITION = LCAdminCommand.traderListText(TYPE,"position");
    public static final TextEntry COMMAND_LIST_POSITION_TOOLTIP = LCAdminCommand.traderListText(TYPE,"position.tooltip");

    private TraderState state = TraderState.NORMAL;
    public TraderState getState() { return this.state; }
    public void setState(TraderState state) {
        if(this.state == state || this.state == TraderState.PERSISTENT)
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
        this.setChanged(builder -> builder.set("position",LCFancyPacketTypes.WORLD_POSITION,this.position));
    }
    private Optional<Block> block = Optional.empty();
    public Block getBlock() { return this.block.orElse(Blocks.AIR); }
    public void updateBlock(Block block) {
        if(this.block.isPresent()) {
            Block b = this.block.get();
            if(b == block)
                return;
        }
        this.block = Optional.of(block);
        this.setChanged(m -> m.setRegistryEntry("block",BuiltInRegistries.BLOCK,block));
    }
    @Nullable
    public Block getBlockOrNull() { return this.block.orElse(null); }

    @Nullable
    @Override
    public Component getDefaultName() {
        if(this.block.isPresent())
            return this.block.get().getName();
        return null;
    }

    @Nullable
    public TraderBlockEntity getBlockEntity(Level level) {
        if(!this.position.isVoid()) {
            if(level.dimension() == this.position.getDimension() && level.isLoaded(this.position.getPos())) {
                if(level.getBlockEntity(this.position.getPos()) instanceof TraderBlockEntity be && be.getTraderID() == this.getTrader().getID())
                    return be;
            }
        }
        return null;
    }

    private WorldNode() {}
    private WorldNode(TraderState state, WorldPosition position, Optional<Block> block)
    {
        this.state = state;
        this.position = position;
        this.block = block;
    }

    @Override
    public void updateArgument(TraderArguments arguments) {
        Optional<Object> arg = arguments.get(TYPE);
        if(arg.isPresent())
        {
            Object val = arg.get();
            if(val instanceof TraderBlockEntity be)
            {
                this.position = be.getPosition();
                this.block = Optional.of(be.getBlockState().getBlock());
            }
            else if(val instanceof BlockEntity be)
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

    @Override
    public void handleSettingsChange(Player player,FancyPacketMap message) {
        if(message.contains("destroyTrader") && this.getPermission(player, LCPermissions.BREAK_TRADER).hasHigherPermission()) {
            TraderBlockEntity be = this.getBlockEntity(player.level());
            if(be != null) {
                LightmansCurrency.LogDebug("Requesting destruction of trader at " + this.position);
                be.requestTraderDestruction(player);
            }
        }
    }

    @Override
    public void addDefaultAllyPermission(Consumer<Permission<?>> handler) {
        handler.accept(LCPermissions.BREAK_TRADER);
    }

    @Override
    public void displayAdminListData(Builder builder) {
        //State
        builder.accept(-1000,COMMAND_LIST_STATE.get(this.state));
        if(!this.position.isVoid()) {
            //Dimension
            Identifier dimension = this.position.getDimension().identifier();
            builder.accept(-990,COMMAND_LIST_DIMENSION.get(dimension));
            //Position
            BlockPos pos = this.position.getPos();
            String position = pos.getX() + " " + pos.getY() + " " + pos.getZ();
            String teleportPosition = pos.getX() + " " + (pos.getY() + 1) + " " + pos.getZ();
            builder.accept(-980,COMMAND_LIST_POSITION.get(Component.literal(position).withStyle(s ->
                    s.withClickEvent(new ClickEvent.SuggestCommand("/execute in " + dimension + " run tp @s " + teleportPosition))
                            .withHoverEvent(new HoverEvent.ShowText(COMMAND_LIST_POSITION_TOOLTIP.get())))));
        }
    }

}
