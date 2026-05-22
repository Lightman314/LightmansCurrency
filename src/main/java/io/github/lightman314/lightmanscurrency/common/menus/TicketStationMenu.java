package io.github.lightman314.lightmanscurrency.common.menus;

import io.github.lightman314.lightmanscurrency.api.misc.item_handlers.LCItemStackHandler;
import io.github.lightman314.lightmanscurrency.api.misc.menus.slots.OutputSlot;
import io.github.lightman314.lightmanscurrency.common.crafting.RecipeValidator;
import io.github.lightman314.lightmanscurrency.common.crafting.TicketStationRecipe;
import io.github.lightman314.lightmanscurrency.common.crafting.input.TicketStationRecipeInput;
import io.github.lightman314.lightmanscurrency.common.menus.slots.ticket.*;
import io.github.lightman314.lightmanscurrency.common.menus.validation.types.BlockEntityValidator;
import io.github.lightman314.lightmanscurrency.common.core.ModMenus;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blockentity.TicketStationBlockEntity;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemHandlerHelper;

import java.util.List;

public class TicketStationMenu extends LazyMessageMenu {
	
	private final IItemHandlerModifiable output = new LCItemStackHandler(1);

    public TicketStationRecipe.ExtraData getExtraData() { return new TicketStationRecipe.ExtraData(this.code,this.durability); }

	private String code = "";
	public String getCode() { return this.code; }
	public void setCode(String code)
	{
		this.code = code;
		if(this.isClient())
			this.SendMessage(this.builder().setString("ChangeCode",code));
	}

    private int durability = 0;
    public int getDurability() { return this.durability; }
    public void setDurability(int durability)
    {
        this.durability = durability;
        if(this.isClient())
            this.SendMessage(this.builder().setInt("ChangeDurability",durability));
    }
	
	public final TicketStationBlockEntity blockEntity;

