package io.github.lightman314.lightmanscurrency.events;

import java.util.UUID;

import io.github.lightman314.lightmanscurrency.common.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.notifications.NotificationData;
import net.minecraftforge.eventbus.api.Event;

public class NotificationEvent extends Event {

	private final UUID playerID;
	public UUID getPlayerID() { return this.playerID; }
	private final NotificationData data;
	public NotificationData getData() { return this.data; }
	protected Notification notification;
	public Notification getNotification() { return this.notification; }
	
	public NotificationEvent(UUID playerID, NotificationData data, Notification notification) {
		this.playerID = playerID;
		this.data = data;
		this.notification = notification;
	}
	
	/**
	 * Events sent when a notification is sent to a player.
	 * Only run server-side.
	 */
	public static class NotificationSent extends NotificationEvent {

		protected NotificationSent(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }
		
		/**
		 * Sent whenever a notification is about to be sent to a player via TradingOffice.postNotification.
		 * Can be used to modify and/or replace the sent notification.
		 * Cancel the event to cancel the notification from being sent.
		 */
		public static class Pre extends NotificationSent
		{
			public Pre(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }
			
			public void setNotification(Notification notification) {
				if(notification == null)
					throw new NullPointerException("Cannot set the notification to null. Cancel the event if you wish for no notification to be sent.");
				this.notification = notification;
			}
			
			@Override
			public boolean isCancelable() { return true; }
			
		}
		
		/**
		 * Sent whenever a notification is successfully sent.
		 * Use this to listen to notifications.
		 */
		public static class Post extends NotificationSent
		{
			public Post(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }
		}
		
	}
	
	/**
	 * Sent when a notification is received on the client.
	 * Cancel to prevent the notification from being posted in chat.
	 */
	public static class NotificationReceivedOnClient extends NotificationEvent {
		
		public NotificationReceivedOnClient(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }
		
		@Override
		public boolean isCancelable() { return true; }
		
	}
	
}
