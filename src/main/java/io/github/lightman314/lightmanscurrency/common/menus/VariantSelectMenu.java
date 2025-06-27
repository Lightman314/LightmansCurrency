package io.github.lightman314.lightmanscurrency.common.menus;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IDeepBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IWideBlock;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.common.menus.providers.EasyMenuProvider;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockValidator;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantSelectMenu extends LazyMessageMenu {

    private final BlockPos pos;
    private final Level level;
    private final IVariantBlock variantBlock;
    private final Block block;
    public Block getBlock() { return this.block; }
    public IVariantBlock getVariantBlock() { return this.variantBlock; }
    public VariantSelectMenu(int id, Inventory inventory, BlockPos pos) {
        super(ModMenus.VARIANT_SELECT.get(), id, inventory);
        this.pos = pos;
        this.level = inventory.player.level();
        this.block = this.level.getBlockState(this.pos).getBlock();
        if(this.block instanceof IVariantBlock b)
        {
            this.variantBlock = b;
            this.addValidator(BlockValidator.of(this.pos,this.block));
        }
        else
        {
            this.variantBlock = null;
            this.addValidator(() -> false);
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    private int pendingClose = -1;
    private final List<Pair<BlockPos,Boolean>> pendingUpdates = new ArrayList<>();

    public void SetVariant(@Nullable ResourceLocation variant)
    {
        if(this.isClient())
        {
            if(variant == null)
                this.SendMessageToServer(this.builder().setFlag("ClearVariant"));
            else
                this.SendMessageToServer(this.builder().setResourceLocation("SetVariant",variant));
        }
        else
        {
            if(variant != null && LCConfig.SERVER.variantBlacklist.matches(variant) && !this.player.isCreative())
            {
                LightmansCurrency.LogWarning(this.player.getName().getString() + " just tried to assign a blacklisted Model Variant (" + variant + ")!");
                return;
            }
            BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
            if(this.level.getBlockEntity(this.pos) instanceof IVariantSupportingBlockEntity be)
            {
                //Set Variant in the Block Entity
                be.setVariant(variant);
                //Update Block State to match variant data presence
                updateVariantState(this.level,this.pos,variant != null);
                //Close the container
                this.pendingClose = 1;
            }
        }
    }

    private void updateVariantState(Level level, BlockPos pos, boolean variant)
    {
        BlockState state = level.getBlockState(pos);
        if(state.getBlock() instanceof IWideBlock wideBlock)
            updateWideStates(wideBlock,level,pos,variant);
        else if(state.getBlock() instanceof IDeepBlock deepBlock)
            updateDeepStates(deepBlock,level,pos,variant);
        else if(state.getBlock() instanceof ITallBlock tallBlock)
            updateTallStates(tallBlock,level,pos,variant);
        else
            updateSingleState(level,pos,variant);
    }

    private void updateWideStates(IWideBlock wideBlock, Level level, BlockPos pos, boolean variant)
    {
        if(wideBlock instanceof IDeepBlock deepBlock)
        {
            //Update deep states on both sides
            updateDeepStates(deepBlock,level,pos,variant);
            updateDeepStates(deepBlock,level,wideBlock.getOtherSide(pos,level.getBlockState(pos)),variant);
        }
        else if(wideBlock instanceof ITallBlock tallBlock)
        {
            //Update tall states on both sides
            updateTallStates(tallBlock,level,pos,variant);
            updateTallStates(tallBlock,level,wideBlock.getOtherSide(pos,level.getBlockState(pos)),variant);
        }
        else
        {
            //Update only my states
            updateSingleState(level,pos,variant);
            pos = wideBlock.getOtherSide(pos,level.getBlockState(pos));
            updateSingleState(level,pos,variant);
        }
    }

    private void updateDeepStates(IDeepBlock deepBlock, Level level, BlockPos pos, boolean variant)
    {
        if(deepBlock instanceof ITallBlock tallBlock)
        {
            //Update Tall States on both depths
            updateTallStates(tallBlock,level,pos,variant);
            updateTallStates(tallBlock,level,deepBlock.getOtherDepth(pos,level.getBlockState(pos)),variant);
        }
        else
        {
            //Update only my states
            updateSingleState(level,pos,variant);
            pos = deepBlock.getOtherDepth(pos,level.getBlockState(pos));
            updateSingleState(level,pos,variant);
        }
    }

    private void updateTallStates(ITallBlock tallBlock, Level level, BlockPos pos, boolean variant)
    {
        //Update only my states
        updateSingleState(level,pos,variant);
        pos = tallBlock.getOtherHeight(pos,level.getBlockState(pos));
        updateSingleState(level,pos,variant);
    }

    private void updateSingleState(Level level, BlockPos pos, boolean variant) { this.updateSingleState(level,pos,variant,true); }
    private void updateSingleState(Level level, BlockPos pos, boolean variant, boolean allowTick) {
        BlockState state = level.getBlockState(pos);
        if(state.getBlock() != this.block)
            return;
        if(variant == state.getValue(IVariantBlock.VARIANT) && allowTick)
        {
            //Toggle to opposite state temporarily to force the block model to update
            level.setBlockAndUpdate(pos,state.setValue(IVariantBlock.VARIANT,!variant));
            this.pendingUpdates.add(Pair.of(pos,variant));
            LightmansCurrency.LogDebug("Scheduled tick at " + pos);
        }
        else
            level.setBlockAndUpdate(pos,state.setValue(IVariantBlock.VARIANT,variant));
    }

    @Nullable
    public ResourceLocation getSelectedVariant() {
        if(this.level.getBlockEntity(this.pos) instanceof IVariantSupportingBlockEntity be)
            return be.getCurrentVariant();
        return null;
    }

    private void sendUpdateModelMessage(BlockPos pos) {
        this.SendMessageToClient(this.builder().setBlockPos("UpdateModel",pos));
    }

    @Override
    public void HandleMessage(LazyPacketData message) {
        if(message.contains("SetVariant"))
            this.SetVariant(message.getResourceLocation("SetVariant"));
        if(message.contains("ClearVariant"))
            this.SetVariant(null);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        this.runPendingUpdates();
        MinecraftForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    public void serverTick(TickEvent.ServerTickEvent event)
    {
        if(event.phase != TickEvent.Phase.START)
            return;
        if(this.pendingClose < 0)
            return;
        if(this.pendingClose-- < 1)
        {
            this.runPendingUpdates();
            this.player.closeContainer();
        }
    }

    private void runPendingUpdates()
    {
        for(Pair<BlockPos,Boolean> update : new ArrayList<>(this.pendingUpdates))
        {
            BlockState existingState = this.level.getBlockState(update.getFirst());
            LightmansCurrency.LogDebug("Executed tick at " + update.getFirst());
            this.updateSingleState(this.level,update.getFirst(),update.getSecond(),false);
        }
        this.pendingUpdates.clear();
    }

    public static MenuProvider providerFor(BlockPos pos) { return new Provider(pos); }

    private record Provider(BlockPos pos) implements EasyMenuProvider
    {
        @Nullable
        @Override
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) { return new VariantSelectMenu(containerId,playerInventory,this.pos); }
    }

}