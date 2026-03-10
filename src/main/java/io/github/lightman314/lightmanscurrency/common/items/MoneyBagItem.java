package io.github.lightman314.lightmanscurrency.common.items;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.item_handler.MoneyBagInventory;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.common.items.data.LootTableEntry;
import io.github.lightman314.lightmanscurrency.common.items.data.MoneyBagData;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.ChatFormatting;
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

import java.util.List;

public class MoneyBagItem extends BlockItem {

    public static final ResourceLocation PROPERTY = LightmansCurrency.id("money_bag_size");

    public MoneyBagItem(Block block, Properties properties) { super(block,properties.stacksTo(1)); }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {

        TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_MONEY_BAG);

        stack.addToTooltip(ModDataComponents.MONEY_BAG_CONTENTS,context,tooltip::add,flag);

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
            MoneyBagInventory contents = getContents(stack);
            ItemStack droppedCoin = contents.removeRandomItem(random);
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

    public static MoneyBagInventory getContents(ItemStack moneybag)
    {
        if(moneybag.getItem() instanceof MoneyBagItem)
            return moneybag.getOrDefault(ModDataComponents.MONEY_BAG_CONTENTS, MoneyBagData.EMPTY).contents().copy();
        return new MoneyBagInventory();
    }

    public static void setContents(ItemStack moneyBag,MoneyBagInventory contents)
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

    public static ItemStack createItem(ItemLike item, List<ItemStack> contents, int size) { return createItem(item,new MoneyBagData(new MoneyBagInventory(contents),size)); }
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
