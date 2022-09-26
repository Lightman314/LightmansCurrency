package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.ScrollBarWidget.IScrollable;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.tradedata.item.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

public class ItemEditWidget extends AbstractWidget implements IScrollable{

	public static final ResourceLocation GUI_TEXTURE = new ResourceLocation(LightmansCurrency.MODID, "textures/gui/item_edit.png");
	
	public static List<CreativeModeTab> ITEM_GROUP_BLACKLIST = ImmutableList.of(CreativeModeTab.TAB_HOTBAR, CreativeModeTab.TAB_INVENTORY, CreativeModeTab.TAB_SEARCH);
	
	private int scroll = 0;
	private int stackCount = 1;
	
	private int columns;
	private int rows;
	
	public int searchOffX;
	public int searchOffY;
	
	public int stackSizeOffX;
	public int stackSizeOffY;
	
	private static List<ItemStack> allItems;
	
	//private List<ItemStack> filteredResultItems;
	private List<ItemStack> searchResultItems;
	
	private String searchString;
	
	EditBox searchInput;
	ScrollListener stackScrollListener;
	private final IItemEditListener listener;
	
	private final Font font;
	
	public ItemEditWidget(int x, int y, int columns, int rows, IItemEditListener listener) {
		super(x, y, columns * 18, rows * 18, new TextComponent(""));
		this.listener = listener;
		
		this.columns = columns;
		this.rows = rows;
		
		this.searchOffX = this.width - 90;
		this.searchOffY = -13;
		
		this.stackSizeOffX = this.width + 13;
		this.stackSizeOffY = 0;
		
		Minecraft mc = Minecraft.getInstance();
		this.font = mc.font;
		
		//Set the search to the default value to initialize the inventory
		this.modifySearch("");
		
	}
	
	public static final void initItemList() {
		
		if(allItems != null)
			return;
		
		allItems = new ArrayList<>();
		
		//Go through all of the item groups to avoid allowing sales of hidden items
		for(CreativeModeTab group : CreativeModeTab.TABS)
		{
			if(!ITEM_GROUP_BLACKLIST.contains(group))
			{
				//Get all of the items in this group
				NonNullList<ItemStack> items = NonNullList.create();
				group.fillItemList(items);
				//Add them to the list after confirming we don't already have it in the list
				for(ItemStack stack : items)
				{
					
					if(!itemListAlreadyContains(stack))
						allItems.add(stack);
					
					if(stack.getItem() == Items.ENCHANTED_BOOK)
					{
						//LightmansCurrency.LogInfo("Attempting to add lower levels of an enchanted book.");
						Map<Enchantment,Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
						enchantments.forEach((enchantment, level) ->{
							for(int newLevel = level - 1; newLevel > 0; newLevel--)
							{
								ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
								EnchantmentHelper.setEnchantments(ImmutableMap.of(enchantment, newLevel), newBook);
								if(!itemListAlreadyContains(newBook))
									allItems.add(newBook);
							}
						});
					}
					
				}
			}
		}
	}
	
	private static boolean itemListAlreadyContains(ItemStack stack)
	{
		for(ItemStack s : allItems)
		{
			if(InventoryUtil.ItemMatches(s, stack))
				return true;
		}
		return false;
	}
	
	private List<ItemStack> getFilteredItems()
	{
		List<ItemStack> results = Lists.newArrayList();
		ItemTradeData trade = this.listener.getTrade();
		ItemTradeRestriction restriction = trade == null ? ItemTradeRestriction.NONE : this.listener.getTrade().getRestriction();
		for(int i = 0; i < allItems.size(); ++i)
		{
			if(restriction.allowItemSelectItem(allItems.get(i)))
				results.add(allItems.get(i));
		}
		return results;
	}
	
	public int getMaxScroll()
	{
		return Math.max(((this.searchResultItems.size() - 1) / this.columns) - this.rows + 1, 0);
	}
	
