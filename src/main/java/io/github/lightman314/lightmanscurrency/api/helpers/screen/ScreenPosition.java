package io.github.lightman314.lightmanscurrency.api.helpers.screen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.concurrent.Immutable;
import java.util.Objects;
import java.util.Optional;

/**
 * A common class (can exist on the physical server) that is used to denote a position on the screen.<br>
 * Exists within the common domain so that config files may read and write screen position values, and/or send this data via packet.
 * @see io.github.lightman314.lightmanscurrency.api.client.gui.helpers.ScreenHelper ScreenHelper
 */
@Immutable
public final class ScreenPosition {

    public static final ScreenPosition ZERO = of(0,0);

    public static final MapCodec<ScreenPosition> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            Codec.INT.fieldOf("x").forGetter(p -> p.x),
            Codec.INT.fieldOf("y").forGetter(p -> p.y)
    ).apply(builder,ScreenPosition::of));
    public static final Codec<ScreenPosition> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<ByteBuf,ScreenPosition> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT,p -> p.x,
            ByteBufCodecs.INT,p -> p.y,
            ScreenPosition::of);

    public final int x;
    public final int y;
    private ScreenPosition(int x, int y) { this.x = x; this.y = y; }

    public ScreenPosition offset(ScreenPosition other) { return of(this.x + other.x, this.y + other.y); }
    public ScreenPosition offset(int x, int y) { return of(this.x + x, this.y + y); }
    public ScreenPosition relativeTo(ScreenPosition corner) { return of(this.x - corner.x,this.y - corner.y); }
    public ScreenPosition relativeTo(int cornerX,int cornerY) { return of(this.x - cornerX,this.y - cornerY); }
    public ScreenPosition invert() { return of(this.x * -1,this.y * -1); }

    public boolean isMouseInArea(ScreenPosition mousePos, int width, int height) { return ScreenArea.of(this, width, height).isInArea(mousePos); }
    public boolean isMouseInArea(int mouseX, int mouseY, int width, int height) { return ScreenArea.of(this, width, height).isInArea(mouseX, mouseY); }
    public boolean isMouseInArea(double mouseX, double mouseY, int width, int height) { return ScreenArea.of(this, width, height).isInArea(mouseX, mouseY); }

    public ScreenArea asArea(int width, int height) { return ScreenArea.of(this, width, height); }

    public static ScreenPosition of(int x, int y) { return new ScreenPosition(x,y); }
    public static ScreenPosition of(double x, double y) { return of((int)x,(int)y); }
    public static ScreenPosition of(ScreenPosition offset, int x, int y) { return offset.offset(x,y); }
    public static Optional<ScreenPosition> ofOptional(int x, int y) { return Optional.of(of(x, y)); }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ScreenPosition pos)
            return pos.x == this.x && pos.y == this.y;
        return false;
    }
    @Override
    public int hashCode() { return Objects.hash(this.x,this.y); }
    @Override
    public String toString() { return this.x + "," + this.y; }

}