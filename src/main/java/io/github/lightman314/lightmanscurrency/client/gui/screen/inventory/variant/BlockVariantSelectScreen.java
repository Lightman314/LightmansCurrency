package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.variant;

import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.DefaultModelVariant;
import io.github.lightman314.lightmanscurrency.api.variants.block.IVariantBlock;
import io.github.lightman314.lightmanscurrency.common.menus.variant.BlockVariantSelectMenu;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockVariantSelectScreen extends VariantSelectScreen<BlockVariantSelectMenu> {

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/block_variant.png");

    public BlockVariantSelectScreen(BlockVariantSelectMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.resize(182,182);
    }

    @Override
    protected boolean hasValidTarget() { return this.menu.getVariantBlock() != null; }
    @Override
    protected List<ResourceLocation> getValidVariants() {
        IVariantBlock block = this.menu.getVariantBlock();
        if(block == null)
            return new ArrayList<>();
        return block.getValidVariants();
    }

    @Override
    protected DefaultModelVariant buildDefaultVariant() { return DefaultModelVariant.of(this.menu.getVariantBlock()); }
    @Override
    protected ItemLike getTargetAsItem() { return this.menu.getBlock(); }

    @Override
    protected ResourceLocation getBackgroundTexture() { return GUI_TEXTURE; }

}