	public void refreshPage()
	{
		
		if(this.scroll < 0)
			this.scroll = 0;
		if(this.scroll > this.getMaxScroll())
			this.scroll = this.getMaxScroll();
		
		//LightmansCurrency.LogInfo("Refreshing page " + this.page + ". Max Page: " + maxPage());
		
		int startIndex = this.scroll * this.columns;
		//Define the display inventories contents
		for(int i = 0; i < this.rows * this.columns; i++)
		{
			int thisIndex = startIndex + i;
			if(thisIndex < this.searchResultItems.size()) //Set to search result item
			{
				ItemStack stack = this.searchResultItems.get(thisIndex).copy();
				stack.setCount(MathUtil.clamp(this.stackCount, 1, stack.getMaxStackSize()));
			}
		}
	}
	
	public void refreshSearch() { this.modifySearch(this.searchString); }
	
	public void modifySearch(String newSearch)
	{
		this.searchString = newSearch.toLowerCase();
		
		//Repopulate the searchResultItems list
		if(this.searchString.length() > 0)
		{
			this.searchResultItems = new ArrayList<>();
			List<ItemStack> validItems = this.listener.restrictItemEditItems() ? this.getFilteredItems() : allItems;
			for(ItemStack stack : validItems)
			{
				//Search the display name
				if(stack.getHoverName().getString().toLowerCase().contains(this.searchString))
				{
					this.searchResultItems.add(stack);
				}
				//Search the registry name
				else if(stack.getItem().getRegistryName().toString().contains(this.searchString))
				{
					this.searchResultItems.add(stack);
				}
				//Search the enchantments?
				else
				{
					AtomicReference<Boolean> enchantmentMatch = new AtomicReference<>(false);
					Map<Enchantment,Integer> enchantments = EnchantmentHelper.getEnchantments(stack);
					enchantments.forEach((enchantment, level) ->{
						if(enchantment.getRegistryName().toString().contains(this.searchString))
							enchantmentMatch.set(true);
						else if(enchantment.getFullname(level).getString().toLowerCase().contains(this.searchString))
							enchantmentMatch.set(true);
					});
					if(enchantmentMatch.get())
						this.searchResultItems.add(stack);
				}
			}
		}
		else //No search string, so the result is just the allItems list
		{
			this.searchResultItems = this.listener.restrictItemEditItems() ? this.getFilteredItems() : allItems;
		}
		
		//Run refresh page code to validate the page # and repopulate the display inventory
		this.refreshPage();
		
	}
	
	public void init(Function<EditBox,EditBox> addWidget, Function<ScrollListener,ScrollListener> addListener) {
		
		this.searchInput = addWidget.apply(new EditBox(this.font, this.x + this.searchOffX + 2, this.y + this.searchOffY + 2, 79, 9, new TranslatableComponent("gui.lightmanscurrency.item_edit.search")));
		this.searchInput.setBordered(false);
		this.searchInput.setMaxLength(32);
		this.searchInput.setTextColor(0xFFFFFF);
		
		this.stackScrollListener = addListener.apply(new ScrollListener(this.x + this.stackSizeOffX, this.y + this.stackSizeOffY, 18, 18, this::stackCountScroll));
		
	}
	
	@Override
	public void render(PoseStack pose, int mouseX, int mouseY, float partialTicks) {
		this.searchInput.visible = this.visible;
		this.stackScrollListener.active = this.visible;
		
		if(!this.visible)
			return;
		
		if(!this.searchInput.getValue().toLowerCase().contentEquals(this.searchString))
			this.modifySearch(this.searchInput.getValue());
		
		int index = this.scroll * this.columns;
		for(int y = 0; y < this.rows && index < this.searchResultItems.size(); ++y)
		{
			int yPos = this.y + y * 18;
			for(int x = 0; x < this.columns && index < this.searchResultItems.size(); ++x)
			{
				//Get the slot position
				int xPos = this.x + x * 18;
				//Render the slot background
				RenderSystem.setShaderTexture(0, GUI_TEXTURE);
				RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
				this.blit(pose, xPos, yPos, 0, 0, 18, 18);
				//Render the slots item
				ItemRenderUtil.drawItemStack(this, this.font, this.getQuantityFixedStack(this.searchResultItems.get(index)), xPos + 1, yPos + 1);
				index++;
			}
		}
		
		//Render the search field
		RenderSystem.setShaderTexture(0, GUI_TEXTURE);
		RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
		this.blit(pose, this.x + this.searchOffX, this.y + this.searchOffY, 18, 0, 90, 12);
		
		//Render the quantity scroll area
		this.blit(pose, this.x + this.stackSizeOffX, this.y + this.stackSizeOffY, 108, 0, 18, 18);
		
	}
	
