package io.github.lightman314.lightmanscurrency.api.config.holder_sets;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.event.ConfigEvent;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ItemListOption;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforge.registries.holdersets.ICustomHolderSet;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.*;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
@EventBusSubscriber
public class ItemListOptionSet implements ICustomHolderSet<Item> {

    private static final Map<Pair<ResourceLocation,String>,ItemListOptionSet> setCache = new HashMap<>();

    public static final HolderSetType TYPE = new Type();

    private final List<Runnable> invalidationListeners = new ArrayList<>();
    private void flagAsChanged() {
        //Reset the cache, and inform the relevant parties that this value has been changed
        this.cache = null;
        this.invalidationListeners.forEach(Runnable::run);
    }

    private boolean registerListener = true;
    private List<Holder<Item>> cache = null;
    @Nullable
    private ItemListOption getOption()
    {
        ConfigFile file = ConfigFile.lookupFile(this.fileID);
        if(file != null && file.getAllOptions().get(this.option) instanceof ItemListOption o)
            return o;
        return null;
    }
    private List<Holder<Item>> getItems()
    {
        if(this.cache == null)
        {
            //Don't bother collecting if the config isn't loaded yet
            ItemListOption o = this.getOption();
            if(o == null || !o.isLoaded())
                return new ArrayList<>();
            //Register a more direct config listener as well so that we'll listen to changes made elsewhere
            if(this.registerListener)
            {
                o.addListener(i -> this.flagAsChanged());
                this.registerListener = false;
            }
            List<Holder<Item>> temp = new ArrayList<>();
            for(Item item : o.get())
                temp.add(BuiltInRegistries.ITEM.wrapAsHolder(item));
            this.cache = ImmutableList.copyOf(temp);
        }
        return this.cache;
    }

    private final ResourceLocation fileID;
    private final String option;
    private ItemListOptionSet(ResourceLocation fileID, String optionKey) { this.fileID = fileID; this.option = optionKey; }

    public static ItemListOptionSet create(ResourceLocation fileID,String optionKey)
    {
        Pair<ResourceLocation,String> key = Pair.of(fileID,optionKey);
        if(!setCache.containsKey(key))
            setCache.put(key,new ItemListOptionSet(fileID,optionKey));
        return setCache.get(key);
    }
    public static ItemListOptionSet create(ItemListOption option)
    {
        String path = null;
        ConfigFile file = option.getFile();
        if(file == null)
            throw new IllegalArgumentException("Config Option was not attached to a config file!");
        String fullKey = option.getFullName();
        if(fullKey == null)
            throw new IllegalArgumentException("Config Option was not a member of the config file!");
        return create(file.getFileID(),fullKey);
    }

    @Override
    public Stream<Holder<Item>> stream() { return this.getItems().stream(); }
    @Override
    public int size() { return this.getItems().size(); }
    @Override
    public Either<TagKey<Item>,List<Holder<Item>>> unwrap() { return Either.right(this.getItems()); }
    @Override
    public Optional<Holder<Item>> getRandomElement(RandomSource random) {
        List<Holder<Item>> list = this.getItems();
        if(list.isEmpty())
            return Optional.empty();
        return Optional.of(list.get(random.nextInt(list.size())));
    }
    @Override
    public Holder<Item> get(int index) {
        return this.getItems().get(index);
    }
    @Override
    public boolean contains(Holder<Item> holder) { return this.getItems().contains(holder); }
    @Override
    public boolean canSerializeIn(HolderOwner<Item> owner) { return true; }
    @Override
    public Optional<TagKey<Item>> unwrapKey() { return Optional.empty(); }
    @Override
    public Iterator<Holder<Item>> iterator() { return this.getItems().iterator(); }
    @Override
    public void addInvalidationListener(Runnable runnable) { this.invalidationListeners.add(runnable); }
    @Override
    public HolderSetType type() { return TYPE; }
    @Override
    public SerializationType serializationType() { return SerializationType.UNKNOWN; }

    //Event Listeners to invalidate and recollect the wallet list when the config is reloaded
    @SubscribeEvent
    public static void configReloaded(ConfigEvent.ConfigReloadedEvent.Post event)
    {
        for(ItemListOptionSet set : setCache.values())
        {
            if(set.fileID == event.getConfig().getFileID())
                set.flagAsChanged();
        }
    }
    @SubscribeEvent
    public static void configSynced(ConfigEvent.ConfigReceivedSyncDataEvent.Post event)
    {
        for(ItemListOptionSet set : setCache.values())
        {
            if(set.fileID == event.getConfig().getFileID())
                set.flagAsChanged();
        }
    }

    private static class Type implements HolderSetType
    {
        private static final MapCodec<ItemListOptionSet> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ResourceLocation.CODEC.fieldOf("fileID").forGetter(s -> s.fileID),
                Codec.STRING.fieldOf("option").forGetter(s -> s.option))
                .apply(builder,ItemListOptionSet::create));
        private MapCodec<? extends ICustomHolderSet<?>> unsafeCodec() { return CODEC; }
        private static final StreamCodec<RegistryFriendlyByteBuf,ItemListOptionSet> STREAM_CODEC = StreamCodec.composite(ResourceLocation.STREAM_CODEC,s -> s.fileID,ByteBufCodecs.STRING_UTF8,s -> s.option,ItemListOptionSet::create);
        private StreamCodec<RegistryFriendlyByteBuf,? extends ICustomHolderSet<?>> unsafeStream() { return STREAM_CODEC; }
        @Override
        public <T> MapCodec<? extends ICustomHolderSet<T>> makeCodec(ResourceKey<? extends Registry<T>> registryKey, Codec<Holder<T>> holderCodec, boolean forceList) {
            return isItem(registryKey) ? (MapCodec<? extends ICustomHolderSet<T>>)unsafeCodec() : MapCodec.unit(new EmptySet<>(this));
        }
        @Override
        public <T> StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>> makeStreamCodec(ResourceKey<? extends Registry<T>> registryKey) {
            return isItem(registryKey) ? (StreamCodec<RegistryFriendlyByteBuf, ? extends ICustomHolderSet<T>>)unsafeStream() : StreamCodec.unit(new EmptySet<>(this));
        }
        private boolean isItem(ResourceKey<? extends Registry<?>> key) { return key == BuiltInRegistries.ITEM.key(); }

    }

    private static class EmptySet<T> implements ICustomHolderSet<T>
    {
        private final Type type;
        private EmptySet(Type type) { this.type = type; }
        @Override
        public HolderSetType type() { return this.type; }
        @Override
        public Stream<Holder<T>> stream() { return Stream.empty(); }
        @Override
        public int size() { return 0; }
        @Override
        public Either<TagKey<T>, List<Holder<T>>> unwrap() { return Either.right(ImmutableList.of()); }
        @Override
        public Optional<Holder<T>> getRandomElement(RandomSource random) { return Optional.empty(); }
        @Override
        public Holder<T> get(int index) { return null; }
        @Override
        public boolean contains(Holder<T> holder) { return false; }
        @Override
        public boolean canSerializeIn(HolderOwner<T> owner) { return false; }
        @Override
        public Optional<TagKey<T>> unwrapKey() { return Optional.empty(); }
        @Override
        public Iterator<Holder<T>> iterator() { return this.stream().iterator(); }
    }

}
