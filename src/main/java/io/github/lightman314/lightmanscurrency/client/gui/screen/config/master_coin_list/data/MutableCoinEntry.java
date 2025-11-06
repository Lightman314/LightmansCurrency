package io.github.lightman314.lightmanscurrency.client.gui.screen.config.master_coin_list.data;

import io.github.lightman314.lightmanscurrency.api.money.coins.data.coin.CoinEntry;
import net.minecraft.world.item.Item;

public class MutableCoinEntry {

    public Item item;
    public int exchangeRate = 10;

    public MutableCoinEntry(Item item) { this.item = item; }
    public MutableCoinEntry(CoinEntry entry) { this.item = entry.getCoin(); this.exchangeRate = entry.getExchangeRate(); }

}