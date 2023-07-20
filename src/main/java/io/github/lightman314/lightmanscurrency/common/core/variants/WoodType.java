package io.github.lightman314.lightmanscurrency.common.core.variants;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.common.crafting.util.WoodDataHelper;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.fml.ModList;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class WoodType {

    private static final List<WoodType> ALL_TYPES = new ArrayList<>();
    private static ImmutableList<WoodType> VALID_TYPES = null;
    private static ImmutableList<WoodType> VANILLA_TYPES = null;

    public static final WoodType OAK = new WoodType("oak");
    public static final WoodType SPRUCE = new WoodType("spruce");
    public static final WoodType BIRCH = new WoodType("birch");
    public static final WoodType JUNGLE = new WoodType("jungle");
    public static final WoodType ACACIA = new WoodType("acacia");

    public static final WoodType DARK_OAK = new WoodType("dark_oak");
    public static final WoodType MANGROVE = new WoodType("mangrove");
    public static final WoodType CHERRY = new WoodType("cherry");
    public static final WoodType BAMBOO = new WoodType("bamboo");
    public static final WoodType CRIMSON = new WoodType("crimson");
    public static final WoodType WARPED = new WoodType("warped");

    public final String name;
    public final MapColor mapColor;
    private final Supplier<WoodData> dataSource;
    private WoodData data = null;
    public WoodData getData() { if(this.data == null && this.dataSource != null) this.data = this.dataSource.get(); return this.data; }

    //Vanilla Only Constructor
    private WoodType(String name) { this(name, MapColor.WOOD, () -> WoodDataHelper.forVanillaType(name)); }
    protected WoodType(String name, Supplier<WoodData> dataSource) { this(name, MapColor.WOOD, dataSource); }
    protected WoodType(String name, MapColor mapColor, Supplier<WoodData> dataSource) { this.name = name; this.mapColor = mapColor; this.dataSource = dataSource; ALL_TYPES.add(this); }


    public boolean isVanilla() { return true; }
    public String getModID() { return "minecraft"; }
    public boolean isValid() { return true; }
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

    public static List<WoodType> moddedValues(String modid) { return ImmutableList.copyOf(ALL_TYPES.stream().filter(t -> t instanceof ModdedWoodType mt && mt.mod.equals(modid)).toList()); }

    public static int sortByWood(WoodType w1, WoodType w2) { return Integer.compare(ALL_TYPES.indexOf(w1), ALL_TYPES.indexOf(w2)); }
    public static class ModdedWoodType extends WoodType
    {
        private final String mod;
        public ModdedWoodType(String name, String mod, @Nullable Supplier<WoodData> dataSource) { super(name, dataSource); this.mod = mod; }
        public ModdedWoodType(String name, MapColor mapColor, String mod, @Nullable Supplier<WoodData> dataSource) { super(name, mapColor, dataSource); this.mod = mod; }
        @Override
        public boolean isVanilla() { return false; }
        @Override
        public String getModID() { return this.mod; }
        @Override
        public boolean isValid() { return ModList.get().isLoaded(this.mod); }
    }

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
