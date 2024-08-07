package io.github.lightman314.lightmanscurrency.api.money.coins.atm.data;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.money.coins.data.ChainData;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.HolderLookup;
import net.minecraft.util.GsonHelper;

import javax.annotation.Nonnull;

public class ATMData {

	public final ChainData chain;

	private final List<ATMExchangeButtonData> exchangeButtons;
	public final List<ATMExchangeButtonData> getExchangeButtons() { return this.exchangeButtons; }
	
	private ATMData(@Nonnull JsonObject jsonData, @Nonnull ChainData chain, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException {
		
		//LightmansCurrency.LogInfo("Loading ATM Data from json:\n" + FileUtil.GSON.toJson(jsonData));

		this.chain = chain;
		
		List<ATMExchangeButtonData> temp = new ArrayList<>();
		JsonArray exchangeButtonDataList = GsonHelper.getAsJsonArray(jsonData,"ConversionButtons", GsonHelper.getAsJsonArray(jsonData,"ExchangeButtons"));
		for(int i = 0; i < exchangeButtonDataList.size(); ++i)
		{
			try { temp.add(ATMExchangeButtonData.parse(exchangeButtonDataList.get(i).getAsJsonObject(),lookup));
			} catch(JsonSyntaxException | ResourceLocationException e) { LightmansCurrency.LogError("Error parsing Exchange Button #" + (i + 1) + ".", e); }
		}
		this.exchangeButtons = ImmutableList.copyOf(temp);
	}
	
	private ATMData(@Nonnull List<ATMExchangeButtonData> exchangeButtons, @Nonnull ChainData chain) {
		this.exchangeButtons = ImmutableList.copyOf(exchangeButtons);
		this.chain = chain;
	}

	@Nonnull
	public JsonObject save(@Nonnull HolderLookup.Provider lookup) {
		JsonObject data = new JsonObject();
		
		JsonArray exchangeButtonDataList = new JsonArray();
		for (ATMExchangeButtonData exchangeButton : this.exchangeButtons)
			exchangeButtonDataList.add(exchangeButton.save(lookup));
		data.add("ExchangeButtons", exchangeButtonDataList);
		
		return data;
	}

	public static ATMData parse(@Nonnull JsonObject json, @Nonnull ChainData chain, @Nonnull HolderLookup.Provider lookup) throws JsonSyntaxException, ResourceLocationException { return new ATMData(json, chain,lookup); }

	public static Builder builder(ChainData.Builder parent) { return new Builder(parent); }

	public static final class Builder
	{

		private final ChainData.Builder parent;

		private final List<ATMExchangeButtonData> exchangeButtons = new ArrayList<>();

		private Builder(@Nonnull ChainData.Builder parent) { this.parent = parent; }

		public ChainData.Builder back() { return this.parent; }

		public List<ATMExchangeButtonData> viewExchangeButtons() { return ImmutableList.copyOf(this.exchangeButtons); }

		public Builder addButton(@Nonnull ATMExchangeButtonData button) { this.exchangeButtons.add(button); return this; }
		public Builder addButtons(@Nonnull List<ATMExchangeButtonData> list) { this.exchangeButtons.addAll(list); return this; }


		public Builder removeButton(int index) { this.exchangeButtons.remove(index); return this; }

		public Builder accept(@Nonnull Consumer<Builder> consumer) { consumer.accept(this); return this; }

		public ATMData build(@Nonnull ChainData chain) { return new ATMData(this.exchangeButtons, chain); }

	}
	
}
