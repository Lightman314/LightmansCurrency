package io.github.lightman314.lightmanscurrency.api.helpers.data.attachment;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentSyncHandler;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import org.jspecify.annotations.Nullable;

public record SyncWithContext<T extends AttachmentWithContext>(StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) implements AttachmentSyncHandler<T> {

    @Override
    public void write(RegistryFriendlyByteBuf buf,T attachment,boolean initialSync) {
        this.streamCodec.encode(buf,attachment);
    }

    @Override
    public @Nullable T read(IAttachmentHolder holder, RegistryFriendlyByteBuf buf, @Nullable T previousValue) {
        T value = this.streamCodec.decode(buf);
        value.attachHolder(holder);
        return value;
    }

}
