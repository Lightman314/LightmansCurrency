package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.api.misc.blocks.IOwnableBlock;
import io.github.lightman314.lightmanscurrency.common.blocks.variant.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.menus.VariantSelectMenu;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class VariantWandItem extends Item {

    public VariantWandItem(Properties properties) { super(properties.stacksTo(1)); }

    public static boolean tryUseWand(Player player, BlockPos pos)
    {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        if(state.getBlock() instanceof IVariantBlock variantBlock)
        {
            //Deny changing the variant if you don't have break-level permissions
            if(state.getBlock() instanceof IOwnableBlock ownable && !ownable.canBreak(player,level,pos,state))
                return false;
            if(level.isClientSide)
                return true;
            player.openMenu(VariantSelectMenu.providerFor(pos),pos);
            return true;
        }
        //Don't do anything if it's not a variant supporting block
        return false;
    }

}
