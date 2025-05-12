package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.renderer.LCItemRenderer;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GachaBallItem extends Item {

    public static final int MAX_INCEPTION_LEVEL = 16;

    public GachaBallItem(Properties properties) { super(properties.stacksTo(1)); }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        super.initializeClient(consumer);
        consumer.accept(LCItemRenderer.USE_LC_RENDERER);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack ball = player.getItemInHand(hand);
        if(ball.getItem() instanceof GachaBallItem)
        {
            ItemStack contents = getContents(ball);
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
        if(InventoryUtil.ItemHasTag(contents,LCTags.Items.GACHA_BLACKLIST) || inceptionLevel(contents) >= MAX_INCEPTION_LEVEL)
            return contents.copy();
        ItemStack stack = new ItemStack(ModItems.GACHA_BALL.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("GachaItem",contents.save(new CompoundTag()));
        CompoundTag display = stack.getOrCreateTagElement("display");
        display.putInt("color",color);
        return stack;
    }

    public static ItemStack makeEmptyCopy(ItemStack gachaBall)
    {
        CompoundTag tag = gachaBall.getTagElement("display");
        int color = tag != null && tag.contains("color", Tag.TAG_ANY_NUMERIC) ? tag.getInt("color") : 0xFFFFFF;
        return createWithItemAndColor(ItemStack.EMPTY,color);
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
        CompoundTag tag = stack.getTag();
        if(tag != null && tag.contains("GachaItem"))
            return ItemStack.of(tag.getCompound("GachaItem"));
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canFitInsideContainerItems() { return false; }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltip, TooltipFlag flag) {
        if(flag.isAdvanced())
        {
            CompoundTag tag = stack.getTag();
            if(tag == null || !tag.contains("GachaItem"))
                return;
            ItemStack contents = ItemStack.of(tag.getCompound("GachaItem"));
            if(contents.isEmpty())
                return;
            tooltip.add(LCText.TOOLTIP_TRADER_GACHA_CONTENTS.get(contents.getCount(),contents.getHoverName()).withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack,context,tooltip,flag);
    }

}