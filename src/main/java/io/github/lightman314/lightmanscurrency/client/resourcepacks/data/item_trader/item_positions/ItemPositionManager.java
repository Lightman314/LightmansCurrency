package io.github.lightman314.lightmanscurrency.client.resourcepacks.data.item_trader.item_positions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class ItemPositionManager extends SimpleJsonResourceReloadListener {

    private static final Gson GSON = (new GsonBuilder()).setPrettyPrinting().disableHtmlEscaping().create();
    public static final ItemPositionManager INSTANCE = new ItemPositionManager();

    private final Map<ResourceLocation,ItemPositionData> itemPositions = new HashMap<>();
    public static ItemPositionData getDataOrEmpty(ResourceLocation id) { return INSTANCE.itemPositions.getOrDefault(id,ItemPositionData.EMPTY); }

    private ItemPositionManager() { super(GSON, "lightmanscurrency/item_position_data"); }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> map, ResourceManager resourceManager, ProfilerFiller filler) {
        this.itemPositions.clear();
        RotationHandler.debugRegisteredHandlers();
        map.forEach((id,json) -> {
            try {
                ItemPositionData data = ItemPositionData.parse(GsonHelper.convertToJsonObject(json,"top element"));
                this.itemPositions.put(id,data);
            } catch (JsonSyntaxException | IllegalArgumentException exception) {
                LightmansCurrency.LogError("Parsing error loading item position data " + id, exception); }
        });
        LightmansCurrency.LogDebug("Loaded " + this.itemPositions.size() + " Item Position entries!");
    }
}
