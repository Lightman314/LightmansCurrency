package io.github.lightman314.lightmanscurrency.discord.events;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.events.TradeEvent.PostTradeEvent;
import net.minecraftforge.eventbus.api.Event;

public class DiscordPostTradeEvent extends Event{
	
	public final PostTradeEvent event;
	private final Consumer<String> addPendingMessage;
	
	public DiscordPostTradeEvent(PostTradeEvent event, Consumer<String> addPendingMessage)
	{
		this.event = event;
		this.addPendingMessage = addPendingMessage;
	}
	
	public void addPendingMessage(String message)
	{
		this.addPendingMessage.accept(message);
	}
	
}
