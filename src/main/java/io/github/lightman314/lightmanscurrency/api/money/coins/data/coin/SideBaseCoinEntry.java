package io.github.lightman314.lightmanscurrency.api.money.coins.data.coin;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.util.List;

public class SideBaseCoinEntry extends MainCoinEntry {

    public final CoinEntry parentCoin;

    public SideBaseCoinEntry(@Nonnull Item coin, @Nonnull CoinEntry parentCoin, int exchangeRate) {
        super(coin, exchangeRate, true);
        this.parentCoin = parentCoin;
    }

    @Override
    protected void writeAdditional(@Nonnull JsonObject json) {
        super.writeAdditional(json);
        json.addProperty("ParentCoin", ForgeRegistries.ITEMS.getKey(this.parentCoin.getCoin()).toString());
    }

    public static CoinEntry parseSub(@Nonnull JsonObject json, @Nonnull List<CoinEntry> coreChain)
    {
        Item coin = parseBase(json);
        int exchangeRate = GsonHelper.getAsInt(json, "exchangeRate");
        ResourceLocation itemID = VersionUtil.parseResource(GsonHelper.getAsString(json, "ParentCoin"));
        Item parentCoin = ForgeRegistries.ITEMS.getValue(itemID);
        if(parentCoin == null || parentCoin == Items.AIR)
            throw new JsonSyntaxException(itemID + " is not a valid item!");
        CoinEntry parentEntry = null;
        for(int i = 0; parentEntry == null && i < coreChain.size(); ++i)
        {
            CoinEntry e = coreChain.get(i);
            if(e.matches(parentCoin))
                parentEntry = e;
        }
        if(parentEntry == null)
            throw new JsonSyntaxException(itemID + " does not match any coins in the core chain!");
        return new SideBaseCoinEntry(coin, parentEntry, exchangeRate);
    }
}
