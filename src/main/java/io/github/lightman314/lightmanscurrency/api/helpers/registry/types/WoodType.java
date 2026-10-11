package io.github.lightman314.lightmanscurrency.api.helpers.registry.types;

import net.minecraft.world.level.material.MapColor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public final class WoodType {

    private static final List<WoodType> ALL_TYPES = new ArrayList<>();
    public static List<WoodType> validValues() { return Collections.unmodifiableList(ALL_TYPES); }

    public static final WoodType OAK = builder("oak").ofName("Oak").ofColor(MapColor.WOOD).build();
    public static final WoodType SPRUCE = builder("spruce").ofName("Spruce").ofColor(MapColor.PODZOL).build();
    public static final WoodType BIRCH = builder("birch").ofName("Birch").ofColor(MapColor.SAND).build();
    public static final WoodType JUNGLE = builder("jungle").ofName("Jungle").ofColor(MapColor.DIRT).build();
    public static final WoodType ACACIA = builder("acacia").ofName("Acacia").ofColor(MapColor.COLOR_ORANGE).build();
    public static final WoodType DARK_OAK = builder("dark_oak").ofName("Dark Oak").ofColor(MapColor.COLOR_BROWN).build();
    public static final WoodType MANGROVE = builder("mangrove").ofName("Mangrove").ofColor(MapColor.COLOR_RED).build();
    public static final WoodType CHERRY = builder("cherry").ofName("Cherry").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType PALE_OAK = builder("pale_oak").ofName("Pale Oak").ofColor(MapColor.QUARTZ).build();
    public static final WoodType BAMBOO = builder("bamboo").ofName("Bamboo").ofColor(MapColor.COLOR_YELLOW).build();
    public static final WoodType CRIMSON = builder("crimson").ofName("Crimson").ofColor(MapColor.CRIMSON_STEM).build();
    public static final WoodType WARPED = builder("warped").ofName("Warped").ofColor(MapColor.WARPED_STEM).build();

    public static int sortByWood(WoodType w1,WoodType w2) { return Integer.compare(ALL_TYPES.indexOf(w1),ALL_TYPES.indexOf(w2)); }

    public final String id;
    public final String displayName;
    public final MapColor mapColor;
    public final Attributes attributes;

    private WoodType(Builder builder) {
        this.id = builder.id;
        this.displayName = builder.getDisplayName();
        this.mapColor = builder.color;
        this.attributes = builder.attributes;
    }

    public String generateID(String prefix) {
        if(!prefix.endsWith("_"))
            prefix += "_";
        return prefix + this.id;
    }

    public String generatePath(String prefix) { return this.generatePath(prefix,""); }
    public String generatePath(String prefix,String suffix) { return prefix + this.id + suffix; }

    private static Builder builder(String id) { return new Builder(id); }

    public static final class Builder {
        private final String id;
        private String displayName = null;
        private String getDisplayName() {
            if(this.displayName == null)
            {
                StringBuilder builder = new StringBuilder();
                boolean makeCapital = true;
                for(int i = 0; i < this.id.length(); ++i) {
                    char c = this.id.charAt(i);
                    if(c == '_') {
                        makeCapital = true;
                        builder.append(' ');
                    }
                    else {
                        if(makeCapital) {
                            makeCapital = false;
                            builder.append(Character.toUpperCase(c));
                        }
                        else builder.append(c);
                    }
                }
                return builder.toString();
            }
            return this.displayName;
        }
        private MapColor color = MapColor.WOOD;
        private Attributes attributes = Attributes.ALL;

        private Builder(String id) { this.id = id; }

        public Builder ofColor(MapColor color) { this.color = color; return this; }
        public Builder ofName(String displayName) { this.displayName = displayName; return this; }
        public Builder withAttributes(Attributes attributes) { this.attributes = attributes; return this; }

        public WoodType build() {
            WoodType newType = new WoodType(this);
            ALL_TYPES.add(newType);
            return newType;
        }

    }

    public record Attributes(boolean hasLog,boolean hasPlanks,boolean hasSlab) {

        public static final Attributes ALL = new Attributes(true,true,true);
        public static final Attributes LOG_ONLY = new Attributes(true,false,false);
        public static final Attributes PLANKS_ONLY = new Attributes(false,true,false);
        public static final Attributes PLANKS_AND_SLAB_ONLY = new Attributes(false,true,true);

        public static final Predicate<Attributes> NEEDS_ALL = a -> a.hasLog && a.hasPlanks && a.hasSlab;
        public static final Predicate<Attributes> NEEDS_LOG = Attributes::hasLog;
        public static final Predicate<Attributes> NEEDS_PLANKS = Attributes::hasPlanks;
        public static final Predicate<Attributes> NEEDS_SLAB = Attributes::hasSlab;
        public static final Predicate<Attributes> NEEDS_LOG_AND_PLANKS = a -> a.hasLog && a.hasPlanks;
        public static final Predicate<Attributes> NEEDS_LOG_AND_SLAB = a -> a.hasLog && a.hasSlab;
        public static final Predicate<Attributes> NEEDS_PLANKS_AND_SLAB = a -> a.hasPlanks && a.hasSlab;

    }

    @Override
    public int hashCode() { return this.id.hashCode(); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WoodType other)
            return this.id.equals(other.id) && this.attributes.equals(other.attributes) && this.displayName.equals(other.displayName) && this.mapColor == other.mapColor;
        return super.equals(obj);
    }

    @Override
    public String toString() { return "WoodType[" + this.id + "]"; }

}