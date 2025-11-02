package io.github.lightman314.lightmanscurrency.common.notifications.types.ejection;

import io.github.lightman314.lightmanscurrency.LCConfig;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.NotificationType;
import io.github.lightman314.lightmanscurrency.api.notifications.SingleLineNotification;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.function.Supplier;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class OwnableBlockEjectedNotification extends SingleLineNotification {

    public static final NotificationType<OwnableBlockEjectedNotification> TYPE = new NotificationType<>(VersionUtil.lcResource("block_ejected"),OwnableBlockEjectedNotification::new);

    private Component name = EasyText.empty();
    private boolean ejected = false;
    private boolean anarchy = false;

    private OwnableBlockEjectedNotification() {}
    public OwnableBlockEjectedNotification(Component name) {
        this.name = name.copy();
        this.ejected = LCConfig.SERVER.safelyEjectMachineContents.get();
        this.anarchy = LCConfig.SERVER.anarchyMode.get();
    }

    public static Supplier<Notification> create(Component name) { return () -> new OwnableBlockEjectedNotification(name); }

    @Override
    protected NotificationType<?> getType() { return TYPE; }

    @Override
    public NotificationCategory getCategory() { return NullCategory.INSTANCE; }
    
    @Override
    public Component getMessage() { return this.getText().get(this.name); }

    private TextEntry getText() {
        if(this.anarchy)
            return LCText.NOTIFICATION_EJECTION_ANARCHY;
        if(this.ejected)
            return LCText.NOTIFICATION_EJECTION_EJECTED;
        return LCText.NOTIFICATION_EJECTION_DROPPED;
    }

    @Override
    protected void saveAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        compound.putString("Name", Component.Serializer.toJson(this.name,lookup));
        if(this.ejected)
            compound.putBoolean("Ejected", this.ejected);
        if(this.anarchy)
            compound.putBoolean("Anarchy", this.anarchy);
    }

    @Override
    protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
        this.name = Component.Serializer.fromJson(compound.getString("Name"),lookup);
        if(compound.contains("Ejected"))
            this.ejected = compound.getBoolean("Ejected");
        if(compound.contains("Anarchy"))
            this.anarchy = compound.getBoolean("Anarchy");
    }

    @Override
    protected boolean canMerge(Notification other) { return false; }

}
