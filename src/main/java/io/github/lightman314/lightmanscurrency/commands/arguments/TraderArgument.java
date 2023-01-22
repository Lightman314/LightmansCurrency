package io.github.lightman314.lightmanscurrency.commands.arguments;

import java.util.List;
import java.util.concurrent.CompletableFuture;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;

import io.github.lightman314.lightmanscurrency.common.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.traders.TraderSaveData;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentSerializer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.TranslatableComponent;
import org.jetbrains.annotations.NotNull;

public class TraderArgument implements ArgumentType<TraderData>{

	private static final SimpleCommandExceptionType ERROR_NOT_FOUND = new SimpleCommandExceptionType(new TranslatableComponent("command.argument.trader.notfound"));
	
	private final boolean acceptPersistentIDs;
	private TraderArgument(boolean acceptPersistentIDs) { this.acceptPersistentIDs = acceptPersistentIDs; }
	
	public static TraderArgument trader() { return new TraderArgument(false); }
	public static TraderArgument traderWithPersistent() { return new TraderArgument(true); }
	
	public static TraderData getTrader(CommandContext<CommandSourceStack> commandContext, String name) {
		return commandContext.getArgument(name, TraderData.class);
	}
	
	@Override
	public TraderData parse(StringReader reader) throws CommandSyntaxException {
		String traderID = reader.readUnquotedString();
		if(isNumerical(traderID))
		{
			try {
				long id = Long.parseLong(traderID);
				if(id >= 0)
				{
					TraderData t = TraderSaveData.GetTrader(false, id);
					if(t != null)
						return t;
				}
			} catch(Throwable ignored) {}
		}
		if(this.acceptPersistentIDs)
		{
			List<TraderData> allTraders = TraderSaveData.GetAllTraders(false);
			for(TraderData t : allTraders)
			{
				if(t.isPersistent() && t.getPersistentID().equals(traderID))
					return t;
			}
		}
		throw ERROR_NOT_FOUND.createWithContext(reader);
	}
	
	private static boolean isNumerical(String string) {
		for(int i = 0; i < string.length(); ++i)
		{
			char c = string.charAt(i);
			if(c < '0' || c > '9')
				return false;
		}
		return true;
	}
	
	public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> commandContext, SuggestionsBuilder suggestionsBuilder) {
		List<TraderData> allTraders = TraderSaveData.GetAllTraders(false);
		for(TraderData t : allTraders)
		{
			//LightmansCurrency.LogDebug("Recommending trader ID '" + t.getID() + "'");
			suggestionsBuilder.suggest(String.valueOf(t.getID()));
			if(this.acceptPersistentIDs && t.isPersistent())
			{
				//LightmansCurrency.LogDebug("Recommending persistent ID '" + t.getPersistentID() + "'");
				suggestionsBuilder.suggest(t.getPersistentID());
			}
		}
		return suggestionsBuilder.buildFuture();
	}
	
	public static class Info implements ArgumentSerializer<TraderArgument>
	{

		@Override
		public void serializeToNetwork(TraderArgument argument, FriendlyByteBuf buffer) { buffer.writeBoolean(argument.acceptPersistentIDs); }

		@Override
		public @NotNull TraderArgument deserializeFromNetwork(FriendlyByteBuf buffer) { return new TraderArgument(buffer.readBoolean()); }

		@Override
		public void serializeToJson(TraderArgument argument, JsonObject json) { json.addProperty("acceptPersistentIDs", argument.acceptPersistentIDs); }
		
	}

}