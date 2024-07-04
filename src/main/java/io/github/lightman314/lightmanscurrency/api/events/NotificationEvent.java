package io.github.lightman314.lightmanscurrency.api.events;

import java.util.UUID;

import javax.annotation.Nonnull;

import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationData;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

/**
 * Events called when a {@link Notification} is given to a player.
 */
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
	 * Events sent when a notification is sent to a player.<br>
	 * Only run server-side.
	 */
	public static class NotificationSent extends NotificationEvent {

		protected NotificationSent(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }
		
		/**
		 * Sent whenever a notification is about to be sent to a player via {@link io.github.lightman314.lightmanscurrency.api.notifications.NotificationAPI#PushPlayerNotification(UUID, Notification)}<br>
		 * Can be used to modify and/or replace the notification.<br>
		 * Cancel the event to prevent the notification from being sent.
		 */
		public static class Pre extends NotificationSent implements ICancellableEvent
		{
			public Pre(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }
			
			public void setNotification(@Nonnull Notification notification) {
				if(notification == null)
					throw new NullPointerException("Cannot set the notification to null. Cancel the event if you wish for no notification to be sent.");
				this.notification = notification;
			}
			
		}
		
		/**
		 * Sent whenever a notification is successfully sent.<br>
		 * Use this to listen to notifications.
		 */
		public static class Post extends NotificationSent
		{
			public Post(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }
		}
		
	}
	
	/**
	 * Sent when a notification is received on the client.<br>
	 * Cancel to prevent the notification from being posted in chat.
	 */
	public static class NotificationReceivedOnClient extends NotificationEvent implements ICancellableEvent{
		
		public NotificationReceivedOnClient(UUID playerID, NotificationData data, Notification notification) { super(playerID, data, notification); }
		
	}
	
}
