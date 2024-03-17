package io.github.lightman314.lightmanscurrency.datagen.common.advancements;

import com.google.common.collect.ImmutableList;
import net.minecraft.advancements.Advancement;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.advancements.AdvancementProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;

public class LCAdvancementProvider extends AdvancementProvider {

    private final List<Consumer<Consumer<Advancement>>> tabs = ImmutableList.of(new LCCurrencyAdvancements());

    public LCAdvancementProvider(DataGenerator generator, ExistingFileHelper existingFileHelper) {
        super(generator, existingFileHelper);
    }

    @Override
    protected void registerAdvancements(@Nonnull Consumer<Advancement> consumer, @Nonnull ExistingFileHelper fileHelper) {
        for(Consumer<Consumer<Advancement>> tab : tabs)
            tab.accept(consumer);
    }
}