package io.github.lightman314.lightmanscurrency.core.lightmanscurrency;

import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.LCRegistries;
import io.github.lightman314.lightmanscurrency.api.helpers.data.PlayerReference;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketMap;
import io.github.lightman314.lightmanscurrency.api.helpers.network.FancyPacketType;
import io.github.lightman314.lightmanscurrency.api.helpers.network.PacketList;
import io.github.lightman314.lightmanscurrency.api.money.values.MoneyValue;
import io.github.lightman314.lightmanscurrency.api.ownership.OwnerHolder;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.Permission;
import io.github.lightman314.lightmanscurrency.api.trader.permissions.PermissionValue;
import io.github.lightman314.lightmanscurrency.api.trader.trade.data.price.TradePrice;
import io.github.lightman314.lightmanscurrency.api.world.data.WorldPosition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.UUID;

public final class LCFancyPacketTypes {
    private LCFancyPacketTypes() {}

    public static final DeferredRegister<FancyPacketType<?>> REGISTER = DeferredRegister.create(LCRegistries.Network.PACKET_TYPE,LCApi.MODID);

    //Basic Types
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<Unit>> NULL = register("null",Unit.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<Boolean>> BOOLEAN = register("bool", ByteBufCodecs.BOOL);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<Integer>> INT = register("int",ByteBufCodecs.INT);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<Long>> LONG = register("long",ByteBufCodecs.LONG);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<Float>> FLOAT = register("float",ByteBufCodecs.FLOAT);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<Double>> DOUBLE = register("double",ByteBufCodecs.DOUBLE);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<String>> STRING = register("string",ByteBufCodecs.STRING_UTF8);

    //Advanced Types
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<UUID>> UUID = register("uuid",UUIDUtil.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<PacketList<?>>> LIST = register("list",PacketList.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<FancyPacketMap>> MAP = register("map",FancyPacketMap.STREAM_CODEC);

    //MC Types
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<Identifier>> ID = register("identifier",Identifier.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<Component>> TEXT = register("text",ComponentSerialization.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<BlockPos>> BLOCK_POS = register("blockpos",BlockPos.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<ItemStack>> ITEM_STACK = register("item_stack",ItemStack.OPTIONAL_STREAM_CODEC);

    //LC Types
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<PlayerReference>> PLAYER_REFERENCE = register("player_reference",PlayerReference.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<OwnerHolder>> OWNER_HOLDER = register("owner_holder",OwnerHolder.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<Permission<?>>> PERMISSION = register("permission",Permission.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<PermissionValue<?>>> PERMISSION_VALUE = register("permission_value",PermissionValue.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<WorldPosition>> WORLD_POSITION = register("world_position",WorldPosition.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<MoneyValue>> MONEY = register("money",MoneyValue.STREAM_CODEC);
    public static final DeferredHolder<FancyPacketType<?>,FancyPacketType<TradePrice>> TRADE_PRICE = register("trade_price",TradePrice.STREAM_CODEC);


    private static <T> DeferredHolder<FancyPacketType<?>,FancyPacketType<T>> register(String name, StreamCodec<? super RegistryFriendlyByteBuf,T> codec) { return REGISTER.register(name,() -> new FancyPacketType<>(codec)); }

}
