package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.InventoryUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public record WalletData(List<ItemStack> items, boolean autoExchange, int bonusSlots) {

    public static final WalletData EMPTY = new WalletData(ImmutableList.of(),false,0);

    public int getBonusSlots(int upgradeLimit) { return LCConfig.SERVER.walletCapacityUpgradeable.get() ? MathUtil.clamp(this.bonusSlots,0,upgradeLimit) : 0; }

    public static WalletData createFor(@Nonnull ItemStack wallet) { return new WalletData(initList(WalletItem.InventorySize(wallet)),true,0); }

    public static final Codec<WalletData> CODEC = RecordCodecBuilder.create(builder ->
            builder.group(ItemStack.OPTIONAL_CODEC.listOf().fieldOf("Items").forGetter(d -> d.items),
                    Codec.BOOL.fieldOf("AutoExchange").forGetter(d -> d.autoExchange),
                    Codec.INT.fieldOf("BonusSlots").orElse(0).forGetter(d -> d.bonusSlots)
                    ).apply(builder,WalletData::new)
    );

    public static final StreamCodec<RegistryFriendlyByteBuf,WalletData> STREAM_CODEC = StreamCodec.of(
            (b,d) -> {
                b.writeInt(d.items.size());
                for(ItemStack i : d.items)
                    ItemStack.OPTIONAL_STREAM_CODEC.encode(b,i);
                b.writeBoolean(d.autoExchange);
                b.writeInt(d.bonusSlots);
            },
            (b) -> {
                int itemCount = b.readInt();
                List<ItemStack> list = new ArrayList<>();
                for(int i = 0; i < itemCount; ++i)
                    list.add(ItemStack.OPTIONAL_STREAM_CODEC.decode(b));
                return new WalletData(ImmutableList.copyOf(list),b.readBoolean(),b.readInt());
            }
    );

    public WalletData withItems(@Nonnull List<ItemStack> items) { return new WalletData(ImmutableList.copyOf(InventoryUtil.copyList(items)),this.autoExchange,this.bonusSlots); }
    public WalletData withItems(@Nonnull Container items) { return new WalletData(ImmutableList.copyOf(InventoryUtil.buildList(items)),this.autoExchange,this.bonusSlots); }
    public WalletData withAutoExchange(boolean autoExchange) { return new WalletData(this.items, autoExchange,this.bonusSlots); }
    public WalletData withBonusSlots(int bonusSlots) { return new WalletData(this.items, this.autoExchange,bonusSlots); }
    public WalletData withAddedBonusSlots(int addedBonusSlots) { return new WalletData(this.items, this.autoExchange,this.bonusSlots + addedBonusSlots); }

    private static List<ItemStack> initList(int size) { return ImmutableList.copyOf(NonNullList.withSize(size,ItemStack.EMPTY)); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof WalletData other)
            return InventoryUtil.ContainerMatches(this.items,other.items) && this.autoExchange == other.autoExchange && this.bonusSlots == other.bonusSlots;
        return false;
    }

    @Override
    public int hashCode() { return Objects.hash(this.items, this.autoExchange, this.bonusSlots); }

}
