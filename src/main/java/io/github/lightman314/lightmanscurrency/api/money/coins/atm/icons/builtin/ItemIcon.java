package io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.builtin;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.IconType;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.atm.ATMExchangeButton;
import io.github.lightman314.lightmanscurrency.api.money.coins.atm.icons.ATMIconData;
import io.github.lightman314.lightmanscurrency.util.FileUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public class ItemIcon extends ATMIconData {
	
	public static final ResourceLocation TYPE_NAME = VersionUtil.lcResource( "item");
	public static final IconType TYPE = IconType.create(TYPE_NAME, ItemIcon::new);
	
	private final boolean simpleItem;
	private final ItemStack item;
	
	public ItemIcon(@Nonnull JsonObject data, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
		super(data);
		
		JsonElement itemData = data.get("item");
		String itemID = GsonHelper.getAsString(data, "item", "NO_RESULT");
		if(!itemID.equals("NO_RESULT"))
		{
			this.item = new ItemStack(BuiltInRegistries.ITEM.get(VersionUtil.parseResource(itemID)));
			this.simpleItem = true;
		}
		else
		{
			this.item = FileUtil.parseItemStack(GsonHelper.convertToJsonObject(itemData, "item"),lookup);
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
	protected void saveAdditional(@Nonnull JsonObject data, @Nonnull HolderLookup.Provider lookup) {
		
		if(this.simpleItem)
			data.addProperty("item", BuiltInRegistries.ITEM.getKey(this.item.getItem()).toString());
		else
			data.add("item", FileUtil.convertItemStack(this.item,lookup));
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
