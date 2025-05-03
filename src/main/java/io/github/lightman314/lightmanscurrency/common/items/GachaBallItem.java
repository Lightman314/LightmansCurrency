package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.items.data.ItemStackData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaBallItem extends Item {

    public static final int MAX_INCEPTION_LEVEL = 16;

    public GachaBallItem(Properties properties) { super(properties.stacksTo(1)); }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack ball = player.getItemInHand(hand);
        if(ball.getItem() instanceof GachaBallItem)
        {
            ItemStack contents = ball.getOrDefault(ModDataComponents.GACHA_ITEM,ItemStackData.EMPTY).stack();
            ball.shrink(1);
            if(ball.isEmpty())
                return InteractionResultHolder.success(contents);
            else
            {
                ItemHandlerHelper.giveItemToPlayer(player,contents);
                return InteractionResultHolder.success(ball);
            }
        }
        return super.use(level, player, hand);
    }

    public static ItemStack createWithItem(ItemStack contents) { return createWithItem(contents,RandomSource.create()); }
    public static ItemStack createWithItem(ItemStack contents,RandomSource random) { return createWithItemAndColor(contents,Color.getFromIndex(random.nextInt(Color.values().length))); }
    public static ItemStack createWithItemAndColor(ItemStack contents,Color color) { return createWithItemAndColor(contents,color.hexColor); }
    public static ItemStack createWithItemAndColor(ItemStack contents,int color) {
        //Don't create a new gacha ball if we are already several gacha balls deep
        if(InventoryUtil.ItemHasTag(contents, LCTags.Items.GACHA_BLACKLIST) || inceptionLevel(contents) >= MAX_INCEPTION_LEVEL)
            return contents.copy();
        ItemStack stack = new ItemStack(ModItems.GACHA_BALL.get());
        stack.set(ModDataComponents.GACHA_ITEM,new ItemStackData(contents.copy()));
        stack.set(DataComponents.DYED_COLOR,new DyedItemColor(color,true));
        return stack;
    }

    public static int inceptionLevel(ItemStack stack)
    {
        int count = 0;
        ItemStack queryStack = stack;
        while(queryStack.getItem() == ModItems.GACHA_BALL.get())
        {
            queryStack = getContents(queryStack);
            count++;
        }
        return count;
    }

    public static ItemStack getContents(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.GACHA_ITEM,ItemStackData.EMPTY).stack();
    }

    @Override
    public boolean canFitInsideContainerItems() { return false; }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        if(flag.isAdvanced())
        {
            ItemStack contents = stack.getOrDefault(ModDataComponents.GACHA_ITEM,ItemStackData.EMPTY).stack();
            if(contents.isEmpty())
                return;
            tooltip.add(LCText.TOOLTIP_TRADER_GACHA_CONTENTS.get(contents.getCount(),contents.getHoverName()).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack,context,tooltip,flag);
    }
}