	public static List<RecipeHolder<TicketStationRecipe>> getAllRecipes(Level level) { return RecipeValidator.getTicketStationRecipes(level); }
	public List<RecipeHolder<TicketStationRecipe>> getAllRecipes() { return getAllRecipes(this.blockEntity.getLevel()); }
	public List<TicketStationRecipe> getRecipeList() { return RecipeValidator.getTicketStationRecipeList(this.blockEntity.getLevel()); }
	public RecipeHolder<TicketStationRecipe> getRecipe(ResourceLocation recipeID)
	{
		for(RecipeHolder<TicketStationRecipe> recipe : this.getAllRecipes())
		{
			if(recipe.id().equals(recipeID))
				return recipe;
		}
		return null;
	}
	public TicketStationMenu(int windowId, Inventory inventory, TicketStationBlockEntity blockEntity)
	{
		super(ModMenus.TICKET_MACHINE.get(), windowId, inventory);
		this.blockEntity = blockEntity;
		this.addValidator(BlockEntityValidator.of(this.blockEntity));
		
		//Slots
		this.addSlot(new TicketModifierSlot(this,this.blockEntity.getStorage(),20,21));
		this.addSlot(new TicketMaterialSlot(this,this.blockEntity.getStorage(),56,21));
		
		this.addSlot(new OutputSlot(this.output, 0, 116, 21));
		
		//Player items
		for(int y = 0; y < 3; y++)
		{
			for(int x = 0; x < 9; x++)
			{
				this.addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 76 + y * 18));
			}
		}
		//Player hotbar
		for(int x = 0; x < 9; x++)
		{
			this.addSlot(new Slot(inventory, x, 8 + x * 18, 134));
		}
	}
	
	@Override
	public void removed(Player playerIn)
	{
		super.removed(playerIn);
		this.clearContainer(playerIn,  this.output);
	}
	
	@Override
	public ItemStack quickMoveStack(Player player, int index)
	{
		
		ItemStack clickedStack = ItemStack.EMPTY;
		
		Slot slot = this.slots.get(index);
		
		if(slot != null && slot.hasItem())
		{
			ItemStack slotStack = slot.getItem();
			clickedStack = slotStack.copy();
			int totalSize = this.blockEntity.getStorage().getSlots() + this.output.getSlots();
			if(index < totalSize)
			{
				if(!this.moveItemStackTo(slotStack, totalSize, this.slots.size(), true))
				{
					return ItemStack.EMPTY;
				}
			}
			else if(!this.moveItemStackTo(slotStack, 0, this.blockEntity.getStorage().getSlots(), false))
			{
				return ItemStack.EMPTY;
			}
			
			if(slotStack.isEmpty())
			{
				slot.set(ItemStack.EMPTY);
			}
			else
			{
				slot.setChanged();
			}
		}
		
		return clickedStack;
		
	}

	public boolean validInputs()
	{
		TicketStationRecipeInput input = this.blockEntity.getRecipeInput(this.getExtraData());
		return this.getAllRecipes().stream().anyMatch(r -> r.value().matches(input, this.blockEntity.getLevel()) && r.value().validData(this.getExtraData()));
	}
	
	public boolean roomForOutput(TicketStationRecipe recipe)
	{
		if(recipe == null)
			return false;
		ItemStack outputStack = this.output.getStackInSlot(0);
		if(outputStack.isEmpty())
			return true;
		return ItemStack.isSameItemSameComponents(recipe.peekAtResult(this.blockEntity.getRecipeInput(this.getExtraData())), outputStack) && outputStack.getMaxStackSize() > outputStack.getCount();
	}
	
	public void craftTickets(boolean fullStack, ResourceLocation recipeID)
	{
		RecipeHolder<TicketStationRecipe> holder = this.getRecipe(recipeID);
		if(holder == null)
			return;
		TicketStationRecipe recipe = holder.value();

		if(!recipe.matches(this.blockEntity.getRecipeInput(this.getExtraData()), this.blockEntity.getLevel()) || !recipe.validData(this.getExtraData()))
			return;

		if(!this.roomForOutput(recipe))
		{
			LightmansCurrency.LogDebug("No room for Ticket Machine outputs. Cannot craft tickets.");
			return;
		}

		int amountToCraft = 1;
		if(fullStack)
			amountToCraft = this.output.getSlotLimit(0);

		for(int i = 0; i < amountToCraft; ++i)
		{
			if(this.assemble(recipe))
				return;
		}
		
	}

	private boolean assemble(TicketStationRecipe recipe)
	{
		TicketStationRecipeInput input = this.blockEntity.getRecipeInput(this.getExtraData());
		if(this.roomForOutput(recipe) && recipe.matches(input, this.blockEntity.getLevel()))
		{
			ItemStack result = recipe.assemble(input, this.blockEntity.getLevel().registryAccess());
			if(!result.isEmpty())
			{
                ItemStack overflow = ItemHandlerHelper.insertItem(this.output,result,true);
                if(overflow.isEmpty())
                {
                    ItemHandlerHelper.insertItemStacked(this.output,result,false);
                    //Remove the consumed items from the input
                    if(recipe.consumeModifier())
                        this.blockEntity.getStorage().extractItem(0,1,false);
                    this.blockEntity.getStorage().extractItem(1,1,false);
                    return false;
                }
			}
		}
		return true;
	}

	public void SendCraftTicketsMessage(boolean fullStack, ResourceLocation recipe)
	{
		this.SendMessageToServer(this.builder().setBoolean("CraftTickets", fullStack).setResourceLocation("Recipe", recipe));
	}

	@Override
	public void processMessage(LazyPacketData message) {
		if(message.contains("CraftTickets"))
			this.craftTickets(message.getBoolean("CraftTickets"), message.getResourceLocation("Recipe"));
		if(message.contains("ChangeCode"))
			this.setCode(message.getString("ChangeCode"));
        if(message.contains("ChangeDurability"))
            this.setDurability(message.getInt("ChangeDurability"));
	}

}
