package io.github.lightman314.lightmanscurrency.api.traders.data.nodes.builtin;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.api.misc.world.WorldPosition;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;

import io.github.lightman314.lightmanscurrency.api.traders.data.TraderState;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.templates.SyncedTraderNode;
import io.github.lightman314.lightmanscurrency.api.traders.data.nodes.TraderNodeType;
import io.github.lightman314.lightmanscurrency.api.variants.item.data.VariantData;
import io.github.lightman314.lightmanscurrency.api.variants.item.data.VariantLock;
import io.github.lightman314.lightmanscurrency.common.core.ModDataComponents;
import io.github.lightman314.lightmanscurrency.common.core.ModItems;
import io.github.lightman314.lightmanscurrency.common.core.custom.ModLazyPackets;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.util.EnumUtil;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.BiConsumer;

public class WorldStateNode extends SyncedTraderNode {

    private static final MapCodec<WorldStateNode> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            TraderState.CODEC.fieldOf("state").forGetter(WorldStateNode::getState),
            WorldPosition.CODEC.fieldOf("position").forGetter(WorldStateNode::getPos),
            BuiltInRegistries.ITEM.byNameCodec().optionalFieldOf("block").forGetter(n -> n.traderBlock),
            ResourceLocation.CODEC.optionalFieldOf("variant").forGetter(n -> n.variant),
            Codec.BOOL.fieldOf("variantLocked").forGetter(n -> n.variantLocked)
    ).apply(builder,WorldStateNode::new));

    public static final TraderNodeType<WorldStateNode> TYPE = TraderNodeType.simple(WorldStateNode::new,MAP_CODEC);

    private TraderState state = TraderState.NORMAL;
    public TraderState getState() { return this.state; }
    public void setState(TraderState state) {
        if(this.state != state)
        {
            this.state = state;
            this.setChanged(builder -> builder.setInt("state",this.state.ordinal()));
        }
    }

    private WorldPosition position = WorldPosition.VOID;
    public WorldPosition getPos() { return this.position; }
    public void setPosition(WorldPosition position) {
        this.position = position;
        this.setChanged(builder -> builder.setCustom("position",this.position,ModLazyPackets.WORLD_POS));
    }

    private Optional<Item> traderBlock = Optional.empty();
    public boolean hasTraderBlock() { return this.traderBlock.isPresent(); }
    @Nullable
    public Item getTraderBlock() { return this.traderBlock.orElse(null); }
    public Item getTraderCategoryBlock() { return this.traderBlock.orElse(ModItems.TRADING_CORE.get()); }
    public ItemStack getTraderBlockStack()
    {
        if(this.traderBlock.isPresent())
        {
            ItemStack stack = new ItemStack(this.traderBlock.get());
            if(!stack.isEmpty())
                this.addVariantToStack(stack);
            return stack;
        }
        return ItemStack.EMPTY;
    }
    public void addVariantToStack(ItemStack stack)
    {
        if(this.variant.isPresent())
            stack.set(ModDataComponents.MODEL_VARIANT,new VariantData(this.variant.get()));
        if(this.variantLocked)
            stack.set(ModDataComponents.VARIANT_LOCK,VariantLock.INSTANCE);
    }
    public void setTraderBlock(@Nullable Item traderBlock) {
        this.traderBlock = Optional.ofNullable(traderBlock == Items.AIR ? null : traderBlock);
        this.setChanged(builder -> builder.setCustom("block",this.traderBlock,ModLazyPackets.ITEM_OPTIONAL));
    }

    private Optional<ResourceLocation> variant = Optional.empty();
    @Nullable
    public ResourceLocation getBlockVariant() { return this.variant.orElse(null); }
    private boolean variantLocked = false;
    public void setTraderBlockVariant(@Nullable ResourceLocation variant,boolean locked)
    {
        this.variant = Optional.ofNullable(variant);
        this.variantLocked = locked;
        this.setChanged(builder -> {
            this.variant.ifPresentOrElse(v -> builder.setResourceLocation("variant",v).remove("novariant"),
                    () -> builder.setFlag("novariant").remove("variant"));
            builder.setBoolean("variant_locked",this.variantLocked);
        });
    }

    private WorldStateNode() {}
    private WorldStateNode(TraderState state, WorldPosition pos, Optional<Item> traderBlock, Optional<ResourceLocation> variant, boolean variantLocked) {
        this.state = state;
        this.position = pos;
        this.traderBlock = traderBlock;
        this.variant = variant;
        this.variantLocked = variantLocked;
    }

    @Override
    public TraderNodeType<?> getType() { return TYPE; }
    @Override
    public void createSyncPacket(LazyPacketData.Builder builder,Player player) {
        builder.setInt("state",this.state.ordinal())
                .setCustom("position",this.position,ModLazyPackets.WORLD_POS)
                .setCustom("block",this.traderBlock,ModLazyPackets.ITEM_OPTIONAL)
                .setBoolean("variant_locked",this.variantLocked);
        this.variant.ifPresentOrElse(v -> builder.setResourceLocation("variant",v),() -> builder.setFlag("novariant"));
    }

    @Override
    public void onDataSync(LazyPacketData data) {
        if(data.contains("state"))
            this.state = EnumUtil.enumFromOrdinal(data.getInt("state"),TraderState.values(),TraderState.NORMAL);
        if(data.contains("position"))
            this.position = data.getCustom("position",ModLazyPackets.WORLD_POS);
        if(data.contains("block"))
            this.traderBlock = data.getCustom("block",ModLazyPackets.ITEM_OPTIONAL);
        if(data.contains("novariant"))
            this.variant = Optional.empty();
        if(data.contains("variant"))
            this.variant = Optional.of(data.getResourceLocation("variant"));
        if(data.contains("variant_locked"))
            this.variantLocked = data.getBoolean("variant_locked");
    }

    @Override
    public void loadOldData(CompoundTag tag, HolderLookup.Provider lookup) {
        //Trader State
        if(tag.contains("State"))
            this.state = EnumUtil.enumFromString(tag.getString("State"),TraderState.values(),TraderState.NORMAL);
        //Position
        if(tag.contains("Location"))
            this.position = WorldPosition.load(tag.getCompound("Location"));
        //Trader Block
        if(tag.contains("TraderBlock"))
        {
            try {
                this.traderBlock = Optional.of(BuiltInRegistries.ITEM.get(ResourceLocation.parse(tag.getString("TraderBlock"))));
            }catch (Throwable ignored) {}
        }
        //Model Variant
        if(tag.contains("TraderVariant"))
        {
            try { this.variant = Optional.of(ResourceLocation.parse(tag.getString("TraderVariant")));
            } catch (Throwable ignored) {}
        }
        if(tag.contains("TraderVariantLocked"))
            this.variantLocked = tag.getBoolean("TraderVariantLocked");

    }

    @Override
    public void handleSettingsChange(Player player, LazyPacketData message) {
        if(message.contains("PickupTrader"))
        {
            if(this.hasPermission(player, Permissions.BREAK_TRADER))
                this.trader.PickupTrader(player,message.getBoolean("PickupTrader"));
            else
                Permissions.PermissionWarning(player,"Pickup Trader", Permissions.BREAK_TRADER);
        }
    }

    @Override
    public void initializeAllyPermissions(BiConsumer<String, Integer> defaultConsumer) {
        defaultConsumer.accept(Permissions.BREAK_TRADER, 0);
    }

}
