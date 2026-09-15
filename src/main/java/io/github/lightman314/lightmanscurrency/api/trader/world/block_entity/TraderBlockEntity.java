package io.github.lightman314.lightmanscurrency.api.trader.world.block_entity;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.CodecInteractionHelper;
import io.github.lightman314.lightmanscurrency.api.money.resource.MoneyResourceHandler;
import io.github.lightman314.lightmanscurrency.api.ownership.holder.OwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnable;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnerHolder;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderState;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.data_components.CopiedTrader;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.OwnerNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.IDisplayNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.interfaces.ITraderDestructionListener;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.trader.tracking.managers.BlockEntityTrackingManager;
import io.github.lightman314.lightmanscurrency.api.world.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.world.data.WorldPosition;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Optional;
import java.util.function.Consumer;

public abstract class TraderBlockEntity extends EasyBlockEntity implements IOwnable, BlockEntityTrackingManager.ITraderTrackingBE {

    private boolean legalBreak = false;
    public void flagAsLegalBreak() { this.legalBreak = true; }
    public boolean isLegalBreak() { return this.legalBreak; }

    public final BlockEntityTrackingManager tracking = new BlockEntityTrackingManager(this,this);

    @Override
    @OverridingMethodsMustInvokeSuper
    public void startTrackingPlayer(Player player) {
        this.tracking.requestTracking(this.getTrader(),player);
    }

    @Override
    @OverridingMethodsMustInvokeSuper
    public void stopTrackingPlayer(Player player) {
        this.tracking.clearPlayer(player);
    }

    @ApiStatus.Internal
    public final void requestTraderDestruction(Player player) {
        if(this.isClient())
            return;
        this.flagAsLegalBreak();
        TraderData trader = this.getTrader();
        if(trader != null) {
            //Collect relevant data
            MoneyResourceHandler playerMoney = LCApi.getMoneyAPI().getPlayersMoneyHandler(player);
            IOwnerHolder owner = trader.getOwner();
            Consumer<ItemStack> itemConsumer = player.getInventory()::placeItemBackInInventory;
            //Place the traders block back into their inventory
            itemConsumer.accept(new ItemStack(this.getBlockState().getBlock()));
            //Now handle trader drops
            for(ITraderDestructionListener listener : trader.getNodes(ITraderDestructionListener.class))
                listener.onTraderDestroyed(owner,itemConsumer,Optional.of(playerMoney));
            //And finally, actually delete the trader
            LCApi.getTraderAPI().deleteTrader(trader);
            //And now we delete the block
            this.level.setBlock(this.worldPosition,Blocks.AIR.defaultBlockState(),Block.UPDATE_ALL);
        }
    }

