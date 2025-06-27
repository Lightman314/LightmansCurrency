package io.github.lightman314.lightmanscurrency.integration.create.pretty_settings;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.equipment.clipboard.ClipboardEntry;
import com.simibubi.create.content.equipment.clipboard.ClipboardOverrides;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextData;
import io.github.lightman314.lightmanscurrency.api.settings.pretty.PrettyTextWriter;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ClipboardPrettyWriter extends PrettyTextWriter {

    public static PrettyTextWriter INSTANCE = new ClipboardPrettyWriter();
    public static final int LINES_PER_PAGE = 15;

    private ClipboardPrettyWriter() {}

    @Override
    public boolean worksOnStack(ItemStack stack) { return stack.getItem() == AllBlocks.CLIPBOARD.asItem(); }

    @Override
    public ItemStack writeLinesToStack(@Nullable Player player, ItemStack stack, PrettyTextData data) {
        List<List<ClipboardEntry>> pages = new ArrayList<>();
        List<ClipboardEntry> currentPage = new ArrayList<>();
        for(Component line : data.lines())
        {
            MutableComponent copy = line.copy().withStyle(Style.EMPTY);
            currentPage.add(new ClipboardEntry(false,copy));
            if(currentPage.size() >= LINES_PER_PAGE)
            {
                pages.add(ImmutableList.copyOf(currentPage));
                currentPage = new ArrayList<>();
            }
        }
        if(!currentPage.isEmpty())
            pages.add(ImmutableList.copyOf(currentPage));
        CompoundTag tag = stack.getOrCreateTag();
        ClipboardEntry.saveAll(pages,stack);
        tag.putBoolean("Readonly",true);
        tag.putInt("Type",ClipboardOverrides.ClipboardType.WRITTEN.ordinal());
        stack.setHoverName(data.machineName());
        return stack;
    }

}