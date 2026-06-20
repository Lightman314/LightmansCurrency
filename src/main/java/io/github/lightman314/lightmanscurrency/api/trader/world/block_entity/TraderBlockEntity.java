package io.github.lightman314.lightmanscurrency.api.trader.world.block_entity;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import io.github.lightman314.lightmanscurrency.api.ownership.builtin.PlayerOwner;
import io.github.lightman314.lightmanscurrency.api.ownership.interfaces.IOwnable;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderState;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderType;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.TraderArguments;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.OwnerNode;
import io.github.lightman314.lightmanscurrency.api.trader.nodes.builtin.WorldNode;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.BuiltInPermissions;
import io.github.lightman314.lightmanscurrency.api.world.blockentity.EasyBlockEntity;
import io.github.lightman314.lightmanscurrency.api.world.data.WorldPosition;
import io.github.lightman314.lightmanscurrency.core.LCDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import javax.annotation.Nullable;

public abstract class TraderBlockEntity extends EasyBlockEntity implements IOwnable {

    private boolean legalBreak = false;
    public void flagAsLegalBreak() { this.legalBreak = true; }
    public boolean isLegalBreak() { return this.legalBreak; }

    private long traderID = -1;
    public long getTraderID() { return this.traderID; }
    @Nullable
    public final TraderData getTrader() {
        TraderData trader = LCApi.getTraderAPI().getTrader(this,this.traderID);
        if(trader != null && this.validTrader(trader))
            return trader;
        return null;
    }

    protected abstract boolean validTrader(TraderData trader);

    @Override
    public final OwnerHolder getOwner() {
        TraderData trader = this.getTrader();
        if(trader != null)
            return trader.getOwner();
        return new OwnerHolder(this);
    }

    public final boolean canBreakTrader(Player player)
    {
        TraderData trader = this.getTrader();
        return trader == null || trader.getPermission(player,BuiltInPermissions.BREAK_TRADER);
    }

    public final WorldPosition getPosition() { return WorldPosition.ofBE(this); }

    private CompoundTag writtenTrader = null;

    public TraderBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(type, worldPosition, blockState);
    }

    public final void onTraderPlacement(@Nullable Player owner,ItemStack stack)
    {
        //If the trader stack has the stored trader component, attempt to keep the trader id
        if(stack.has(LCDataComponents.STORED_TRADER))
        {
            long traderID = stack.get(LCDataComponents.STORED_TRADER).traderID();
            //Attempt to copy the trader id from the item
            TraderData trader = LCApi.getTraderAPI().getTrader(this,traderID);
            if(trader != null && this.validTrader(trader) && trader.hasNode(WorldNode.TYPE))
            {
                WorldNode node = trader.getNode(WorldNode.TYPE);
                if(node.getState().isItem())
                {
                    node.setState(TraderState.NORMAL);
                    this.traderID = traderID;
                    //Remove the data component from the stack just to be safe
                    stack.remove(LCDataComponents.STORED_TRADER);
                    return;
                }
            }
        }
        //Otherwise create a new trader
        TraderData trader = this.createNewTrader(this.collectArguments(owner));
        this.traderID = LCApi.getTraderAPI().initializeTrader(trader);
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
                DataContext<Tag> context = DataContext.create(NbtOps.INSTANCE,this.registryAccess());
                TraderData trader = context.read(this.writtenTrader,TraderData.CODEC);
                if(this.validTrader(trader))
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
                if(trader.getState().allowRecovery)
                {
                    //Update the traders state to inform the game that this trader does in-fact still exist within the world
                    trader.ifNodePresent(WorldNode.TYPE,node -> {
                        node.setPosition(this.getPosition());
                        node.setNormalState();
                    });
                }
            }
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