package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.Objects;

public record WalletData(List<ItemStack> items, boolean autoExchange) {

    public static final WalletData EMPTY = new WalletData(ImmutableList.of(),false);

    public static WalletData createFor(@Nonnull WalletItem item) { return new WalletData(initList(WalletItem.InventorySize(item)),true); }

    public static final Codec<WalletData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(ItemStack.OPTIONAL_CODEC.listOf().fieldOf("Items").forGetter(d -> d.items),
                    Codec.BOOL.fieldOf("AutoExchange").forGetter(d -> d.autoExchange)
                    ).apply(builder,WalletData::new)
    );

    public WalletData withItems(@Nonnull List<ItemStack> items) { return new WalletData(ImmutableList.copyOf(InventoryUtil.copyList(items)),this.autoExchange); }
    public WalletData withItems(@Nonnull Container items) { return new WalletData(ImmutableList.copyOf(InventoryUtil.buildList(items)),this.autoExchange); }
    public WalletData withAutoExchange(boolean autoExchange) { return new WalletData(this.items, autoExchange); }

    private static List<ItemStack> initList(int size) { return ImmutableList.copyOf(NonNullList.withSize(size,ItemStack.EMPTY)); }

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
