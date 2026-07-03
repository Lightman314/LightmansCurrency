package io.github.lightman314.lightmanscurrency.features.wallet;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCTags;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.RegistryCodecs;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Range;

public record WalletUpgradeData(HolderSet<Item> upgradeItems,@Range(from = 1,to = WalletItem.MAX_WALLET_SLOTS - 1) int bonusSlots,@Range(from = 1,to = WalletItem.MAX_WALLET_SLOTS - 1) int maxUpgrades) {

    public static final WalletUpgradeData DEFAULT = new WalletUpgradeData(LCTags.Items.WALLET_UPGRADE_MATERIAL,6,4);

    public WalletUpgradeData(TagKey<Item> upgradeTag,int bonusSlots,int maxUpgrades) {
        HolderGetter<Item> registrationLookup = BuiltInRegistries.acquireBootstrapRegistrationLookup(BuiltInRegistries.ITEM);
        this(registrationLookup.getOrThrow(upgradeTag),bonusSlots,maxUpgrades);
    }

    public static final Codec<WalletUpgradeData> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            RegistryCodecs.homogeneousList(Registries.ITEM).fieldOf("upgradeItems").forGetter(WalletUpgradeData::upgradeItems),
            Codec.intRange(1,WalletItem.MAX_WALLET_SLOTS - 1).fieldOf("bonusSlots").forGetter(WalletUpgradeData::bonusSlots),
            Codec.intRange(1,WalletItem.MAX_WALLET_SLOTS - 1).fieldOf("maxUpgrades").forGetter(WalletUpgradeData::maxUpgrades)
    ).apply(builder,WalletUpgradeData::new));

    public static final StreamCodec<RegistryFriendlyByteBuf,WalletUpgradeData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderSet(Registries.ITEM),WalletUpgradeData::upgradeItems,
            ByteBufCodecs.INT,WalletUpgradeData::bonusSlots,
            ByteBufCodecs.INT,WalletUpgradeData::maxUpgrades,
            WalletUpgradeData::new);

}