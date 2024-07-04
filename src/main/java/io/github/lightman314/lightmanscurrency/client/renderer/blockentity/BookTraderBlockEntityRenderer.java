package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRenderer;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.BookTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.item.ItemTraderData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import javax.annotation.Nonnull;
import java.util.List;

public class BookTraderBlockEntityRenderer implements BlockEntityRenderer<BookTraderBlockEntity> {

    public BookTraderBlockEntityRenderer(BlockEntityRendererProvider.Context ignored) { }

    @Override
    public void render(@Nonnull BookTraderBlockEntity blockEntity, float partialTicks, @Nonnull PoseStack pose, @Nonnull MultiBufferSource buffer, int lightLevel, int id)
    {
        ItemTraderData trader = blockEntity.getTraderData();
        if(trader == null)
            return;

        for(int tradeSlot = 0; tradeSlot < trader.getTradeCount() && tradeSlot < blockEntity.maxRenderIndex(); tradeSlot++)
        {

            ItemTradeData trade = trader.getTrade(tradeSlot);
            if(trade.hasStock(trader))
            {
                BookRenderer renderer = GetRenderer(ItemTraderBlockEntityRenderer.GetRenderItems(trade));

                if(renderer != null)
                {
                    pose.pushPose();
                    //Offset pose

                    Vector3f offset = blockEntity.GetBookRenderPos(tradeSlot);

                    List<Quaternionf> rotations = blockEntity.GetBookRenderRot(tradeSlot);

                    float scale = blockEntity.GetBookRenderScale(tradeSlot);

                    pose.translate(offset.x, offset.y, offset.z);

                    for(Quaternionf r : rotations)
                        pose.mulPose(r);

                    pose.scale(scale, scale, scale);

                    renderer.render(blockEntity, partialTicks, pose, buffer, lightLevel, id);

                    pose.popPose();
                }
            }
        }

    }

    private static BookRenderer GetRenderer(List<ItemStack> renderItems) {
        for(ItemStack book : renderItems)
        {
            BookRenderer renderer = BookRenderer.GetRenderer(book);
            if(renderer != null)
                return renderer;
        }
        return null;
    }


}
