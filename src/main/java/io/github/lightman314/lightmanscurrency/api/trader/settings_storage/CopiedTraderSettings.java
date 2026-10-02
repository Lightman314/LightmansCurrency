package io.github.lightman314.lightmanscurrency.api.trader.settings_storage;

import com.mojang.serialization.Codec;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipProvider;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;

import java.util.function.Consumer;

public final class CopiedTraderSettings implements TooltipProvider {

    public static final Codec<CopiedTraderSettings> CODEC = CompoundTag.CODEC.xmap(CopiedTraderSettings::new,s -> s.data);
    public static final StreamCodec<ByteBuf,CopiedTraderSettings> STREAM_CODEC = StreamHelper.COMPOUND_TAG.map(CopiedTraderSettings::new,s -> s.data);

    public CopiedTraderSettings(CompoundTag data) { this.data = data; }
    public CopiedTraderSettings(TagValueOutput data) { this(data.buildResult()); }

    private final CompoundTag data;
    public ValueInput getData(ProblemReporter problemReporter, HolderLookup.Provider registryAccess) { return TagValueInput.create(problemReporter,registryAccess,this.data); }

    @Override
    public void addToTooltip(Item.TooltipContext context, Consumer<Component> consumer, TooltipFlag flag, DataComponentGetter components) {

    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this)
            return true;
        return obj instanceof CopiedTraderSettings cts && cts.data.equals(this.data);
    }
    @Override
    public int hashCode() { return this.data.hashCode(); }

}
