package io.github.lightman314.lightmanscurrency.api.variants.block;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.*;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.common.blockentity.variant.IVariantSupportingBlockEntity;
import io.github.lightman314.lightmanscurrency.common.menus.variant.BlockVariantSelectMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public interface IVariantBlock {

    BooleanProperty VARIANT = BooleanProperty.create("variant");

    default int getModelIndex(BlockState state)
    {
        int index = 0;
        boolean wide = this instanceof IWideBlock;
        boolean tall = this instanceof ITallBlock;
        boolean deep = this instanceof IDeepBlock;
        if(wide && !state.getValue(IWideBlock.ISLEFT))
            index += 1;
        if(deep && !state.getValue(IDeepBlock.IS_FRONT))
            index += wide ? 2 : 1;
        if(tall && !state.getValue(ITallBlock.ISBOTTOM))
            index += wide && deep ? 4 : (wide || deep ? 2 : 1);
        return index;
    }

    default ResourceLocation getBlockID()
    {
        if(this instanceof Block block)
            return BuiltInRegistries.BLOCK.getKey(block);
        throw new IllegalStateException("IVariantBlock must be applied to a Block class!");
    }

    default ResourceLocation getItemID()
    {
        if(this instanceof ItemLike item)
            return BuiltInRegistries.ITEM.getKey(item.asItem());
        else
            return getBlockID();
    }

    default boolean isRotatable() { return this instanceof IRotatableBlock; }

    default List<ResourceLocation> getValidVariants() { return ModelVariantDataManager.getPotentialVariants(this.getBlockID()); }

    default int requiredModels() { return this.modelsFromBlockState(); }

    default int modelsFromBlockState()
    {
        int count = 1;
        if(this instanceof IWideBlock)
            count *= 2;
        if(this instanceof ITallBlock)
            count *= 2;
        if(this instanceof IDeepBlock)
            count *= 2;
        return count;
    }

    @Nullable
    default ResourceLocation getCustomDefaultModel(int index) { return null; }

    static boolean tryUseWand(Player player, BlockPos pos)
    {
        Level level = player.level();
        BlockState state = level.getBlockState(pos);
        IVariantBlock block = VariantProvider.getVariantBlock(state.getBlock());
        if(block != null)
        {
            //Deny changing the variant if you don't have break-level permissions
            if(state.getBlock() instanceof IOwnableBlock ownable && !ownable.canBreak(player,level,pos,state))
                return false;
            if(level.isClientSide)
                return true;
            //Prevent opening the menu if the
            if(level.getBlockEntity(pos) instanceof IVariantSupportingBlockEntity be && be.isVariantLocked() && !player.isCreative())
            {
                player.displayClientMessage(LCText.TOOLTIP_MODEL_VARIANT_LOCKED.getWithStyle(ChatFormatting.RED),true);
                return true;
            }
            player.openMenu(BlockVariantSelectMenu.providerFor(pos),pos);
            return true;
        }
        //Don't do anything if it's not a variant supporting block
        return false;
    }

}
