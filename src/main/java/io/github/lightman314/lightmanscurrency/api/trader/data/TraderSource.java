package io.github.lightman314.lightmanscurrency.api.trader.data;

import com.google.common.collect.Lists;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public interface TraderSource {

    default boolean isSimple() { return this.getSimpleTrader() != null; }
    @Nullable
    TraderData getSimpleTrader();
    List<TraderData> getTraders();
    @Nullable
    default Component getNameOverride() { return null; }
    default boolean forceSearch() { return false; }

    static TraderSource blockEntity(Level level,BlockPos pos) {
        return blockEntity(level,pos,be -> {
            if(be instanceof TraderSource s)
                return s;
            return null;
        });
    }
    static TraderSource blockEntity(Level level,BlockPos pos,Function<BlockEntity,TraderSource> source)
    {
        return wrap(() -> {
            BlockEntity be = level.getBlockEntity(pos);
            return source.apply(be);
        });
    }

    static TraderSource wrap(Supplier<TraderSource> supplier) { return new Wrapper(supplier); }

    interface Simple extends TraderSource {
        default boolean isSimple() { return true; }
        default List<TraderData> getTraders() { return Lists.newArrayList(this.getSimpleTrader()); }
    }

    interface Multi extends TraderSource {
        default boolean isSimple() { return false; }
        @Override
        @Nullable
        default TraderData getSimpleTrader() { return null; }
    }

    final class Wrapper implements TraderSource
    {
        private final Supplier<TraderSource> source;
        private Wrapper(Supplier<TraderSource> source) { this.source = source; }
        @Override
        public boolean isSimple() {
            TraderSource s = this.source.get();
            return s != null && s.isSimple();
        }
        @Override
        @Nullable
        public TraderData getSimpleTrader() {
            TraderSource s = this.source.get();
            return s == null ? null : s.getSimpleTrader();
        }
        @Override
        public List<TraderData> getTraders() {
            TraderSource s = this.source.get();
            return s == null ? new ArrayList<>() : s.getTraders();
        }
        @Override
        @Nullable
        public Component getNameOverride() {
            TraderSource s = this.source.get();
            return s == null ? null : s.getNameOverride();
        }
        @Override
        public boolean forceSearch() {
            TraderSource s = this.source.get();
            return s != null && s.forceSearch();
        }
    }

}