package io.github.lightman314.lightmanscurrency.api.traders.discount_codes;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.items.CouponItem;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.function.Consumer;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CouponSource implements IDiscountCodeSource {

    private final IItemHandler container;
    private final Consumer<ItemStack> overflowHandler;
    public CouponSource(Container container,Consumer<ItemStack> overflowHandler) { this(new InvWrapper(container),overflowHandler); }
    public CouponSource(IItemHandler container,Consumer<ItemStack> overflowHandler) { this.container = container; this.overflowHandler = overflowHandler; }

    @Override
    public boolean containsCode(String code) {
        int hash = code.hashCode();
        for(int i = 0; i < this.container.getSlots(); ++i)
        {
            if(this.isCode(hash,this.container.getStackInSlot(i)))
                return true;
        }
        return false;
    }

    @Override
    public Set<Integer> getDiscountCodes() {
        Set<Integer> temp = new HashSet<>();
        for(int i = 0; i < this.container.getSlots(); ++i)
        {
            ItemStack item = container.getStackInSlot(i);
            if(InventoryUtil.ItemHasTag(item, LCTags.Items.COUPONS))
            {
                //Check coupon code (leaving seperate for 1.20 rewrite)
                temp.add(CouponItem.GetCouponCode(item));
            }
        }
        return temp;
    }

    protected boolean isCode(int code, ItemStack item)
    {
        if(item.isEmpty())
            return false;
        if(InventoryUtil.ItemHasTag(item, LCTags.Items.COUPONS))
        {
            //Check coupon code (leaving seperate for 1.20 rewrite)
            return code == CouponItem.GetCouponCode(item);
        }
        return false;
    }

    @Override
    public boolean consumeCode(String code) {
        int hash = code.hashCode();
        List<Pair<Integer,Integer>> targets = new ArrayList<>();
        for(int i = 0; i < this.container.getSlots(); ++i)
        {
            ItemStack item = this.container.getStackInSlot(i).copy();
            if(InventoryUtil.ItemHasTag(item, LCTags.Items.COUPONS))
            {
                int c = CouponItem.GetCouponCode(item);
                if(hash == c)
                {
                    int uses = TicketItem.getUseCount(item);
                    if(uses < 0) //Found a non-damageable coupon, so don't break anything
                        return true;
                    else
                        targets.add(Pair.of(i,item.get(ModDataComponents.TICKET_USES)));
                }
            }
        }
        //Sort from smallest to largest
        targets.sort(Comparator.comparingInt(Pair::getSecond));
        //Actually apply damage to the coupons
        for(var pair : targets)
        {
            int target = pair.getFirst();
            ItemStack extracted = this.container.extractItem(target,Integer.MAX_VALUE,true);
            if(extracted.isEmpty() || !this.isCode(hash,extracted))
                continue;
            extracted = this.container.extractItem(target,1,false);
            if(extracted.isEmpty())
                continue;
            if(!this.isCode(hash,extracted))
            {
                this.quickInsert(target,extracted);
                continue;
            }
            //Damage the item
            int uses = TicketItem.getUseCount(extracted);
            if(uses > 1)
            {
                TicketItem.setUseCount(extracted,uses - 1);
                this.quickInsert(target,extracted);
            }
            //If only 1 use left, simply delete the coupon
            return true;
        }
        return false;
    }

    private void quickInsert(int preferredSlot,ItemStack stack)
    {
        ItemStack remainder = this.container.insertItem(preferredSlot,stack,false);
        if(!remainder.isEmpty())
        {
            remainder = ItemHandlerHelper.insertItem(this.container,remainder,false);
            if(!remainder.isEmpty())
                this.overflowHandler.accept(remainder);
        }
    }

}
