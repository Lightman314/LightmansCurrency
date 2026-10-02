package io.github.lightman314.lightmanscurrency.api.trader.settings_storage.transformers;

import io.github.lightman314.lightmanscurrency.api.ownership.Owner;
import io.github.lightman314.lightmanscurrency.api.trader.settings_storage.SettingsItemTransformer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
import java.util.List;

public class BookSettingTransformer extends SettingsItemTransformer.PrettyLineWriter {

    public static final SettingsItemTransformer INSTANCE = new BookSettingTransformer();
    public static final int LINES_PER_PAGE = 14;

    private BookSettingTransformer() {}

    @Override
    protected boolean isForItem(ItemStack item) { return item.is(Items.BOOK) || item.is(Items.WRITABLE_BOOK) || item.is(Items.WRITTEN_BOOK); }

    @Override
    protected ItemStack writeLinesToStack(ItemStack item,PrettyLineData data) {
        ItemStack copyStack = item.transmuteCopy(Items.WRITTEN_BOOK);
        //Build the title
        String titleString = data.machineName().getString();
        if(titleString.length() > 32)
            titleString = "placeholder";
        Filterable<String> title = Filterable.passThrough(titleString);
        //Build the author
        String author = data.player() != null ? data.player().getGameProfile().name() : Owner.GUI_OWNER_NULL.get().getString(32);
        List<Filterable<Component>> pages = new ArrayList<>();
        MutableComponent currentPage = null;
        int lines = 0;
        for(Component line : data.lines()) {
            MutableComponent copy = line.copy();
            MutableComponent testPage = currentPage == null ? copy : currentPage.copy().append("\n").append(copy);
            if(pageTooLong(testPage)) {
                pages.add(Filterable.passThrough(currentPage));
                currentPage = copy;
                lines = 1;
            }
            else {
                currentPage = testPage;
                lines++;
                if(lines >= LINES_PER_PAGE) {
                    pages.add(Filterable.passThrough(currentPage));
                    currentPage = null;
                    lines = 0;
                }
            }
        }
        if(lines > 0 && currentPage != null)
            pages.add(Filterable.passThrough(currentPage));
        WrittenBookContent content = new WrittenBookContent(title,author,0,pages,false);
        copyStack.set(DataComponents.WRITTEN_BOOK_CONTENT,content);
        return copyStack;
    }

    private static boolean pageTooLong(Component page) {
        try {
            Tag t = ComponentSerialization.CODEC.encodeStart(NbtOps.INSTANCE,page).getOrThrow();
            return t.toString().length() > 32767;
        } catch (IllegalStateException ignored) { return true; }
    }

}
