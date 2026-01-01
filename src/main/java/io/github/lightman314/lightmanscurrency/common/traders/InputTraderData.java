package io.github.lightman314.lightmanscurrency.common.traders;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;

/**
 * @deprecated Extend {@link io.github.lightman314.lightmanscurrency.common.traders.input.InputTraderData InputTraderData} instead
 */
@Deprecated(since = "2.3.0.4")
public abstract class InputTraderData extends io.github.lightman314.lightmanscurrency.common.traders.input.InputTraderData {
    protected InputTraderData(TraderType<?> type) { super(type);}
    protected InputTraderData(TraderType<?> type, ImmutableList<Direction> ignoreSides) { super(type, ignoreSides); }
    protected InputTraderData(TraderType<?> type, Level level, BlockPos pos) { super(type, level, pos); }
    protected InputTraderData(TraderType<?> type, Level level, BlockPos pos, ImmutableList<Direction> ignoreSides) { super(type, level, pos, ignoreSides); }
}