package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;

public record SoundEntry(int weight,@Nonnull ResourceLocation sound) {

    public static final ResourceLocation DEFAULT_COIN_SOUND = VersionUtil.lcResource("coins_clinking");
    public static final List<SoundEntry> WALLET_DEFAULT = ImmutableList.of(new SoundEntry(1,DEFAULT_COIN_SOUND));

    public static final Codec<SoundEntry> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(Codec.INT.fieldOf("weight").forGetter(SoundEntry::weight),
                            ResourceLocation.CODEC.fieldOf("sound").forGetter(SoundEntry::sound))
                    .apply(builder,SoundEntry::new));

    @Nonnull
    public static ResourceLocation getRandomEntry(@Nonnull RandomSource random, @Nonnull List<SoundEntry> entries, @Nonnull ResourceLocation defaultValue)
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

    @Nonnull
    public static Builder builder() { return new Builder(); }

    @MethodsReturnNonnullByDefault
    @FieldsAreNonnullByDefault
    @ParametersAreNonnullByDefault
    public static class Builder
    {
        private final List<SoundEntry> list = new ArrayList<>();

        public Builder addVanilla(int weight,String sound) { return this.add(weight,VersionUtil.vanillaResource(sound)); }
        public Builder addModded(int weight,String modid,String sound) { return this.add(weight,VersionUtil.modResource(modid,sound)); }
        public Builder addLC(int weight,String modid,String sound) { return this.add(weight,VersionUtil.lcResource(sound)); }
        public Builder add(int weight,ResourceLocation sound) { this.list.add(new SoundEntry(weight,sound)); return this; }

        public List<SoundEntry> build() { return ImmutableList.copyOf(this.list); }

    }

}