    private long traderID = -1;
    public long getTraderID() { return this.traderID; }
    protected final void setTraderID(long traderID) {
        if(this.traderID == traderID)
            return;
        if(this.traderID >= 0)
            this.tracking.endTracking(this.traderID);
        this.traderID = traderID;
        //Start tracking for all players within range
        TraderData trader = this.getTrader();
        if(trader != null)
            this.tracking.requestTracking(trader);
        //Invalidate the capabilities for this BE as they may have changed
        this.invalidateCapabilities();
    }
    @Nullable
    public final TraderData getTrader() {
        TraderData trader = LCApi.getTraderAPI().getTrader(this,this.traderID);
        if(trader != null && this.validTrader(trader))
            return trader;
        return null;
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        //Clear components via DataComponentGetter#get
        components.get(DataComponents.CUSTOM_NAME);
        components.get(LCDataComponents.STORED_TRADER);
        components.get(LCDataComponents.COPIED_TRADER);
        //Don't do anything here on the logical client please :)
        if(this.isClient())
            return;
        if(components.has(LCDataComponents.COPIED_TRADER)) {
            TraderData trader = components.get(LCDataComponents.COPIED_TRADER).tryCreate(this.registryAccess());
            if(trader != null && this.validTrader(trader) && trader.hasNode(WorldNode.TYPE)) {
                //Update the world position
                WorldNode node = trader.getNode(WorldNode.TYPE);
                node.setPosition(this.getPosition());
                //Initialize the trader and store its trader id locally
                this.setTraderID(LCApi.getTraderAPI().initializeTrader(trader));
                return;
            } else if(trader != null) {
                LightmansCurrency.LogWarning("Attempted to load the copied trader of type " + trader.getType() + " however it is not valid for the " + BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(this.getType()) + " block entity.");
            }
        }
        if(components.has(LCDataComponents.STORED_TRADER)) {
            long traderID = components.get(LCDataComponents.STORED_TRADER).traderID();
            //Attempt to copy the trader id from the item
            TraderData trader = LCApi.getTraderAPI().getTrader(this,traderID);
            if(trader != null && this.validTrader(trader) && trader.hasNode(WorldNode.TYPE))
            {
                WorldNode node = trader.getNode(WorldNode.TYPE);
                if(node.getState().isItem())
                {
                    node.setState(TraderState.NORMAL);
                    this.setTraderID(traderID);
                }
            }
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        TraderData trader = this.getTrader();
        if(trader != null) {
            components.set(LCDataComponents.COPIED_TRADER,new CopiedTrader(trader,this.registryAccess()));
            Component customName = IDisplayNode.getCustomTraderName(trader);
            if(customName != null)
                components.set(DataComponents.CUSTOM_NAME,customName);
        }
    }

    @Override
    @SuppressWarnings("deprecation")
    public void removeComponentsFromTag(ValueOutput output) {
        output.discard("traderID");
        output.discard("heldTrader");
    }

    protected abstract boolean validTrader(TraderData trader);

    @Override
    public final IOwnerHolder getOwner() {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.getOwner();
        return new OwnerHolder(this);
    }

    public final boolean canBreakTrader(Player player)
    {
        TraderData trader = this.getTrader();
        return trader == null || trader.getPermission(player,BuiltInPermissions.BREAK_TRADER).hasLowerPermission();
    }

    public final WorldPosition getPosition() { return WorldPosition.ofBE(this); }

    private CompoundTag writtenTrader = null;

    public TraderBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public final void onTraderPlacement(@Nullable Player owner, ItemStack stack)
    {
        //Remove any stored trader data if present as that should be single-use only
        stack.remove(LCDataComponents.STORED_TRADER);
        if(this.traderID >= 0)
            return;
        //Otherwise create a new trader
        TraderData trader = this.createNewTrader(this.collectArguments(owner));
        this.setTraderID(LCApi.getTraderAPI().initializeTrader(trader));
        this.sendUpdate();
    }

    protected final TraderArguments collectArguments(@Nullable Player player)
    {
        //The WorldNode argument is literally the trader BE itself
        TraderArguments arguments = TraderArguments.builder()
                .with(WorldNode.TYPE,this);
        if(player != null)
            arguments.with(OwnerNode.TYPE,PlayerOwner.of(player));
        this.addAdditionalArguments(arguments,player);
        return arguments;
    }

    protected void addAdditionalArguments(TraderArguments arguments,@Nullable Player player) { }

    protected abstract TraderData createNewTrader(TraderArguments arguments);

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putLong("traderID",this.traderID);
        output.storeNullable("heldTrader",CompoundTag.CODEC,this.writtenTrader);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.traderID = input.getLongOr("traderID",-1);
        this.writtenTrader = input.read("heldTrader",CompoundTag.CODEC).orElse(null);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        //Only sync the trader ID for traders
        CompoundTag tag = new CompoundTag();
        tag.putLong("traderID",this.traderID);
        return tag;
    }

    @Override
    public void onLoad() {
        if(this.isServer())
        {
            //Attempt to load the written trader
            boolean updateState = true;
            if(this.writtenTrader != null)
            {
                CodecInteractionHelper<Tag> context = CodecInteractionHelper.create(NbtOps.INSTANCE,this.registryAccess());
                TraderData trader = context.read(this.writtenTrader,TraderData.CODEC);
                if(trader != null && this.validTrader(trader))
                {
                    //Check if we're at the same world position
                    WorldNode node = trader.getNode(WorldNode.TYPE);
                    if(node != null)
                    {
                        if(!this.getPosition().equals(node.getPosition()))
                        {
                            //Update the traders world position and register it
                            node.setPosition(this.getPosition());
                            node.setNormalState();
                            this.traderID = LCApi.getTraderAPI().initializeTrader(trader);
                            updateState = false;
                        }
                    }
                }
            }
            if(updateState)
            {
                TraderData trader = this.getTrader();
                if(trader != null && trader.getState().allowRecovery)
                {
                    //Update the traders state to inform the game that this trader does in-fact still exist within the world
                    trader.ifNodePresent(WorldNode.TYPE,node -> {
                        node.setPosition(this.getPosition());
                        node.setNormalState();
                    });
                }
            }
            //Request tracking for all players that can see this chunk
            this.tracking.requestTracking(this.getTrader());
            this.sendUpdate();
        }
    }

    public static abstract class SingleType extends TraderBlockEntity
    {
        public SingleType(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
            super(type, worldPosition, blockState);
        }
        protected abstract TraderType getTraderType();
        @Override
        protected TraderData createNewTrader(TraderArguments arguments) { return new TraderData(this.getTraderType(),arguments); }
        @Override
        protected boolean validTrader(TraderData trader) { return trader.getType() == this.getTraderType(); }
    }

}