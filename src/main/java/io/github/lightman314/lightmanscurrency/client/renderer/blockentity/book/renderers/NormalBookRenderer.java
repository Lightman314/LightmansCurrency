package io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.renderers;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRenderer;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.BookRendererGenerator;
import io.github.lightman314.lightmanscurrency.client.renderer.blockentity.book.SimpleBookRenderer;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;

public class NormalBookRenderer extends SimpleBookRenderer {

    public static final ResourceLocation MODEL_LOCATION = VersionUtil.lcResource("block/bookshelf_trader/books/book");


    public static final NormalBookRenderer INSTANCE = new NormalBookRenderer(new ItemStack(Items.BOOK));
    public static final BookRendererGenerator GENERATOR = new Generator();
    private NormalBookRenderer(ItemStack book) { super(book); }

    @Override
    protected ResourceLocation getBookModel() { return MODEL_LOCATION; }

    private static class Generator implements BookRendererGenerator {

        private final List<Item> bookItems = ImmutableList.of(Items.BOOK, Items.WRITABLE_BOOK, Items.WRITTEN_BOOK);
        @Nullable
        @Override
        public BookRenderer createRendererForItem(@Nonnull ItemStack book) {
            if(this.bookItems.contains(book.getItem()))
                return INSTANCE;
            return null;
        }
    }

}
