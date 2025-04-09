package io.github.lightman314.lightmanscurrency.api.easy_data;

import com.google.common.collect.ImmutableMap;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.api.easy_data.util.NotificationReplacer;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.notifications.Notification;
import io.github.lightman314.lightmanscurrency.common.text.TextEntry;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public final class EasyDataSettings<T> {

    public final IEasyDataHost host;
    public final Component dataName;
    public final String tagKey;
    public final String simpleKey;
    public final DataCategory category;
    @Nullable
    private final NotificationReplacer<T> notificationReplacer;
    private final Map<ResourceLocation,Object> customFields;


    private EasyDataSettings(Builder<T,?> builder) {
        this.host = Objects.requireNonNull(builder.host,"An IEasyDataHost must be defined!");
        this.dataName = Objects.requireNonNullElse(builder.dataName,LCText.GUI_OWNER_NULL.get());
        this.tagKey = Objects.requireNonNull(builder.tagKey,"A key or tagKey must be defined!");
        this.simpleKey = Objects.requireNonNull(builder.simpleKey,"A key or tagKey must be defined!");
        this.category = builder.category;
        this.notificationReplacer = builder.notificationReplacer;
        this.customFields = ImmutableMap.copyOf(builder.customFields);
    }

    @Nullable
    public Notification filterNotification(T oldValue, T newValue, PlayerReference player, @Nullable Notification original) {
        if(this.notificationReplacer != null)
            return this.notificationReplacer.replaceNotification(oldValue,newValue,player,this,original);
        return original;
    }

    @Nullable
    public Object getCustomField(ResourceLocation type) { return this.customFields.get(type); }

    public static <T,X extends EasyData<T>> Builder<T,X> builder(Function<EasyDataSettings<T>,X> builder) { return new Builder<>(builder); }

    public static class Builder<T,X extends EasyData<T>> {

        private final Function<EasyDataSettings<T>,X> builder;
        private IEasyDataHost host = null;
        private Component dataName = null;
        private String simpleKey = null;
        private String tagKey = null;
        private DataCategory category = DataCategory.NULL;
        private NotificationReplacer<T> notificationReplacer = null;
        private final Map<ResourceLocation,Object> customFields = new HashMap<>();

        private Builder(Function<EasyDataSettings<T>,X> builder) { this.builder = Objects.requireNonNull(builder,"The Easy Data Builder cannot be null!"); }

        public Builder<T,X> host(IEasyDataHost host) { this.host = Objects.requireNonNull(host,"The host cannot be null!"); return this; }
        public Builder<T,X> name(Component name) { this.dataName = name; return this; }
        public Builder<T,X> key(String key) {
            this.simpleKey = Objects.requireNonNull(key,"The key cannot be null!");
            if(this.simpleKey.isEmpty())
                throw new IllegalArgumentException("The key cannot be empty!");
            if(this.tagKey == null)
                this.tagKey = key;
            if(this.dataName == null)
                this.dataName = TextEntry.dataName(LightmansCurrency.MODID,key).get();
            return this;
        }
        public Builder<T,X> tagKey(String tagKey) {
            this.tagKey = Objects.requireNonNull(tagKey,"The tagKey cannot be null!");
            if(this.tagKey.isEmpty())
                throw  new IllegalArgumentException("The tagKey cannot be empty!");
            if(this.simpleKey == null)
                this.simpleKey = tagKey;
            if(this.dataName == null)
                this.dataName = TextEntry.dataName(LightmansCurrency.MODID,tagKey).get();
            return this;
        }
        public Builder<T,X> category(DataCategory category) { this.category = Objects.requireNonNull(category,"The category cannot be null!"); return this; }
        public Builder<T,X> notificationReplacer(@Nullable NotificationReplacer<T> replacer) { this.notificationReplacer = replacer; return this; }

        public Builder<T,X> custom(ResourceLocation type,Object value) { this.customFields.put(type,value); return this; }

        public X build() { return this.builder.apply(new EasyDataSettings<>(this)); }

    }


}