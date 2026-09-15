package io.github.lightman314.lightmanscurrency.api.config.data;

import com.google.common.collect.ImmutableList;
import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.config.ConfigFile;
import io.github.lightman314.lightmanscurrency.api.config.events.ConfigEvent;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ItemListOption;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.HolderSetHelper;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderOwner;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforge.registries.holdersets.ICustomHolderSet;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

@EventBusSubscriber
public class ItemListOptionSet implements ICustomHolderSet<Item> {

    private static final Map<Pair<Identifier,String>,ItemListOptionSet> setCache = new HashMap<>();

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

    private final Identifier fileID;
    private final String option;
    private ItemListOptionSet(Pair<Identifier,String> key) { this.fileID = key.getFirst(); this.option = key.getSecond(); }

    public static ItemListOptionSet create(Identifier fileID,String optionKey) {
        return setCache.computeIfAbsent(Pair.of(fileID,optionKey),ItemListOptionSet::new);
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
    public boolean isBound() { return true; }

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

    //Event Listeners to invalidate and recollect the item list when the config is reloaded
    @SubscribeEvent
    private static void configReloaded(ConfigEvent.ConfigReloadedEvent.Post event)
    {
        for(ItemListOptionSet set : setCache.values())
        {
            if(set.fileID == event.getConfig().getFileID())
                set.flagAsChanged();
        }
    }
    @SubscribeEvent
    private static void configSynced(ConfigEvent.ConfigReceivedSyncDataEvent.Post event)
    {
        for(ItemListOptionSet set : setCache.values())
        {
            if(set.fileID == event.getConfig().getFileID())
                set.flagAsChanged();
        }
    }

    private static class Type extends HolderSetHelper.SingleRegistryType<Item>
    {
        private static final MapCodec<ItemListOptionSet> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                        Identifier.CODEC.fieldOf("fileID").forGetter(s -> s.fileID),
                        Codec.STRING.fieldOf("option").forGetter(s -> s.option))
                .apply(builder,ItemListOptionSet::create));
        private static final StreamCodec<RegistryFriendlyByteBuf,ItemListOptionSet> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC,s -> s.fileID,
                ByteBufCodecs.STRING_UTF8,s -> s.option,
                ItemListOptionSet::create);

        @Override
        protected ResourceKey<Registry<Item>> getTargetRegistry() { return Registries.ITEM; }
        @Override
        protected MapCodec<ItemListOptionSet> unsafeCodec() { return CODEC; }
        @Override
        protected StreamCodec<RegistryFriendlyByteBuf,ItemListOptionSet> unsafeStream() { return STREAM_CODEC; }

    }

}