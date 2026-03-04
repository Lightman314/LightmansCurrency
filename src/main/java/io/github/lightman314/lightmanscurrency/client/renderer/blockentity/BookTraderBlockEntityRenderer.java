package io.github.lightman314.lightmanscurrency.client.renderer.blockentity;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.traders.data.TraderData;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRenderer;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.BookTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemStorageNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.nodes.ItemTradeNode;
import io.github.lightman314.lightmanscurrency.common.traders.item.storage.TraderItemStorage;
import io.github.lightman314.lightmanscurrency.common.traders.item.trade.ItemTradeData;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class BookTraderBlockEntityRenderer implements BlockEntityRenderer<BookTraderBlockEntity> {

    private BookTraderBlockEntityRenderer() { }

    public static BookTraderBlockEntityRenderer create(BlockEntityRendererProvider.Context ignored) { return new BookTraderBlockEntityRenderer(); }

    @Override
    public void render(BookTraderBlockEntity blockEntity, float partialTicks, PoseStack pose, MultiBufferSource buffer, int lightLevel, int id)
    {
        //Don't render books when the item render limit is 0
        if(LCConfig.CLIENT.itemRenderLimit.get() <= 0)
            return;

        TraderData trader = blockEntity.getTraderData();
        if(trader == null)
            return;
        ItemTradeNode tradeNode = trader.getNode(ItemTradeNode.TYPE);
        ItemStorageNode storageNode = trader.getNode(ItemStorageNode.TYPE);
        if(tradeNode == null || storageNode == null)
            return;
        TraderItemStorage storage = storageNode.getStorage();
        boolean infiniteStock = trader.hasInfiniteStock();
        for(int tradeSlot = 0; tradeSlot < trader.getTradeCount() && tradeSlot < blockEntity.maxRenderIndex(); tradeSlot++)
        {
            ItemTradeData trade = tradeNode.getTrade(tradeSlot);
            if(trade.hasStock(trader))
            {
                BookRenderer renderer = GetRenderer(ItemTraderBlockEntityRenderer.GetRenderItems(trade,storage,infiniteStock));

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
