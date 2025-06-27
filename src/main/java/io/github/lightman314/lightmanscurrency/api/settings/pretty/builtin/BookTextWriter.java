package io.github.lightman314.lightmanscurrency.api.settings.pretty.builtin;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextData;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import io.github.lightman314.lightmanscurrency.common.util.TagUtil;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

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
        ItemStack copyStack = new ItemStack(Items.WRITTEN_BOOK);
        CompoundTag tag = stack.getOrCreateTag().copy();
        //Build the title
        String titleString = data.machineName().getString();
        if(titleString.length() > 32)
            titleString = LCText.DATA_NAME_BACKUP.get().getString(32);
        tag.putString("title",titleString);
        //Build the author
        String author = player != null ? player.getGameProfile().getName() : LCText.GUI_OWNER_NULL.get().getString();
        tag.putString("author",author);
        tag.putInt("generation",0);
        List<String> pages = new ArrayList<>();
        MutableComponent currentPage = null;
        int lines = 0;
        for(Component line : data.lines())
        {
            MutableComponent copy = line.copy();
            MutableComponent testPage = currentPage == null ? copy : currentPage.copy().append("\n").append(copy);
            if(pageTooLong(testPage))
            {
                pages.add(Component.Serializer.toJson(currentPage));
                currentPage = copy;
                lines = 1;
            }
            else
            {
                currentPage = testPage;
                lines++;
                if(lines >= LINES_PER_PAGE)
                {
                    pages.add(Component.Serializer.toJson(currentPage));
                    currentPage = null;
                    lines = 0;
                }
            }
        }
        if(lines > 0 && currentPage != null)
            pages.add(Component.Serializer.toJson(currentPage));
        tag.put("pages", TagUtil.writeStringList(pages));

        copyStack.setTag(tag);
        return copyStack;
    }

    private static boolean pageTooLong(Component page)
    {
        return Component.Serializer.toJson(page).length() > 32767;
    }

}