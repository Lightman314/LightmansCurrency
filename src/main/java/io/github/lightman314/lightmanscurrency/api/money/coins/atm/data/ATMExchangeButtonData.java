package io.github.lightman314.lightmanscurrency.api.money.coins.atm.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.ATMAPI;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.ItemIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SimpleArrowIcon;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin.SimpleArrowIcon.ArrowType;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;

public class ATMExchangeButtonData {

	public final ScreenPosition position;
	public final int width;
	public final int height;
	public final String command;
	private final List<ATMIconData> icons;
	public ImmutableList<ATMIconData> getIcons() { return ImmutableList.copyOf(this.icons); }
	
	public static ATMExchangeButtonData parse(@Nonnull JsonObject data, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException { return new ATMExchangeButtonData(data,lookup); }
	
	private ATMExchangeButtonData(@Nonnull JsonObject data, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
		this.position = ScreenPosition.of(GsonHelper.getAsInt(data, "x"), GsonHelper.getAsInt(data,"y"));
		this.width = GsonHelper.getAsInt(data,"width");
		this.height = GsonHelper.getAsInt(data,"height",18);
		if(this.height <= 0)
			throw new JsonSyntaxException("height cannot be 0 or less!");
		this.command = ATMAPI.UpdateCommand(GsonHelper.getAsString(data,"command"));
		
		this.icons = new ArrayList<>();
		if(data.has("icons"))
		{
			JsonArray iconListData = GsonHelper.getAsJsonArray(data, "icons");
			for(int i = 0; i < iconListData.size(); ++i)
			{
				try {
					JsonObject iconData = iconListData.get(i).getAsJsonObject();
					this.icons.add(ATMAPI.parseIcon(iconData,lookup));
				} catch(JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error parsing ATM Icon #" + (i + 1) + ".", e);}
			}
		}
		else
		{
			LightmansCurrency.LogWarning("ATM Button Data has no 'icons' entry. Button will be blank.");
		}
	}
	
	public ATMExchangeButtonData(int xPos, int yPos, int width, @Nonnull String command, @Nonnull List<ATMIconData> icons) { this(xPos,yPos,width,0,command,icons);}
	public ATMExchangeButtonData(int xPos, int yPos, int width, int height, @Nonnull String command, @Nonnull List<ATMIconData> icons) {
		this.position = ScreenPosition.of(xPos, yPos);
		this.width = width;
		this.height = height == 0 ? 18 : height;
		this.command = ATMAPI.UpdateCommand(command);
		this.icons = icons;
	}

	@Nonnull
	public JsonObject save(@Nonnull HolderLookup.Provider lookup) {
		JsonObject data = new JsonObject();
		
		data.addProperty("x", this.position.x);
		data.addProperty("y", this.position.y);
		data.addProperty("width", this.width);
		data.addProperty("height",this.height);
		data.addProperty("command", this.command);
		
		JsonArray iconListData = new JsonArray();
		for (ATMIconData icon : this.icons)
			iconListData.add(icon.save(lookup));
		data.add("icons", iconListData);
		
		return data;
	}
	
	public static void generateMain(@Nonnull ATMData.Builder builder) {
		//Exchange All
		builder.addButton(exchangeAllUpDefault());
		builder.addButton(exchangeAllDownDefault());
		//Copper <-> Iron
		builder.addButton(exchangeSingle(6, 61, ModItems.COIN_IRON, ModItems.COIN_COPPER, "exchangeDown-lightmanscurrency:coin_iron"));
		builder.addButton(exchangeSingle(6, 88, ModItems.COIN_COPPER, ModItems.COIN_IRON, "exchangeUp-lightmanscurrency:coin_copper"));
		//Iron <-> Gold
		builder.addButton(exchangeSingle(41, 61, ModItems.COIN_GOLD, ModItems.COIN_IRON, "exchangeDown-lightmanscurrency:coin_gold"));
		builder.addButton(exchangeSingle(41, 88, ModItems.COIN_IRON, ModItems.COIN_GOLD, "exchangeUp-lightmanscurrency:coin_iron"));
		//Gold <-> Emerald
		builder.addButton(exchangeSingle(75, 61, ModItems.COIN_EMERALD, ModItems.COIN_GOLD, "exchangeDown-lightmanscurrency:coin_emerald"));
		builder.addButton(exchangeSingle(75, 88, ModItems.COIN_GOLD, ModItems.COIN_EMERALD, "exchangeUp-lightmanscurrency:coin_gold"));
		//Emerald <-> Diamond
		builder.addButton(exchangeSingle(109, 61, ModItems.COIN_DIAMOND, ModItems.COIN_EMERALD, "exchangeDown-lightmanscurrency:coin_diamond"));
		builder.addButton(exchangeSingle(109, 88, ModItems.COIN_EMERALD, ModItems.COIN_DIAMOND, "exchangeUp-lightmanscurrency:coin_emerald"));
		//Diamond <-> Netherite
		builder.addButton(exchangeSingle(144, 61, ModItems.COIN_NETHERITE, ModItems.COIN_DIAMOND, "exchangeDown-lightmanscurrency:coin_netherite"));
		builder.addButton(exchangeSingle(144, 88, ModItems.COIN_DIAMOND, ModItems.COIN_NETHERITE, "exchangeUp-lightmanscurrency:coin_diamond"));
	}

	public static void generateChocolate(@Nonnull ATMData.Builder builder) {
		//builder.addButton(exchangeAllUpChocolate());
		//builder.addButton(exchangeAllDownChocolate());
		//Copper <-> Iron
		builder.addButton(exchangeSingle(6, 61, ModItems.COIN_CHOCOLATE_IRON, ModItems.COIN_CHOCOLATE_COPPER, "exchangeDown-lightmanscurrency:coin_chocolate_iron"));
		builder.addButton(exchangeSingle(6, 88, ModItems.COIN_COPPER, ModItems.COIN_CHOCOLATE_IRON, "exchangeUp-lightmanscurrency:coin_chocolate_copper"));
		//Iron <-> Gold
		builder.addButton(exchangeSingle(41, 61, ModItems.COIN_CHOCOLATE_GOLD, ModItems.COIN_CHOCOLATE_IRON, "exchangeDown-lightmanscurrency:coin_chocolate_gold"));
		builder.addButton(exchangeSingle(41, 88, ModItems.COIN_CHOCOLATE_IRON, ModItems.COIN_CHOCOLATE_GOLD, "exchangeUp-lightmanscurrency:coin_chocolate_iron"));
		//Gold <-> Emerald
		builder.addButton(exchangeSingle(75, 61, ModItems.COIN_CHOCOLATE_EMERALD, ModItems.COIN_CHOCOLATE_GOLD, "exchangeDown-lightmanscurrency:coin_chocolate_emerald"));
		builder.addButton(exchangeSingle(75, 88, ModItems.COIN_CHOCOLATE_GOLD, ModItems.COIN_CHOCOLATE_EMERALD, "exchangeUp-lightmanscurrency:coin_chocolate_gold"));
		//Emerald <-> Diamond
		builder.addButton(exchangeSingle(109, 61, ModItems.COIN_CHOCOLATE_DIAMOND, ModItems.COIN_CHOCOLATE_EMERALD, "exchangeDown-lightmanscurrency:coin_chocolate_diamond"));
		builder.addButton(exchangeSingle(109, 88, ModItems.COIN_CHOCOLATE_EMERALD, ModItems.COIN_CHOCOLATE_DIAMOND, "exchangeUp-lightmanscurrency:coin_chocolate_emerald"));
		//Diamond <-> Netherite
		builder.addButton(exchangeSingle(144, 61, ModItems.COIN_CHOCOLATE_NETHERITE, ModItems.COIN_CHOCOLATE_DIAMOND, "exchangeDown-lightmanscurrency:coin_chocolate_netherite"));
		builder.addButton(exchangeSingle(144, 88, ModItems.COIN_CHOCOLATE_DIAMOND, ModItems.COIN_CHOCOLATE_NETHERITE, "exchangeUp-lightmanscurrency:coin_chocolate_diamond"));
	}

	private static ATMExchangeButtonData exchangeAllUpDefault() {
		return new ATMExchangeButtonData(5, 34, 82, "exchangeAllUp",
			Lists.newArrayList(
				new ItemIcon(-2,1,ModItems.COIN_COPPER.get()),
				new SimpleArrowIcon(10,6,ArrowType.RIGHT),
				new ItemIcon(12,1,ModItems.COIN_IRON.get()),
				new SimpleArrowIcon(24,6,ArrowType.RIGHT),
				new ItemIcon(26,1,ModItems.COIN_GOLD.get()),
				new SimpleArrowIcon(38,6,ArrowType.RIGHT),
				new ItemIcon(40,1,ModItems.COIN_EMERALD.get()),
				new SimpleArrowIcon(52,6,ArrowType.RIGHT),
				new ItemIcon(54,1,ModItems.COIN_DIAMOND.get()),
				new SimpleArrowIcon(66,6,ArrowType.RIGHT),
				new ItemIcon(68,1,ModItems.COIN_NETHERITE.get())
			)
		);
	}

	private static ATMExchangeButtonData exchangeAllUpChocolate() {
		return new ATMExchangeButtonData(5, 34, 82, "exchangeAllUp",
				Lists.newArrayList(
						new ItemIcon(-2,1,ModItems.COIN_CHOCOLATE_COPPER.get()),
						new SimpleArrowIcon(10,6,ArrowType.RIGHT),
						new ItemIcon(12,1,ModItems.COIN_CHOCOLATE_IRON.get()),
						new SimpleArrowIcon(24,6,ArrowType.RIGHT),
						new ItemIcon(26,1,ModItems.COIN_CHOCOLATE_GOLD.get()),
						new SimpleArrowIcon(38,6,ArrowType.RIGHT),
						new ItemIcon(40,1,ModItems.COIN_CHOCOLATE_EMERALD.get()),
						new SimpleArrowIcon(52,6,ArrowType.RIGHT),
						new ItemIcon(54,1,ModItems.COIN_CHOCOLATE_DIAMOND.get()),
						new SimpleArrowIcon(66,6,ArrowType.RIGHT),
						new ItemIcon(68,1,ModItems.COIN_CHOCOLATE_NETHERITE.get())
				)
		);
	}
	
	private static ATMExchangeButtonData exchangeAllDownDefault() {
		return new ATMExchangeButtonData(89, 34, 82, "exchangeAllDown",
			Lists.newArrayList(
				new ItemIcon(-2,1,ModItems.COIN_NETHERITE.get()),
				new SimpleArrowIcon(10,6,ArrowType.RIGHT),
				new ItemIcon(12,1,ModItems.COIN_DIAMOND.get()),
				new SimpleArrowIcon(24,6,ArrowType.RIGHT),
				new ItemIcon(26,1,ModItems.COIN_EMERALD.get()),
				new SimpleArrowIcon(38,6,ArrowType.RIGHT),
				new ItemIcon(40,1,ModItems.COIN_GOLD.get()),
				new SimpleArrowIcon(52,6,ArrowType.RIGHT),
				new ItemIcon(54,1,ModItems.COIN_IRON.get()),
				new SimpleArrowIcon(66,6,ArrowType.RIGHT),
				new ItemIcon(68,1,ModItems.COIN_COPPER.get())
			)
		);
	}

	private static ATMExchangeButtonData exchangeAllDownChocolate() {
		return new ATMExchangeButtonData(89, 34, 82, "exchangeAllDown",
				Lists.newArrayList(
						new ItemIcon(-2,1,ModItems.COIN_CHOCOLATE_NETHERITE.get()),
						new SimpleArrowIcon(10,6,ArrowType.RIGHT),
						new ItemIcon(12,1,ModItems.COIN_CHOCOLATE_DIAMOND.get()),
						new SimpleArrowIcon(24,6,ArrowType.RIGHT),
						new ItemIcon(26,1,ModItems.COIN_CHOCOLATE_EMERALD.get()),
						new SimpleArrowIcon(38,6,ArrowType.RIGHT),
						new ItemIcon(40,1,ModItems.COIN_CHOCOLATE_GOLD.get()),
						new SimpleArrowIcon(52,6,ArrowType.RIGHT),
						new ItemIcon(54,1,ModItems.COIN_CHOCOLATE_IRON.get()),
						new SimpleArrowIcon(66,6,ArrowType.RIGHT),
						new ItemIcon(68,1,ModItems.COIN_CHOCOLATE_COPPER.get())
				)
		);
	}
	
	private static ATMExchangeButtonData exchangeSingle(int x, int y, Supplier<? extends ItemLike> from, Supplier<? extends ItemLike> to, String command) {
		return new ATMExchangeButtonData(x, y, 26, command,
			Lists.newArrayList(
				new ItemIcon(-2,1,from.get()),
				new SimpleArrowIcon(10,6,ArrowType.RIGHT),
				new ItemIcon(12,1,to.get())
			)
		);
	}
	
	
	
}
