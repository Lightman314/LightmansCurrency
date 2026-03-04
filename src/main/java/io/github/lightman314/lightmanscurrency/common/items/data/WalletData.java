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

public record WalletData(WalletInventory inventory, boolean autoExchange, int bonusSlots) {

    public static final WalletData EMPTY = new WalletData(new WalletInventory(0),false,0);

    public int getBonusSlots(int upgradeLimit) { return LCConfig.SERVER.walletCapacityUpgradeable.get() ? MathUtil.clamp(this.bonusSlots,0,upgradeLimit) : 0; }

    public static WalletData createFor(ItemStack wallet) { return new WalletData(new WalletInventory(WalletItem.InventorySize(wallet)),true,0); }

    private static final Codec<WalletData> OLD_CODEC = RecordCodecBuilder.create(builder ->
            builder.group(WalletInventory.CODEC.fieldOf("Items").forGetter(WalletData::inventory),
                    Codec.BOOL.fieldOf("AutoExchange").forGetter(WalletData::autoExchange),
                    Codec.INT.fieldOf("BonusSlots").orElse(0).forGetter(WalletData::bonusSlots)
            ).apply(builder,WalletData::new)
    );

    public static final Codec<WalletData> CODEC = Codec.withAlternative(
            RecordCodecBuilder.create(builder ->
            builder.group(WalletInventory.CODEC.fieldOf("inventory").forGetter(WalletData::inventory),
                    Codec.BOOL.fieldOf("auto_exchange").forGetter(WalletData::autoExchange),
                    Codec.INT.optionalFieldOf("bonus_slots",0).forGetter(WalletData::bonusSlots)
            ).apply(builder,WalletData::new))
            ,OLD_CODEC);


    public static final StreamCodec<RegistryFriendlyByteBuf,WalletData> STREAM_CODEC = StreamCodec.composite(
            WalletInventory.STREAM_CODEC,WalletData::inventory,
            ByteBufCodecs.BOOL,WalletData::autoExchange,
            ByteBufCodecs.INT,WalletData::bonusSlots,
            WalletData::new);

    public WalletData withItems(List<ItemStack> items) { return new WalletData(new WalletInventory(items),this.autoExchange,this.bonusSlots); }
    public WalletData withItems(IItemHandler items) { return withItems(ItemHandlerUtil.toList(items)); }
    public WalletData withAutoExchange(boolean autoExchange) { return new WalletData(this.inventory, autoExchange,this.bonusSlots); }
    public WalletData withBonusSlots(int bonusSlots) { return new WalletData(this.inventory, this.autoExchange,bonusSlots); }
    public WalletData withAddedBonusSlots(int addedBonusSlots) { return new WalletData(this.inventory, this.autoExchange,this.bonusSlots + addedBonusSlots); }

    private static List<ItemStack> initList(int size) { return ImmutableList.copyOf(NonNullList.withSize(size,ItemStack.EMPTY)); }

}
