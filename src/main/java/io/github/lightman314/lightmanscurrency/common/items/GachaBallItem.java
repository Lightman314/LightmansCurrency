package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.client.renderer.LCItemRenderer;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
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
            ItemStack contents = ItemStack.EMPTY;
            CompoundTag tag = ball.getTag();
            if(tag != null && tag.contains("GachaItem"))
                contents = ItemStack.of(tag.getCompound("GachaItem"));
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
        ItemStack stack = new ItemStack(ModItems.GACHA_BALL.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.put("GachaItem",contents.save(new CompoundTag()));
        CompoundTag display = stack.getOrCreateTagElement("display");
        display.putInt("color",color);
        return stack;
    }

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
    }

}