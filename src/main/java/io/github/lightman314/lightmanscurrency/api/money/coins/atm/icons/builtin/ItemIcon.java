package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class ItemIcon extends ATMIconData {
	
	public static final ResourceLocation TYPE_NAME = VersionUtil.lcResource("item");
	public static final IconType TYPE = IconType.create(TYPE_NAME, ItemIcon::new);
	
	private final boolean simpleItem;
	public final ItemStack item;
	
	public ItemIcon(JsonObject data) throws JsonSyntaxException, ResourceLocationException {
		super(data);

		if(!data.has("item") && data.get("item").isJsonPrimitive())
		{
			this.item = new ItemStack(ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(GsonHelper.getAsString(data,"item"))));
			this.simpleItem = true;
		}
		else
		{
			this.item = FileUtil.parseItemStack(GsonHelper.getAsJsonObject(data,"item"));
			this.item.setCount(1);
			this.simpleItem = false;
		}
	}
	
	public ItemIcon(int xPos, int yPos, @Nonnull ItemLike item) {
		super(xPos, yPos);
		this.item = new ItemStack(item);
		this.simpleItem = true;
	}

	public ItemIcon(int xPos, int yPos, @Nonnull ItemStack item)
	{
		super(xPos, yPos);
		this.item = item;
		this.simpleItem = false;
	}
	

	@Override
	protected void saveAdditional(@Nonnull JsonObject data) {
		
		if(this.simpleItem)
			data.addProperty("item", ForgeRegistries.ITEMS.getKey(this.item.getItem()).toString());
		else
			data.add("item", FileUtil.convertItemStack(this.item));
	}
	
	@Nonnull
	@Override
	public ResourceLocation getType() { return TYPE_NAME; }
	
}
