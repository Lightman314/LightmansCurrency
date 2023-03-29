package io.github.lightman314.lightmanscurrency.discord.events;

import java.util.function.Consumer;

import io.github.lightman314.lightmanscurrency.common.events.TradeEvent.PostTradeEvent;
import net.minecraftforge.eventbus.api.Event;

/**
 * @deprecated Notifications are now automatically sent from any LC Notification sent to a player.
 * This event will no longer be posted.
 */
@Deprecated
public class DiscordPostTradeEvent extends Event{
	
	public final PostTradeEvent event;
	private final Consumer<String> addPendingMessage;
	
	public DiscordPostTradeEvent(PostTradeEvent event, Consumer<String> addPendingMessage)
	{
		this.event = event;
		this.addPendingMessage = addPendingMessage;
	}
	
	public void addPendingMessage(String message) { this.addPendingMessage.accept(message); }
	
}
