package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ATMItemIcon extends ATMIconData {

	public static final ATMIconType TYPE = ATMIconType.create(ATMItemIcon::new);
	
	private final boolean simpleItem;
    public final ItemStack item;
	
	public ATMItemIcon(JsonObject data, DataContext<JsonElement> context) throws JsonSyntaxException, ResourceLocationException {
		super(data);

        if(data.has("item") && data.get("item").isJsonPrimitive())
        {
            this.item = new ItemStack(BuiltInRegistries.ITEM.get(VersionUtil.parseResource(GsonHelper.getAsString(data, "item"))));
            this.simpleItem = true;
        }
		else
		{
			this.item = ItemStack.SINGLE_ITEM_CODEC.decode(context.ops(),GsonHelper.getAsJsonObject(data,"item")).getOrThrow(JsonSyntaxException::new).getFirst();
			this.simpleItem = false;
		}
	}
	
	public ATMItemIcon(int xPos, int yPos, ItemLike item) {
		super(xPos, yPos);
		this.item = new ItemStack(item);
		this.simpleItem = true;
	}

	public ATMItemIcon(int xPos, int yPos, ItemStack item)
	{
		super(xPos, yPos);
		this.item = item;
		this.simpleItem = false;
	}

	@Override
	protected void saveAdditional(JsonObject data,DataContext<JsonElement> context) {
		
		if(this.simpleItem)
			data.addProperty("item",BuiltInRegistries.ITEM.getKey(this.item.getItem()).toString());
		else
			data.add("item",ItemStack.SINGLE_ITEM_CODEC.encodeStart(context.ops(),this.item).getOrThrow());
	}

	@Override
	public ATMIconType getType() { return TYPE; }
	
}
