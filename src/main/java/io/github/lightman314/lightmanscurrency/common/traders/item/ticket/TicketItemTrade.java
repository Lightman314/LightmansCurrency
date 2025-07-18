package io.github.lightman314.lightmanscurrency.common.traders.item.ticket;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LCTags;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.ticket.TicketGroupData;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.menus.TicketStationMenu;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TicketItemTrade extends ItemTradeData {

    private final TicketKioskRestriction restriction = new TicketKioskRestriction(this);

    private final TicketSaleData ticketData1 = new TicketSaleData(0);
    private final TicketSaleData ticketData2 = new TicketSaleData(1);

    public TicketItemTrade(boolean validateRules) {
        super(validateRules);
        super.setRestriction(this.restriction);
    }

    @Override
    public void setRestriction(ItemTradeRestriction restriction) { }

    @Nonnull
    @Override
    public ItemTradeRestriction getRestriction() { return this.restriction; }

    @Override
    public void setItem(ItemStack itemStack, int index) {
        super.setItem(itemStack, index);
        if(index < 2)
            this.getTicketData(index).onSellItemChanged();
    }

    @Override
    public boolean isValid() {
        return super.isValid() && this.ticketData1.isValid() && this.ticketData2.isValid();
    }

    @Nullable
    public TicketSaleData getTicketData(int index)
    {
        return switch (index) {
            case 0 -> this.ticketData1;
            case 1 -> this.ticketData2;
            default -> null;
        };
    }

    @Override
    public CompoundTag getAsNBT(@Nonnull HolderLookup.Provider lookup) {
        CompoundTag tag = super.getAsNBT(lookup);
        tag.put("TicketData1",this.ticketData1.save());
        tag.put("TicketData2",this.ticketData2.save());
        return tag;
    }

    @Override
    public void loadFromNBT(CompoundTag tag, @Nonnull HolderLookup.Provider lookup) {
        super.loadFromNBT(tag, lookup);

        if(tag.contains("TicketData1"))
            this.ticketData1.load(tag.getCompound("TicketData1"));
        else //Update old trade data to match the new ticket kiosk data saving methods
            updateFromOldData(this.ticketData1,0);

        if(tag.contains("TicketData2"))
            this.ticketData2.load(tag.getCompound("TicketData2"));
        else
            updateFromOldData(this.ticketData2,1);
    }

    private void updateFromOldData(TicketSaleData data, int index)
    {
        ItemStack sellItem = this.getSellItemInternal(index);
        if(TicketItem.isTicket(sellItem))
        {
            TicketGroupData group = TicketGroupData.getForTicket(sellItem);
            if(group != null)
            {
                //Turn the "sell item" back into the master ticket
                ItemStack masterTicket = sellItem.transmuteCopy(group.masterTicket);
                //Setting the item will automatically trigger the "onSaleItemChanged" method to recalculate the recipe id
                this.setItem(masterTicket,index);
                //Try to find the exact recipe for a ticket, just in case it defaults to the pass recipe
                for(RecipeHolder<TicketStationRecipe> recipe : this.ticketData1.getMatchingRecipes())
                {
                    if(recipe.value().assembleWithKiosk(masterTicket,data.code).getItem() == sellItem.getItem())
                    {
                        data.recipeID = recipe.id();
                        return;
                    }
                }
            }
        }
    }

    public class TicketSaleData
    {
        private final int index;
        public TicketSaleData(int index) { this.index = index; }
        @Nullable
        ResourceLocation recipeID = null;
        public ResourceLocation getRecipe() { return this.recipeID; }
        public void setRecipe(ResourceLocation recipe) { this.recipeID = recipe; }
        public void onSellItemChanged()
        {
            if(this.isPotentiallyRecipeMode() && this.tryGetRecipe() == null)
            {
                List<RecipeHolder<TicketStationRecipe>> allRecipes = this.getMatchingRecipes();
                if(allRecipes.isEmpty())
                    return;
                this.recipeID = allRecipes.getFirst().id();
            }
        }
        String code = "";
        public String getCode() { return this.code; }
        public void setCode(String couponCode) {
            if(couponCode.length() > 16)
                couponCode = couponCode.substring(0,16);
            this.code = couponCode;
        }
        public List<RecipeHolder<TicketStationRecipe>> getMatchingRecipes() {
            ItemStack sellItem = TicketItemTrade.this.getSellItemInternal(this.index);
            Level level = LightmansCurrency.getProxy().safeGetDummyLevel();
            if(level != null)
            {
                List<RecipeHolder<TicketStationRecipe>> list = new ArrayList<>(TicketStationMenu.getAllRecipes(level).stream().filter(r ->
                        r.value().matchesTicketKioskSellItem(sellItem)).toList());
                //Figure out if we should add an empty recipe as the default value, as some recipes may conflict with normal material sales
                for(RecipeHolder<TicketStationRecipe> recipe : list)
                {
                    if(recipe.value().allowIgnoreKioskRecipe())
                    {
                        list.addFirst(new RecipeHolder<>(null,null));
                        return list;
                    }
                }
                return list;
            }
            return ImmutableList.of();
        }
        @Nullable
        public TicketStationRecipe tryGetRecipe()
        {
            if(this.recipeID == null || !this.isPotentiallyRecipeMode())
                return null;
            Level level = LightmansCurrency.getProxy().safeGetDummyLevel();
            if(level != null)
            {
                for(RecipeHolder<TicketStationRecipe> holder : TicketStationMenu.getAllRecipes(level))
                {
                    if(holder.id().equals(this.recipeID))
                    {
                        //Don't return the trade if it doesn't match our required item
                        if(!holder.value().matchesTicketKioskSellItem(TicketItemTrade.this.getSellItemInternal(this.index)))
                            return null;
                        return holder.value();
                    }
                }
            }
            return null;
        }
        public boolean requestingCodeInput()
        {
            TicketStationRecipe recipe = this.tryGetRecipe();
            return recipe != null && recipe.requiredCodeInput();
        }
        public ItemStack getCraftingResult(boolean replaceName)
        {
            ItemStack sellItem = TicketItemTrade.this.getSellItemInternal(this.index);
            if(!this.isRecipeMode())
                return sellItem;
            TicketStationRecipe recipe = this.tryGetRecipe();
            if(recipe != null)
            {
                ItemStack result = recipe.assembleWithKiosk(sellItem,this.code);
                if(result.isEmpty())
                    return result;
                result.setCount(sellItem.getCount());
                if(replaceName)
                {
                    String customName = TicketItemTrade.this.getCustomName(this.index);
                    if(!customName.isBlank())
                        result.set(DataComponents.CUSTOM_NAME, EasyText.literal(customName));
                }
                return result;
            }
            return sellItem;
        }
        public boolean isValid()
        {
            ItemStack sellItem = TicketItemTrade.this.getSellItemInternal(this.index);
            TicketStationRecipe recipe = this.tryGetRecipe();
            if(recipe != null)
                return recipe.matchesTicketKioskSellItem(sellItem) && (recipe.validCode(this.code));
            return sellItem.isEmpty() || InventoryUtil.ItemHasTag(sellItem,LCTags.Items.TICKET_MATERIAL);
        }
        public boolean isPotentiallyRecipeMode() { return !TicketItemTrade.this.isPurchase() && !this.getMatchingRecipes().isEmpty(); }
        public boolean isRecipeMode() { return this.tryGetRecipe() != null; }

        public CompoundTag save()
        {
            CompoundTag tag = new CompoundTag();
            if(this.recipeID != null)
                tag.putString("Recipe",this.recipeID.toString());
            tag.putString("Code",this.code);
            return tag;
        }

        public void load(CompoundTag tag)
        {
            if(tag.contains("Recipe"))
                this.recipeID = VersionUtil.parseResource(tag.getString("Recipe"));
            this.code = tag.getString("Code");
        }

    }

}
