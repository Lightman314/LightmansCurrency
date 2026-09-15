package io.github.lightman314.lightmanscurrency.api.helpers.data.attachment;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

@SuppressWarnings("deprecation")
public record SerializerWithContext<T extends AttachmentWithContext>(MapCodec<T> codec) implements IAttachmentSerializer<T> {
    @Override
    public T read(IAttachmentHolder holder, ValueInput input) {
        T value = input.read(this.codec).orElseThrow();
        value.attachHolder(holder);
        return value;
    }
    @Override
    public boolean write(T attachment,ValueOutput output) {
        output.store(this.codec,attachment);
        return true;
    }
}
