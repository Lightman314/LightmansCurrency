package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.custom_models;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.common.blockentity.trader.ItemTraderBlockEntity;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class CustomModelTest {

    private static final Map<ResourceLocation,Function<JsonObject,CustomModelTest>> TYPES = new HashMap<>();

    public static void register(ResourceLocation type, Function<JsonObject,CustomModelTest> parser)
    {
        if(!TYPES.containsKey(type))
            TYPES.put(type,parser);
    }

    private final ResourceLocation type;
    public CustomModelTest(ResourceLocation type) { this.type = type;}

    public abstract boolean test(ItemTraderBlockEntity blockEntity, ItemStack item);

    public final JsonObject write() {
        JsonObject json = new JsonObject();
        this.writeAdditional(json);
        json.addProperty("type",this.type.toString());
        return json;
    }

    protected abstract void writeAdditional(JsonObject json);

    @Nonnull
    public static CustomModelTest parse(JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        String typeString = GsonHelper.getAsString(json,"type");
        ResourceLocation type = VersionUtil.parseResource(typeString);
        if(TYPES.containsKey(type))
            return TYPES.get(type).apply(json);
        else
            throw new JsonSyntaxException(typeString + " is not a valid Custom Model Test Type!");
    }

}
