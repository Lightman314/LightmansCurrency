package io.github.lightman314.lightmanscurrency.features.tickets;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.ListHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.RecipeHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.resource.NormalItemStorage;
import io.github.lightman314.lightmanscurrency.api.text.TextEntry;
import io.github.lightman314.lightmanscurrency.api.world.menu.MessageMenu;
import io.github.lightman314.lightmanscurrency.api.world.menu.slots.EasyResourceSlot;
import io.github.lightman314.lightmanscurrency.api.world.menu.validation.builtin.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.core.LCMenuTypes;
import io.github.lightman314.lightmanscurrency.core.LCRecipeTypes;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketRecipeData;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.features.tickets.recipe.TicketStationRecipeInput;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeMap;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class TicketStationMenu extends MessageMenu.Validated {

    public static final TextEntry GUI_LABEL_CODE = TextEntry.gui(LCApi.MODID,"ticket_station.label.code");
    public static final TextEntry GUI_LABEL_DURABILITY = TextEntry.gui(LCApi.MODID,"ticket_station.label.durability");
    
    private final TicketStationBlockEntity blockEntity;
    public TicketStationBlockEntity getBlockEntity() { return this.blockEntity; }

    private final TicketResultSlot resultSlot;

    private TicketRecipeData recipeData = TicketRecipeData.DEFAULT;
    public TicketRecipeData getRecipeData() { return this.recipeData; }

    public TicketStationRecipeInput getRecipeInput() { return this.blockEntity.getRecipeInput(this.recipeData); }

    public TicketStationMenu(int containerId, Player player,BlockPos position) {
        this(containerId,player,tryGetBlockEntity(player,position));
    }
    public TicketStationMenu(int containerId,Player player,TicketStationBlockEntity blockEntity) {
        this(LCMenuTypes.TICKET_STATION.get(),containerId,player,blockEntity);
    }
    protected TicketStationMenu(MenuType<?> type,int containerId, Player player,TicketStationBlockEntity blockEntity) {
        super(type,containerId,player,new BlockEntityValidator(blockEntity));
        this.blockEntity = blockEntity;

        NormalItemStorage storage = this.blockEntity == null ? new NormalItemStorage(2) : this.blockEntity.getStorage();
        //Add Slots
        EasyResourceSlot modifierSlot = new EasyResourceSlot(storage,0,17,20);
        modifierSlot.setBackground(List.of(LCApi.id("container/slot/ticket"),Identifier.withDefaultNamespace("container/slot/dye")));
        this.addSlot(modifierSlot);
        this.addSlot(new EasyResourceSlot(storage,1,66,20));

        this.resultSlot = new TicketResultSlot(this,143,20);
        this.addSlot(this.resultSlot);

        this.addSlotListener(this.resultSlot);
        //this.addSlot(new EasyResourceSlot.OutputOnly(this.resultContents,0,148,20));

        //Inventory Slots
        Inventory inventory = player.getInventory();
        for(int y = 0; y < 3; ++y) {
            for(int x = 0; x < 9; ++x) {
                this.addSlot(new Slot(inventory,9 + x + y * 9,8 + x * 18,119 + y * 18));
            }
        }
        for(int x = 0; x < 9; ++x)
            this.addSlot(new Slot(inventory,x,8 + x * 18,177));

    }

    public final List<RecipeHolder<TicketStationRecipe>> getAvailableRecipes() {
        TicketStationRecipeInput recipeInput = this.blockEntity.getRecipeInput();
        Level level = this.getLevel();
        RecipeMap map = RecipeHelper.getRecipeMap(level);
        return map.getRecipesFor(LCRecipeTypes.TICKET_STATION.get(),recipeInput,level).toList();
    }

    private static TicketStationBlockEntity tryGetBlockEntity(Player player,BlockPos pos) {
        if(player.level().getBlockEntity(pos) instanceof TicketStationBlockEntity be)
            return be;
        return null;
    }

    @Override
    public ItemStack quickMoveStack(Player player,int index) {
        ItemStack clickedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        //Special quick-move logic for the result slot
        if(slot == this.resultSlot)
            return this.resultSlot.onQuickMove();
        if(slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            clickedStack = slotStack.copy();
            if(index < 3) {
                if(!this.moveStackTo(slotStack,3,this.slots.size(),true))
                    return ItemStack.EMPTY;
            }
            else if(!this.moveStackTo(slotStack,0,2,false))
                return ItemStack.EMPTY;
            if(slotStack.isEmpty())
                slot.set(ItemStack.EMPTY);
            else
                slot.setChanged();
        }
        return clickedStack;
    }

    @Override
    public void clicked(int slotIndex, int buttonNum, ContainerInput containerInput, Player player) {
        super.clicked(slotIndex, buttonNum, containerInput, player);
        if(ListHelper.getOrNull(this.slots,slotIndex) == this.resultSlot && containerInput == ContainerInput.CLONE)
            this.resultSlot.onClone();
    }

    @Nullable
    private RecipeHolder<TicketStationRecipe> selectedRecipe = null;
    @Nullable
    public RecipeHolder<TicketStationRecipe> getSelectedRecipe() {
        if(this.selectedRecipe != null && this.selectedRecipe.value().matches(this.getRecipeInput(),this.getLevel()))
            return this.selectedRecipe;
        //If only one valid recipe is present, automatically select it
        List<RecipeHolder<TicketStationRecipe>> validRecipes = this.getAvailableRecipes();
        if(validRecipes.size() == 1) {
            this.selectedRecipe = validRecipes.getFirst();
            return this.selectedRecipe;
        }
        return null;
    }
    public void setSelectedRecipe(@Nullable RecipeHolder<TicketStationRecipe> holder) {
        this.selectedRecipe = holder;
        this.resultSlot.recalculateResultItem();
        if(this.isClient()) {
            if(this.selectedRecipe == null)
                this.send(FancyPacketMap.map().setFlag("clearRecipe"));
            else
                this.send(FancyPacketMap.map().setResourceKey("setRecipe",this.selectedRecipe.id()));
        }
    }

    public boolean usesCodeInput() { return this.getSelectedRecipe() != null && this.selectedRecipe.value().requiredCodeInput(); }
    public void setCode(String code) {
        if(!code.isEmpty() && !TicketStationRecipeInput.isValidCode(code))
            return;
        this.recipeData = this.recipeData.withCode(code);
        this.resultSlot.recalculateResultItem();
        if(this.isClient())
            this.send(FancyPacketMap.map().setString("setCode",code));
    }

    public boolean usesDurabilityInput() { return this.getSelectedRecipe() != null && this.selectedRecipe.value().requiredDurabilityInput(); }
    public void setDurability(int durability) {
        if(durability < 0)
            return;
        this.recipeData = this.recipeData.withDurability(durability);
        this.resultSlot.recalculateResultItem();
        if(this.isClient())
            this.send(FancyPacketMap.map().setInt("setDurability",durability));
    }

    @Override
    protected void handleMessage(FancyPacketMap message) {
        if(message.contains("clearRecipe"))
            this.setSelectedRecipe(null);
        if(message.contains("setRecipe")) {
            ResourceKey<Recipe<?>> recipeKey = message.getResourceKey("setRecipe",Registries.RECIPE);
            if(recipeKey == null)
                this.setSelectedRecipe(null);
            RecipeHolder<?> holder = RecipeHelper.getRecipeMap(this.getLevel()).byKey(recipeKey);
            if(holder != null && holder.value() instanceof TicketStationRecipe)
                this.setSelectedRecipe((RecipeHolder<TicketStationRecipe>)holder);
        }
        if(message.contains("setCode"))
            this.setCode(message.getString("setCode"));
        if(message.contains("setDurability"))
            this.setDurability(message.getInt("setDurability"));
    }

}
