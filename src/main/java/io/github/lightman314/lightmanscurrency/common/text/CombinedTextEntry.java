package io.github.lightman314.lightmanscurrency.common.text;

import com.google.common.collect.ImmutableList;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;

public final class CombinedTextEntry {

    private final List<Supplier<String>> keys;
    public void forEachKey(@Nonnull Consumer<String> consumer) { this.keys.forEach(s -> consumer.accept(s.get())); }
    public CombinedTextEntry(@Nonnull List<Supplier<String>> keys) { this.keys = ImmutableList.copyOf(keys); }

    @SafeVarargs
    public static CombinedTextEntry items(@Nonnull Supplier<? extends ItemLike>... items) {
        List<Supplier<String>> list = new ArrayList<>();
        for(var item : items)
            list.add(() -> item.get().asItem().getDescriptionId());
        return new CombinedTextEntry(list);
    }

}
