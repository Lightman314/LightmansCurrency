package io.github.lightman314.lightmanscurrency.common.blocks;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IEasyEntityBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.LazyShapes;
import io.github.lightman314.lightmanscurrency.common.blockentity.MoneyBagBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModBlockEntities;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.data.types.LootTableEntry;
import io.github.lightman314.lightmanscurrency.common.items.MoneyBagItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FallingBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import org.jetbrains.annotations.Range;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoneyBagBlock extends FallingBlock implements IRotatableBlock, IEasyEntityBlock {

    public static final IntegerProperty SIZE = IntegerProperty.create("size",0,3);
    public static final ImmutableList<VoxelShape> SHAPES = ImmutableList.of(
            box(5,0,5,11,6,11), //0
            box(3,0,3,13,9,13), //1
            box(2,0,2,14,11,14), //2
            LazyShapes.BOX_T); //3

    public MoneyBagBlock(Properties properties) { super(properties); }

    @Override
    protected MapCodec<? extends FallingBlock> codec() { return simpleCodec(MoneyBagBlock::new); }

    @Nonnull
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) { return state.setValue(FACING, rotation.rotate(state.getValue(FACING))); }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING).add(SIZE);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection()).setValue(SIZE,MoneyBagItem.getSize(context.getItemInHand()));
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) { return Shapes.empty(); }

    protected VoxelShape shapeForSize(int size) { return SHAPES.get(size); }

    @Nonnull
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) { return this.shapeForSize(state.getValue(SIZE)); }

    @Nonnull
    @Override
    public Collection<BlockEntityType<?>> getAllowedTypes() { return Lists.newArrayList(ModBlockEntities.MONEY_BAG.get()); }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MoneyBagBlockEntity(pos,state); }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        if(level.getBlockEntity(pos) instanceof MoneyBagBlockEntity be)
            be.loadFromItem(stack);
        super.setPlacedBy(level, pos, state, placer, stack);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        //Only allow default block interaction with an empty hand
        if(stack.isEmpty())
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if(!level.isClientSide && level.getBlockEntity(pos) instanceof MoneyBagBlockEntity be)
        {
            if(be.tryInsertItem(stack,player))
            {
                stack.shrink(1);
                level.playSound(null,pos,ModSounds.COINS_CLINKING.get(),SoundSource.BLOCKS,0.25f,1f);
            }
            level.playSound(null,pos,SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.BLOCKS,0.5f,1f);
        }
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide && level.getBlockEntity(pos) instanceof MoneyBagBlockEntity be)
        {
            ItemStack taken = be.removeRandomItem();
            if(!taken.isEmpty())
            {
                ItemHandlerHelper.giveItemToPlayer(player,taken);
                level.playSound(null,pos,ModSounds.COINS_CLINKING.get(),SoundSource.BLOCKS,0.25f,1f);
            }
            level.playSound(null,pos,SoundEvents.ARMOR_EQUIP_LEATHER.value(),SoundSource.BLOCKS,0.5f,1f);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if(state.is(newState.getBlock()))
        {
            super.onRemove(state,level,pos,newState,movedByPiston);
            return;
        }
        if(level.getBlockEntity(pos) instanceof MoneyBagBlockEntity be && be.shouldDropItem)
        {
            //Get size first so that it'll be accurate
            int size = be.getBlockSize();
            List<ItemStack> contents = be.clearContents();
            //Drop the money bag
            Block.popResource(level,pos,MoneyBagItem.createItem(this,contents,size));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos, Player player) {
        ItemStack stack = super.getCloneItemStack(state,target,level,pos,player);
        if(player.isCreative() && player.isCrouching() && level.getBlockEntity(pos) instanceof MoneyBagBlockEntity be)
            be.copyContentsTo(stack);
        return stack;
    }

    //Copied from parent
    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (isFree(level.getBlockState(pos.below())) && pos.getY() >= level.getMinBuildHeight()) {
            if(level.getBlockEntity(pos) instanceof MoneyBagBlockEntity be)
            {
                //Flag the block to not drop the item when removed from the world
                be.shouldDropItem = false;
                //Create the Falling block
                FallingBlockEntity fallingBlock = FallingBlockEntity.fall(level, pos, state);
                //Copy the Block Entity data to the falling block
                fallingBlock.blockData = be.saveWithoutMetadata(level.registryAccess());
                //Calculate the fall damage of the falling block
                int size = be.getBlockSize();
                float damagePerDistance = LCConfig.SERVER.moneyBagBaseFallDamage.get() + (size * LCConfig.SERVER.moneyBagFallDamagerPerSize.get());
                int damageLimit = LCConfig.SERVER.moneyBagMaxFallDamageBase.get() + (size * LCConfig.SERVER.moneyBagMaxFallDamagePerSize.get());
                if(damagePerDistance > 0f && damageLimit > 0)
                    fallingBlock.setHurtsEntities(damagePerDistance,damageLimit);
                this.falling(fallingBlock);
            }
        }
    }

    @Override
    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {

        BlockPos startPos = fallingBlock.getStartPos();
        int fall = startPos.getY() - pos.getY();
        LightmansCurrency.LogDebug("Money Bag fell " + fall + "m");
        if(fall >= LCConfig.SERVER.moneyBagCoinLossFallDistance.get() && level.getBlockEntity(pos) instanceof MoneyBagBlockEntity be)
        {
            int testCount = Math.round(fall * 2f);
            double dropChance = LCConfig.SERVER.moneyBagCoinLossChance.get();
            if(dropChance <= 0d)
                return;
            List<ItemStack> drops = new ArrayList<>();
            RandomSource random = level.getRandom();
            for(int i = 0; i < testCount && !be.isEmpty(); ++i)
            {
                if(random.nextDouble() < dropChance)
                    drops.add(be.removeRandomItem());
            }
            if(!drops.isEmpty())
            {
                LightmansCurrency.LogDebug("Money Bag dropped " + drops.size() + " coins after landing");
                Vec3 itemSpawn = getDropPosition(pos,state.getValue(SIZE));
                for(ItemStack item : drops)
                {
                    ItemEntity entity = new ItemEntity(level,pos.getX(),pos.getY(),pos.getZ(),item);
                    entity.setDeltaMovement(
                            random.triangle(0d,0.5d),//Random X velocity between -0.5 and 0.5
                            fall * 0.25d + random.triangle(2d,1d), //Random Y velocity of 1/4 fall distance + 1-3
                            random.triangle(0d,0.5d)); //Random Z velocity between -0.5 and 0.5
                    level.addFreshEntity(entity);
                }
            }
        }
    }

    public static Vec3 getDropPosition(BlockPos pos, @Range(from = 0,to = 3) int size)
    {
        Vec3 center = pos.getBottomCenter();
        return switch (size) {
            case 0 -> center.add(0d,7d/16d,0d);
            case 1 -> center.add(0d,10d/16d,0d);
            case 2 -> center.add(0d,12d/16d,0d);
            default ->  center.add(0d,17d/16d,0d);
        };
    }

    public ItemStack getDropItem(ItemStack stack, CompoundTag tag, HolderLookup.Provider lookup)
    {
        if(stack == null || stack.isEmpty() || !stack.is(this.asItem()))
            return stack;

        if(tag.contains("CustomName"))
            stack.set(DataComponents.CUSTOM_NAME,Component.Serializer.fromJson(tag.getString("CustomName"),lookup));

        List<ItemStack> contents = new ArrayList<>();
        ListTag list = tag.getList("Contents", Tag.TAG_COMPOUND);
        for(int i = 0; i < list.size(); ++i)
        {
            ItemStack item = InventoryUtil.loadItemNoLimits(list.getCompound(i),lookup);
            if(item.isEmpty())
                continue;
            contents.add(item);
        }
        MoneyBagItem.setContents(stack,contents);
        if(tag.contains("LootTable"))
        {
            ResourceKey<LootTable> lootTable = ResourceKey.create(Registries.LOOT_TABLE, VersionUtil.parseResource(tag.getString("LootTable")));
            long lootTableSeed = -1;
            if(tag.contains("LootTableSeed"))
                lootTableSeed = tag.getLong("LootTableSeed");
            stack.set(ModDataComponents.LOOT_TABLE_ENTRY,new LootTableEntry(lootTable,lootTableSeed));
        }
        return stack;
    }

}