	public void tick() { this.searchInput.tick(); }
	
	private ItemStack getQuantityFixedStack(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.setCount(Math.min(stack.getMaxStackSize(), this.stackCount));
		return copy;
	}
	
	public void renderTooltips(Screen screen, PoseStack pose, int mouseX, int mouseY) {
		if(!this.visible)
			return;
		int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
		if(hoveredSlot >= 0)
		{
			hoveredSlot += this.scroll * this.columns;
			if(hoveredSlot < this.searchResultItems.size())
			{
				screen.renderComponentTooltip(pose, ItemRenderUtil.getTooltipFromItem(this.searchResultItems.get(hoveredSlot)), mouseX, mouseY);
			}
		}
		if(this.isMouseOverStackSizeScroll(mouseX,mouseY))
			screen.renderTooltip(pose, new TranslatableComponent("tooltip.lightmanscurrency.item_edit.scroll"), mouseX, mouseY);
	}
	
	private boolean isMouseOverStackSizeScroll(int mouseX, int mouseY) {
		return mouseX >= this.x + this.stackSizeOffX && mouseX < this.x + this.stackSizeOffX + 18 && mouseY >= this.y + this.stackSizeOffY && mouseY < this.y + this.stackSizeOffY + 18;
	}
	
	private int isMouseOverSlot(double mouseX, double mouseY) {
		
		int foundColumn = -1;
		int foundRow = -1;
		
		for(int x = 0; x < this.columns && foundColumn < 0; ++x)
		{
			if(mouseX >= this.x + x * 18 && mouseX < this.x + (x * 18) + 18)
				foundColumn = x;
		}
		for(int y = 0; y < this.rows && foundRow < 0; ++y)
		{
			if(mouseY >= this.y + y * 18 && mouseY < this.y + (y * 18) + 18)
				foundRow = y;
		}
		if(foundColumn < 0 || foundRow < 0)
			return -1;
		return (foundRow * this.columns) + foundColumn;
	}
	
	public interface IItemEditListener {
		public ItemTradeData getTrade();
		public boolean restrictItemEditItems();
		public void onItemClicked(ItemStack item);
	}


	@Override
	public void updateNarration(NarrationElementOutput narrator) { }
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
		if(hoveredSlot >= 0)
		{
			hoveredSlot += this.scroll * this.columns;
			if(hoveredSlot < this.searchResultItems.size())
			{
				ItemStack stack = this.getQuantityFixedStack(this.searchResultItems.get(hoveredSlot));
				this.listener.onItemClicked(stack);
				return true;
			}
		}
		return false;
	}
	
	public boolean stackCountScroll(double mouseX, double mouseY, double delta) {
		if(delta > 0)
		{
			if(this.stackCount < 64)
				this.stackCount++;
		}
		else if(delta < 0)
		{
			if(this.stackCount > 1)
				this.stackCount--;
		}
		return true;
	}
	
	@Override
	public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
		if(delta < 0)
		{			
			if(this.scroll < this.getMaxScroll())
				this.scroll++;
			else
				return false;
		}
		else if(delta > 0)
		{
			if(this.scroll > 0)
				this.scroll--;
			else
				return false;
		}
		return true;
	}

	@Override
	public int currentScroll() { return this.scroll; }

	@Override
	public void setScroll(int newScroll) {
		this.scroll = newScroll;
		this.refreshPage();
	}
	
}
