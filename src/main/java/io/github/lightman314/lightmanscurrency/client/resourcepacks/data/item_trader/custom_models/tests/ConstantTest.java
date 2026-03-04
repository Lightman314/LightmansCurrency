package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelTest;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class ConstantTest extends CustomModelTest {

    public static final ResourceLocation TYPE = LightmansCurrency.id("constant");

    private final boolean result;
    public ConstantTest(boolean result) { super(TYPE); this.result = result; }

    @Override
    public boolean test(@Nullable BlockEntity blockEntity, ItemStack item) { return this.result; }

    @Override
    protected void writeAdditional(JsonObject json) { json.addProperty("value",this.result); }

    public static ConstantTest parse(JsonObject json) throws JsonSyntaxException { return new ConstantTest(GsonHelper.getAsBoolean(json,"value")); }

}
