package io.github.lightman314.lightmanscurrency.common.items.data;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.include.com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public record WalletData(List<ItemStack> items, boolean autoExchange) {

    public static final WalletData EMPTY = new WalletData(ImmutableList.of(),false);

    public static WalletData createFor(@Nonnull WalletItem item) { return new WalletData(NonNullList.withSize(WalletItem.InventorySize(item),ItemStack.EMPTY),true); }

    public static final Codec<WalletData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(ItemStack.OPTIONAL_CODEC.listOf().fieldOf("Items").forGetter(d -> d.items),
                    Codec.BOOL.fieldOf("AutoExchange").forGetter(d -> d.autoExchange)
                    ).apply(builder,WalletData::new)
    );

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WalletData other) {
            return this.items.equals(other.items) && this.autoExchange == other.autoExchange;
        }
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.items, this.autoExchange); }

}
