package io.github.lightman314.lightmanscurrency.api.helpers.screen;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.concurrent.Immutable;

/**
 * A common class (can exist on the physical server) that is used to denote a {@link ScreenPosition} as well as a width and height.<br>
 * Often used in my advanced screen implementations to more easily represent the actual portion of gui that the screen or widget is in.
 */
@Immutable
public final class ScreenArea {

    public static final ScreenArea ZERO = new ScreenArea(ScreenPosition.ZERO,0,0);

    public static final MapCodec<ScreenArea> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            ScreenPosition.MAP_CODEC.forGetter(a -> a.pos),
            Codec.INT.fieldOf("width").forGetter(a -> a.width),
            Codec.INT.fieldOf("height").forGetter(a -> a.height)
    ).apply(builder,ScreenArea::of));
    public static final Codec<ScreenArea> CODEC = MAP_CODEC.codec();

    public static final StreamCodec<ByteBuf,ScreenArea> STREAM_CODEC = StreamCodec.composite(
            ScreenPosition.STREAM_CODEC,a -> a.pos,
            ByteBufCodecs.INT,a -> a.width,
            ByteBufCodecs.INT,a -> a.height,
            ScreenArea::of);

    public final int x;
    public final int y;
    public final ScreenPosition pos;
    public final int width;
    public final int height;

    private ScreenArea(ScreenPosition pos, int width, int height) {
        this.x = pos.x;
        this.y = pos.y;
        this.pos = pos;
        this.width = width;
        this.height = height;
    }

    public int centerX() { return this.x + this.halfWidth(); }
    public int centerY() { return this.y + this.halfHeight(); }

    public int halfWidth() { return this.width / 2; }
    public int halfHeight() { return this.height / 2;}

    public int right() { return this.x + this.width; }
    public int bottom() { return this.y + this.height; }

    public static ScreenArea of(int x, int y, int width, int height) { return of(ScreenPosition.of(x,y),width,height); }
    public static ScreenArea of(ScreenPosition position, int width, int height) { return new ScreenArea(position, width, height); }

    public boolean isInArea(ScreenPosition mousePos) { return this.isInArea(mousePos.x, mousePos.y); }
    public boolean isInArea(int mouseX, int mouseY) { return mouseX >= this.pos.x && mouseX < this.pos.x + this.width && mouseY >= this.pos.y && mouseY < this.pos.y + this.height; }
    public boolean isInArea(double mouseX, double mouseY) { return mouseX >= this.pos.x && mouseX < this.pos.x + this.width && mouseY >= this.pos.y && mouseY < this.pos.y + this.height; }
    public boolean isOutsideOf(ScreenArea area) { return isOutsideOf(this.x,this.right(),area.x,area.right()) || isOutsideOf(this.y,this.bottom(),area.y,area.bottom()); }
    public boolean overlaps(ScreenArea area) { return overlaps(area.x,area.right(),this.x,this.right()) && overlaps(area.y,area.bottom(),this.y,this.bottom()); }

    public static boolean isOutsideOf(int min1,int max1,int min2,int max2) { return min1 < min2 || max1 > max2; }
    public static boolean overlaps(int min1,int max1,int min2,int max2) { return min1 <= max2 && min2 <= max1; }

    public ScreenArea atPosition(int x, int y) { return of(x, y, this.width, this.height); }
    public ScreenArea atPosition(ScreenPosition newPos) { return of(newPos, this.width, this.height); }
    public ScreenArea offsetPosition(int x, int y) { return of(this.pos.offset(x,y), this.width, this.height); }
    public ScreenArea offsetPosition(ScreenPosition offset) { return of(this.pos.offset(offset), this.width, this.height); }
    public ScreenArea ofSize(int width, int height) { return of(this.pos, width, height); }
    public ScreenArea shrinkWidth(int widthDelta) { return of(this.pos, this.width - widthDelta, this.height); }
    public ScreenArea shrinkHeight(int heightDelta) { return of(this.pos, this.width, this.height - heightDelta); }
    public ScreenArea lowered(int amount) { return of(this.x,this.y + amount,this.width,this.height - amount); }

    public ScreenPosition cornerTopLeft() { return this.pos; }
    public ScreenPosition cornerTopRight() { return this.pos.offset(this.width,0); }
    public ScreenPosition cornerBottomLeft() { return this.pos.offset(0,this.height); }
    public ScreenPosition cornerBottomRight() { return this.pos.offset(this.width,this.height); }

    public boolean isOutside(ScreenArea area) { return !isInArea(area.pos) || !isInArea(area.pos.offset(area.width,area.height)); }

    @Override
    public String toString() {
        return this.x + "," + this.y + "[" + this.width + "," + this.height + "]";
    }

}