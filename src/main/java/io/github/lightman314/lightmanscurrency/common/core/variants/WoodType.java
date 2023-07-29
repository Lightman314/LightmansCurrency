package io.github.lightman314.lightmanscurrency.common.core.variants;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import net.minecraft.world.level.material.MapColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class WoodType implements IOptionalKey {

    private static final List<WoodType> ALL_TYPES = new ArrayList<>();
    private static ImmutableList<WoodType> VALID_TYPES = null;
    private static ImmutableList<WoodType> VANILLA_TYPES = null;

    public static final WoodType OAK = new WoodType("oak", MapColor.WOOD);
    public static final WoodType SPRUCE = new WoodType("spruce", MapColor.PODZOL);
    public static final WoodType BIRCH = new WoodType("birch", MapColor.SAND);
    public static final WoodType JUNGLE = new WoodType("jungle", MapColor.DIRT);
    public static final WoodType ACACIA = new WoodType("acacia", MapColor.COLOR_ORANGE);

    public static final WoodType DARK_OAK = new WoodType("dark_oak", MapColor.COLOR_BROWN);
    public static final WoodType MANGROVE = new WoodType("mangrove", MapColor.COLOR_RED);
    public static final WoodType CHERRY = new WoodType("cherry", MapColor.TERRACOTTA_WHITE);
    public static final WoodType BAMBOO = new WoodType("bamboo", MapColor.COLOR_YELLOW);
    public static final WoodType CRIMSON = new WoodType("crimson", MapColor.CRIMSON_STEM);
    public static final WoodType WARPED = new WoodType("warped", MapColor.WARPED_STEM);

    public final String name;
    public final MapColor mapColor;
    @Nullable
    public WoodData getData() { return WoodDataHelper.get(this); }

    protected WoodType(@Nonnull String name, @Nonnull MapColor mapColor) { this.name = name; this.mapColor = mapColor; ALL_TYPES.add(this); }
    protected WoodType(@Nonnull String name, @Nonnull MapColor mapColor, @Nonnull WoodData data) { this(name, mapColor); WoodDataHelper.register(this, data); }

    public final String generateID(String prefix) {
        if(!prefix.endsWith("_"))
            prefix += "_";
        if(this.isModded())
            prefix += this.getModID() + "_";
        return prefix + this.name;
    }

    public final String generateResourceLocation(String prefix) { return this.generateResourceLocation(prefix,""); }
    public final String generateResourceLocation(String prefix, String postFix) {
        if(this.isModded())
            prefix += this.getModID() + "/";
        return prefix + this.name + postFix;
    }

    public final boolean isVanilla() { return this.getModID().equals("minecraft"); }
    @Nonnull
    public String getModID() { return "minecraft"; }
    public final boolean isMod(String modid) { return this.getModID().equalsIgnoreCase(modid); }
    public boolean isValid() { return true; }
    @Override
    public final boolean isModded() { return !this.isVanilla(); }
    @Override
    public String toString() { return this.name; }

    public static ImmutableList<WoodType> vanillaValues() {
        if(VANILLA_TYPES == null)
            VANILLA_TYPES = ImmutableList.copyOf(ALL_TYPES.stream().filter(WoodType::isVanilla).toList());
        return VANILLA_TYPES;
    }

    public static List<WoodType> validValues() {
        if(VALID_TYPES == null)
            VALID_TYPES = ImmutableList.copyOf(ALL_TYPES.stream().filter(WoodType::isValid).toList());
        return VALID_TYPES;
    }

    public static List<WoodType> moddedValues(String modid) { return ImmutableList.copyOf(ALL_TYPES.stream().filter(t -> t.getModID().equals(modid)).toList()); }

    public static int sortByWood(WoodType w1, WoodType w2) { return Integer.compare(ALL_TYPES.indexOf(w1), ALL_TYPES.indexOf(w2)); }

    @Nullable
    public static WoodType fromTypeName(String name)
    {
        for(WoodType type : ALL_TYPES)
        {
            if(type.name.equalsIgnoreCase(name))
                return type;
        }
        return null;
    }


}
