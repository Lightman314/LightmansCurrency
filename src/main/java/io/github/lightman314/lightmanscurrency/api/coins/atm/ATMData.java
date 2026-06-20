package io.github.lightman314.lightmanscurrency.api.coins.atm;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.helpers.data.DataContext;
import io.github.lightman314.lightmanscurrency.api.coins.data.ChainData;
import net.minecraft.IdentifierException;
import net.minecraft.util.GsonHelper;

public class ATMData {

    public final ChainData chain;

    private final List<ATMExchangeButtonData> exchangeButtons;
    public final List<ATMExchangeButtonData> getExchangeButtons() { return this.exchangeButtons; }

    private ATMData(JsonObject jsonData,ChainData chain,DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException {

        //LightmansCurrency.LogInfo("Loading ATM Data from json:\n" + FileUtil.GSON.toJson(jsonData));

        this.chain = chain;

        List<ATMExchangeButtonData> temp = new ArrayList<>();
        String key = jsonData.has("ConversionButtons") ? "ConversionButtons" : "ExchangeButtons";
        JsonArray exchangeButtonDataList = GsonHelper.getAsJsonArray(jsonData,"ConversionButtons", GsonHelper.getAsJsonArray(jsonData,"ExchangeButtons"));
        for(int i = 0; i < exchangeButtonDataList.size(); ++i)
        {
            try { temp.add(ATMExchangeButtonData.parse(GsonHelper.convertToJsonObject(exchangeButtonDataList.get(i),key + "[" + i + "]"),context));
            } catch(JsonSyntaxException | IdentifierException e) { LightmansCurrency.LogError("Error parsing Exchange Button #" + (i + 1) + ".", e); }
        }
        this.exchangeButtons = ImmutableList.copyOf(temp);
    }

    private ATMData(List<ATMExchangeButtonData> exchangeButtons, ChainData chain) {
        this.exchangeButtons = ImmutableList.copyOf(exchangeButtons);
        this.chain = chain;
    }

    public JsonObject save(DataContext<JsonElement> context) {
        JsonObject data = new JsonObject();

        JsonArray exchangeButtonDataList = new JsonArray();
        for (ATMExchangeButtonData exchangeButton : this.exchangeButtons)
            exchangeButtonDataList.add(exchangeButton.save(context));
        data.add("ExchangeButtons", exchangeButtonDataList);

        return data;
    }

    public static ATMData parse(JsonObject json,ChainData chain,DataContext<JsonElement> context) throws JsonSyntaxException, IdentifierException { return new ATMData(json,chain,context); }

    public static Builder builder(ChainData.Builder parent) { return new Builder(parent); }

    public static final class Builder
    {

        private final ChainData.Builder parent;

        private final List<ATMExchangeButtonData> exchangeButtons = new ArrayList<>();

        private Builder(ChainData.Builder parent) { this.parent = parent; }

        public ChainData.Builder back() { return this.parent; }

        public List<ATMExchangeButtonData> viewExchangeButtons() { return ImmutableList.copyOf(this.exchangeButtons); }

        public Builder addButton(ATMExchangeButtonData button) { this.exchangeButtons.add(button); return this; }
        public Builder addButtons(List<ATMExchangeButtonData> list) { this.exchangeButtons.addAll(list); return this; }

        public Builder removeButton(int index) { this.exchangeButtons.remove(index); return this; }

        public Builder accept(Consumer<Builder> consumer) { consumer.accept(this); return this; }

        public ATMData build(ChainData chain) { return new ATMData(this.exchangeButtons, chain); }

    }

}