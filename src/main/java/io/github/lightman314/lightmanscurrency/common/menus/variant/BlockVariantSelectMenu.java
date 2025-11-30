package io.github.lightman314.lightmanscurrency.common.menus.variant;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IDeepBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IWideBlock;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.api.variants.block.block_entity.IVariantDataStorage;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockVariantSelectMenu extends VariantSelectMenu {

    private final BlockPos pos;
    private final Level level;
    private final IVariantBlock variantBlock;
    private final Block block;
    public Block getBlock() { return this.block; }
    public IVariantBlock getVariantBlock() { return this.variantBlock; }
    public BlockVariantSelectMenu(int id, Inventory inventory, BlockPos pos) {
        super(ModMenus.VARIANT_SELECT_BLOCK.get(), id, inventory);
        this.pos = pos;
        this.level = inventory.player.level();
        this.block = this.level.getBlockState(this.pos).getBlock();
        this.variantBlock = VariantProvider.getVariantBlock(this.block);
        if(this.variantBlock  != null)
            this.addValidator(BlockValidator.of(this.pos,this.block));
        else
            this.addValidator(() -> false);
        NeoForge.EVENT_BUS.register(this);
    }

    private int pendingClose = -1;
    private final List<Pair<BlockPos,Boolean>> pendingUpdates = new ArrayList<>();

    @Override
    protected void changeVariant(@Nullable ResourceLocation variant) {
        BlockEntity blockEntity = this.level.getBlockEntity(this.pos);
        IVariantDataStorage data = IVariantDataStorage.get(this.level,this.pos);
        if(data != null)
        {
            if(data.isVariantLocked() && !this.player.isCreative())
            {
                LightmansCurrency.LogDebug(this.player.getName().getString() + " attempted to change the variant of a locked block!");
                return;
            }
            //Set Variant in the Block Entity
            data.setVariant(variant);
            //Update Block State to match variant data presence
            updateVariantState(this.level,this.pos,variant != null);
            //Close the container
            this.pendingClose = 1;
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
        IVariantDataStorage data = IVariantDataStorage.get(this.level,this.pos);
        if(data != null)
            return data.getCurrentVariant();
        return null;
    }

    private void sendUpdateModelMessage(BlockPos pos) {
        this.SendMessageToClient(this.builder().setBlockPos("UpdateModel",pos));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    @Override
    public void removed(Player player) {
        this.runPendingUpdates();
        NeoForge.EVENT_BUS.unregister(this);
    }

    @SubscribeEvent
    private void serverTick(ServerTickEvent.Pre event)
    {
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
        public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) { return new BlockVariantSelectMenu(containerId,playerInventory,this.pos); }
    }

}
