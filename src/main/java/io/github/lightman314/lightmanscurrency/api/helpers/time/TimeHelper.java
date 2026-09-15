package io.github.lightman314.lightmanscurrency.api.helpers.time;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.proxy.LCProxy;
import io.github.lightman314.lightmanscurrency.network.message.system.SPacketSyncTime;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.text.SimpleDateFormat;
import java.util.Date;

@EventBusSubscriber
public final class TimeHelper {

    public static final long DURATION_SECOND = 1000;
    public static final long DURATION_MINUTE = DURATION_SECOND * 60;
    public static final long DURATION_HOUR = DURATION_MINUTE * 60;
    public static final long DURATION_DAY = DURATION_HOUR * 24;
    public static final long DURATION_YEAR = DURATION_DAY * 365;

    public static long getCurrentTime() { return System.currentTimeMillis() + LCProxy.get().getTimeDesync(); }

    public static boolean timerExpired(long timestamp,long timerDuration) { return timestamp + timerDuration <= getCurrentTime(); }
    public static boolean timerNotExpired(long timestamp,long timerDuration) { return timestamp + timerDuration > getCurrentTime() ; }

    /**
     * Returns a formatted string representing the time given, as defined in the {@link LCConfig.Client#timeFormat} config option.
     * @param timestamp The time (in milliseconds) that the thing happened
     * @return The Formatted String representing the given time.
     */
    public static String formatTime(long timestamp) { return new SimpleDateFormat(LCConfig.CLIENT.timeFormat.get()).format(new Date(timestamp + LCProxy.get().getTimeDesync())); }

    public static long getDuration(long days,long hours,long minutes,long seconds) {
        days = Math.max(days,0);
        hours = Math.max(hours,0);
        minutes = Math.max(minutes,0);
        seconds = Math.max(seconds,0);

        hours += 24 * days;
        minutes += 60 * hours;
        seconds += 60 * minutes;
        return seconds * 1000;
    }

    @SubscribeEvent
    private static void onPlayerJoin(PlayerEvent.PlayerLoggedInEvent event) { SPacketSyncTime.sendToPlayer(event.getEntity()); }

}