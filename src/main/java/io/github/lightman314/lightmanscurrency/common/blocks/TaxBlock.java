package io.github.lightman314.lightmanscurrency.common.blocks;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.common.blockentity.TaxBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.interfaces.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.templates.RotatableBlock;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.taxes.TaxEntry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Collection;

public class TaxBlock extends RotatableBlock implements IOwnableBlock, IEasyEntityBlock {

    public TaxBlock(Properties properties) { super(properties.pushReaction(PushReaction.BLOCK)); }

    @Override
    public void setPlacedBy(@Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState state, @Nullable LivingEntity entity, @Nonnull ItemStack stack) {
        if(level.getBlockEntity(pos) instanceof TaxBlockEntity taxBlock && entity instanceof Player player)
            taxBlock.initialize(player);
    }

    @Override
    @SuppressWarnings("deprecation")
    public void onRemove(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean flag) {
        if(level.getBlockEntity(pos) instanceof TaxBlockEntity taxBlock)
            taxBlock.onRemove();
        super.onRemove(state, level, pos, newState, flag);
    }

    @Override
    public boolean canBreak(Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        if(level.getBlockEntity(pos) instanceof TaxBlockEntity taxBlock)
        {
            TaxEntry entry = taxBlock.getTaxEntry();
            if(entry != null)
                return entry.getOwner().isAdmin(player);
        }
        return true;
    }

    @Override
    public Collection<BlockEntityType<?>> getAllowedTypes() { return ImmutableList.of(ModBlockEntities.TAX_BLOCK.get()); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) { return new TaxBlockEntity(pos, state); }

}
