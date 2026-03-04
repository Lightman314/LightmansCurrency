package io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRendererGenerator;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.SimpleBookRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

public class EnchantedBookRenderer extends SimpleBookRenderer {

    public static final ModelResourceLocation MODEL_LOCATION = modelLocation(LightmansCurrency.id( "block/bookshelf_trader/books/enchanted"));

    public static final BookRendererGenerator GENERATOR = new Generator();

    private EnchantedBookRenderer(ItemStack book) { super(book); }

    @Override
    protected ModelResourceLocation getBookModel() { return MODEL_LOCATION; }

    private static class Generator implements BookRendererGenerator
    {
        @Nullable
        @Override
        public BookRenderer createRendererForItem(ItemStack book) {
            if(book.getItem() == Items.ENCHANTED_BOOK)
                return new EnchantedBookRenderer(book);
            return null;
        }
    }

}
