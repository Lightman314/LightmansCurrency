package io.github.lightman314.lightmanscurrency.common.atm.icons;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMConversionButton;
import io.github.lightman314.lightmanscurrency.client.util.ItemRenderUtil;
import io.github.lightman314.lightmanscurrency.common.atm.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemIcon extends ATMIconData {
	
	public static final ResourceLocation TYPE_NAME = new ResourceLocation(LightmansCurrency.MODID, "item");
	public static final IconType TYPE = IconType.create(TYPE_NAME, ItemIcon::new);
	
	private final boolean simpleItem;
	private final ItemStack item;
	
	public ItemIcon(JsonObject data) throws RuntimeException {
		super(data);
		
		JsonElement itemData = data.get("item");
		if(itemData.isJsonPrimitive())
		{
			this.item = new ItemStack(ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemData.getAsString())));
			simpleItem = true;
		}
		else
		{
			try {
				this.item = FileUtil.parseItemStack(itemData.getAsJsonObject());
			} catch(Exception e) { throw new RuntimeException(e); }
			
			//Always force quantity 1
			this.item.setCount(1);
			simpleItem = false;
		}
	}
	
	public ItemIcon(int xPos, int yPos, ItemLike item) {
		super(xPos, yPos);
		this.item = new ItemStack(item);
		this.simpleItem = true;
	}
	

	@Override
	protected void saveAdditional(JsonObject data) {
		
		if(this.simpleItem)
			data.addProperty("item", ForgeRegistries.ITEMS.getKey(this.item.getItem()).toString());
		else
			data.add("item", FileUtil.convertItemStack(this.item));
	}
	
	@Override
	protected ResourceLocation getType() { return TYPE_NAME; }

	@Override
	@OnlyIn(Dist.CLIENT)
	public void render(ATMConversionButton button, PoseStack pose, boolean isHovered) {
		ItemRenderUtil.drawItemStack(button, null, this.item, button.getX() + this.xPos, button.getY() + this.yPos, "");
	}
	
}
