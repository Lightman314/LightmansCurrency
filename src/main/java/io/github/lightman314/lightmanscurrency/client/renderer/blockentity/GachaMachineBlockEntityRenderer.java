package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.GachaMachineBlockEntity;
import io.github.lightman314.lightmanscurrency.common.items.GachaBallItem;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaStorage;
import io.github.lightman314.lightmanscurrency.common.traders.gacha.GachaTrader;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;

@ParametersAreNonnullByDefault
public class GachaMachineBlockEntityRenderer implements BlockEntityRenderer<GachaMachineBlockEntity> {

    public static final int HEIGHT = 3;
    public static final int WIDTH = 5;
    public static final int MAX_DISPLAY_COUNT = WIDTH * WIDTH * HEIGHT;

    private final ItemRenderer itemRenderer;
    public GachaMachineBlockEntityRenderer(BlockEntityRendererProvider.Context context) { this.itemRenderer = context.getItemRenderer(); }

    @Override
    public void render(GachaMachineBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource bufferSource, int packedLight, int packedOverlay) {

        GachaTrader trader = be.getTraderData();
        if(trader == null)
            return;

        //Create consistent random generator for the position
        RandomSource random = RandomSource.create(Objects.hash(be.getLevel().dimension().location(),be.getBlockPos()));
        GachaStorage storage = trader.getStorage();

        int itemsToDraw = Math.min(MAX_DISPLAY_COUNT, LCConfig.CLIENT.itemRenderLimit.get());
        if(itemsToDraw <= 0)
            return;

        List<ItemStack> randomItems = storage.peekRandomItems(random,itemsToDraw);
        int i = 0;
        for(int y = 0; y < HEIGHT; ++y)
        {
            for(int x = 0; x < WIDTH; ++x)
            {
                for(int z = 0; z < WIDTH; ++z)
                {
                    if(i >= randomItems.size())
                        return;
                    ItemStack item = randomItems.get(i++);
                    ItemStack ball = GachaBallItem.createWithItem(item,random);
                    pose.pushPose();

                    pose.translate((3.5d + (x * 2.3d)) / 16d,(8.33d + (2.66d * y)) / 16d, (3.5d + (z * 2.3d)) / 16d);
                    pose.scale(6f/16f,6f/16f,6f/16f);

                    this.itemRenderer.renderStatic(ball,ItemDisplayContext.FIXED,packedLight,OverlayTexture.NO_OVERLAY,pose,bufferSource,null,packedOverlay);

                    pose.popPose();
                }
            }
        }

    }

}