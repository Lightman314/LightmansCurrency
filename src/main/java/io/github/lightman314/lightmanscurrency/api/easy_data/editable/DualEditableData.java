package io.github.lightman314.lightmanscurrency.api.easy_data.editable;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.easy_data.util.DualEditableNotificationReplacer;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Consumer;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class DualEditableData<T,A,B> extends EasyData<T> {

    private static final ResourceLocation REPLACER_TYPE = VersionUtil.lcResource("dual_editable_notification_replacer");
    private final DualEditableNotificationReplacer<T,A,B> editNotificationReplacer;

    public DualEditableData(EasyDataSettings<T> builder) {
        super(builder);
        if(builder.getCustomField(REPLACER_TYPE) instanceof DualEditableNotificationReplacer<?,?,?> r)
            this.editNotificationReplacer = (DualEditableNotificationReplacer<T,A,B>)r;
        else
            this.editNotificationReplacer = null;
    }

    public static <T,A,B,X extends DualEditableData<T,A,B>> EasyDataSettings.Builder<T,X> builder(Function<EasyDataSettings<T>,X> builder, DualEditableNotificationReplacer<T,A,B> editableNotificationReplacer) { return EasyDataSettings.builder(builder).custom(REPLACER_TYPE,editableNotificationReplacer); }

    public final void tryEdit(Player player, A value1, B value2) {
        if(this.settings.category.canEdit(player,this.settings.host))
        {
            PlayerReference pr = PlayerReference.of(player);
            Notification notification = this.edit(pr,value1,value2);
            if(this.editNotificationReplacer != null)
                notification = this.editNotificationReplacer.replaceNotification(this.get(),value1,value2,pr,this.settings,notification);
            //Push notification
            if(notification != null)
            {
                Consumer<Notification> consumer = this.settings.host.dataChangeNotifier();
                if(consumer != null)
                    consumer.accept(notification);
            }
        }
    }

    @Nullable
    protected abstract Notification edit(PlayerReference player, A value1, B value2);

}
