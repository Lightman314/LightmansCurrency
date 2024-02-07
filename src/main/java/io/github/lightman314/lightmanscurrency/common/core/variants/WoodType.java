package io.github.lightman314.lightmanscurrency.common.core.variants;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodData;
import io.github.lightman314.lightmanscurrency.datagen.util.WoodDataHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.MaterialColor;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Predicate;

public class WoodType implements IOptionalKey {

    private static final List<WoodType> ALL_TYPES = new ArrayList<>();
    private static ImmutableList<WoodType> VALID_TYPES = null;
    private static ImmutableList<WoodType> VANILLA_TYPES = null;
    private static ImmutableList<WoodType> MODDED_TYPES = null;

    public static final WoodType OAK = vb("oak").ofName("Oak").ofColor(MaterialColor.WOOD).build();
    public static final WoodType SPRUCE = vb("spruce").ofName("Spruce").ofColor(MaterialColor.PODZOL).build();
    public static final WoodType BIRCH = vb("birch").ofName("Birch").ofColor(MaterialColor.SAND).build();
    public static final WoodType JUNGLE = vb("jungle").ofName("Jungle").ofColor(MaterialColor.DIRT).build();
    public static final WoodType ACACIA = vb("acacia").ofName("Acacia").ofColor(MaterialColor.COLOR_ORANGE).build();

    public static final WoodType DARK_OAK = vb("dark_oak").ofName("Dark Oak").ofColor(MaterialColor.COLOR_BROWN).build();
    //1.19+
    public static final WoodType MANGROVE = vb("mangrove").ofName("Mangrove").ofColor(MaterialColor.COLOR_RED).build();
    //1.20+
    //public static final WoodType CHERRY = vb("cherry").ofName("Cherry").ofColor(MaterialColor.TERRACOTTA_WHITE).build();
    //public static final WoodType BAMBOO = vb("bamboo").ofName("Bamboo").ofColor(MaterialColor.COLOR_YELLOW).build();
    public static final WoodType CRIMSON = vb("crimson").ofName("Crimson").ofColor(MaterialColor.CRIMSON_STEM).build();
    public static final WoodType WARPED = vb("warped").ofName("Warped").ofColor(MaterialColor.WARPED_STEM).build();

    public final String id;
    private final String modid;
    public final String displayName;
    public final MaterialColor mapColor;
    public final Attributes attributes;
    @Nullable
    public WoodData getData() { return WoodDataHelper.get(this); }

    private WoodType(Builder builder)
    {
        this.id = builder.id;
        this.modid = builder.modid;
        this.displayName = builder.getDisplayName();
        this.mapColor = builder.color;
        this.attributes = builder.attributes;
    }

    public final String generateID(String prefix) {
        if(!prefix.endsWith("_"))
            prefix += "_";
        if(this.isModded())
            prefix += this.getModID() + "_";
        return prefix + this.id;
    }

    public final String generateResourceLocation(String prefix) { return this.generateResourceLocation(prefix,""); }
    public final String generateResourceLocation(String prefix, String postFix) {
        if(this.isModded())
            prefix += this.getModID() + "/";
        return prefix + this.id + postFix;
    }

    public final boolean isVanilla() { return this.getModID().equals("minecraft"); }
    @Nonnull
    public String getModID() { return this.modid; }
    public final boolean isMod(String modid) { return this.getModID().equalsIgnoreCase(modid); }
    public boolean isValid() { return true; }
    @Override
    public final boolean isModded() { return !this.isVanilla(); }

    @Override
    public String toString() { return this.id; }

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

    public static List<WoodType> moddedValues()
    {
        if(MODDED_TYPES == null)
            MODDED_TYPES = ImmutableList.copyOf(validValues().stream().filter(WoodType::isModded).toList());
        return MODDED_TYPES;
    }
    public static List<WoodType> moddedValues(String modid) { return ImmutableList.copyOf(moddedValues().stream().filter(t -> t.isMod(modid)).toList()); }

