package io.github.lightman314.lightmanscurrency.common.loot;

import io.github.lightman314.lightmanscurrency.LCConfig;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public enum ConfigItemTier {

    T1(1,LCConfig.COMMON.lootItem1),
    T2(2,LCConfig.COMMON.lootItem2),
    T3(3,LCConfig.COMMON.lootItem3),
    T4(4,LCConfig.COMMON.lootItem4),
    T5(5,LCConfig.COMMON.lootItem5),
    T6(6,LCConfig.COMMON.lootItem6);

    private final Supplier<? extends ItemLike> item;
    public final int tier;
    public Item getItem() { return this.item.get().asItem(); }
    ConfigItemTier(int tier, Supplier<? extends ItemLike> item) { this.tier = tier; this.item = item; }

    @Nullable
    public static ConfigItemTier get(int tier)
    {
        return switch (tier) {
            case 1 -> T1;
            case 2 -> T2;
            case 3 -> T3;
            case 4 -> T4;
            case 5 -> T5;
            case 6 -> T6;
            default -> null;
        };
    }

}
