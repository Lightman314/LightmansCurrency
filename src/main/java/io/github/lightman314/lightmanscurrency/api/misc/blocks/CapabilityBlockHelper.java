package io.github.lightman314.lightmanscurrency.api.misc.blocks;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.capabilities.IBlockCapabilityProvider;

import java.util.ArrayList;
import java.util.List;

public class CapabilityBlockHelper {

    private static boolean loopGuard = false;
    private static Block[] capabilityBlocks = null;
    private static Block[] getCapabilityBlocks() {
        if(capabilityBlocks != null)
        {
            List<Block> list = new ArrayList<>();
            for(Block b : BuiltInRegistries.BLOCK)
            {
                if(b instanceof ICapabilityBlock)
                    list.add(b);
            }
            capabilityBlocks = list.toArray(Block[]::new);
        }
        return capabilityBlocks;
    }

    public static <T,C> IBlockCapabilityProvider<T,C> wrapProvider(IBlockCapabilityProvider<T,C> provider)
    {
        return (level,pos,state,be,context) -> {
            if(loopGuard)
            {
                loopGuard = false;
                return null;
            }
            if(state.getBlock() instanceof ICapabilityBlock handlerBlock)
            {
                BlockPos newPos = handlerBlock.getCapabilityBlockPos(state, pos);
                if(newPos.equals(pos))
                    return null;
                loopGuard = true;
                return provider.getCapability(level,newPos,state,be,context);
            }
            return null;
        };
    }

}
