package io.github.lightman314.lightmanscurrency.api.money.coins.old_compat;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.ResourceLocationException;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;

/**
 * Placeholder class to temporarily hold old coin data values to update into the new system
 */
@Deprecated
public class OldCoinData {

    //Coin item
    public final Item coinItem;
    //Coins chain id
    public final String chain;
    //Value inputs
    public final Item worthOtherCoin;
    public final int worthOtherCoinCount;
    //Coin's display initial 'c','d', etc.
    public final String initialTranslation;
    //Coin's plural form
    public final String pluralTranslation;
    //Is this hidden or not
    public final boolean isHidden;


    private OldCoinData(Item coinItem, String chain, Item worthOtherCoin, int worthOtherCoinCount, String initialTranslation, String pluralTranslation, boolean hidden)
    {
        this.coinItem = coinItem;
        this.chain = chain;
        this.worthOtherCoin = worthOtherCoin;
        this.worthOtherCoinCount = worthOtherCoinCount;
        this.initialTranslation = initialTranslation;
        this.pluralTranslation = pluralTranslation;
        this.isHidden = hidden;
    }

    public static OldCoinData parse(@Nonnull JsonObject json) throws JsonSyntaxException, ResourceLocationException
    {
        //Coin Item
        Item coinItem = ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(GsonHelper.getAsString(json, "coinitem")));
        String chain = GsonHelper.getAsString(json, "chain");
        //Relative Worth
        Item otherCoin = null;
        int otherCoinCount = 0;
        if(json.has("worth"))
        {
            JsonObject worthData = json.get("worth").getAsJsonObject();
            otherCoin = ForgeRegistries.ITEMS.getValue(VersionUtil.parseResource(GsonHelper.getAsString(worthData, "coin")));
            otherCoinCount = GsonHelper.getAsInt(worthData, "count");
        }
        //Initial
        String initial = null;
        if(json.has("initial"))
            initial = GsonHelper.getAsString(json, "initial");
        //Plural
        String plural = null;
        if(json.has("plural"))
            plural = GsonHelper.getAsString(json, "plural");
        //Hidden
        boolean hidden = json.has("hidden") && GsonHelper.getAsBoolean(json, "hidden");
        return new OldCoinData(coinItem, chain, otherCoin, otherCoinCount, initial, plural, hidden);
    }

}
