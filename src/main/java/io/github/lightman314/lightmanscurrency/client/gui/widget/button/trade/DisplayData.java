package io.github.lightman314.lightmanscurrency.client.gui.widget.button.trade;

import com.google.common.collect.Lists;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public record DisplayData(int xOffset, int yOffset, int width, int height) {

    public int xOffset() { return this.xOffset; }
    public int yOffset() { return this.yOffset; }
    public int width() { return this.width; }
    public int height() { return this.height; }
    public ScreenArea asArea() { return ScreenArea.of(this.xOffset, this.yOffset, this.width, this.height); }

    /**
     * Divides the display area horizontally into the given number of pieces.
     * Will always return a list of the length count
     */
    @Nonnull
    public List<DisplayData> divide(int count) {
        if (count <= 1)
            return Lists.newArrayList(this);
        int partialWidth = this.width / count;
        int x = this.xOffset;
        List<DisplayData> result = new ArrayList<>();
        for (int i = 0; i < count; ++i) {
            result.add(new DisplayData(x, this.yOffset, partialWidth, this.height));
            x += partialWidth;
        }
        return result;
    }

}