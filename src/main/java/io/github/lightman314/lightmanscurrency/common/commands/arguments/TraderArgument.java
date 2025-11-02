package io.github.lightman314.lightmanscurrency.common.commands.arguments;

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

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.traders.TraderAPI;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.common.data.types.TraderDataCache;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nonnull;

public class TraderArgument implements ArgumentType<TraderData>{

	public static final SimpleCommandExceptionType ERROR_NOT_FOUND = new SimpleCommandExceptionType(LCText.ARGUMENT_TRADER_NOT_FOUND.get());
	public static final SimpleCommandExceptionType ERROR_NOT_RECOVERABLE = new SimpleCommandExceptionType(LCText.ARGUMENT_TRADER_NOT_RECOVERABLE.get());
	private final boolean acceptPersistentIDs;
	private final boolean onlyRecoverableTraders;
	private TraderArgument(boolean acceptPersistentIDs, boolean onlyRecoverableTraders) { this.acceptPersistentIDs = acceptPersistentIDs; this.onlyRecoverableTraders = onlyRecoverableTraders; }

	public static TraderArgument trader() { return new TraderArgument(false, false); }
	public static TraderArgument recoverableTrader() { return new TraderArgument(false, true); }
	public static TraderArgument traderWithPersistent() { return new TraderArgument(true, false); }
	
	public static TraderData getTrader(CommandContext<CommandSourceStack> commandContext, String name) throws CommandSyntaxException{
		return commandContext.getArgument(name, TraderData.class);
	}
	
	@Override
	public TraderData parse(StringReader reader) throws CommandSyntaxException {
		String traderID = reader.readUnquotedString();
		TraderDataCache data = TraderDataCache.TYPE.get(false);
		if(isNumerical(traderID))
		{
			try {
				long id = Long.parseLong(traderID);
				if(id >= 0)
				{
					TraderData t = data.getTrader(id);
					if(t != null)
					{
						if(this.onlyRecoverableTraders && !t.isRecoverable())
							throw ERROR_NOT_RECOVERABLE.createWithContext(reader);
						return t;
					}
				}
			} catch(Throwable error) { if(error instanceof CommandSyntaxException e) throw e; }
		}
		if(this.acceptPersistentIDs)
		{
			TraderData t = data.getTrader(traderID);
			if(t != null)
				return t;
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
		List<TraderData> allTraders = TraderAPI.getApi().GetAllTraders(false);
		for(TraderData t : allTraders)
		{
			if(!this.onlyRecoverableTraders || t.isRecoverable())
			{
				//LightmansCurrency.LogDebug("Recommending trader ID '" + t.getID() + "'");
				suggestionsBuilder.suggest(String.valueOf(t.getID()));
				if(this.acceptPersistentIDs && t.isPersistent())
				{
					//LightmansCurrency.LogDebug("Recommending persistent ID '" + t.getPersistentID() + "'");
					suggestionsBuilder.suggest(t.getPersistentID());
				}
			}
		}
		return suggestionsBuilder.buildFuture();
	}
	
	public static class Info implements ArgumentTypeInfo<TraderArgument,TraderArgument.Info.Template>
	{
		
		@Override
		public void serializeToNetwork(Template template, FriendlyByteBuf buffer) { buffer.writeBoolean(template.acceptPersistentIDs); buffer.writeBoolean(template.onlyRecoverableTraders); }

		@Override
		@Nonnull
		public Template deserializeFromNetwork(FriendlyByteBuf buffer) { return new Template(buffer.readBoolean(),buffer.readBoolean()); }

		@Override
		public void serializeToJson(Template template, JsonObject json) { json.addProperty("acceptPersistentIDs", template.acceptPersistentIDs); json.addProperty("onlyRecoverableTraders", template.onlyRecoverableTraders); }

		@Override
		@Nonnull
		public Template unpack(TraderArgument argument) { return new Template(argument.acceptPersistentIDs,argument.onlyRecoverableTraders); }
		
		public final class Template implements ArgumentTypeInfo.Template<TraderArgument>
		{
			final boolean acceptPersistentIDs;
			final boolean onlyRecoverableTraders;

			Template(boolean checkPersistentIDs, boolean onlyRecoverableTraders) { this.acceptPersistentIDs = checkPersistentIDs; this.onlyRecoverableTraders = onlyRecoverableTraders; }

			@Override
			@Nonnull
			public TraderArgument instantiate(@Nonnull CommandBuildContext context) { return new TraderArgument(this.acceptPersistentIDs,this.onlyRecoverableTraders); }

			@Override
			@Nonnull
			public ArgumentTypeInfo<TraderArgument, ?> type() { return Info.this; }
			
		}
		
	}

}
