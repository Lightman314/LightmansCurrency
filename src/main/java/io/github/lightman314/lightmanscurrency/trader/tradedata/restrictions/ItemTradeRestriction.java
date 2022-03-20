package io.github.lightman314.lightmanscurrency.trader.tradedata.restrictions;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

import com.mojang.datafixers.util.Pair;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.trader.tradedata.ItemTradeData;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.NewRegistryEvent;
import net.minecraftforge.registries.RegistryBuilder;

@Mod.EventBusSubscriber(modid = LightmansCurrency.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ItemTradeRestriction extends ForgeRegistryEntry<ItemTradeRestriction>{

	static Supplier<IForgeRegistry<ItemTradeRestriction>> ITEM_TRADE_RESTRICTIONS;
	
	private static final List<ItemTradeRestriction> RESTRICTIONS = new ArrayList<>();
	
	public static final ItemTradeRestriction NONE = register("none", new ItemTradeRestriction("NONE"));
	public static final ItemTradeRestriction ARMOR_HEAD = register("armor_head", new EquipmentRestriction(EquipmentSlot.HEAD, "ARMOR_HEAD"));
	public static final ItemTradeRestriction ARMOR_CHEST = register("armor_chest", new EquipmentRestriction(EquipmentSlot.CHEST, "ARMOR_CHEST"));
	public static final ItemTradeRestriction ARMOR_LEGS = register("armor_legs", new EquipmentRestriction(EquipmentSlot.LEGS, "ARMOR_LEGS"));
	public static final ItemTradeRestriction ARMOR_FEET = register("armor_feet", new EquipmentRestriction(EquipmentSlot.FEET, "ARMOR_FEET"));
	public static final ItemTradeRestriction TICKET_KIOSK = register("ticket_kiosk", new TicketKioskRestriction("TICKET"));
	
	//Restricion functionality
	private final String classicType;
	
	public ItemTradeRestriction() { this.classicType = ""; }
	
	public ItemTradeRestriction(String classicType) { this.classicType = classicType; }
	
	public ItemStack modifySellItem(ItemStack sellItem, ItemTradeData trade) { return sellItem; }
	
	public boolean allowSellItem(ItemStack itemStack) { return true; }
	
	public ItemStack filterSellItem(ItemStack itemStack) { return itemStack; }
	
	public boolean allowItemSelectItem(ItemStack itemStack) { return true; }
	
	public int getSaleStock(ItemStack sellItem, Container traderStorage)
	{
		return InventoryUtil.GetItemCount(traderStorage, sellItem) / sellItem.getCount();
	}
	
	public void removeItemsFromStorage(ItemStack sellItem, Container traderStorage)
	{
		InventoryUtil.RemoveItemCount(traderStorage, sellItem);
	}
	
	@OnlyIn(Dist.CLIENT)
	public Pair<ResourceLocation,ResourceLocation> getEmptySlotBG() { return null; }
	
	//Registry functions
	private static ItemTradeRestriction register(String key, ItemTradeRestriction restriction)
	{
		restriction.setRegistryName(key);
		RESTRICTIONS.add(restriction);
		return restriction;
	}
	
	@SubscribeEvent
	public static void createRegistry(NewRegistryEvent event)
	{
		RegistryBuilder<ItemTradeRestriction> builder = new RegistryBuilder<ItemTradeRestriction>();
		builder.setType(ItemTradeRestriction.class);
		ResourceLocation key = new ResourceLocation(LightmansCurrency.MODID, "item_trade_restrictions");
		builder.setName(key);
		builder.setDefaultKey(key);
		ITEM_TRADE_RESTRICTIONS = event.create(builder);
	}
	
	@SubscribeEvent
	public static void registerRestrictions(final RegistryEvent.Register<ItemTradeRestriction> event)
	{
		RESTRICTIONS.forEach(restriction -> event.getRegistry().register(restriction));
		RESTRICTIONS.clear();
	}
	
	public static ItemTradeRestriction get(String key)
	{
		ResourceLocation testKey = null;
		try {
			 testKey = new ResourceLocation(key);
		} catch(Exception e) {} //Catch invalid resource locations
		ItemTradeRestriction restriction = null;
		if(testKey != null)
			restriction = ITEM_TRADE_RESTRICTIONS.get().getValue(testKey);
		if(restriction == null)
		{
			//Search through the classic names
			AtomicReference<ItemTradeRestriction> temp = new AtomicReference<ItemTradeRestriction>();
			ITEM_TRADE_RESTRICTIONS.get().forEach(r ->{
				if(r.classicType.equals(key))
					temp.set(r);
			});
			if(temp.get() != null)
				return temp.get();
			//Return none by default
			return ItemTradeRestriction.NONE;
		}
		return restriction;
	}
	
}
