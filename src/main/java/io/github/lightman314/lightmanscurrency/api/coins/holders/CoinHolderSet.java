package io.github.lightman314.lightmanscurrency.api.coins.holders;

import com.mojang.datafixers.util.Either;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import io.github.lightman314.lightmanscurrency.api.coins.data.coin.CoinEntry;
import io.github.lightman314.lightmanscurrency.api.coins.events.ChainDataReloadedEvent;
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
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.holdersets.HolderSetType;
import net.neoforged.neoforge.registries.holdersets.ICustomHolderSet;

import java.util.*;
import java.util.stream.Stream;

@EventBusSubscriber
public class CoinHolderSet implements ICustomHolderSet<Item> {

    private static final Map<Pair<String,Boolean>,CoinHolderSet> setCache = new HashMap<>();

    public static final HolderSetType TYPE = new Type();

    private final List<Runnable> invalidationListeners = new ArrayList<>();
    private void flagAsChanged() {
        //Reset the cache, and inform the relevant parties that this value has changed
        this.cache = null;
        this.invalidationListeners.forEach(Runnable::run);
    }

    private List<Holder<Item>> cache = null;
    private final String chain;
    private final boolean includeSideChains;
    private CoinHolderSet(Pair<String,Boolean> key) {
        this.chain = key.getFirst();
        this.includeSideChains = key.getSecond();
    }

    public static CoinHolderSet create(String chain,boolean includeSideChains) {
        return setCache.computeIfAbsent(Pair.of(chain,includeSideChains),CoinHolderSet::new);
    }

    private List<Holder<Item>> getItems() {
        if(this.cache == null) {
            if(LCApi.getCoinAPI().dataNotLoaded())
                return List.of();
            ChainData data = LCApi.getCoinAPI().lookupChain(this.chain);
            if(data == null)
                this.cache = List.of();
            else {
                this.cache = data.getAllEntries(this.includeSideChains,ChainData.SORT_LOWEST_VALUE_FIRST).stream().map(CoinEntry::getCoin).map(BuiltInRegistries.ITEM::wrapAsHolder).toList();
            }
        }
        return this.cache;
    }

    @Override
    public HolderSetType type() { return TYPE; }
    @Override
    public Stream<Holder<Item>> stream() { return this.getItems().stream(); }
    @Override
    public int size() { return this.getItems().size(); }
    @Override
    public boolean isBound() { return true; }
    @Override
    public void addInvalidationListener(Runnable runnable) { this.invalidationListeners.add(runnable); }
    @Override
    public Either<TagKey<Item>, List<Holder<Item>>> unwrap() { return Either.right(this.getItems()); }

    @Override
    public Optional<Holder<Item>> getRandomElement(RandomSource random) {
        List<Holder<Item>> list = this.getItems();
        if(list.isEmpty())
            return Optional.empty();
        return Optional.of(list.get(random.nextInt(list.size())));
    }
    @Override
    public Holder<Item> get(int index) { return this.getItems().get(index); }
    @Override
    public boolean contains(Holder<Item> value) { return this.getItems().contains(value); }
    @Override
    public boolean canSerializeIn(HolderOwner<Item> owner) { return true; }
    @Override
    public Optional<TagKey<Item>> unwrapKey() { return Optional.empty(); }
    @Override
    public Iterator<Holder<Item>> iterator() { return this.getItems().iterator(); }

    //Event Listeners to invalidate and recollect the cache
    @SubscribeEvent
    private static void coinDataReloaded(ChainDataReloadedEvent.Server event) {
        for(CoinHolderSet set : setCache.values())
            set.flagAsChanged();
    }

    @SubscribeEvent
    private static void coinDataSynced(ChainDataReloadedEvent.Client event) {
        for(CoinHolderSet set : setCache.values())
            set.flagAsChanged();
    }

    private static class Type extends HolderSetHelper.SingleRegistryType<Item> {

        private static final MapCodec<CoinHolderSet> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                Codec.STRING.fieldOf("chain").forGetter(s -> s.chain),
                Codec.BOOL.optionalFieldOf("sideChains",true).forGetter(s -> s.includeSideChains)
        ).apply(builder,CoinHolderSet::create));
        private static final StreamCodec<RegistryFriendlyByteBuf,CoinHolderSet> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8,s -> s.chain,
                ByteBufCodecs.BOOL,s -> s.includeSideChains,
                CoinHolderSet::create);

        @Override
        protected ResourceKey<Registry<Item>> getTargetRegistry() { return Registries.ITEM; }
        @Override
        protected MapCodec<CoinHolderSet> unsafeCodec() { return MAP_CODEC; }
        @Override
        protected StreamCodec<RegistryFriendlyByteBuf,CoinHolderSet> unsafeStream() { return STREAM_CODEC; }
    }


}