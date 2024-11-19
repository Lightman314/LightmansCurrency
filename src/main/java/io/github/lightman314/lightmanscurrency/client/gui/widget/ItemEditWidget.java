package io.github.lightman314.lightmanscurrency.client.gui.widget;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.easy.EasyScreenHelper;
import io.github.lightman314.lightmanscurrency.client.gui.easy.interfaces.ITooltipSource;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.IScrollable;
import io.github.lightman314.lightmanscurrency.client.gui.widget.scroll.ScrollBarWidget;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.items.TicketItem;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import io.github.lightman314.lightmanscurrency.common.util.LookupHelper;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

public class ItemEditWidget extends EasyWidgetWithChildren implements IScrollable, ITooltipSource {

	public static final ResourceLocation GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "textures/gui/item_edit.png");

	private static ItemEditWidget latestInstance = null;
	private static boolean rebuilding = false;

	private static final List<Function<CreativeModeTab,Boolean>> ITEM_GROUP_BLACKLIST = new ArrayList<>();

	public static void BlacklistCreativeTabs(CreativeModeTab... tabs) {
		for(CreativeModeTab tab : tabs)
			BlacklistCreativeTab(t -> tab == t);
	}

	@SafeVarargs
	public static void BlacklistCreativeTabs(ResourceKey<CreativeModeTab>... tabs) {
		for(ResourceKey<CreativeModeTab> tab : tabs)
			BlacklistCreativeTab(t -> BuiltInRegistries.CREATIVE_MODE_TAB.get(tab) == t);
	}

	@SafeVarargs
	public static void BlacklistCreativeTabs(Supplier<CreativeModeTab>... tabs) {
		for(Supplier<CreativeModeTab> tab : tabs)
			BlacklistCreativeTab(t -> tab.get() == t);
	}

	public static void BlacklistCreativeTab(Function<CreativeModeTab,Boolean> tabMatcher) {
		if(!ITEM_GROUP_BLACKLIST.contains(tabMatcher))
			ITEM_GROUP_BLACKLIST.add(tabMatcher);
	}

	public static boolean IsCreativeTabAllowed(CreativeModeTab tab) {
		for(Function<CreativeModeTab,Boolean> test : ITEM_GROUP_BLACKLIST)
		{
			if(test.apply(tab))
				return false;
		}
		return true;
	}

	private static final List<Predicate<ItemStack>> ITEM_BLACKLIST = Lists.newArrayList((s) -> s.getItem() instanceof TicketItem);

	public static void BlacklistItem(Supplier<? extends ItemLike> item) { BlacklistItem(item.get()); }
	public static void BlacklistItem(ItemLike item) { BlacklistItem((s) -> s.getItem() == item.asItem()); }

	public static void BlacklistItem(Predicate<ItemStack> itemFilter) {
		if(!ITEM_BLACKLIST.contains(itemFilter))
			ITEM_BLACKLIST.add(itemFilter);
	}

	private static final List<ItemInsertRule> ITEM_ADDITIONS = new ArrayList<>();
	public static void AddExtraItem(ItemStack item) { ITEM_ADDITIONS.add(ItemInsertRule.atEnd(item)); }
	public static void AddExtraItemAfter(ItemStack item, @Nonnull Item afterItem) { ITEM_ADDITIONS.add(ItemInsertRule.afterItem(item, afterItem)); }
	public static void AddExtraItemAfter(ItemStack item, @Nonnull Predicate<ItemStack> afterItem) { ITEM_ADDITIONS.add(ItemInsertRule.afterCheck(item, afterItem)); }
	public static void AddExtraItemBefore(ItemStack item, @Nonnull Item beforeItem) { ITEM_ADDITIONS.add(ItemInsertRule.beforeItem(item, beforeItem)); }
	public static void AddExtraItemBefore(ItemStack item, @Nonnull Predicate<ItemStack> beforeItem) { ITEM_ADDITIONS.add(ItemInsertRule.beforeCheck(item, beforeItem)); }

	public static boolean isItemAllowed(ItemStack item) {
		for(Predicate<ItemStack> blacklist : ITEM_BLACKLIST)
		{
			if(blacklist.test(item))
				return false;
		}
		return true;
	}


	private int scroll = 0;
	private int stackCount = 1;

	private final int columns;
	private final int rows;

	private final ScreenPosition searchOffset;

	private final ScreenPosition stackSizeOffset;

	private static final List<ItemStack> allItems = new ArrayList<>();
	private static final Map<ResourceLocation,List<ItemStack>> preFilteredItems = new HashMap<>();

	private List<ItemStack> searchResultItems = new ArrayList<>();

	private String searchString;

	EditBox searchInput;
	ScrollListener stackScrollListener;
	private final IItemEditListener listener;

	private final Font font;

	private final ItemEditWidget oldItemEdit;
	@Nullable
	private EditBox getOldSearchInput() { return this.oldItemEdit != null ? this.oldItemEdit.searchInput : null; }
	private String getOldSearchString() { return this.oldItemEdit != null ? this.oldItemEdit.searchString : ""; }

	private ItemEditWidget(@Nonnull Builder builder)
	{
		super(builder);
		latestInstance = this;
		this.listener = builder.handler;
		this.oldItemEdit = builder.oldWidget;

		this.columns = builder.columns;
		this.rows = builder.rows;

        this.searchOffset = Objects.requireNonNullElse(builder.searchOffset,ScreenPosition.of(this.width - 90, -13));

		this.stackSizeOffset = Objects.requireNonNullElse(builder.stackSizeOffset,ScreenPosition.of(this.width + 13, 0));

		Minecraft mc = Minecraft.getInstance();
		this.font = mc.font;

		//Attempt to reload the item list, but using a different thread now :)
		//Will hopefully not encounter any errors that normally happen during the PlayerLoginEvent call
		ConfirmItemListLoaded();

		//Set the search to the default value to initialize the inventory
		this.modifySearch(this.getOldSearchString());
		if(this.oldItemEdit != null)
			this.setScroll(this.oldItemEdit.scroll);

	}

	/**
	 * Re-initializes the item edit list on a different thread.
	 * If no changes to the creative items have happened, and any items are cached it will not re-initialize the cached items.
	 */
	public static void ConfirmItemListLoaded()
	{
		if(allItems.isEmpty()) //Flag as rebuilding asap if the list is empty
			rebuilding = true;
		new Thread(ItemEditWidget::safeInitItemList).start();
	}

	/**
	 * Re-initializes the item edit list. Runs on the same thread, so may cause lag if called at an inappropriate time.
	 * If no changes to the creative items have happened, and any items are cached it will not re-initialize the cached items.
	 */
	public static void safeInitItemList()
	{
		try { initItemList(); }
		catch (Throwable t) { LightmansCurrency.LogError("Error occurred while attempting to set up the Item List!\nPlease report this error to the relevant mod author (if another mod is mentioned in the error), not to the Lightman's Currency Dev!", t); }
		rebuilding = false;
	}

	private static void initItemList() {

		Minecraft mc = Minecraft.getInstance();
		if(mc == null)
			return;

		LocalPlayer player = mc.player;
		if(player == null)
			return;

		FeatureFlagSet flagSet = player.connection.enabledFeatures();
		boolean hasPermissions  = mc.options.operatorItemsTab().get() && player.canUseGameMasterBlocks();
		RegistryAccess lookup = mc.player.level().registryAccess();

		//Force Creative Tab content rebuild
		if(!CreativeModeTabs.tryRebuildTabContents(flagSet, hasPermissions, lookup) && !allItems.isEmpty())
		{
			//Ignore if we have existing results, and the tab contents have not been changed.
			LightmansCurrency.LogDebug("Creative Tab Contents have not changed. Used existing filtered results.");
			return;
		}

		LightmansCurrency.LogInfo("Pre-filtering item list for Item Edit items.");
		//Flag as rebuilding
		rebuilding = true;

		allItems.clear();

		//Go through all the item groups to avoid allowing sales of hidden items
		for(CreativeModeTab creativeTab : CreativeModeTabs.allTabs())
		{
			if(IsCreativeTabAllowed(creativeTab))
			{
				//Add all items in this creative tab to the list
				//while also confirming we don't already have it in the list
				try{
					for(ItemStack stack : creativeTab.getDisplayItems())
					{
						if(isItemAllowed(stack))
						{
							addToList(stack);
							if(stack.getItem() == Items.ENCHANTED_BOOK)
							{
								//LightmansCurrency.LogInfo("Attempting to add lower levels of an enchanted book.");
								ItemEnchantments enchantments = stack.getAllEnchantments(lookup.lookupOrThrow(Registries.ENCHANTMENT));
								for(var entry : enchantments.entrySet())
								{
									for(int newLevel = entry.getIntValue() - 1; newLevel > 0; newLevel--)
									{
										ItemStack newBook = new ItemStack(Items.ENCHANTED_BOOK);
										ItemEnchantments.Mutable e = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
										e.set(entry.getKey(),newLevel);
										newBook.set(DataComponents.ENCHANTMENTS,e.toImmutable());
										if(isItemAllowed(newBook))
											addToList(newBook);
									}
								}
							}
						}
					}
				} catch (Throwable t) { LightmansCurrency.LogError("Error getting display items from the '" + creativeTab.getDisplayName().getString() + "' tab!\nThis tab will be ignored!", t); }
			}
		}

		//Add Extra Items with no before or after rules
		for(ItemInsertRule extraItemRule : ITEM_ADDITIONS)
		{
			if(extraItemRule.shouldInsertAtEnd())
			{
				ItemStack extraItem = extraItemRule.insertStack.copy();
				if(isItemAllowed(extraItem) && notYetInList(extraItem))
					allItems.add(extraItem.copy());
			}
		}

		preFilteredItems.clear();

		ItemTradeRestriction.forEach((type, restriction) -> preFilteredItems.put(type, allItems.stream().filter(restriction::allowItemSelectItem).collect(Collectors.toList())));

		if(latestInstance != null)
			latestInstance.refreshSearch();

	}

	private static void addToList(ItemStack stack)
	{
		stack = stack.copy();
		if(notYetInList(stack))
		{
			//Add any before rules
			for(ItemInsertRule insertRule : ITEM_ADDITIONS)
			{
				if(insertRule.shouldInsertBefore(stack))
				{
					ItemStack extraItem = insertRule.insertStack.copy();
					if(isItemAllowed(extraItem) && notYetInList(extraItem))
						allItems.add(extraItem);
				}
			}
			//Add the item itself
			allItems.add(stack);

			//Add any after rules
			for(ItemInsertRule insertRule : ITEM_ADDITIONS)
			{
				if(insertRule.shouldInsertAfter(stack))
				{
					ItemStack extraItem = insertRule.insertStack.copy();
					if(isItemAllowed(extraItem) && notYetInList(extraItem))
						allItems.add(extraItem);
				}
			}
		}
	}

	private static boolean notYetInList(ItemStack stack) { return allItems.stream().noneMatch(s -> InventoryUtil.ItemMatches(s, stack)); }

	@Nonnull
	private List<ItemStack> getFilteredItems()
	{
		if(this.listener.restrictItemEditItems())
		{
			ItemTradeData trade = this.listener.getTrade();
			ItemTradeRestriction restriction = trade == null ? ItemTradeRestriction.NONE : trade.getRestriction();
			return getFilteredItems(restriction);
		}
		return new ArrayList<>(allItems);
	}

	@Nonnull
	private List<ItemStack> getFilteredItems(ItemTradeRestriction restriction)
	{
		//If the items are still being collected, don't try to access the lists
		if(rebuilding)
			return new ArrayList<>();
		ResourceLocation type = ItemTradeRestriction.getId(restriction);
		if(type == ItemTradeRestriction.NO_RESTRICTION_KEY && restriction != ItemTradeRestriction.NONE)
		{
			LightmansCurrency.LogWarning("Item Trade Restriction of class '" + restriction.getClass().getSimpleName() + "' was not registered, and is now being used to filter items.\nPlease register during the common setup so that this filtering can be done before the screen is opened to prevent in-game lag.");
			return new ArrayList<>(allItems).stream().filter(restriction::allowItemSelectItem).collect(Collectors.toList());
		}
		if(!preFilteredItems.containsKey(type))
		{
			LightmansCurrency.LogWarning("Item Trade Restriction of type '" + type + "' was registered AFTER the Player logged-in to the world. Please ensure that they're registered during the common setup phase so that filtering can be done at a less critical time.");
			preFilteredItems.put(type, new ArrayList<>(allItems).stream().filter(restriction::allowItemSelectItem).collect(Collectors.toList()));
		}
		return preFilteredItems.get(type);
	}

	public int getMaxScroll() { return Math.max(((this.searchResultItems.size() - 1) / this.columns) - this.rows + 1, 0); }

	public void refreshPage()
	{

		this.validateScroll();

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

	public void modifySearch(@Nonnull String newSearch)
	{
		this.searchString = newSearch.toLowerCase();

		//Repopulate the searchResultItems list
		if(!this.searchString.isEmpty())
		{
			this.searchResultItems = new ArrayList<>();
			for(ItemStack stack : this.getFilteredItems())
			{
				//Search the display name
				if(stack.getHoverName().getString().toLowerCase().contains(this.searchString))
				{
					this.searchResultItems.add(stack);
				}
				//Search the registry name
				else if(BuiltInRegistries.ITEM.getKey(stack.getItem()).toString().contains(this.searchString))
				{
					this.searchResultItems.add(stack);
				}
				//Search the enchantments?
				else
				{
					boolean enchantmentMatch = false;
					ItemEnchantments enchantments = stack.getAllEnchantments(LookupHelper.getRegistryAccess().lookupOrThrow(Registries.ENCHANTMENT));
					for(var entry : enchantments.entrySet())
					{
						if(entry.getKey().getRegisteredName().contains(this.searchString))
							enchantmentMatch = true;
						else if(Enchantment.getFullname(entry.getKey(),entry.getIntValue()).getString().toLowerCase().contains(this.searchString))
							enchantmentMatch = true;
					}
					if(enchantmentMatch)
						this.searchResultItems.add(stack);
				}
			}
		}
		else //No search string, so the result is just the allItems list
		{
			this.searchResultItems = this.getFilteredItems();
		}

		//Run refresh page code to validate the page # and repopulate the display inventory
		this.refreshPage();

	}

	@Override
	public void addChildren(@Nonnull ScreenArea area) {
		this.searchInput = this.addChild(new EditBox(this.font, area.x + this.searchOffset.x + 2, area.y + this.searchOffset.y + 2, 79, 9, this.getOldSearchInput(), LCText.GUI_ITEM_EDIT_SEARCH.get()));
		this.searchInput.setBordered(false);
		this.searchInput.setMaxLength(32);
		this.searchInput.setTextColor(0xFFFFFF);
		this.searchInput.setResponder(this::modifySearch);

		this.stackScrollListener = this.addChild(ScrollListener.builder()
				.position(area.pos.offset(this.stackSizeOffset))
				.size(18,18)
				.listener(this::stackCountScroll)
				.build());

		this.addChild(ScrollBarWidget.builder()
				.onRight(this)
				.smallKnob()
				.addon(EasyAddonHelper.visibleCheck(this::isVisible))
				.build());

	}

	@Override
	protected void renderTick() {
		this.searchInput.visible = this.visible;
		this.stackScrollListener.active = this.visible;
	}

	@Override
	public void renderWidget(@Nonnull EasyGuiGraphics gui) {
		//Removed search check as this is now handled by EditBox#setResponder

		int index = this.scroll * this.columns;
		for(int y = 0; y < this.rows && index < this.searchResultItems.size(); ++y)
		{
			int yPos = y * 18;
			for(int x = 0; x < this.columns && index < this.searchResultItems.size(); ++x)
			{
				//Get the slot position
				int xPos = x * 18;
				//Render the slot background
				gui.resetColor();
				gui.blit(GUI_TEXTURE, xPos, yPos, 0, 0, 18, 18);
				//Render the slots item
				gui.renderItem(this.getQuantityFixedStack(this.searchResultItems.get(index)), xPos + 1, yPos + 1);
				index++;
			}
		}

		//Render the search field
		gui.resetColor();
		gui.blit(GUI_TEXTURE, this.searchOffset, 18, 0, 90, 12);

		//Render the quantity scroll area
		gui.blit(GUI_TEXTURE, this.stackSizeOffset, 108, 0, 18, 18);

	}

	private ItemStack getQuantityFixedStack(ItemStack stack) {
		ItemStack copy = stack.copy();
		copy.setCount(Math.min(stack.getMaxStackSize(), this.stackCount));
		return copy;
	}

	@Override
	public List<Component> getTooltipText(int mouseX, int mouseY) {
		if(!this.isVisible())
			return null;
		int hoveredSlot = this.isMouseOverSlot(mouseX, mouseY);
		if(hoveredSlot >= 0)
		{
			hoveredSlot += this.scroll * this.columns;
			if(hoveredSlot < this.searchResultItems.size())
				return EasyScreenHelper.getTooltipFromItem(this.searchResultItems.get(hoveredSlot));
		}
		if(this.isMouseOverStackSizeScroll(mouseX,mouseY))
			return LCText.TOOLTIP_ITEM_EDIT_SCROLL.getAsList();
		return null;
	}

	private boolean isMouseOverStackSizeScroll(int mouseX, int mouseY) {
		return this.stackSizeOffset.offset(this.getPosition()).asArea(18,18).isMouseInArea(mouseX,mouseY);
	}

	private int isMouseOverSlot(double mouseX, double mouseY) {
		if(!this.isVisible())
			return -1;

		int foundColumn = -1;
		int foundRow = -1;

		for(int x = 0; x < this.columns && foundColumn < 0; ++x)
		{
			if(mouseX >= this.getX() + x * 18 && mouseX < this.getX() + (x * 18) + 18)
				foundColumn = x;
		}
		for(int y = 0; y < this.rows && foundRow < 0; ++y)
		{
			if(mouseY >= this.getY() + y * 18 && mouseY < this.getY() + (y * 18) + 18)
				foundRow = y;
		}
		if(foundColumn < 0 || foundRow < 0)
			return -1;
		return (foundRow * this.columns) + foundColumn;
	}

	public interface IItemEditListener {
		ItemTradeData getTrade();
		boolean restrictItemEditItems();
		void onItemClicked(ItemStack item);
	}

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

	public boolean stackCountScroll(double delta) {
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
	public boolean mouseScrolled(double mouseX, double mouseY, double deltaX, double deltaY) {
		if(deltaY < 0)
		{
			if(this.scroll < this.getMaxScroll())
				this.scroll++;
			else
				return false;
		}
		else if(deltaY > 0)
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

	private static class ItemInsertRule
	{

		public final ItemStack insertStack;
		private final Predicate<ItemStack> afterItemCheck;
		private final Predicate<ItemStack> beforeItemCheck;

		private final Predicate<ItemStack> NULLCHECK = (s) -> false;
		private ItemInsertRule(ItemStack insertStack, @Nullable Predicate<ItemStack> afterItemCheck, @Nullable Predicate<ItemStack> beforeItemCheck)
		{
			this.insertStack = insertStack;
			this.afterItemCheck = afterItemCheck == null ? NULLCHECK : afterItemCheck;
			this.beforeItemCheck = beforeItemCheck == null ? NULLCHECK : beforeItemCheck;
		}

		public static ItemInsertRule atEnd(ItemStack insertStack) { return new ItemInsertRule(insertStack, null, null); }
		public static ItemInsertRule afterItem(ItemStack insertStack, @Nonnull Item item) { return new ItemInsertRule(insertStack, (s) -> s.getItem() == item, null); }
		public static ItemInsertRule afterCheck(ItemStack insertStack, @Nonnull Predicate<ItemStack> check) { return new ItemInsertRule(insertStack, check, null); }
		public static ItemInsertRule beforeItem(ItemStack insertStack, @Nonnull Item item) { return new ItemInsertRule(insertStack, null, (s) -> s.getItem() == item); }
		public static ItemInsertRule beforeCheck(ItemStack insertStack, @Nonnull Predicate<ItemStack> check) { return new ItemInsertRule(insertStack, null, check); }

		public boolean shouldInsertBefore(ItemStack insertedItem) { return this.beforeItemCheck.test(insertedItem); }
		public boolean shouldInsertAfter(ItemStack insertedItem) { return this.afterItemCheck.test(insertedItem); }
		public boolean shouldInsertAtEnd() { return this.afterItemCheck == NULLCHECK && this.beforeItemCheck == null; }

	}

	@Nonnull
	public static Builder builder() { return new Builder(); }

	@MethodsReturnNonnullByDefault
	@FieldsAreNonnullByDefault
	@ParametersAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(18,18); }
		@Override
		protected Builder getSelf() { return this; }

		int columns = 1;
		int rows = 1;
		@Nullable
		IItemEditListener handler = null;
		@Nullable
		ItemEditWidget oldWidget = null;
		@Nullable
		ScreenPosition searchOffset = null;
		@Nullable
		ScreenPosition stackSizeOffset = null;

		public Builder columns(int columns) { this.columns = columns; this.changeWidth(18 * this.columns); return this; }
		public Builder rows(int rows) { this.rows = rows; this.changeHeight(18 * this.rows); return this; }
		public Builder handler(IItemEditListener handler) { this.handler = handler; return this; }
		public Builder oldWidget(@Nullable ItemEditWidget oldWidget) { this.oldWidget = oldWidget; return this; }

		public Builder searchOffset(ScreenPosition searchOffset) { this.searchOffset = searchOffset; return this; }
		public Builder searchOffset(int searchOffX, int searchOffY) { this.searchOffset = ScreenPosition.of(searchOffX,searchOffY); return this; }
		public Builder stackSizeOffset(ScreenPosition stackSizeOffset) { this.stackSizeOffset = stackSizeOffset; return this; }
		public Builder stackSizeOffset(int stackSizeOffX, int stackSizeOffY) { this.stackSizeOffset = ScreenPosition.of(stackSizeOffX,stackSizeOffY); return this; }

		public ItemEditWidget build() { return new ItemEditWidget(this); }

	}

}