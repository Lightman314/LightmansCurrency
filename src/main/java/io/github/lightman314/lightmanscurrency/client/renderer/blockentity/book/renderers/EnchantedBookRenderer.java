package io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRendererGenerator;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.SimpleBookRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;

public class EnchantedBookRenderer extends SimpleBookRenderer {

    public static final ResourceLocation MODEL_LOCATION = new ResourceLocation(LightmansCurrency.MODID, "block/bookshelf_trader/books/enchanted");

    public static final BookRendererGenerator GENERATOR = new Generator();

    private EnchantedBookRenderer(ItemStack book) { super(book); }

    @Override
    protected ResourceLocation getBookModel() { return MODEL_LOCATION; }

    private static class Generator implements BookRendererGenerator
    {
        @Nullable
        @Override
        public BookRenderer createRendererForItem(@Nonnull ItemStack book) {
            if(book.getItem() == Items.ENCHANTED_BOOK)
                return new EnchantedBookRenderer(book);
            return null;
        }
    }

}
