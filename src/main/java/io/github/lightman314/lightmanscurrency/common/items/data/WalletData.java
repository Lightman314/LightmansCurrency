package io.github.lightman314.lightmanscurrency.common.items.data;

import com.google.common.collect.ImmutableList;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.common.items.WalletItem;
import io.github.lightman314.lightmanscurrency.util.ItemHandlerUtil;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.List;

public record WalletData(ImmutableInventory items, boolean autoExchange, int bonusSlots) {

    public static final WalletData EMPTY = new WalletData(ImmutableInventory.EMPTY,false,0);

    public WalletInventory inventory() { return this.items.makeMutable(WalletInventory::new); }

    public int getBonusSlots(int upgradeLimit) { return LCConfig.SERVER.walletCapacityUpgradeable.get() ? MathUtil.clamp(this.bonusSlots,0,upgradeLimit) : 0; }

    public static WalletData createFor(ItemStack wallet) { return new WalletData(ImmutableInventory.ofSize(WalletItem.InventorySize(wallet)),true,0); }

    private static final Codec<WalletData> OLD_CODEC = RecordCodecBuilder.create(builder ->
            builder.group(ImmutableInventory.CODEC.fieldOf("Items").forGetter(WalletData::items),
                    Codec.BOOL.fieldOf("AutoExchange").forGetter(WalletData::autoExchange),
                    Codec.INT.fieldOf("BonusSlots").orElse(0).forGetter(WalletData::bonusSlots)
            ).apply(builder,WalletData::new)
    );

    public static final Codec<WalletData> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(builder ->
            builder.group(ImmutableInventory.CODEC.fieldOf("items").forGetter(WalletData::items),
                    Codec.BOOL.fieldOf("auto_exchange").forGetter(WalletData::autoExchange),
                    Codec.INT.optionalFieldOf("bonus_slots",0).forGetter(WalletData::bonusSlots)
            ).apply(builder,WalletData::new))
            ,OLD_CODEC);


    public static final StreamCodec<RegistryFriendlyByteBuf,WalletData> STREAM_CODEC = StreamCodec.composite(
            ImmutableInventory.STREAM_CODEC,WalletData::items,
            ByteBufCodecs.BOOL,WalletData::autoExchange,
            ByteBufCodecs.INT,WalletData::bonusSlots,
            WalletData::new);

    public WalletData withItems(List<ItemStack> items) { return new WalletData(ImmutableInventory.ofList(items),this.autoExchange,this.bonusSlots); }
    public WalletData withItems(IItemHandler items) { return withItems(ItemHandlerUtil.toList(items)); }
    public WalletData withAutoExchange(boolean autoExchange) { return new WalletData(this.items, autoExchange,this.bonusSlots); }
    public WalletData withBonusSlots(int bonusSlots) { return new WalletData(this.items, this.autoExchange,bonusSlots); }
    public WalletData withAddedBonusSlots(int addedBonusSlots) { return new WalletData(this.items, this.autoExchange,this.bonusSlots + addedBonusSlots); }

    private static List<ItemStack> initList(int size) { return ImmutableList.copyOf(NonNullList.withSize(size,ItemStack.EMPTY)); }

}
