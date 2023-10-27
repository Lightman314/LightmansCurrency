package io.github.lightman314.lightmanscurrency.common.atm;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.common.atm.icons.ItemIcon;
import io.github.lightman314.lightmanscurrency.common.atm.icons.SimpleArrowIcon;
import io.github.lightman314.lightmanscurrency.common.atm.icons.SimpleArrowIcon.ArrowType;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.money.ATMUtil;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.RegistryObject;

public class ATMExchangeButtonData {

	public final ScreenPosition position;
	public final int width;
	public final String command;
	private final List<ATMIconData> icons;
	public ImmutableList<ATMIconData> getIcons() { return ImmutableList.copyOf(this.icons); }
	
	public static ATMExchangeButtonData parse(JsonObject data) throws Exception { return new ATMExchangeButtonData(data); }
	
	private ATMExchangeButtonData(JsonObject data) throws Exception {
		this.position = ScreenPosition.of(data.get("x").getAsInt(), data.get("y").getAsInt());
		this.width = data.get("width").getAsInt();
		this.command = ATMUtil.UpdateCommand(data.get("command").getAsString());
		
		this.icons = new ArrayList<>();
		if(data.has("icons"))
		{
			JsonArray iconListData = data.getAsJsonArray("icons");
			for(int i = 0; i < iconListData.size(); ++i)
			{
				try {
					JsonObject iconData = iconListData.get(i).getAsJsonObject();
					this.icons.add(ATMIconData.parse(iconData));
				} catch(Exception e) { LightmansCurrency.LogError("Error parsing ATM Icon #" + (i + 1) + ".", e);}
			}
		}
		else
		{
			LightmansCurrency.LogWarning("ATM Button Data has no 'icons' entry. Button will be blank.");
		}
	}
	
	public ATMExchangeButtonData(int xPos, int yPos, int width, String command, List<ATMIconData> icons) {
		this.position = ScreenPosition.of(xPos, yPos);
		this.width = width;
		this.command = ATMUtil.UpdateCommand(command);
		this.icons = icons;
	}
	
	public JsonObject save() {
		JsonObject data = new JsonObject();
		
		data.addProperty("x", this.position.x);
		data.addProperty("y", this.position.y);
		data.addProperty("width", this.width);
		data.addProperty("command", this.command);
		
		JsonArray iconListData = new JsonArray();
		for (ATMIconData icon : this.icons)
			iconListData.add(icon.save());
		data.add("icons", iconListData);
		
		return data;
	}
	
	public static List<ATMExchangeButtonData> generateDefault() {
		return Lists.newArrayList(
				//Convert All
				exchangeAllUpDefault(5,34),
				exchangeAllDownDefault(89,34),
				//Copper <-> Iron
				exchangeSingle(6, 61, ModItems.COIN_IRON, ModItems.COIN_COPPER, "exchangeDown-lightmanscurrency:coin_iron"),
				exchangeSingle(6, 88, ModItems.COIN_COPPER, ModItems.COIN_IRON, "exchangeUp-lightmanscurrency:coin_copper"),
				//Iron <-> Gold
				exchangeSingle(41, 61, ModItems.COIN_GOLD, ModItems.COIN_IRON, "exchangeDown-lightmanscurrency:coin_gold"),
				exchangeSingle(41, 88, ModItems.COIN_IRON, ModItems.COIN_GOLD, "exchangeUp-lightmanscurrency:coin_iron"),
				//Gold <-> Emerald
				exchangeSingle(75, 61, ModItems.COIN_EMERALD, ModItems.COIN_GOLD, "exchangeDown-lightmanscurrency:coin_emerald"),
				exchangeSingle(75, 88, ModItems.COIN_GOLD, ModItems.COIN_EMERALD, "exchangeUp-lightmanscurrency:coin_gold"),
				//Emerald <-> Diamond
				exchangeSingle(109, 61, ModItems.COIN_DIAMOND, ModItems.COIN_EMERALD, "exchangeDown-lightmanscurrency:coin_diamond"),
				exchangeSingle(109, 88, ModItems.COIN_EMERALD, ModItems.COIN_DIAMOND, "exchangeUp-lightmanscurrency:coin_emerald"),
				//Diamond <-> Netherite
				exchangeSingle(144, 61, ModItems.COIN_NETHERITE, ModItems.COIN_DIAMOND, "exchangeDown-lightmanscurrency:coin_netherite"),
				exchangeSingle(144, 88, ModItems.COIN_DIAMOND, ModItems.COIN_NETHERITE, "exchangeUp-lightmanscurrency:coin_diamond")
			);
	}
	
	private static ATMExchangeButtonData exchangeAllUpDefault(int x, int y) {
		return new ATMExchangeButtonData(x, y, 82, "exchangeAllUp",
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
	
	private static ATMExchangeButtonData exchangeAllDownDefault(int x, int y) {
		return new ATMExchangeButtonData(x, y, 82, "exchangeAllDown",
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
	
	private static ATMExchangeButtonData exchangeSingle(int x, int y, RegistryObject<? extends ItemLike> from, RegistryObject<? extends ItemLike> to, String command) {
		return new ATMExchangeButtonData(x, y, 26, command,
			Lists.newArrayList(
				new ItemIcon(-2,1,from.get()),
				new SimpleArrowIcon(10,6,ArrowType.RIGHT),
				new ItemIcon(12,1,to.get())
			)
		);
	}
	
	
	
}
