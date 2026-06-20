package io.github.lightman314.lightmanscurrency.core;

import com.google.common.collect.ImmutableSet;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.trader.item.blocks.ItemTraderBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

public final class LCBlockEntities {
    private LCBlockEntities() {}

    public static final DeferredRegister<BlockEntityType<?>> REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE,LCApi.MODID);

    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<ItemTraderBlockEntity>> ITEM_TRADER = register2("item_trader",ItemTraderBlockEntity::new,merge(LCBlocks.DISPLAY_CASE.getFutureSet()));

    @SafeVarargs
    public static Supplier<Set<Block>> easySet(Supplier<? extends Block>... blocks) {
        return () -> {
            Set<Block> set = new HashSet<>();
            for(var sup : blocks)
                set.add(sup.get());
            return set;
        };
    }
    @SafeVarargs
    public static Supplier<Set<Block>> merge(Supplier<Set<Block>>... blockSources) {
        return () -> {
            Set<Block> set = new HashSet<>();
            for(var sup : blockSources)
                set.addAll(sup.get());
            return set;
        };
    }
    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>,BlockEntityType<T>> register1(String name,BlockEntityType.BlockEntitySupplier<T> factory,Supplier<Block[]> blocks) {
        return register2(name,factory,() -> ImmutableSet.copyOf(blocks.get()));
    }
    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>,BlockEntityType<T>> register2(String name, BlockEntityType.BlockEntitySupplier<T> factory,Supplier<Set<Block>> blocks) {
        return REGISTER.register(name,() -> new BlockEntityType<>(factory,blocks.get()));
    }

}