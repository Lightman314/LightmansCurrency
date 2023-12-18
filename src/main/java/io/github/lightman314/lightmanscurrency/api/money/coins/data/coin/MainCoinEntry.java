package io.github.lightman314.lightmanscurrency.api.money.coins.data.coin;

import com.google.gson.JsonObject;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;

import javax.annotation.Nonnull;

public class MainCoinEntry extends CoinEntry {

    private final int exchangeRate;

    @Override
    public final int getExchangeRate() { return this.exchangeRate; }

    public MainCoinEntry(@Nonnull Item coin, int exchangeRate) { super(coin); this.exchangeRate = exchangeRate; }
    protected MainCoinEntry(@Nonnull Item coin, int exchangeRate, boolean sideChain) { super(coin, sideChain); this.exchangeRate = exchangeRate; }

    @Override
    protected void writeAdditional(@Nonnull JsonObject json) { json.addProperty("exchangeRate", this.exchangeRate); }

    public static CoinEntry parseMain(@Nonnull JsonObject json) { return parseMain(json, false); }
    public static CoinEntry parseMain(@Nonnull JsonObject json, boolean hidden)
    {
        Item coin = parseBase(json);
        int exchangeRate = GsonHelper.getAsInt(json, "exchangeRate");
        return new MainCoinEntry(coin, exchangeRate, hidden);
    }

}
