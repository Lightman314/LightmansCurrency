package io.github.lightman314.lightmanscurrency.common.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.common.blockentity.MoneyBagBlockEntity;
import io.github.lightman314.lightmanscurrency.common.core.ModSounds;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class MoneyBagItem extends BlockItem {

    public static final ResourceLocation PROPERTY = VersionUtil.lcResource("money_bag_size");

    public MoneyBagItem(Block block, Properties properties) { super(block,properties.stacksTo(1)); }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level context, List<Component> tooltip, TooltipFlag flag) {

        TooltipItem.addTooltip(tooltip, LCText.TOOLTIP_MONEY_BAG);

        List<ItemStack> contents = getContents(stack);
        if(!contents.isEmpty())
        {
            if(Screen.hasControlDown())
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
        CompoundTag tag = stack.getTag();
        if(tag != null && tag.contains("LootTable"))
        {
            tooltip.add(LCText.TOOLTIP_CONTAINER_ITEM_LOOT_TABLE.get(tag.getString("LootTable")).withStyle(ChatFormatting.DARK_GRAY));
        }

        super.appendHoverText(stack,context,tooltip,flag);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
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
                item.setThrower(attacker.getUUID());
                level.addFreshEntity(item);
            }
        }
        return true;
    }

    public static List<ItemStack> getContents(ItemStack moneybag)
    {
        if(moneybag.getItem() instanceof MoneyBagItem)
        {
            CompoundTag tag = moneybag.getTag();
            if(tag == null || !tag.contains("Contents"))
                return new ArrayList<>();
            else
            {
                List<ItemStack> contents = new ArrayList<>();
                ListTag list = tag.getList("Contents", Tag.TAG_COMPOUND);
                for(int i = 0; i < list.size(); ++i)
                {
                    ItemStack item = InventoryUtil.loadItemNoLimits(list.getCompound(i));
                    if(!item.isEmpty())
                        contents.add(item);
                }
                return contents;
            }
        }
        return new ArrayList<>();
    }

    public static void setContents(ItemStack moneyBag,List<ItemStack> contents)
    {
        if(moneyBag.getItem() instanceof MoneyBagItem)
        {
            CompoundTag tag = moneyBag.getOrCreateTag();
            ListTag list = new ListTag();
            for(ItemStack item : contents)
            {
                if(item.isEmpty())
                    continue;
                list.add(InventoryUtil.saveItemNoLimits(item));
            }
            tag.put("Contents",list);
            tag.putInt("Size",MoneyBagBlockEntity.getBlockSize(contents));
        }
    }

    public static int getSize(ItemStack moneybag)
    {
        if(moneybag.getItem() instanceof MoneyBagItem)
        {
            CompoundTag tag = moneybag.getTag();
            if(tag == null || !tag.contains("Size"))
                return 0;
            return MathUtil.clamp(tag.getInt("Size"),0,3);
        }
        return 0;
    }

    public static ItemStack createItem(ItemLike item, List<ItemStack> contents)
    {
        ItemStack stack = new ItemStack(item);
        if(contents.isEmpty())
            return stack;

        setContents(stack,contents);

        return stack;
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack item) {
        if(slot != EquipmentSlot.MAINHAND)
            return super.getAttributeModifiers(slot,item);
        ImmutableMultimap.Builder<Attribute,AttributeModifier> builder = ImmutableMultimap.builder();
        int size = getSize(item);
        //Add Attack Damage
        float damage = LCConfig.SERVER.moneyBagBaseAttack.get() + (LCConfig.SERVER.moneyBagAttackPerSize.get() * size);
        if(damage != 0f)
        {
            builder.put(Attributes.ATTACK_DAMAGE,
                    new AttributeModifier(BASE_ATTACK_DAMAGE_UUID,"Weapon modifier",damage,AttributeModifier.Operation.ADDITION));
        }
        //Reduce attack speed
        float speed = LCConfig.SERVER.moneyBagBaseAtkSpeed.get() + (LCConfig.SERVER.moneyBagAtkSpeedPerSize.get() * size);
        if(speed != 0f)
        {
            builder.put(Attributes.ATTACK_SPEED,
                    new AttributeModifier(BASE_ATTACK_SPEED_UUID,"Weapon modifier",speed, AttributeModifier.Operation.ADDITION));
        }
        return builder.build();
    }

}