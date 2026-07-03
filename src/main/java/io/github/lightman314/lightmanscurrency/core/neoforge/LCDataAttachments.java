package io.github.lightman314.lightmanscurrency.core.neoforge;

import com.mojang.serialization.MapCodec;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.features.wallet.WalletAttachment;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;
import java.util.function.UnaryOperator;

public final class LCDataAttachments {
    private LCDataAttachments() {}

    public static final DeferredRegister<AttachmentType<?>> REGISTER = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES,LCApi.MODID);

    public static final DeferredHolder<AttachmentType<?>,AttachmentType<WalletAttachment>> WALLET = register("wallet",WalletAttachment::new,WalletAttachment.CODEC,WalletAttachment.STREAM_CODEC);

    private static <T> DeferredHolder<AttachmentType<?>,AttachmentType<T>> register(String name,Supplier<T> constructor, MapCodec<T> codec,StreamCodec<? super RegistryFriendlyByteBuf,T> streamCodec) {
        return register(name,constructor, builder ->
                builder.serialize(codec)
                .sync(streamCodec)
                .copyOnDeath());
    }
    private static <T> DeferredHolder<AttachmentType<?>,AttachmentType<T>> register(String name,Supplier<T> constructor,UnaryOperator<AttachmentType.Builder<T>> builder) {
        return register(name,() -> builder.apply(AttachmentType.builder(constructor)));
    }
    private static <T> DeferredHolder<AttachmentType<?>,AttachmentType<T>> register(String name,Supplier<AttachmentType.Builder<T>> builder) {
        return REGISTER.register(name,() -> builder.get().build());
    }

}