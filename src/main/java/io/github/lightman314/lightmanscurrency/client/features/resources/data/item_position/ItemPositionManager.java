package io.github.lightman314.lightmanscurrency.client.features.resources.data.item_position;

import net.minecraft.resources.FileToIdConverter;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;

import java.util.HashMap;
import java.util.Map;

public class ItemPositionManager extends SimpleJsonResourceReloadListener<ItemPositionData> {

    public static final ItemPositionManager INSTANCE = new ItemPositionManager();

    protected ItemPositionManager() {
        super(ItemPositionData.CODEC,FileToIdConverter.json("lightmanscurrency/item_position_data"));
    }

    private final Map<Identifier,ItemPositionData> itemPositions = new HashMap<>();
    public static ItemPositionData getDataOrEmpty(Identifier id) { return INSTANCE.itemPositions.getOrDefault(id,ItemPositionData.EMPTY); }

    @Override
    protected void apply(Map<Identifier, ItemPositionData> preparations, ResourceManager manager, ProfilerFiller profiler) {
        this.itemPositions.clear();
        this.itemPositions.putAll(preparations);
    }

}