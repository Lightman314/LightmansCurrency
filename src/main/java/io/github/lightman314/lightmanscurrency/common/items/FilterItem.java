package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.common.items.data.FilterData;
import io.github.lightman314.lightmanscurrency.common.menus.ItemFilterMenu;
import io.github.lightman314.lightmanscurrency.api.filter.IItemTradeFilter;
import io.github.lightman314.lightmanscurrency.common.util.TooltipHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FilterItem extends Item implements IItemTradeFilter {

    public FilterItem(Properties properties) { super(properties); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltip, TooltipFlag flag) {
        List<Component> extraTooltips = this.getCustomTooltip(stack);
        if(extraTooltips != null)
            tooltip.addAll(extraTooltips);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack heldItem = player.getItemInHand(hand);
        if(!level.isClientSide)
        {
            int indexOfItem = -1;
            Inventory inv = player.getInventory();
            for(int i = 0; i < inv.getContainerSize() && indexOfItem < 0; ++i)
            {
                if(inv.getItem(i) == heldItem)
                    indexOfItem = i;
            }
            if(indexOfItem >= 0)
            {
                ItemFilterMenu.OpenMenu(player,indexOfItem);
            }
        }
        return InteractionResultHolder.success(heldItem);
    }

    @Nullable
    public Predicate<ItemStack> getFilter(ItemStack stack)
    {
        FilterData filter = FilterData.parse(stack);
        if(filter.isEmpty())
            return null;
        return filter.asItemPredicate();
    }

    @Override
    public List<ItemStack> getDisplayableItems(ItemStack stack,@Nullable IItemHandler availableItems) {
        FilterData filter = FilterData.parse(stack);
        Set<Item> items = new HashSet<>();
        for(ResourceLocation entry : filter.entries())
        {
            Item i = ForgeRegistries.ITEMS.getValue(entry);
            if(i != Items.AIR && shouldDisplayItem(i,availableItems))
                items.add(i);
        }
        for(ResourceLocation tag : filter.tags())
        {
            TagKey<Item> tagKey = TagKey.create(ForgeRegistries.ITEMS.getRegistryKey(),tag);
            for(Item i : ForgeRegistries.ITEMS.tags().getTag(tagKey))
            {
                if(i != Items.AIR && shouldDisplayItem(i,availableItems))
                    items.add(i);
            }
        }
        return items.stream().map(i -> new ItemStack(i,stack.getCount())).toList();
    }

    @Nullable
    @Override
    public List<Component> getCustomTooltip(ItemStack stack) {
        FilterData filter = FilterData.parse(stack);
        if(filter.isEmpty())
            return null;
        List<Component> tooltip = new ArrayList<>();
        //List Items
        if(!filter.entries().isEmpty())
        {
            tooltip.add(LCText.TOOLTIP_ITEM_TRADE_FILTER_ITEM_LABEL.getWithStyle(ChatFormatting.GOLD));
            MutableComponent itemLine = EasyText.empty();
            boolean notFirst = false;
            for(ResourceLocation entry : filter.entries())
            {
                if(notFirst)
                    itemLine.append(LCText.GUI_SEPERATOR.get());
                Item item = ForgeRegistries.ITEMS.getValue(entry);
                if(item != Items.AIR)
                {
                    itemLine.append(new ItemStack(item).getHoverName());
                    notFirst = true;
                }
            }
            tooltip.addAll(TooltipHelper.splitTooltips(itemLine));
        }
        if(!filter.tags().isEmpty())
        {
            tooltip.add(LCText.TOOLTIP_ITEM_TRADE_FILTER_TAG_LABEL.getWithStyle(ChatFormatting.GOLD));
            MutableComponent tagLine = EasyText.empty();
            boolean notFirst = false;
            for(ResourceLocation entry : filter.tags())
            {
                if(notFirst)
                    tagLine.append(LCText.GUI_SEPERATOR.get());
                tagLine.append("#" + entry.toString());
                notFirst = true;
            }
            tooltip.addAll(TooltipHelper.splitTooltips(tagLine));
        }
        return tooltip;
    }

    private static boolean shouldDisplayItem(Item item, @Nullable IItemHandler availableItems)
    {
        if(availableItems == null)
            return true;
        for(int i = 0; i < availableItems.getSlots(); ++i)
        {
            if(availableItems.getStackInSlot(i).getItem() == item)
                return true;
        }
        return false;
    }

}
