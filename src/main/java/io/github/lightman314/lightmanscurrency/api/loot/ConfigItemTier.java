package io.github.lightman314.lightmanscurrency.api.loot;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.api.config.options.builtin.ItemOption;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;

public enum ConfigItemTier {
    T1(LCConfig.COMMON.lootItem1),
    T2(LCConfig.COMMON.lootItem2),
    T3(LCConfig.COMMON.lootItem3),
    T4(LCConfig.COMMON.lootItem4),
    T5(LCConfig.COMMON.lootItem5),
    T6(LCConfig.COMMON.lootItem6);

    public static final Codec<ConfigItemTier> CODEC = EnumHelper.buildCodec(ConfigItemTier.class,"Config Item Tier");
    public static final StreamCodec<ByteBuf,ConfigItemTier> STREAM_CODEC = EnumHelper.buildStreamCodec(ConfigItemTier.class,"Config Item Tier");

    private final ItemOption item;
    public Item getItem() { return this.item.get(); }
    ConfigItemTier(ItemOption item) { this.item = item; }

}