package io.github.lightman314.lightmanscurrency.common.traders.item;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.BookRestriction;
import io.github.lightman314.lightmanscurrency.common.traders.item.tradedata.restrictions.ItemTradeRestriction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

public class ItemTraderDataBook extends ItemTraderData {

    public static final TraderType<ItemTraderDataBook> TYPE = new TraderType<>(ResourceLocation.fromNamespaceAndPath(LightmansCurrency.MODID, "item_trader_book"),ItemTraderDataBook::new);

    private ItemTraderDataBook() { super(TYPE); }
    public ItemTraderDataBook(int tradeCount, Level level, BlockPos pos) { super(TYPE, tradeCount, level, pos); }

    @Override
    protected ItemTradeRestriction getTradeRestriction(int tradeIndex) { return BookRestriction.INSTANCE; }

}
