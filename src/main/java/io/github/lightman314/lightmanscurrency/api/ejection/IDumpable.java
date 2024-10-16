package io.github.lightman314.lightmanscurrency.api.ejection;

import java.util.List;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.ejection.builtin.BasicEjectionData;
import io.github.lightman314.lightmanscurrency.api.misc.player.OwnerData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface IDumpable {

    @Nonnull
    EjectionData buildEjectionData(@Nonnull Level level, @Nonnull BlockPos pos, @Nullable BlockState state);

    static IDumpable preCollected(List<ItemStack> contents, Component name, OwnerData owner) { return new LazyDumpable(contents, name, owner); }

    class LazyDumpable implements IDumpable
    {
        private final ImmutableList<ItemStack> contents;
        private final Component name;
        private final OwnerData owner = new OwnerData(() -> true);

        protected LazyDumpable(List<ItemStack> contents, Component name, OwnerData owner) { this.contents = ImmutableList.copyOf(contents); this.name = name; this.owner.copyFrom(owner); }

        @Nonnull
        @Override
        public EjectionData buildEjectionData(@Nonnull Level level, @Nonnull BlockPos pos, @Nullable BlockState state) {
            return new BasicEjectionData(this.owner, InventoryUtil.buildInventory(this.contents),this.name);
        }

    }

}