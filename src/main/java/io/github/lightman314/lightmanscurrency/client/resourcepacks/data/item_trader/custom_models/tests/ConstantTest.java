package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelTest;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ConstantTest extends CustomModelTest {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("constant");

    private final boolean result;
    public ConstantTest(boolean result) { super(TYPE); this.result = result; }

    @Override
    public boolean test(ItemTraderBlockEntity blockEntity, ItemStack item) { return this.result; }

    @Override
    protected void writeAdditional(JsonObject json) { json.addProperty("value",this.result); }

    public static ConstantTest parse(JsonObject json) throws JsonSyntaxException { return new ConstantTest(GsonHelper.getAsBoolean(json,"value")); }

}
