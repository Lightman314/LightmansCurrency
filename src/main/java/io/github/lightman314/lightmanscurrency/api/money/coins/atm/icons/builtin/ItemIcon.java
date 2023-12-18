package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

public class ItemIcon extends ATMIconData {
	
	public static final ResourceLocation TYPE_NAME = new ResourceLocation(LightmansCurrency.MODID, "item");
	public static final IconType TYPE = IconType.create(TYPE_NAME, ItemIcon::new);
	
	private final boolean simpleItem;
	private final ItemStack item;
	
	public ItemIcon(JsonObject data) throws JsonSyntaxException, ResourceLocationException {
		super(data);
		
		JsonElement itemData = data.get("item");
		String itemID = GsonHelper.getAsString(data, "item", "NO_RESULT");
		if(!itemID.equals("NO_RESULT"))
		{
			this.item = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemID)));
			this.simpleItem = true;
		}
		else
		{
			this.item = FileUtil.parseItemStack(GsonHelper.convertToJsonObject(itemData, "item"));
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
	protected ResourceLocation getType() { return TYPE_NAME; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(@Nonnull ATMExchangeButton button, @Nonnull EasyGuiGraphics gui, boolean isHovered) {
		gui.renderItem(this.item, this.xPos, this.yPos, "");
	}
	
}
