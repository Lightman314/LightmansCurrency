package io.github.lightman314.lightmanscurrency.api.notifications;

import com.mojang.datafixers.kinds.App;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.codecs.CodecHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart1;
import io.github.lightman314.lightmanscurrency.api.misc.ISidedObject;
import io.github.lightman314.lightmanscurrency.api.misc.IClientTracker;
import io.github.lightman314.lightmanscurrency.util.TimeUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class Notification implements ISidedObject {

    public static final Codec<Notification> CODEC = Codec.withAlternative(
            //Desired Codec
            NotificationType.CODEC.dispatch(Notification::getType,NotificationType::codec),
            //Fallback Codec for old data
            CodecHelper.oldValueLoader(Notification::loadOldData,"Notification"));
    public static final Codec<List<Notification>> LIST_CODEC = CODEC.listOf();

    public static final StreamCodec<RegistryFriendlyByteBuf,Notification> STREAM_CODEC = NotificationType.STREAM_CODEC.dispatch(Notification::getType,NotificationType::streamCodec);

	private boolean isClient = false;
	@Override
	public final boolean isClient() { return this.isClient; }
	@Override
	public final boolean isServer() { return ISidedObject.super.isServer(); }

	private long timeStamp;
	public long getTimeStamp() { return this.timeStamp; }
	public final boolean hasTimeStamp() { return this.getTimeStamp() > 0; }

	private boolean seen = false;
	public boolean wasSeen() { return this.seen; }
	public void setSeen() { this.seen = true; }
	
	private int count = 1;
	public int getCount() { return this.count; }

    public final void mergeCommonData(CommonData data) {
        this.seen = data.seen();
        this.count = data.count();
        this.timeStamp = data.timestamp();
    }
    public final CommonData getCommonData() { return new CommonData(this.seen,this.count,this.timeStamp); }

	protected Notification() { this.timeStamp = TimeUtil.getCurrentTime(); }
    protected Notification(CommonData data) { this.seen = data.seen(); this.count = data.count(); this.timeStamp = data.timestamp(); }

	public abstract NotificationType<?> getType();

	public abstract NotificationCategory getCategory();

	public abstract List<Component> getMessageLines();
	public List<Component> getGeneralMessage() { return this.getModifiedMessage(line -> LCText.NOTIFICATION_FORMAT_GENERAL.get(this.getCategory().getName(),line)); }
	public List<Component> getChatMessage() {
		return this.getModifiedMessage(line -> LCText.NOTIFICATION_FORMAT_CHAT.get(
				LCText.NOTIFICATION_FORMAT_CHAT_TITLE.get(this.getCategory().getName()).withStyle(ChatFormatting.GOLD),
				line
		));
	}

	protected final List<Component> getModifiedMessage(UnaryOperator<Component> edit) {
		List<Component> message = new ArrayList<>(this.getMessageLines());
		if(message.isEmpty())
			return message;
		message.set(0,edit.apply(message.getFirst()));
		return message;
	}
	
	public Component getTimeStampMessage() { return LCText.NOTIFICATION_TIMESTAMP.get(TimeUtil.formatTime(this.timeStamp)); }

	public final CompoundTag save(HolderLookup.Provider lookup) { return (CompoundTag)CODEC.encodeStart(RegistryOps.create(NbtOps.INSTANCE,lookup),this).getOrThrow(); }

    public static Notification load(CompoundTag tag, HolderLookup.Provider lookup)
    {
        return CODEC.decode(RegistryOps.create(NbtOps.INSTANCE,lookup),tag).getOrThrow().getFirst();
    }

	protected final void loadOld(CompoundTag compound, HolderLookup.Provider lookup) {
		if(compound.contains("Seen"))
			this.seen = true;
		if(compound.contains("Count", Tag.TAG_INT))
			this.count = compound.getInt("Count");
		if(compound.contains("TimeStamp", Tag.TAG_LONG))
			this.timeStamp = compound.getLong("TimeStamp");
		else
			this.timeStamp = 0;
		this.loadAdditional(compound, lookup);
	}
	
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {}

    @Deprecated
    private static Notification loadOldData(CompoundTag tag, HolderLookup.Provider lookup)
    {
        if(tag.contains("Type") || tag.contains("type"))
        {
            ResourceLocation type = ResourceLocation.parse(tag.contains("Type") ? tag.getString("Type") : tag.getString("type"));
            if(LCRegistries.NOTIFICATION_TYPES.containsKey(type))
            {
                try {
                    return LCRegistries.NOTIFICATION_TYPES.get(type).loadOldData(tag, lookup);
                } catch (Throwable t) {
                    LightmansCurrency.LogError("Error loading Notification of type '" + type + "'", t);
                    return null;
                }
            }
            else
            {
                LightmansCurrency.LogError("Cannot load notification type " + type + " as no NotificationType has been registered with that name");
                return null;
            }
        }
        else
        {
            LightmansCurrency.LogError("Cannot deserialize notification as tag is missing the 'type' tag");
            return null;
        }
    }

	/**
	 * Determines whether the new notification should stack or not.
	 * @param other The other notification. Use this to determine if the other notification is a duplicate or not.
	 * @return True if the notification was stacked.
	 */
	public boolean onNewNotification(Notification other) {
		if(this.canMerge(other))
		{
			this.count++;
			this.seen = false;
			this.timeStamp = TimeUtil.getCurrentTime();
			return true;
		}
		return false;
	}
	
	/**
	 * Whether the other notification should be merged with this one.
	 */
	protected abstract boolean canMerge(Notification other);

	@Override
	public Notification flagAsClient() { return this.flagAsClient(true); }
	@Override
	public Notification flagAsClient(boolean isClient) { this.isClient = isClient; return this; }
	@Override
	public Notification flagAsClient(IClientTracker context) { return this.flagAsClient(context.isClient()); }

    //Codec Helpers
    public static <T extends Notification> App<RecordCodecBuilder.Mu<T>, CommonData> baseFields()
    {
        return CommonData.CODEC.forGetter(Notification::getCommonData);
    }

    public static <T extends Notification> SPart1<RegistryFriendlyByteBuf,T, CommonData> baseStreamFields()
    {
        return new SPart1<>(CommonData.STREAM_CODEC,Notification::getCommonData);
    }

}
