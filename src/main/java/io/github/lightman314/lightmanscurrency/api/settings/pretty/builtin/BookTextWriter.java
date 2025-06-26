package io.github.lightman314.lightmanscurrency.api.settings.pretty.builtin;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextData;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BookTextWriter extends PrettyTextWriter {

    public static final PrettyTextWriter INSTANCE = new BookTextWriter();
    public static final int LINES_PER_PAGE = 14;

    private BookTextWriter() {}

    //Apply to written books or items flagged as being transformed into a written book
    @Override
    public boolean worksOnStack(ItemStack stack) { return stack.getItem() == Items.WRITTEN_BOOK || InventoryUtil.ItemHasTag(stack, LCTags.Items.SETTINGS_REPLACE_WITH_WRITTEN_BOOK); }

    @Override
    public ItemStack writeLinesToStack(@Nullable Player player, ItemStack stack, PrettyTextData data) {
        ItemStack copyStack = stack.transmuteCopy(Items.WRITTEN_BOOK);
        //Build the title
        String titleString = data.machineName().getString();
        if(titleString.length() > 32)
            titleString = LCText.DATA_NAME_BACKUP.get().getString(32);
        Filterable<String> title = Filterable.passThrough(titleString);
        //Build the author
        String author = player != null ? player.getGameProfile().getName() : LCText.GUI_OWNER_NULL.get().getString();
        List<Filterable<Component>> pages = new ArrayList<>();
        MutableComponent currentPage = null;
        int lines = 0;
        for(Component line : data.lines())
        {
            MutableComponent copy = line.copy();
            MutableComponent testPage = currentPage == null ? copy : currentPage.copy().append("\n").append(copy);
            if(pageTooLong(testPage))
            {
                pages.add(Filterable.passThrough(currentPage));
                currentPage = copy;
                lines = 1;
            }
            else
            {
                currentPage = testPage;
                lines++;
                if(lines >= LINES_PER_PAGE)
                {
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

    private static boolean pageTooLong(Component page)
    {
        return Component.Serializer.toJson(page,LookupHelper.getRegistryAccess()).length() > 32767;
    }

}
