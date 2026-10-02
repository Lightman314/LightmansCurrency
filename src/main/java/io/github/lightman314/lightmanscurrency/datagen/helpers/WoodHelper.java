package io.github.lightman314.lightmanscurrency.datagen.helpers;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.types.WoodType;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class WoodHelper {

    private static final Map<WoodType,WoodData> CACHE = new HashMap<>();

    private static final WoodData DEFAULT;

    static {
        //Register vanilla wood data
        DEFAULT = WoodData.builder(WoodType.OAK)
                        .withLog(() -> Items.OAK_LOG,Identifier.withDefaultNamespace("block/oak_log"))
                        .withPlanksAndSlab(() -> Items.OAK_PLANKS,() -> Items.OAK_SLAB,Identifier.withDefaultNamespace("block/oak_planks"))
                        .build();
        WoodData.builder(WoodType.SPRUCE)
                .withLog(() -> Items.SPRUCE_LOG,Identifier.withDefaultNamespace("block/spruce_log"))
                .withPlanksAndSlab(() -> Items.SPRUCE_PLANKS,() -> Items.SPRUCE_SLAB,Identifier.withDefaultNamespace("block/spruce_planks"))
                .build();
        WoodData.builder(WoodType.BIRCH)
                .withLog(() -> Items.BIRCH_LOG,Identifier.withDefaultNamespace("block/birch_log"))
                .withPlanksAndSlab(() -> Items.BIRCH_PLANKS,() -> Items.BIRCH_SLAB,Identifier.withDefaultNamespace("block/birch_planks"))
                .build();
        WoodData.builder(WoodType.JUNGLE)
                .withLog(() -> Items.JUNGLE_LOG,Identifier.withDefaultNamespace("block/jungle_log"))
                .withPlanksAndSlab(() -> Items.JUNGLE_PLANKS,() -> Items.JUNGLE_SLAB,Identifier.withDefaultNamespace("block/jungle_planks"))
                .build();
        WoodData.builder(WoodType.ACACIA)
                .withLog(() -> Items.ACACIA_LOG,Identifier.withDefaultNamespace("block/acacia_log"))
                .withPlanksAndSlab(() -> Items.ACACIA_PLANKS,() -> Items.ACACIA_SLAB,Identifier.withDefaultNamespace("block/acacia_planks"))
                .build();
        WoodData.builder(WoodType.CHERRY)
                .withLog(() -> Items.CHERRY_LOG,Identifier.withDefaultNamespace("block/cherry_log"))
                .withPlanksAndSlab(() -> Items.CHERRY_PLANKS,() -> Items.CHERRY_SLAB,Identifier.withDefaultNamespace("block/cherry_planks"))
                .build();
        WoodData.builder(WoodType.PALE_OAK)
                .withLog(() -> Items.PALE_OAK_LOG,Identifier.withDefaultNamespace("block/pale_oak_log"))
                .withPlanksAndSlab(() -> Items.PALE_OAK_PLANKS,() -> Items.PALE_OAK_SLAB,Identifier.withDefaultNamespace("block/pale_oak_planks"))
                .build();
        WoodData.builder(WoodType.DARK_OAK)
                .withLog(() -> Items.DARK_OAK_LOG,Identifier.withDefaultNamespace("block/dark_oak_log"))
                .withPlanksAndSlab(() -> Items.DARK_OAK_PLANKS,() -> Items.DARK_OAK_SLAB,Identifier.withDefaultNamespace("block/dark_oak_planks"))
                .build();
        WoodData.builder(WoodType.MANGROVE)
                .withLog(() -> Items.MANGROVE_LOG,Identifier.withDefaultNamespace("block/mangrove_log"))
                .withPlanksAndSlab(() -> Items.MANGROVE_PLANKS,() -> Items.MANGROVE_SLAB,Identifier.withDefaultNamespace("block/mangrove_planks"))
                .build();
        WoodData.builder(WoodType.CRIMSON)
                .withLog(() -> Items.CRIMSON_STEM,Identifier.withDefaultNamespace("block/crimson_stem"))
                .withPlanksAndSlab(() -> Items.CRIMSON_PLANKS,() -> Items.CRIMSON_SLAB,Identifier.withDefaultNamespace("block/crimson_planks"))
                .build();
        WoodData.builder(WoodType.WARPED)
                .withLog(() -> Items.WARPED_STEM,Identifier.withDefaultNamespace("block/warped_stem"))
                .withPlanksAndSlab(() -> Items.WARPED_PLANKS,() -> Items.WARPED_SLAB,Identifier.withDefaultNamespace("block/warped_planks"))
                .build();
        WoodData.builder(WoodType.BAMBOO)
                .withLog(() -> Items.BAMBOO_BLOCK,Identifier.withDefaultNamespace("block/bamboo_block"))
                .withPlanksAndSlab(() -> Items.BAMBOO_PLANKS,() -> Items.BAMBOO_SLAB,Identifier.withDefaultNamespace("block/bamboo_planks"))
                .build();
    }

    public static WoodData get(WoodType type) { return CACHE.getOrDefault(type,DEFAULT); }

    public static Optional<Item> getLog(WoodType type) { return Optional.ofNullable(get(type).getLog()); }
    public static Optional<Item> getPlank(WoodType type) { return Optional.ofNullable(get(type).getPlank()); }
    public static Optional<Item> getSlab(WoodType type) { return Optional.ofNullable(get(type).getSlab()); }

    /**
     * Combines the optionals of {@link #getLog(WoodType)} and {@link #getPlank(WoodType)}, allowing you to check if both are present before performing an action.<br>
     * {@link Pair#getFirst()} contains the log item, and {@link Pair#getSecond()} contains the plank item.
     */
    public static Optional<Pair<Item,Item>> getLogAndPlank(WoodType type) { return combine(getLog(type),getPlank(type)); }
    /**
     * Combines the optionals of {@link #getLog(WoodType)} and {@link #getSlab(WoodType)}, allowing you to check if both are present before performing an action.<br>
     * {@link Pair#getFirst()} contains the log item, and {@link Pair#getSecond()} contains the slab item.
     */
    public static Optional<Pair<Item,Item>> getLogAndSlab(WoodType type) { return combine(getLog(type),getSlab(type)); }
    /**
     * Combines the optionals of {@link #getPlank(WoodType)} and {@link #getSlab(WoodType)}, allowing you to check if both are present before performing an action.<br>
     * {@link Pair#getFirst()} contains the plank item, and {@link Pair#getSecond()} contains the slab item.
     */
    public static Optional<Pair<Item,Item>> getPlankAndSlab(WoodType type) { return combine(getPlank(type),getSlab(type)); }

    /**
     * Combines the optionals of {@link #getLog(WoodType)}, {@link #getPlank(WoodType)} and {@link #getSlab(WoodType)}, allowing you to check if all are present before performing an action.
     */
    public static Optional<WoodItems> getAllItems(WoodType type) {
        WoodData data = get(type);
        Item log = data.getLog();
        Item plank = data.getPlank();
        Item slab = data.getSlab();
        if(log == null || plank == null || slab == null)
            return Optional.empty();
        return Optional.of(new WoodItems(log,plank,slab));
    }

    public static Optional<Pair<Item,Item>> combine(Optional<Item> itemA,Optional<Item> itemB) {
        if(itemA.isPresent() && itemB.isPresent())
            return Optional.of(Pair.of(itemA.get(),itemB.get()));
        return Optional.empty();
    }

    public static Identifier getLogSideTexture(WoodType type) {
        Identifier texture = get(type).getLogSideTexture();
        return texture == null ? DEFAULT.getLogSideTexture() : texture;
    }
    public static Identifier getLogTopTexture(WoodType type) {
        Identifier texture = get(type).getLogTopTexture();
        return texture == null ? DEFAULT.getLogTopTexture() : texture;
    }
    public static Identifier getPlankTexture(WoodType type) {
        Identifier texture = get(type).getPlankTexture();
        return texture == null ? DEFAULT.getPlankTexture() : texture;
    }

    public static void register(WoodType type,WoodData data) {
        CACHE.put(type,data);
    }

    public record WoodItems(Item log,Item plank,Item slab) { }

}