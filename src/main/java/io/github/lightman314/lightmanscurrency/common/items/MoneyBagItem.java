package io.github.lightman314.lightmanscurrency.common.items;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.MoneyBagBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.data.types.LootTableEntry;
import io.github.lightman314.lightmanscurrency.common.items.data.MoneyBagData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoneyBagItem extends BlockItem {

    public static final ResourceLocation PROPERTY = VersionUtil.lcResource("money_bag_size");

    public MoneyBagItem(Block block, Properties properties) { super(block,properties.stacksTo(1)); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_MONEY_BAG);

        List<ItemStack> contents = getContents(stack);
        if(!contents.isEmpty())
        {
            if(flag.hasControlDown())
            {
                for (ItemStack coin : contents) {
                    if (coin.getCount() > 1)
                        tooltip.add(LCText.TOOLTIP_COIN_JAR_CONTENTS_MULTIPLE.get(coin.getCount(), coin.getHoverName()));
                    else
                        tooltip.add(LCText.TOOLTIP_COIN_JAR_CONTENTS_SINGLE.get(coin.getHoverName()));
                }
            }
            else
                tooltip.add(LCText.TOOLTIP_COIN_JAR_HOLD_CTRL.get().withStyle(ChatFormatting.YELLOW));
        }
        if(flag.isAdvanced())
            tooltip.add(LCText.TOOLTIP_MONEY_BAG_SIZE.get(getSize(stack)).withStyle(ChatFormatting.DARK_GRAY));
        if(stack.has(ModDataComponents.LOOT_TABLE_ENTRY))
        {
            LootTableEntry entry = stack.get(ModDataComponents.LOOT_TABLE_ENTRY);
            tooltip.add(LCText.TOOLTIP_CONTAINER_ITEM_LOOT_TABLE.get(entry.lootTable().location()).withStyle(ChatFormatting.DARK_GRAY));
        }

        super.appendHoverText(stack,context,tooltip,flag);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) { return true; }

    @Override
    public void postHurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        //Make Coin Noises
        int size = getSize(stack);
        attacker.playSound(ModSounds.COINS_CLINKING.get(),0.25f + (0.2f * size), 1f);
        double dropChance = LCConfig.SERVER.moneyBagCoinLossChance.get();
        RandomSource random = attacker.getRandom();
        if(dropChance > 0d && random.nextDouble() < dropChance)
        {
            //Drop a random coin
            List<ItemStack> contents = getContents(stack);
            ItemStack droppedCoin = MoneyBagBlockEntity.removeRandomItem(contents,random);
            if(!droppedCoin.isEmpty())
            {
                //Update the money bags contents
                setContents(stack,contents);
                //Spawn the dropped coin
                Level level = attacker.level();
                Vec3 position = attacker.getEyePosition();
                Vec3 lookdirection = attacker.getLookAngle();
                double averageSpeed = 5d;
                double maxDelta = 0.25d;
                Vec3 itemSpeed = lookdirection.multiply(random.triangle(averageSpeed,maxDelta),random.triangle(averageSpeed,maxDelta),random.triangle(averageSpeed,maxDelta));
                ItemEntity item = new ItemEntity(level,position.x,position.y,position.z,droppedCoin);
                item.setDeltaMovement(itemSpeed);
                item.setThrower(attacker);
                level.addFreshEntity(item);
            }
        }
    }

    public static List<ItemStack> getContents(ItemStack moneybag)
    {
        if(moneybag.getItem() instanceof MoneyBagItem)
            return InventoryUtil.copyList(moneybag.getOrDefault(ModDataComponents.MONEY_BAG_CONTENTS, MoneyBagData.EMPTY).contents());
        return new ArrayList<>();
    }

    public static void setContents(ItemStack moneyBag,List<ItemStack> contents)
    {
        if(moneyBag.getItem() instanceof MoneyBagItem)
            moneyBag.set(ModDataComponents.MONEY_BAG_CONTENTS,MoneyBagData.of(contents));
    }

    public static int getSize(ItemStack moneybag)
    {
        if(moneybag.getItem() instanceof MoneyBagItem)
            return MathUtil.clamp(moneybag.getOrDefault(ModDataComponents.MONEY_BAG_CONTENTS, MoneyBagData.EMPTY).size(),0,3);
        return 0;
    }

    public static ItemStack createItem(ItemLike item, List<ItemStack> contents, int size) { return createItem(item,new MoneyBagData(ImmutableList.copyOf(InventoryUtil.copyList(contents)),size)); }
    public static ItemStack createItem(ItemLike item, MoneyBagData data)
    {
        ItemStack stack = new ItemStack(item);
        if(data.contents().isEmpty())
            return stack;
        stack.set(ModDataComponents.MONEY_BAG_CONTENTS,data);
        return stack;
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        ItemAttributeModifiers.Builder attributes = ItemAttributeModifiers.builder();

        int size = getSize(stack);

        //Add Attack Damage
        float damage = LCConfig.SERVER.moneyBagBaseAttack.get() + (LCConfig.SERVER.moneyBagAttackPerSize.get() * size);
        if(damage != 0f)
        {
            attributes.add(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(BASE_ATTACK_DAMAGE_ID,damage,AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        //Reduce attach speed
        float speed = LCConfig.SERVER.moneyBagBaseAtkSpeed.get() + (LCConfig.SERVER.moneyBagAtkSpeedPerSize.get() * size);
        if(speed != 0f)
        {
            attributes.add(Attributes.ATTACK_SPEED,
                    new AttributeModifier(BASE_ATTACK_SPEED_ID,speed, AttributeModifier.Operation.ADD_VALUE),
                    EquipmentSlotGroup.MAINHAND);
        }
        return attributes.build();
    }

}
