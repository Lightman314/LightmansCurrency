package io.github.lightman314.lightmanscurrency.api.misc;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class BlockProtectionHelper {

    private static final List<BiPredicate<BlockState, BlockEntity>> PROTECTED_CHECKS = new ArrayList<>();

    public static boolean ShouldProtect(@Nonnull BlockState state, @Nullable BlockEntity blockEntity)
    {
        return PROTECTED_CHECKS.stream().anyMatch(p -> p.test(state,blockEntity));
    }

    public static void ProtectBlockTag(@Nonnull TagKey<Block> tag) { ProtectBlock(b -> {
        for(Holder<Block> holder : BuiltInRegistries.BLOCK.getTagOrEmpty(tag))
        {
            if(holder.value() == b)
                return true;
        }
        return false;
    });}
    public static void ProtectBlock(@Nonnull Supplier<Block> block) { ProtectBlock(b -> b == block.get());}
    public static void ProtectBlock(@Nonnull Predicate<Block> predicate) { Protect((s,e) -> predicate.test(s.getBlock())); }
    public static void ProtectBlockState(@Nonnull Predicate<BlockState> predicate) { Protect((s,e) -> predicate.test(s)); }
    public static void ProtectBlockEntity(@Nonnull Supplier<BlockEntityType<?>> type) { ProtectBlockEntity(e -> e != null && e.getType() == type.get());}
    public static void ProtectBlockEntity(@Nonnull Predicate<BlockEntity> predicate) { Protect((s,e) -> predicate.test(e));}
    public static void Protect(@Nonnull BiPredicate<BlockState,BlockEntity> predicate)
    {
        if(predicate != null)
            PROTECTED_CHECKS.add(Objects.requireNonNull(predicate));
    }

}