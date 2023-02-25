package io.github.lightman314.lightmanscurrency.common.core.variants;

import com.google.common.collect.ImmutableList;
import net.minecraftforge.fml.ModList;

import java.util.ArrayList;
import java.util.List;

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
    public static final WoodType CRIMSON = new WoodType("crimson");
    public static final WoodType WARPED = new WoodType("warped");

    public final String name;
    protected WoodType(String name) { this.name = name; ALL_TYPES.add(this); }

    public boolean isVanilla() { return true; }
    public boolean isValid() { return true; }
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

    public static int sortByWood(WoodType w1, WoodType w2) { return Integer.compare(ALL_TYPES.indexOf(w1), ALL_TYPES.indexOf(w2)); }
    public static class ModdedWoodType extends WoodType
    {

        //BYG?
        private static final String BYG_MOD = "byg";


        //BOP?

        private final String mod;
        public ModdedWoodType(String name, String mod) { super(name); this.mod = mod; }
        @Override
        public boolean isVanilla() { return false; }
        @Override
        public boolean isValid() { return ModList.get().isLoaded(this.mod); }
    }


}
