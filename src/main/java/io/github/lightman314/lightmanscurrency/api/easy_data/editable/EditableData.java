package io.github.lightman314.lightmanscurrency.api.easy_data.editable;

import io.github.lightman314.lightmanscurrency.api.easy_data.EasyData;
import io.github.lightman314.lightmanscurrency.api.easy_data.EasyDataSettings;
import io.github.lightman314.lightmanscurrency.api.easy_data.util.EditableNotificationReplacer;
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
public abstract class EditableData<T,A> extends EasyData<T> {

    private static final ResourceLocation REPLACER_TYPE = VersionUtil.lcResource("editable_notification_replacer");
    private final EditableNotificationReplacer<T,A> editNotificationReplacer;

    public EditableData(EasyDataSettings<T> builder) {
        super(builder);
        if(builder.getCustomField(REPLACER_TYPE) instanceof EditableNotificationReplacer<?,?> r)
            this.editNotificationReplacer = (EditableNotificationReplacer<T,A>)r;
        else
            this.editNotificationReplacer = null;
    }

    public static <T,F,X extends EditableData<T,F>> EasyDataSettings.Builder<T,X> builder(Function<EasyDataSettings<T>,X> builder, EditableNotificationReplacer<T,F> editableNotificationReplacer) { return EasyDataSettings.builder(builder).custom(REPLACER_TYPE,editableNotificationReplacer); }

    public final void tryEdit(Player player, A value) {
        if(this.settings.category.canEdit(player,this.settings.host))
        {
            PlayerReference pr = PlayerReference.of(player);
            Notification notification = this.edit(pr,value);
            if(this.editNotificationReplacer != null)
                notification = this.editNotificationReplacer.replaceNotification(this.get(),value,pr,this.settings,notification);
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
    protected abstract Notification edit(PlayerReference player, A newValue);

}