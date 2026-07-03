package io.github.lightman314.lightmanscurrency.api.command.arguments;

import com.google.gson.JsonObject;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.NumberHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.interfaces.ISidedContext;
import io.github.lightman314.lightmanscurrency.api.text.LCText;
import io.github.lightman314.lightmanscurrency.api.trader.data.TraderData;
import io.github.lightman314.lightmanscurrency.features.api_impl.data.TraderDataCache;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.synchronization.ArgumentTypeInfo;
import net.minecraft.network.FriendlyByteBuf;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class TraderArgument implements ArgumentType<TraderData> {

    public static final SimpleCommandExceptionType ERROR_NOT_FOUND = new SimpleCommandExceptionType(LCText.Commands.ARGUMENT_TRADER_NOT_FOUND.get());
    public static final SimpleCommandExceptionType ERROR_NOT_RECOVERABLE = new SimpleCommandExceptionType(LCText.Commands.ARGUMENT_TRADER_NOT_RECOVERABLE.get());

    private final ValidTraders filter;
    private TraderArgument(ValidTraders filter) { this.filter = filter; }

    public static TraderData getTrader(CommandContext<CommandSourceStack> commandContext,String name) throws CommandSyntaxException {
        return commandContext.getArgument(name,TraderData.class);
    }

    public static TraderArgument trader() { return new TraderArgument(ValidTraders.NORMAL); }
    public static TraderArgument recoverableTrader() { return new TraderArgument(ValidTraders.RECOVERABLE); }
    public static TraderArgument traderWithPersistent() { return new TraderArgument(ValidTraders.NORMAL_OR_PERSISTENT); }

    @Override
    public TraderData parse(StringReader reader) throws CommandSyntaxException {
        String traderID = reader.readUnquotedString();
        TraderDataCache data = TraderDataCache.TYPE.get(ISidedContext.LOGICAL_SERVER);
        if(NumberHelper.isLong(traderID))
        {
            try {
                long id = Long.parseLong(traderID);
                if(id >= 0)
                {
                    TraderData t = data.getTrader(id);
                    if(t != null)
                    {
                        if(this.filter.onlyRecoverableTraders() && !t.getState().allowRecovery)
                            throw ERROR_NOT_RECOVERABLE.createWithContext(reader);
                        return t;
                    }
                }
            } catch (Throwable error) { if(error instanceof CommandSyntaxException e) throw e; }
        }
        if(this.filter.allowPersistentIDs())
        {
            //TODO look up trader by persistent trader id
            //TraderData t = data.getTrader(traderID);
        }
        throw ERROR_NOT_FOUND.createWithContext(reader);
    }

    @Override
    public <S> CompletableFuture<Suggestions> listSuggestions(CommandContext<S> context, SuggestionsBuilder builder) {
        List<TraderData> allTraders = LCApi.getTraderAPI().getAllTraders(ISidedContext.LOGICAL_SERVER);
        for(TraderData t : allTraders)
        {
            if(!this.filter.onlyRecoverableTraders() || t.getState().allowRecovery)
            {
                this.trySuggest(builder,String.valueOf(t.getID()));
                if(this.filter.allowPersistentIDs())
                {
                    //TODO get persistent node suggest that as well
                }
            }
        }
        return builder.buildFuture();
    }

    protected final void trySuggest(SuggestionsBuilder builder,String string) {
        String remaining = builder.getRemaining();
        if(string.startsWith(remaining))
            builder.suggest(string);
    }

    @Override
    public Collection<String> getExamples() { return List.of("0","5","example_id"); }

    private enum ValidTraders {
        NORMAL,
        NORMAL_OR_PERSISTENT,
        RECOVERABLE;
        public boolean allowPersistentIDs() { return this == NORMAL_OR_PERSISTENT; }
        public boolean onlyRecoverableTraders() { return this == RECOVERABLE; }
    }

    public static class Info implements ArgumentTypeInfo<TraderArgument,TraderArgument.Info.Template>
    {
        @Override
        public void serializeToNetwork(Template template,FriendlyByteBuf out) { out.writeInt(template.filter.ordinal()); }
        @Override
        public Template deserializeFromNetwork(FriendlyByteBuf in) { return new Template(EnumHelper.enumFromOrdinal(in.readInt(),ValidTraders.values(),ValidTraders.NORMAL)); }
        @Override
        public void serializeToJson(Template template,JsonObject out) { out.addProperty("filter",template.filter.toString()); }
        @Override
        public Template unpack(TraderArgument argument) { return new Template(argument.filter); }

        public final class Template implements ArgumentTypeInfo.Template<TraderArgument>
        {
            private final ValidTraders filter;
            Template(ValidTraders filter) { this.filter = filter; }
            @Override
            public TraderArgument instantiate(CommandBuildContext context) {
                return new TraderArgument(this.filter);
            }

            @Override
            public ArgumentTypeInfo<TraderArgument, ?> type() { return Info.this; }
        }

    }

}