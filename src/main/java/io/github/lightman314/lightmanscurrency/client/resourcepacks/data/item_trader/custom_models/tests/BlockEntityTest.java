package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.tests;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models.CustomModelTest;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class BlockEntityTest extends CustomModelTest {

    public static final ResourceLocation TYPE = VersionUtil.lcResource("block_entity_type");

    private final ResourceLocation type;
    public BlockEntityTest(ResourceLocation type) { super(TYPE); this.type = type; }

    @Override
    public boolean test(@Nullable BlockEntity blockEntity, ItemStack item) {
        return blockEntity != null && this.type.equals(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(blockEntity.getType()));
    }

    @Override
    protected void writeAdditional(JsonObject json) { json.addProperty("type",this.type.toString()); }

    public static BlockEntityTest parse(JsonObject json) throws JsonSyntaxException, ResourceLocationException { return new BlockEntityTest(VersionUtil.parseResource(GsonHelper.getAsString(json,"type"))); }

}
