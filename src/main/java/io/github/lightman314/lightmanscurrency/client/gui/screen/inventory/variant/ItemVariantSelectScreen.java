package io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.variant;

import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.variants.VariantProvider;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.DefaultModelVariant;
import io.github.lightman314.lightmanscurrency.api.variants.item.IVariantItem;
import io.github.lightman314.lightmanscurrency.common.menus.variant.ItemVariantSelectMenu;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemVariantSelectScreen extends VariantSelectScreen<ItemVariantSelectMenu> {

    public static final ResourceLocation GUI_TEXTURE = VersionUtil.lcResource("textures/gui/container/item_variant.png");

    private Item lastItem = Items.AIR;
    private IVariantItem lastVariant = null;
    public ItemVariantSelectScreen(ItemVariantSelectMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.resize(182,236);
    }

    @Override
    protected int getHeight() { return 2; }

    @Override
    protected void renderTick() {
        Item newItem = this.menu.getVariantStack().getItem();
        if(this.lastItem != newItem)
        {
            this.lastItem = newItem;
            this.lastVariant = VariantProvider.getVariantItem(this.lastItem);
            this.onVariantTargetChanged();
        }
    }

    @Override
    protected boolean hasValidTarget() { return this.lastVariant != null; }

    @Override
    protected List<ResourceLocation> getValidVariants() {
        if(this.lastVariant != null)
            return this.lastVariant.getValidVariants();
        return new ArrayList<>();
    }

    @Override
    protected DefaultModelVariant buildDefaultVariant() {
        return DefaultModelVariant.of(this.lastVariant);
    }

    @Override
    protected ItemLike getTargetAsItem() { return this.lastItem; }

    @Override
    protected ResourceLocation getBackgroundTexture() { return GUI_TEXTURE; }

    @Override
    protected void renderBG(EasyGuiGraphics gui) {
        super.renderBG(gui);
        //Render Inventory Label
        gui.drawString(this.playerInventoryTitle,11,142,0x404040);
    }
}
