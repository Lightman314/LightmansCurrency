package io.github.lightman314.lightmanscurrency.features.network_terminal;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.trader.world.menu.terminal.TradingTerminalMenu;
import io.github.lightman314.lightmanscurrency.api.world.block.RotatableBlock;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.BlockValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.function.Function;

public class TradingTerminalBlock extends RotatableBlock {

    public TradingTerminalBlock(Properties properties) { super(properties); }
    public TradingTerminalBlock(Properties properties,VoxelShape shape) { super(properties,shape); }
    public TradingTerminalBlock(Properties properties,Function<Direction,VoxelShape> shape) { super(properties,shape); }

    public static final TextEntry MESSAGE_QUARANTINED = TextEntry.message(LCApi.MODID,"dimension_quarantined.terminal");

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if(!level.isClientSide()) {
            //If the dimension is quarantined, deny access to the terminal
            if(LCApi.getQuarantineAPI().isQuarantined(level)) {
                player.sendSystemMessage(MESSAGE_QUARANTINED.get());
                return InteractionResult.SUCCESS;
            }
            player.openMenu(TradingTerminalMenu.createProvider(new BlockValidator(this,pos)));
        }
        return InteractionResult.SUCCESS;
    }

}
