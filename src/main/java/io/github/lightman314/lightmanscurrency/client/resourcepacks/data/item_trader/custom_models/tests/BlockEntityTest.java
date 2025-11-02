package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelTest;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockEntityTest extends CustomModelTest {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("block_entity_type");

    private final ResourceLocation type;
    public BlockEntityTest(ResourceLocation type) { super(TYPE); this.type = type; }

    @Override
    public boolean test(ItemTraderBlockEntity blockEntity, ItemStack item) {
        return this.type.equals(ForgeRegistries.BLOCK_ENTITY_TYPES.getKey(blockEntity.getType()));
    }

    @Override
    protected void writeAdditional(JsonObject json) { json.addProperty("type",this.type.toString()); }

    public static BlockEntityTest parse(JsonObject json) throws JsonSyntaxException, ResourceLocationException { return new BlockEntityTest(VersionUtil.parseResource(GsonHelper.getAsString(json,"type"))); }

}