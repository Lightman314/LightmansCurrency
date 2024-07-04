package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.client.renderer.LCItemRenderer;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

public class CustomBlockModelItem extends BlockItem {

    public CustomBlockModelItem(Block block, Properties properties) { super(block, properties); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(@Nonnull Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(LCItemRenderer.USE_LC_RENDERER);
    }

}
