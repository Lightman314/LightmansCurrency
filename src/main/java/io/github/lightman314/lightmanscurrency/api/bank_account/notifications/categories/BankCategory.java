package io.github.lightman314.lightmanscurrency.api.bank_account.notifications.categories;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.icon.IconData;
import io.github.lightman314.lightmanscurrency.api.icon.builtin.SpriteIcon;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategory;
import io.github.lightman314.lightmanscurrency.api.notifications.category.NotificationCategoryType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

public class BankCategory extends NotificationCategory {

    private static final MapCodec<BankCategory> MAP_CODEC = ComponentSerialization.CODEC.xmap(BankCategory::new,BankCategory::getName).fieldOf("account");
    private static final StreamCodec<RegistryFriendlyByteBuf,BankCategory> STREAM_CODEC = ComponentSerialization.STREAM_CODEC.map(BankCategory::new,BankCategory::getName);

    public static final NotificationCategoryType<BankCategory> TYPE = new NotificationCategoryType<>(MAP_CODEC,STREAM_CODEC);

    private final Component name;
    public BankCategory(Component name) { this.name = name; }

    @Override
    public Component getName() { return this.name; }
    @Override
    public IconData getIcon() { return SpriteIcon.of(LCApi.id("icon/bank")); }
    @Override
    public NotificationCategoryType<?> getType() { return TYPE; }
    @Override
    public boolean equals(NotificationCategory other) { return other instanceof BankCategory c && c.name.equals(this.name); }
    @Override
    public int hashCode() { return this.name.hashCode(); }

}
