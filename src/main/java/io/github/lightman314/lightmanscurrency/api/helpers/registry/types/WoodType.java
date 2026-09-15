package io.github.lightman314.lightmanscurrency.api.helpers.registry.types;

import com.google.common.collect.ImmutableList;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

@EventBusSubscriber
public final class WoodType implements IOptionalKey {

    private static boolean locked = false;
    private static final List<Consumer<WoodType>> newTypeListeners = new ArrayList<>();
    public static void registerListener(Consumer<WoodType> listener) {
        if(locked)
            return;
        newTypeListeners.add(listener);
    }

    private static final List<WoodType> ALL_TYPES = new ArrayList<>();
    private static final List<WoodType> VALID_TYPES = new ArrayList<>();
    public static List<WoodType> validValues() { return ImmutableList.copyOf(VALID_TYPES); }
    private static final List<WoodType> VANILLA_TYPES = new ArrayList<>();
    public static List<WoodType> vanillaValues() { return ImmutableList.copyOf(VANILLA_TYPES); }
    private static final List<WoodType> MODDED_TYPES = new ArrayList<>();
    public static List<WoodType> moddedValues() { return ImmutableList.copyOf(MODDED_TYPES); }
    public static List<WoodType> moddedValues(String modid) { return moddedValues().stream().filter(t -> t.isMod(modid)).toList(); }
    public static boolean hasModdedValues() { return !MODDED_TYPES.isEmpty(); }

    public static final WoodType OAK = vb("oak").ofName("Oak").ofColor(MapColor.WOOD).build();
    public static final WoodType SPRUCE = vb("spruce").ofName("Spruce").ofColor(MapColor.PODZOL).build();
    public static final WoodType BIRCH = vb("birch").ofName("Birch").ofColor(MapColor.SAND).build();
    public static final WoodType JUNGLE = vb("jungle").ofName("Jungle").ofColor(MapColor.DIRT).build();
    public static final WoodType ACACIA = vb("acacia").ofName("Acacia").ofColor(MapColor.COLOR_ORANGE).build();
    public static final WoodType DARK_OAK = vb("dark_oak").ofName("Dark Oak").ofColor(MapColor.COLOR_BROWN).build();
    public static final WoodType MANGROVE = vb("mangrove").ofName("Mangrove").ofColor(MapColor.COLOR_RED).build();
    public static final WoodType CHERRY = vb("cherry").ofName("Cherry").ofColor(MapColor.TERRACOTTA_WHITE).build();
    public static final WoodType PALE_OAK = vb("pale_oak").ofName("Pale Oak").ofColor(MapColor.QUARTZ).build();
    public static final WoodType BAMBOO = vb("bamboo").ofName("Bamboo").ofColor(MapColor.COLOR_YELLOW).build();
    public static final WoodType CRIMSON = vb("crimson").ofName("Crimson").ofColor(MapColor.CRIMSON_STEM).build();
    public static final WoodType WARPED = vb("warped").ofName("Warped").ofColor(MapColor.WARPED_STEM).build();

    public static int sortByWood(WoodType w1,WoodType w2) { return Integer.compare(ALL_TYPES.indexOf(w1),ALL_TYPES.indexOf(w2)); }

    @Nullable
    public static WoodType fromTypeID(Identifier id) {
        for(WoodType type : ALL_TYPES)
        {
            if(type.id.equals(id))
                return type;
        }
        return null;
    }

    public final Identifier id;
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
        if(this.isModded())
            prefix += this.id.getNamespace() + "_";
        return prefix + this.id.getPath();
    }
    public String translationSegment() {
        if(this.isModded())
            return this.id.getNamespace() + "." + this.id.getPath();
        return this.id.getPath();
    }

    public String generatePath(String prefix) { return this.generatePath(prefix,""); }
    public String generatePath(String prefix,String suffix) {
        if(this.isModded())
            prefix += this.id.getNamespace() + "/";
        return prefix + this.id.getPath() + suffix;
    }

    @Override
    public boolean isVanilla() { return this.id.getNamespace().equals("minecraft"); }
    @Override
    public boolean isModded() { return !this.isVanilla(); }
    public String getModID() { return this.id.getNamespace(); }
    public boolean isMod(String modid) { return this.id.getNamespace().equalsIgnoreCase(modid); }
    public boolean isValid() { return ModList.get().isLoaded(this.getModID()); }

    private static Builder vb(String id) { return new Builder(Identifier.withDefaultNamespace(id)); }
    public static Builder builder(Identifier id) {
        if(id.getNamespace().equalsIgnoreCase("minecraft"))
            throw new IllegalArgumentException("Cannot make a custom Wood Type with a modid of minecraft!");
        return new Builder(id);
    }

    public static final class Builder {
        private final Identifier id;
        private String displayName = null;
        private String getDisplayName() {
            if(this.displayName == null)
            {
                String id = this.id.getPath();
                StringBuilder builder = new StringBuilder();
                boolean makeCapital = true;
                for(int i = 0; i < id.length(); ++i) {
                    char c = id.charAt(i);
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

        private Builder(Identifier id) {
            this.id = id;
        }

        public Builder ofColor(MapColor color) { this.color = color; return this; }
        public Builder ofName(String displayName) { this.displayName = displayName; return this; }
        public Builder withAttributes(Attributes attributes) { this.attributes = attributes; return this; }

        public WoodType build() {
            WoodType newType = new WoodType(this);
            if(locked)
            {
                LightmansCurrency.LogWarning("Attempted to build a new WoodType after the common setup event!");
                return newType;
            }
            ALL_TYPES.add(newType);
            if(newType.isVanilla())
                VANILLA_TYPES.add(newType);
            if(newType.isModded())
                MODDED_TYPES.add(newType);
            if(newType.isValid())
                VALID_TYPES.add(newType);
            for(Consumer<WoodType> l : new ArrayList<>(newTypeListeners))
                l.accept(newType);
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

        public static Consumer<WoodType> filteredConsumer(Consumer<WoodType> consumer,Predicate<Attributes> filter) {
            return type -> {
                if(type.isValid() && filter.test(type.attributes))
                    consumer.accept(type);
            };
        }
    }

    @SubscribeEvent
    private static void lockData(FMLCommonSetupEvent event) { locked = true; newTypeListeners.clear(); }

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