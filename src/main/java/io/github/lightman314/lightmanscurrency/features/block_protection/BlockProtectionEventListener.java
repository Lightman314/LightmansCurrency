package io.github.lightman314.lightmanscurrency.features.block_protection;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IProtectedBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

import java.util.Optional;

@EventBusSubscriber
public class BlockProtectionEventListener {

    @SubscribeEvent
    private static void onBlockBreak(BreakBlockEvent event) {
        //No protection in anarchy mode
        if(LCConfig.SERVER.anarchyMode.get())
            return;
        LevelAccessor level = event.getLevel();
        BlockState state = event.getState();
        if(state.getBlock() instanceof IProtectedBlock pb && !pb.canBreakBlock(level,state,event.getPos(),event.getPlayer())) {
            //Cancel the event if the player cannot break this block
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    private static void blockBreakSpeed(PlayerEvent.BreakSpeed event) {
        //No protection in anarchy mode
        if(LCConfig.SERVER.anarchyMode.get())
            return;

        BlockState state = event.getState();
        Optional<BlockPos> pos = event.getPosition();
        if(pos.isPresent() && state.getBlock() instanceof IProtectedBlock pb) {
            if(!pb.canBreakBlock(event.getEntity().level(),state,pos.get(),event.getEntity()))
                event.setCanceled(true);
        }

    }

}
