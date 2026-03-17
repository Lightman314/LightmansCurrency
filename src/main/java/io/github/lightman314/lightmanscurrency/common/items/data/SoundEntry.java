package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

public record SoundEntry(int weight,ResourceLocation sound) {

    public static final ResourceLocation DEFAULT_COIN_SOUND = LightmansCurrency.id("coins_clinking");
    public static final List<SoundEntry> WALLET_DEFAULT = ImmutableList.of(new SoundEntry(1,DEFAULT_COIN_SOUND));

    public static final Codec<SoundEntry> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(Codec.INT.fieldOf("weight").forGetter(SoundEntry::weight),
                    ResourceLocation.CODEC.fieldOf("sound").forGetter(SoundEntry::sound))
                    .apply(builder,SoundEntry::new));

    public static final StreamCodec<ByteBuf,SoundEntry> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,SoundEntry::weight,
            ResourceLocation.STREAM_CODEC,SoundEntry::sound,
            SoundEntry::new);

    public static ResourceLocation getRandomEntry(RandomSource random, List<SoundEntry> entries, ResourceLocation defaultValue)
    {
        int totalWeight = 0;
        for(SoundEntry entry : entries)
            totalWeight += entry.weight;
        int r = random.nextInt(totalWeight);
        for(SoundEntry entry : entries)
        {
            if(r < entry.weight)
                return entry.sound;
            r -= entry.weight;
        }
        return defaultValue;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder
    {
        private final List<SoundEntry> list = new ArrayList<>();

        public Builder addVanilla(int weight,String sound) { return this.add(weight,ResourceLocation.withDefaultNamespace(sound)); }
        public Builder addModded(int weight,String modid,String sound) { return this.add(weight,ResourceLocation.fromNamespaceAndPath(modid,sound)); }
        public Builder addLC(int weight,String modid,String sound) { return this.add(weight,LightmansCurrency.id(sound)); }
        public Builder add(int weight,ResourceLocation sound) { this.list.add(new SoundEntry(weight,sound)); return this; }

        public List<SoundEntry> build() { return ImmutableList.copyOf(this.list); }

    }

}