    public static boolean hasModdedValues() { return moddedValues().size() > 0; }

    public static int sortByWood(WoodType w1, WoodType w2) { return Integer.compare(ALL_TYPES.indexOf(w1), ALL_TYPES.indexOf(w2)); }

    @Nullable
    public static WoodType fromTypeID(String name)
    {
        for(WoodType type : ALL_TYPES)
        {
            if(type.id.equalsIgnoreCase(name))
                return type;
        }
        return null;
    }

    @Override
    public int hashCode() { return new ResourceLocation(this.modid, this.id).hashCode(); }

    private static Builder vb(@Nonnull String id) { return new Builder(id); }
    public static Builder builder(@Nonnull String id, @Nonnull String modid) { return new Builder(id, modid); }

    public static final class Builder
    {

        private final String id;
        private String displayName = null;
        @Nonnull
        private String getDisplayName()
        {
            if(this.displayName == null)
            {
                StringBuilder builder = new StringBuilder();
                boolean makeCapital = true;
                for(int i = 0; i < this.id.length(); ++i)
                {
                    char c = this.id.charAt(i);
                    if(c == '_')
                    {
                        makeCapital = true;
                        builder.append(' ');
                    }
                    else
                    {
                        if(makeCapital)
                        {
                            makeCapital = false;
                            builder.append(("" + c).toUpperCase(Locale.ROOT));
                        }
                        else
                            builder.append(c);
                    }
                }
                return builder.toString();
            }
            else
                return this.displayName;
        }
        private MaterialColor color = MaterialColor.WOOD;
        private final String modid;
        private Attributes attributes = Attributes.ALL;
        private WoodData data = null;


        private Builder(@Nonnull String id) {
            this.id = id;
            this.modid = "minecraft";
        }

        private Builder(@Nonnull String id, @Nonnull String modid) {
            this.id = id;
            this.modid = modid;
            if(this.modid.equals("minecraft"))
                throw new RuntimeException("Cannot make a custom Wood Type with a modid of minecraft!");
        }

        public Builder ofColor(@Nonnull MaterialColor color) { this.color = color; return this; }
        public Builder ofName(@Nonnull String displayName) { this.displayName = displayName; return this; }
        public Builder withAttributes(@Nonnull Attributes attributes) { this.attributes = attributes; return this; }

        public WoodType build()
        {
            WoodType newType = new WoodType(this);
            if(this.data != null)
                WoodDataHelper.register(newType, data);
            ALL_TYPES.add(newType);
            return newType;
        }

    }

    public record Attributes(boolean hasCustomLog, boolean hasCustomPlanks, boolean hasCustomSlab)
    {
        public static final Attributes ALL = new Attributes(true, true, true);
        public static final Attributes LOG_ONLY = new Attributes(true, false, false);
        public static final Attributes PLANKS_ONLY = new Attributes(false, true, false);
        public static final Attributes PLANKS_AND_SLAB_ONLY = new Attributes(false, true, true);
        @Override
        public int hashCode() { return Objects.hash(this.hasCustomLog, this.hasCustomPlanks, this.hasCustomSlab); }

        public static final Predicate<Attributes> needsAll = a -> a.hasCustomLog && a.hasCustomPlanks && a.hasCustomSlab;
        public static final Predicate<Attributes> needsLog = a -> a.hasCustomLog;
        public static final Predicate<Attributes> needsLogAndPlanks = a -> a.hasCustomLog && a.hasCustomPlanks;
        public static final Predicate<Attributes> needsLogAndSlab = a -> a.hasCustomLog && a.hasCustomSlab;
        public static final Predicate<Attributes> needsPlanks = a -> a.hasCustomPlanks;
        public static final Predicate<Attributes> needsPlanksAndSlab = a -> a.hasCustomPlanks && a.hasCustomSlab;
        public static final Predicate<Attributes> needsSlab = a -> a.hasCustomSlab;

    }

}