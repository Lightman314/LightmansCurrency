package io.github.lightman314.lightmanscurrency.client.renderer;

import com.google.common.base.Suppliers;
import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.renderer.item.GachaBallRenderer;
import io.github.lightman314.lightmanscurrency.common.items.GachaBallItem;
import io.github.lightman314.lightmanscurrency.util.DebugUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class LCItemRenderer extends BlockEntityWithoutLevelRenderer {

    private final BlockEntityRenderDispatcher blockEntityRenderDispatcher;
    private LCItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet set) { super(dispatcher, set); this.blockEntityRenderDispatcher = dispatcher;  }

    public static final Supplier<BlockEntityWithoutLevelRenderer> INSTANCE = Suppliers.memoize(() -> new LCItemRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels()));

    public static final IClientItemExtensions USE_LC_RENDERER = new IClientItemExtensions() {
        @Nonnull
        public BlockEntityWithoutLevelRenderer getCustomRenderer() { return INSTANCE.get(); }
    };

    private static List<Function<Block, BlockEntity>> beSources;
    @Nonnull
    private static List<Function<Block, BlockEntity>> getBeSources() { if(beSources == null) beSources = new ArrayList<>(); return beSources; }

    public static void registerBlockEntitySource(@Nonnull Function<Block, BlockEntity> beSource) { getBeSources().add(beSource); }

    private static BlockEntity findBE(Block block)
    {
        BlockEntity be;
        for(Function<Block, BlockEntity> source : beSources)
        {
            be = source.apply(block);
            if(be != null)
                return be;
        }
        return null;
    }

    private boolean debug = false;

    @Override
    public void renderByItem(@Nonnull ItemStack stack, @Nonnull ItemDisplayContext context, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffer, int lightLevel, int id) {
        Item item = stack.getItem();
        if(item instanceof BlockItem bi)
        {
            Block block = bi.getBlock();
            BlockEntity be = findBE(block);
            if(be != null)
            {
                if(!this.debug)
                {
                    LightmansCurrency.LogDebug("Rendered custom block entity for " + DebugUtil.getItemDebug(stack) + " item!");
                    this.debug = true;
                }
                this.blockEntityRenderDispatcher.renderItem(be, pose, buffer, lightLevel, id);
            }
        }
        else if(item instanceof GachaBallItem)
            GachaBallRenderer.renderGachaBall(stack,pose,buffer,lightLevel,id);
    }

}
