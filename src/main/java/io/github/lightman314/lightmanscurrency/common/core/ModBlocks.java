package io.github.lightman314.lightmanscurrency.common.core;

import java.util.function.*;

import javax.annotation.Nullable;

import com.mojang.datafixers.util.Function3;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.common.blocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.tradeinterface.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.*;
import io.github.lightman314.lightmanscurrency.common.blocks.traderblocks.reference.*;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBiBundle;
import io.github.lightman314.lightmanscurrency.common.core.groups.RegistryObjectBundle;
import io.github.lightman314.lightmanscurrency.common.core.variants.Color;
import io.github.lightman314.lightmanscurrency.common.core.variants.WoodType;
import io.github.lightman314.lightmanscurrency.common.items.*;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister<Block> REGISTER = DeferredRegister.create(BuiltInRegistries.BLOCK,LightmansCurrency.MODID);

	private static <T extends Block> ResourceLocation idGetter(T block) { return BuiltInRegistries.BLOCK.getKey(block); }

	private static BlockItemFactory getDefaultGenerator() { return BlockItem::new; }
	private static BlockItemFactory getDefaultFireproofGenerator() {
        return new BlockItemFactory() {
            @Override
            public Item buildItem(Block block, Item.Properties properties) { return new BlockItem(block,properties); }
            @Override
            public Item.Properties propertyBuilder(Item.Properties p) { return p.fireResistant(); }
        };
	}
	private static BlockItemFactory getCoinJarGenerator() { return CoinJarItem::new; }
	private static BlockItemFactory getMoneyBagGenerator() { return MoneyBagItem::new; }

	private static final SoundType CHOCOLATE_SOUND = SoundType.MUD_BRICKS;

	//Coin piles
	public static final Supplier<CoinpileBlock> COINPILE_COPPER = register("coinpile_copper",CoinpileBlock::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinpileBlock> COINPILE_IRON = register("coinpile_iron",CoinpileBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinpileBlock> COINPILE_GOLD = register("coinpile_gold",CoinpileBlock::new,
            p -> p.mapColor(MapColor.GOLD)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinpileBlock> COINPILE_EMERALD = register("coinpile_emerald",CoinpileBlock::new,
            p -> p.mapColor(MapColor.EMERALD)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinpileBlock> COINPILE_DIAMOND = register("coinpile_diamond",CoinpileBlock::new,
            p -> p.mapColor(MapColor.DIAMOND)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinpileBlock> COINPILE_NETHERITE = register("coinpile_netherite",getDefaultFireproofGenerator(),CoinpileBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));

    //Chocolate Coin Piles
	public static final Supplier<CoinpileBlock> COINPILE_CHOCOLATE_COPPER = register("coinpile_chocolate_copper",CoinpileBlock::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinpileBlock> COINPILE_CHOCOLATE_IRON = register("coinpile_chocolate_iron",CoinpileBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinpileBlock> COINPILE_CHOCOLATE_GOLD = register("coinpile_chocolate_gold",CoinpileBlock::new,
            p -> p.mapColor(MapColor.GOLD)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinpileBlock> COINPILE_CHOCOLATE_EMERALD = register("coinpile_chocolate_emerald",CoinpileBlock::new,
            p -> p.mapColor(MapColor.EMERALD)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinpileBlock> COINPILE_CHOCOLATE_DIAMOND = register("coinpile_chocolate_diamond",CoinpileBlock::new,
            p -> p.mapColor(MapColor.DIAMOND)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinpileBlock> COINPILE_CHOCOLATE_NETHERITE = register("coinpile_chocolate_netherite",getDefaultFireproofGenerator(),CoinpileBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));

	//Coin Blocks
	public static final Supplier<CoinBlock> COINBLOCK_COPPER = register("coinblock_copper",CoinBlock::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinBlock> COINBLOCK_IRON = register("coinblock_iron",CoinBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinBlock> COINBLOCK_GOLD = register("coinblock_gold",CoinBlock::new,
            p -> p.mapColor(MapColor.GOLD)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinBlock> COINBLOCK_EMERALD = register("coinblock_emerald",CoinBlock::new,
            p -> p.mapColor(MapColor.EMERALD)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinBlock> COINBLOCK_DIAMOND = register("coinblock_diamond",CoinBlock::new,
            p -> p.mapColor(MapColor.DIAMOND)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinBlock> COINBLOCK_NETHERITE = register("coinblock_netherite",getDefaultFireproofGenerator(),CoinBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));

    //Chocolate Coin Blocks
	public static final Supplier<CoinBlock> COINBLOCK_CHOCOLATE_COPPER = register("coinblock_chocolate_copper",CoinBlock::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinBlock> COINBLOCK_CHOCOLATE_IRON = register("coinblock_chocolate_iron",CoinBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinBlock> COINBLOCK_CHOCOLATE_GOLD = register("coinblock_chocolate_gold",CoinBlock::new,
            p -> p.mapColor(MapColor.GOLD)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinBlock> COINBLOCK_CHOCOLATE_EMERALD = register("coinblock_chocolate_emerald",CoinBlock::new,
            p -> p.mapColor(MapColor.EMERALD)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinBlock> COINBLOCK_CHOCOLATE_DIAMOND = register("coinblock_chocolate_diamond",CoinBlock::new,
            p -> p.mapColor(MapColor.DIAMOND)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));
	public static final Supplier<CoinBlock> COINBLOCK_CHOCOLATE_NETHERITE = register("coinblock_chocolate_netherite",CoinBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK)
                    .strength(3.0f, 6.0f)
                    .sound(CHOCOLATE_SOUND));

	//Machines
	//Misc Machines
	public static final Supplier<ATMBlock> ATM = register("atm",ATMBlock::new,
            p -> p.mapColor(MapColor.COLOR_GRAY)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));
	public static final Supplier<CoinMintBlock> COIN_MINT = register("coinmint",CoinMintBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE)
                    .strength(2.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));
    public static final Supplier<CashRegisterBlock> CASH_REGISTER = register("cash_register",CashRegisterItem::new,p -> new CashRegisterBlock(p,Block.box(1d,0d,1d,15d,10d,15d)),
            p -> p.mapColor(MapColor.COLOR_GRAY)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));

    //Item Traders
	//Display Case
	public static final RegistryObjectBundle<DisplayCaseBlock,Color> DISPLAY_CASE = registerColored("display_case",DisplayCaseBlock::new,
            (p,c) -> p.strength(2.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.GLASS),
            Color.WHITE);

	//Vending Machines
	public static final RegistryObjectBundle<VendingMachineBlock,Color> VENDING_MACHINE = registerColored("vending_machine",VendingMachineBlock::new,
            (p,c) -> p.mapColor(c.mapColor)
                    .strength(5.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL),
            Color.WHITE);

	//Large Vending Machines
	public static final RegistryObjectBundle<VendingMachineLargeBlock,Color> VENDING_MACHINE_LARGE = registerColored("vending_machine_large",VendingMachineLargeBlock::new,
            (p,c) -> p.mapColor(c.mapColor)
                    .strength(5.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL),
            Color.WHITE);

	//Wooden Shelves
	public static final RegistryObjectBundle<ShelfBlock,WoodType> SHELF = registerWooden("shelf",WoodType.Attributes.needsSlab,p -> new ShelfBlock(p),
            (p,w) -> p.mapColor(w.mapColor)
                    .strength(2.0f, Float.POSITIVE_INFINITY));
	public static final RegistryObjectBundle<ShelfBlock,WoodType> SHELF_2x2 = registerWooden("shelf_2x2",WoodType.Attributes.needsSlab,p -> new ShelfBlock(p,4),
            (p,w) -> p.mapColor(w.mapColor)
                    .strength(2.0f, Float.POSITIVE_INFINITY));

	//Card Shelves
	public static final RegistryObjectBiBundle<CardDisplayBlock,WoodType,Color> CARD_DISPLAY = registerWoodenAndColored("card_display",WoodType.Attributes.needsLog,
            (p,w,c) -> new CardDisplayBlock(p,w.generateID("block.lightmanscurrency.card_display"),c),
            (p,w,c) -> p.mapColor(w.mapColor)
                    .strength(2.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.WOOD),
            Color.RED);

	//Armor Display
	public static final Supplier<ArmorDisplayBlock> ARMOR_DISPLAY = register("armor_display",ArmorDisplayBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(5.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));

	//Freezer
	public static final RegistryObjectBundle<FreezerBlock,Color> FREEZER = registerColored("freezer",
            (p,c) -> new FreezerBlock(p,FreezerBlock.GenerateDoorModel(c)),
            (p,c) -> p.mapColor(c.mapColor)
                    .strength(5.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL),
            Color.BLACK);

    //Ticket Kiosk
    public static final Supplier<TicketKioskBlock> TICKET_KIOSK = register("ticket_kiosk",TicketKioskBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(3.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));

    //Bookshelf Traders
    public static final RegistryObjectBundle<Block,WoodType> BOOKSHELF_TRADER = registerWooden("bookshelf_trader",WoodType.Attributes.needsPlanksAndSlab,BookTraderBlock::new,
            (p,w) -> p.mapColor(w.mapColor)
                    .strength(3.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.WOOD));

	//Network Traders
	public static final Supplier<NetworkItemTraderBlock> ITEM_NETWORK_TRADER_1 = register("item_network_trader_1",p -> new NetworkItemTraderBlock(p,4),
            p -> p.mapColor(MapColor.METAL)
                    .strength(5.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));
	public static final Supplier<NetworkItemTraderBlock> ITEM_NETWORK_TRADER_2 = register("item_network_trader_2",p -> new NetworkItemTraderBlock(p,8),
            p -> p.mapColor(MapColor.METAL)
                    .strength(5.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));
	public static final Supplier<NetworkItemTraderBlock> ITEM_NETWORK_TRADER_3 = register("item_network_trader_3",p -> new NetworkItemTraderBlock(p,12),
            p -> p.mapColor(MapColor.METAL)
                    .strength(5.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));
	public static final Supplier<NetworkItemTraderBlock> ITEM_NETWORK_TRADER_4 = register("item_network_trader_4",p -> new NetworkItemTraderBlock(p,16),
            p -> p.mapColor(MapColor.METAL)
                    .strength(5.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));

    //Paygate
    public static final Supplier<PaygateBlock> PAYGATE = register("paygate",PaygateBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(3.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));

    //Slot Machine
    public static final Supplier<SlotMachineBlock> SLOT_MACHINE = register("slot_machine",SlotMachineBlock::new,
            p -> p.mapColor(MapColor.COLOR_YELLOW)
                    .strength(3.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));

    //Gatcha Machine
    public static final RegistryObjectBundle<GachaMachineBlock,Color> GACHA_MACHINE = registerColored("gacha_machine", GachaMachineBlock::new,
            (p,c) -> p.mapColor(c.mapColor)
                    .strength(3.0f,Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));

    //Command Trader
    public static final Supplier<CommandTraderBlock> COMMAND_TRADER = register("command_trader",CommandTraderBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(3.0f,Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));

	//Trader Interface
	public static final Supplier<ItemTraderInterfaceBlock> ITEM_TRADER_INTERFACE = register("item_trader_interface",ItemTraderInterfaceBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(5.0f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));

	//Terminal
	public static final Supplier<TerminalBlock> TERMINAL = register("terminal",p -> new TerminalBlock(p,Block.box(1d,0d,1d,15d,16d,15d)),
            p -> p.mapColor(MapColor.METAL)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));

	public static final Supplier<TerminalBlock> GEM_TERMINAL = register("gem_terminal",p -> new TerminalBlock(p,Block.box(2d, 0d, 2d, 14d, 12d, 14d)),
            p -> p.mapColor(MapColor.COLOR_PURPLE)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.AMETHYST_CLUSTER));


	//Ticket Station
	public static final Supplier<TicketStationBlock> TICKET_STATION = register("ticket_machine",TicketStationBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(3.0f, 6.0f)
                    .sound(SoundType.METAL));

	//Coin Chest
	public static final Supplier<CoinChestBlock> COIN_CHEST = register("coin_chest",CoinChestBlock::new,
            p -> p.mapColor(MapColor.WOOD)
                    .strength(2.5f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.WOOD));

	//Tax Block
	public static final Supplier<TaxCollectorBlock> TAX_COLLECTOR = register("tax_block",TaxCollectorBlock::new,
            p -> p.mapColor(MapColor.METAL)
                    .strength(3f, Float.POSITIVE_INFINITY)
                    .sound(SoundType.METAL));

	//Money Bag
	public static final Supplier<Block> MONEY_BAG = register("money_bag",getMoneyBagGenerator(),MoneyBagBlock::new,
            p -> p.mapColor(MapColor.SAND)
                    .strength(0.1f,2.0f)
                    .sound(SoundType.COBWEB));

	//Coin Jars
	public static final Supplier<Block> PIGGY_BANK = register("piggy_bank",getCoinJarGenerator(),p -> new CoinJarBlock(p,Block.box(4d, 0d, 4d, 12d, 8d, 12d)),
            p -> p.mapColor(MapColor.COLOR_PINK)
                    .strength(0.1f, 2.0f)
                    .sound(SoundType.STONE));
	public static final Supplier<Block> COINJAR_BLUE = register("coinjar_blue",getCoinJarGenerator(),p -> new CoinJarBlock(p,Block.box(4d, 0d, 4d, 12d, 8d, 12d)),
            p -> p.mapColor(MapColor.COLOR_BLUE)
                    .strength(0.1f, 2.0f)
                    .sound(SoundType.STONE));
	public static final Supplier<Block> SUS_JAR = register("sus_jar",p -> new CoinJarBlock(p,Block.box(4, 0d, 4d, 12d, 8d, 12d),true),
            p -> p.mapColor(MapColor.SNOW)
                    .strength(0.1f, 2.0f)
                    .sound(SoundType.STONE));

	//Auciton Stands
	public static final RegistryObjectBundle<Block,WoodType> AUCTION_STAND = registerWooden("auction_stand",WoodType.Attributes.needsLog,AuctionStandBlock::new,
            (p,w) -> p.mapColor(w.mapColor)
                    .strength(2.0f));

	
	static {

        //Add alias's for old trader names
        //Item Network Traders
        addAlias("item_trader_server_sml","item_network_trader_1");
        addAlias("item_trader_server_med","item_network_trader_2");
        addAlias("item_trader_server_lrg","item_network_trader_3");
        addAlias("item_trader_server_xlrg","item_network_trader_4");
		
	}
	
	
	/**
	* Block Registration Code
	*/
	public static <T extends Block> DeferredHolder<Block,T> register(String name, Function<BlockBehaviour.Properties,T> factory, UnaryOperator<BlockBehaviour.Properties> propertyBuilder)
	{
		return register(name, getDefaultGenerator(),factory,propertyBuilder);
	}

    public static <T extends Block> DeferredHolder<Block,T> register(String name,BlockItemFactory itemFactory,Function<BlockBehaviour.Properties,T> factory,UnaryOperator<BlockBehaviour.Properties> propertyBuilder)
	{
        DeferredHolder<Block,T> block = REGISTER.register(name,() -> factory.apply(propertyBuilder.apply(BlockBehaviour.Properties.of())));
		if(block != null)
			ModItems.register(name,p -> itemFactory.buildItem(block.get(),p),itemFactory::propertyBuilder);
		return block;
	}

	// Colored block registration code
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, Function<BlockBehaviour.Properties,T> block,BiFunction<BlockBehaviour.Properties,Color,BlockBehaviour.Properties> propertyBuilder) { return registerColored(name,block,propertyBuilder,null); }
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, BiFunction<BlockBehaviour.Properties,Color,T> block,BiFunction<BlockBehaviour.Properties,Color,BlockBehaviour.Properties> propertyBuilder) { return registerColored(name,block,propertyBuilder,null); }
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, Function<BlockBehaviour.Properties,T> block,BiFunction<BlockBehaviour.Properties,Color,BlockBehaviour.Properties> propertyBuilder, @Nullable Color dontNameThisColor) { return registerColored(name,getDefaultGenerator(),block,propertyBuilder,dontNameThisColor); }
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, BiFunction<BlockBehaviour.Properties,Color,T> block,BiFunction<BlockBehaviour.Properties,Color,BlockBehaviour.Properties> propertyBuilder, @Nullable Color dontNameThisColor) { return registerColored(name, getDefaultGenerator(), block, propertyBuilder, dontNameThisColor); }

	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, BlockItemFactory itemFactory, Function<BlockBehaviour.Properties,T> block,BiFunction<BlockBehaviour.Properties,Color,BlockBehaviour.Properties> propertyBuilder,@Nullable Color dontNameThisColor) { return registerColored(name,itemFactory,(p,c) -> block.apply(p),propertyBuilder,dontNameThisColor); }
	private static <T extends Block> RegistryObjectBundle<T,Color> registerColored(String name, BlockItemFactory itemFactory, BiFunction<BlockBehaviour.Properties,Color,T> block,BiFunction<BlockBehaviour.Properties,Color,BlockBehaviour.Properties> propertyBuilder,@Nullable Color dontNameThisColor)
	{
		RegistryObjectBundle<T,Color> bundle = new RegistryObjectBundle<>(Color::sortByColor);
		for(Color color : Color.values())
		{
			String thisName = name;
			if(color != dontNameThisColor) //Add the color name to the end unless this is the color flagged to not be named
				thisName += "_" + color.getResourceSafeName();
			//Register the block normally
			bundle.put(color,register(thisName,itemFactory,p -> block.apply(p,color),p -> propertyBuilder.apply(p,color)));
		}
		return bundle.lock();
	}
	
	/**
	 * Wooden block registration code
	 */
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name,Predicate<WoodType.Attributes> check,Function<BlockBehaviour.Properties,T> block,BiFunction<BlockBehaviour.Properties,WoodType,BlockBehaviour.Properties> propertyBuilder) { return registerWooden(name,check,getDefaultGenerator(),block,propertyBuilder); }
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name,Predicate<WoodType.Attributes> check,BiFunction<WoodType,BlockBehaviour.Properties,T> block,BiFunction<BlockBehaviour.Properties,WoodType,BlockBehaviour.Properties> propertyBuilder) { return registerWooden(name,check,getDefaultGenerator(),block,propertyBuilder); }

	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name,Predicate<WoodType.Attributes> check,BlockItemFactory itemFactory,Function<BlockBehaviour.Properties,T> block,BiFunction<BlockBehaviour.Properties,WoodType,BlockBehaviour.Properties> propertyBuilder) { return registerWooden(name,check,itemFactory,(w,p) -> block.apply(p),propertyBuilder); }
	private static <T extends Block> RegistryObjectBundle<T,WoodType> registerWooden(String name,Predicate<WoodType.Attributes> check,BlockItemFactory itemFactory,BiFunction<WoodType,BlockBehaviour.Properties,T> block,BiFunction<BlockBehaviour.Properties,WoodType,BlockBehaviour.Properties> propertyBuilder)
	{
		RegistryObjectBundle<T,WoodType> bundle = new RegistryObjectBundle<>(WoodType::sortByWood);
		for(WoodType woodType : WoodType.validValues())
		{
			if(!check.test(woodType.attributes))
				continue;
			String thisName = woodType.generateID(name);
			//Register the block normally
			bundle.put(woodType,register(thisName,itemFactory,p -> block.apply(woodType,p),p -> propertyBuilder.apply(p,woodType)));
		}
		return bundle.lock();
	}

	/**
	 * Wooden and colored block registration code
	 */
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name,Predicate<WoodType.Attributes> check,Function3<BlockBehaviour.Properties,WoodType,Color,T> block,Function3<BlockBehaviour.Properties,WoodType,Color,BlockBehaviour.Properties> propertyBuilder) { return registerWoodenAndColored(name,check,block,propertyBuilder,null); }
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name,Predicate<WoodType.Attributes> check,Function3<BlockBehaviour.Properties,WoodType,Color,T> block,Function3<BlockBehaviour.Properties,WoodType,Color,BlockBehaviour.Properties> propertyBuilder,@Nullable Color ignoreColor) { return registerWoodenAndColored(name,check,getDefaultGenerator(),block,propertyBuilder,ignoreColor); }

	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name,Predicate<WoodType.Attributes> check,BlockItemFactory itemFactory,Function3<BlockBehaviour.Properties,WoodType,Color,T> block,Function3<BlockBehaviour.Properties,WoodType,Color,BlockBehaviour.Properties> propertyBuilder) { return registerWoodenAndColored(name,check,itemFactory,block,propertyBuilder,null); }
	private static <T extends Block> RegistryObjectBiBundle<T,WoodType,Color> registerWoodenAndColored(String name,Predicate<WoodType.Attributes> check,BlockItemFactory itemFactory,Function3<BlockBehaviour.Properties,WoodType,Color,T> block,Function3<BlockBehaviour.Properties,WoodType,Color,BlockBehaviour.Properties> propertyBuilder,@Nullable Color ignoreColor)
	{
		RegistryObjectBiBundle<T,WoodType,Color> bundle = new RegistryObjectBiBundle<>(WoodType::sortByWood, Color::sortByColor);
		for(WoodType woodType: WoodType.validValues())
		{
			if(check.test(woodType.attributes))
			{
				for(Color color : Color.values())
				{
					String thisName;
					if(color == ignoreColor)
						thisName = woodType.generateID(name);
					else
						thisName = woodType.generateID(name) + "_" + color.getResourceSafeName();
					//Register the block normally
					bundle.put(woodType, color, register(thisName, itemFactory, p -> block.apply(p,woodType,color),p -> propertyBuilder.apply(p,woodType,color)));
				}
			}
		}
		return bundle.lock();
	}

    public static void addAlias(String oldName,String newName)
    {
        REGISTER.addAlias(LightmansCurrency.id(oldName),LightmansCurrency.id(newName));
        ModItems.addAlias(oldName,newName);
    }

    public interface BlockItemFactory
    {
        Item buildItem(Block block,Item.Properties properties);
        default Item.Properties propertyBuilder(Item.Properties p) { return p; }
    }

}
