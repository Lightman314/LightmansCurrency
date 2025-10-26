package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions.rotation.*;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Quaternionf;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class RotationHandler
{

    private static boolean setup = false;

    private static final String SPINNING = "SPINNING";
    private static final String FACING = "FACING";
    private static final String FACING_UP = "FACING_UP";

    private final RotationHandlerType type;
    protected RotationHandler(RotationHandlerType type) { this.type = type; }

    private static final Map<ResourceLocation,RotationHandlerType> ROTATION_HANDLERS = new HashMap<>();
    public static ResourceLocation getRotationType(RotationHandler handler) { return getRotationType(handler.type); }
    public static ResourceLocation getRotationType(RotationHandlerType type) {
        AtomicReference<ResourceLocation> result = new AtomicReference<>(null);
        ROTATION_HANDLERS.forEach((id,t) -> {
                if(t == type)
                    result.set(id);
        });
        return result.get();
    }

    public static void setup()
    {
        if(setup)
            return;
        registerRotationType(VersionUtil.lcResource("spinning"),SpinningRotation.TYPE);
        registerRotationType(VersionUtil.lcResource("facing"), FacingRotation.TYPE);
        registerRotationType(VersionUtil.lcResource("facing_up"), FacingUpRotation.TYPE);
        setup = true;
    }

    public static void registerRotationType(ResourceLocation id,RotationHandlerType type)
    {
        RotationHandlerType old = ROTATION_HANDLERS.put(id,type);
        if(old != null)
            LightmansCurrency.LogError("Duplicate Rotation Type " + id + " was registered!");
    }

    public static RotationHandler parseRotationHandler(JsonElement element,String memberName) throws JsonSyntaxException, ResourceLocationException
    {
        if(element.isJsonPrimitive())
        {
            //Parse deprecated inputs
            String deprecatedKey = GsonHelper.convertToString(element,memberName);
            return switch (deprecatedKey)
            {
                case SPINNING -> SpinningRotation.createDefault();
                case FACING -> FacingRotation.getInstance();
                case FACING_UP -> FacingUpRotation.createDefault();
                default -> throw new JsonSyntaxException("Unable to parse deprecated rotation type of " + deprecatedKey);
            };
        }
        else
        {
            JsonObject json = GsonHelper.convertToJsonObject(element,memberName);
            ResourceLocation type = VersionUtil.parseResource(GsonHelper.getAsString(json,"type"));
            RotationHandlerType parser = ROTATION_HANDLERS.get(type);
            if(parser == null)
                throw new JsonSyntaxException("Unknown rotation handler type " + type);
            return parser.parse(json);
        }
    }

    protected abstract List<Quaternionf> rotate(BlockState state, float partialTicks);

    public final JsonObject write()
    {
        JsonObject json = new JsonObject();
        this.writeAdditional(json);
        json.addProperty("type",getRotationType(this).toString());
        return json;
    }
    protected abstract void writeAdditional(JsonObject json);

    public static void debugRegisteredHandlers()
    {
        StringBuilder values = new StringBuilder();
        ROTATION_HANDLERS.forEach((key,handler) -> {
                if(!values.isEmpty())
                    values.append(", ");
                values.append(key);
            });
        LightmansCurrency.LogDebug("Registered Rotation Handlers: " + values);
    }


}
