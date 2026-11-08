package io.github.lightman314.lightmanscurrency.features.tickets;

import io.github.lightman314.lightmanscurrency.api.helpers.ResourceHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyVanillaSlot;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipeInput;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerListener;
import net.minecraft.world.inventory.RecipeCraftingHolder;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.transaction.Transaction;

public class TicketResultSlot extends EasyVanillaSlot.OutputOnly implements ISidedContext, ContainerListener {

    private final TicketStationMenu menu;

    private int removeCount = 0;

    public TicketResultSlot(TicketStationMenu menu,int x, int y) {
        super(new ResultContainer(),0, x, y);
        this.menu = menu;
        this.recalculateResultItem();
    }

    @Override
    public boolean isClient() { return this.menu.isClient(); }

    private RecipeHolder<TicketStationRecipe> getSelectedRecipe() { return this.menu.getSelectedRecipe(); }
    private TicketStationRecipeInput getRecipeInput() { return this.menu.getRecipeInput(); }
    private NormalItemStorage getIngredientStorage() { return this.menu.getBlockEntity().getStorage(); }
    private Player getPlayer() { return this.menu.getPlayer(); }
    private Level getLevel() { return this.menu.getLevel(); }

    public void recalculateResultItem() {
        if(this.isClient())
            return;
        RecipeHolder<TicketStationRecipe> holder = this.getSelectedRecipe();
        if(holder == null)
            this.container.setItem(0,ItemStack.EMPTY);
        else {
            TicketStationRecipe recipe = holder.value();
            TicketStationRecipeInput input = this.getRecipeInput();
            if(recipe.matches(input,this.getLevel()) && recipe.validData(input))
                this.container.setItem(0,recipe.assemble(input));
            else
                this.container.setItem(0,ItemStack.EMPTY);
        }
    }

    @Override
    public ItemStack remove(int amount) {
        if(this.hasItem())
            this.removeCount += Math.max(amount,this.getItem().getCount());
        return super.remove(amount);
    }

    @Override
    protected void onSwapCraft(int count) {
        this.removeCount += count;
    }

    @Override
    public void onTake(Player player,ItemStack carried) {
        this.triggerRecipe(player,carried);
        super.onTake(player, carried);
    }

    public ItemStack onQuickMove() {
        //Validate result item just to be safe
        this.recalculateResultItem();
        if(!this.hasItem())
            return ItemStack.EMPTY;
        RecipeHolder<TicketStationRecipe> holder = this.getSelectedRecipe();
        if(holder != null) {
            TicketStationRecipe recipe = holder.value();
            NormalItemStorage storage = this.getIngredientStorage();
            ItemStack result = this.getItem().copy();
            int takeable;
            if(recipe.consumeModifier() && storage.getAmountAsInt(0) > 0)
                takeable = Math.min(storage.getAmountAsInt(0),storage.getAmountAsInt(1));
            else
                takeable = storage.getAmountAsInt(1);
            takeable = Math.min(takeable,this.getItem().getMaxStackSize());
            this.removeCount = takeable;
            result.setCount(takeable);
            if(!this.menu.moveStackTo(result,3,this.menu.slots.size(),true)) {
                return ItemStack.EMPTY;
            }
            //Update the amount to remove based on how many items actually fit in the players inventory
            this.removeCount = takeable - result.getCount();
            result.setCount(this.removeCount);
            this.triggerRecipe(this.getPlayer(),result);
            return result;
        }
        return ItemStack.EMPTY;
    }

    protected void triggerRecipe(Player player,ItemStack item) {
        int taken = this.removeCount;
        this.checkTakeAchievements(item);
        RecipeHolder<TicketStationRecipe> holder = this.getSelectedRecipe();
        if(holder != null) {
            TicketStationRecipe recipe = holder.value();
            TicketStationRecipeInput input = this.getRecipeInput();
            if(recipe.matches(input,this.getLevel()) && recipe.validData(input)) {
                try(Transaction transaction = Transaction.openRoot()) {
                    NormalItemStorage storage = this.getIngredientStorage();
                    if(recipe.consumeModifier())
                        ResourceHelper.extractFromIndex(storage,0,taken,transaction);
                    ResourceHelper.extractFromIndex(storage,1,taken,transaction);
                    transaction.commit();
                    recipe.afterAssembly(input);
                    this.recalculateResultItem();
                }
            }
        }
    }
    
    public void onClone() {
        if(this.isClient() || this.getItem().isEmpty())
            return;
        RecipeHolder<TicketStationRecipe> holder = this.getSelectedRecipe();
        if(holder != null) {
            holder.value().afterAssembly(this.getRecipeInput());
            this.recalculateResultItem();
        }
    }

    protected void checkTakeAchievements(ItemStack carried) {
        if (this.removeCount > 0) {
            carried.onCraftedBy(this.getPlayer(),this.removeCount);
        }

        if (this.container instanceof RecipeCraftingHolder recipeCraftingHolder) {
            recipeCraftingHolder.awardUsedRecipes(this.getPlayer(),this.getIngredientStorage().getContents());
        }

        this.removeCount = 0;
    }

    @Override
    public void slotChanged(AbstractContainerMenu container, int slotIndex, ItemStack itemStack) {
        if(slotIndex < 3)
            this.recalculateResultItem();
    }

    @Override
    public void dataChanged(AbstractContainerMenu container, int id, int value) { }

}