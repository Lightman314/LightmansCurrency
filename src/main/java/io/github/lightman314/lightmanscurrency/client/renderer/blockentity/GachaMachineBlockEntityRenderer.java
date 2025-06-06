package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.GachaMachineBlock;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaStorage;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class GachaMachineBlockEntityRenderer implements BlockEntityRenderer<GachaMachineBlockEntity> {

    public static final int HEIGHT = 3;
    public static final int WIDTH = 5;
    public static final int MAX_DISPLAY_COUNT = WIDTH * WIDTH * HEIGHT;

    private final ItemRenderer itemRenderer;
    private GachaMachineBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        this.itemRenderer = context.getItemRenderer();
    }

    public static GachaMachineBlockEntityRenderer create(BlockEntityRendererProvider.Context context) { return new GachaMachineBlockEntityRenderer(context); }

    @Override
    public void render(GachaMachineBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        GachaTrader trader = be.getTraderData();
        if(trader == null)
            return;

        GachaStorage storage = trader.getStorage();
        //Render Full Contents
        if(LCConfig.CLIENT.gachaMachineFancyGraphics.get())
        {
            //Create consistent random generator for the position
            RandomSource random = RandomSource.create(Objects.hash(be.getLevel().dimension().location(),be.getBlockPos()));

            int itemsToDraw = Math.min(MAX_DISPLAY_COUNT, LCConfig.CLIENT.itemRenderLimit.get());
            if(itemsToDraw <= 0)
                return;

            List<ItemStack> contents = storage.getRandomizedContents();
            int i = 0;
            for(int y = 0; y < HEIGHT; ++y)
            {
                for(int x = 0; x < WIDTH; ++x)
                {
                    for(int z = 0; z < WIDTH; ++z)
                    {
                        if(i >= contents.size())
                            return;
                        ItemStack ball = contents.get(i++);
                        pose.pushPose();

                        pose.translate((3.5d + (x * 2.3d)) / 16d,(8.33d + (2.66d * y)) / 16d, (3.5d + (z * 2.3d)) / 16d);
                        pose.scale(6f/16f,6f/16f,6f/16f);

                        this.itemRenderer.renderStatic(ball,ItemDisplayContext.FIXED,packedLight,OverlayTexture.NO_OVERLAY,pose,bufferSource,null,packedOverlay);

                        pose.popPose();
                    }
                }
            }
        }
        //Render Simple Graphics
        else
        {
            int itemCount = storage.getItemCount();
            int modelIndex = -1;
            //Basic 1
            if(itemCount > 12 && itemCount <= 37)
                modelIndex = 0;
            //Basic 2
            else if(itemCount > 37 && itemCount <= 62)
                modelIndex = 1;
            //Basic 3
            else if(itemCount > 62)
                modelIndex = 2;
            if(modelIndex >= 0)
            {
                Quaternionf rotation = null;
                if(be.getBlockState().getBlock() instanceof IRotatableBlock rb)
                {
                    Direction facing = rb.getFacing(be.getBlockState());
                    rotation = MathUtil.fromAxisAngleDegree(MathUtil.getYP(), facing.get2DDataValue() * -90f);
                }
                List<ResourceLocation> models = GachaMachineBlock.BASIC_MODELS;
                if(be.getBlockState().getBlock() instanceof GachaMachineBlock gb)
                    models = gb.getBasicModels();

                pose.pushPose();

                //Translate to the center as items are drawn completely centered
                pose.translate(0.5f,0.5f,0.5f);

                //Rotate
                if(rotation != null)
                    pose.mulPose(rotation);

                //Render the model
                ResourceLocation modelID = models.get(modelIndex);

                BakedModel model = Minecraft.getInstance().getModelManager().getModel(ModelResourceLocation.standalone(modelID));

                this.itemRenderer.render(new ItemStack(be.getBlockState().getBlock()),ItemDisplayContext.FIXED,false,pose,bufferSource,packedLight,OverlayTexture.NO_OVERLAY,model);

                pose.popPose();

            }
        }


    }

    //Rotate Basic

}
