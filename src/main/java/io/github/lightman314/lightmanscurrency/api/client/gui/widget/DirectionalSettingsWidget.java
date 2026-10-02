package io.github.lightman314.lightmanscurrency.api.client.gui.widget;

import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.api.LCApi;
import io.github.lightman314.lightmanscurrency.api.client.gui.helpers.FancyGuiExtractor;
import io.github.lightman314.lightmanscurrency.api.client.gui.sprites.SizedSprite;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.button.SpriteButton;
import io.github.lightman314.lightmanscurrency.api.client.gui.widget.interfaces.TooltipSource;
import io.github.lightman314.lightmanscurrency.api.helpers.EnumHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.MathHelper;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenArea;
import io.github.lightman314.lightmanscurrency.api.helpers.screen.ScreenPosition;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IDeepBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.world.block.interfaces.IWideBlock;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.world.data.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.world.data.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.client.features.rendering.BlockStateRenderState;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.*;

public class DirectionalSettingsWidget extends AbstractMultiWidget.LateChildren {

    private static final List<Direction> DIRECTIONS = List.of(Direction.DOWN,Direction.UP,Direction.NORTH,Direction.SOUTH,Direction.WEST,Direction.EAST);

    public static final int SPACING = 4;
    public static final int DOUBLE_SPACING = SPACING * 2;

    private final Supplier<IDirectionalSettingsObject> context;
    private final Consumer<Direction> onPress;
    private final Consumer<Direction> onAltPress;
    private DisplayBlockAttributes attributes;

    public DirectionalSettingsWidget(Builder builder) {
        super(builder);
        this.context = builder.context;
        this.onPress = builder.handler;
        this.onAltPress = builder.altHandler;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) { return false; }

    @Override
    protected void addLateChildren(ScreenArea area) {
        //Get the attributes
        this.attributes = /*DisplayBlockAttributes.NULL;//*/DisplayBlockAttributes.of(this.context);
        //Recalculate the size
        area = this.recalculateSize(attributes);
        //Generate buttons that only display the frame as the actual block will be drawn below them
        for(Direction side : DIRECTIONS) {
            this.addChild(SpriteButton.builder()
                    .atPos(area.pos.offset(attributes.getSidePos(side)))
                    .withSprite(attributes.getSideSprite(side,this.context))
                    .onPress(() -> this.onPress.accept(side))
                    .onAltPress(() -> this.onAltPress.accept(side))
                    .tooltip(TooltipSource.deferredList(this.tooltipForSide(side)))
                    .visible(this.willSideBeVisible(side))
                    .build());
        }
    }

    private Supplier<List<Component>> tooltipForSide(Direction side) {
        return () -> {
            List<Component> list = new ArrayList<>();
            list.add(DirectionalSettings.GUI_INPUT_SIDES.getComponent(side));
            DirectionalSettingsState state = this.stateForSide(side);
            if(state != DirectionalSettingsState.NONE)
                list.add(state.getText());
            return list;
        };
    }

    private static DirectionalSettingsState stateForSide(Supplier<IDirectionalSettingsObject> context,Direction side) {
        IDirectionalSettingsObject data = context.get();
        return data == null ? DirectionalSettingsState.NONE : data.getSidedState(side);
    }
    private DirectionalSettingsState stateForSide(Direction side) { return stateForSide(this.context,side); }

    private BooleanSupplier willSideBeVisible(Direction side) { return () -> this.isSideVisible(side); }
    private boolean isSideVisible(Direction side) {
        IDirectionalSettingsObject data = this.context.get();
        return this.visible && data != null && !data.getIgnoredSides().contains(side);
    }

    private ScreenArea recalculateSize(DisplayBlockAttributes attributes) {
        int width = DOUBLE_SPACING;
        //First column width
        width += attributes.blockDepth * 16;
        //Second column width
        width += attributes.blockWidth * 16;
        //Third column width
        width += Math.max(attributes.blockDepth,attributes.blockWidth) * 16;
        this.setWidth(width);

        int height = DOUBLE_SPACING;
        //First row height
        height += attributes.blockDepth * 16;
        //Second row height
        height += attributes.blockHeight * 16;
        //Third row height
        height += Math.max(attributes.blockHeight,attributes.blockDepth) * 16;
        this.setHeight(height);
        //Offset to the left half of our defined width
        this.setX(this.getX() - (width / 2));
        return this.getArea();
    }

    @Override
    protected void extractRenderState(FancyGuiExtractor gui, ScreenArea area) {
        if(this.attributes.isNull()) {
            //Render fallback background
            for(Direction side : DIRECTIONS) {
                if(!this.isSideVisible(side))
                    continue;
                gui.blitSprite(getFallbackSprite(side),attributes.getSidePosX(side),attributes.getSidePosY(side));
            }
        }
        else {

            for(Direction side : DIRECTIONS) {
                if(!this.isSideVisible(side))
                    continue;
                Quaternionfc rotation = this.attributes.getSideRotation(side);
                BlockState cornerState = this.attributes.displayBlock.defaultBlockState();
                if(this.attributes.displayBlock instanceof IRotatableBlock)
                    cornerState = cornerState.setValue(IRotatableBlock.FACING,Direction.NORTH);
                PendingDrawStates statesToDraw = new PendingDrawStates();
                cornerState = this.attributes.initializeCornerState(side,cornerState);
                statesToDraw.add(0,0,cornerState);
                //Now apply modifiers
                if(this.attributes.wide)
                    statesToDraw.addMutation(this.attributes.getWideStateOffset(side),IWideBlock.ISLEFT);
                if(this.attributes.tall)
                    statesToDraw.addMutation(this.attributes.getTallStateOffset(side),ITallBlock.ISBOTTOM);
                if(this.attributes.deep)
                    statesToDraw.addMutation(this.attributes.getDepthStateOffset(side),IDeepBlock.ISFRONT);

                //Offset to position
                ScreenPosition position = area.pos.offset(this.attributes.getSidePos(side));
                //Sumbit to the final state
                for(var stateToDraw : statesToDraw.statesToDraw) {
                    ScreenPosition statePos = position.offset(stateToDraw.getFirst());
                    //LightmansCurrency.LogDebug("Drawing " + stateToDraw.getSecond() + " at " + statePos);
                    gui.submitPictureInPictureRenderState(new BlockStateRenderState(stateToDraw.getSecond(),statePos.x,statePos.y,rotation));
                }

            }

        }

    }

    private enum SideSize {
        //Naming format = WIDTH_BY_HEIGHT
        ONE_BY_ONE(16,16),
        TWO_BY_ONE(32,16),
        ONE_BY_TWO(16,32),
        TWO_BY_TWO(32,32);
        private final int width;
        private final int height;
        SideSize(int width,int height) {
            this.width = width;
            this.height = height;
        }
        public Identifier spriteForState(DirectionalSettingsState state) {
            return LCApi.id("widget/directional/" + EnumHelper.resourceSafeName(this) + "/" + EnumHelper.resourceSafeName(state));
        }
        static SideSize of(boolean wide,boolean tall) { return wide ? (tall ? TWO_BY_TWO : TWO_BY_ONE) : (tall ? ONE_BY_TWO : ONE_BY_ONE); }
    }

    private static SizedSprite getFallbackSprite(Direction facing) {
        return new SizedSprite.Simple(LCApi.id("widget/directional/fallback/" + facing.getSerializedName()),16,16);
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder extends AbstractBuilder<Builder,DirectionalSettingsWidget> {

        public Builder() { super(0,0); }

        private Supplier<IDirectionalSettingsObject> context = () -> null;
        private Consumer<Direction> handler = d -> {};
        private Consumer<Direction> altHandler = d -> {};

        public Builder forSettings(Supplier<IDirectionalSettingsObject> source) { this.context = source; return this; }
        public Builder onPress(Consumer<Direction> handler) { this.handler = handler; return this; }
        public Builder onAltPress(Consumer<Direction> altHandler) { this.altHandler = altHandler; return this; }
        public Builder onPress(BiConsumer<Direction,Boolean> handler) {
            this.handler = s -> handler.accept(s,false);
            this.altHandler = s -> handler.accept(s,true);
            return this;
        }

        @Override
        protected Builder getSelf() { return this; }
        @Override
        public DirectionalSettingsWidget build() { return new DirectionalSettingsWidget(this); }
    }

    private static class DisplayBlockAttributes {
        public static final DisplayBlockAttributes NULL = new DisplayBlockAttributes(null);

        final boolean tall;
        final int blockHeight;
        final boolean wide;
        final int blockWidth;
        final boolean deep;
        final int blockDepth;
        final Block displayBlock;
        public final boolean isNull() { return this.displayBlock == null; }
        private DisplayBlockAttributes(@Nullable Block displayBlock) { this(displayBlock,null); }
        private DisplayBlockAttributes(@Nullable Block displayBlock, @Nullable Identifier variant) {
            this.displayBlock = displayBlock;
            this.tall = this.displayBlock instanceof ITallBlock;
            this.blockHeight = this.tall ? 2 : 1;
            this.wide = this.displayBlock instanceof IWideBlock;
            this.blockWidth = this.wide ? 2 : 1;
            this.deep = this.displayBlock instanceof IDeepBlock;
            this.blockDepth = this.deep ? 2 : 1;
        }

        public static DisplayBlockAttributes of(Supplier<IDirectionalSettingsObject> context) {
            IDirectionalSettingsObject parent = context.get();
            Block displayBlock = parent == null ? null : parent.getDisplayBlock();
            if(displayBlock != null)
                return new DisplayBlockAttributes(displayBlock);
            return NULL;
        }

        public ScreenPosition getSidePos(Direction side) { return ScreenPosition.of(getSidePosX(side),getSidePosY(side)); }

        public int getSidePosX(Direction side) {
            return switch (side) {
                case WEST -> 0;
                case UP,SOUTH,DOWN -> this.blockDepth * 16 + SPACING;
                default -> this.blockDepth * 16 + this.blockWidth * 16 + DOUBLE_SPACING;
            };
        }

        public int getSidePosY(Direction side) {
            return switch (side) {
                case UP -> 0;
                case WEST,SOUTH,EAST -> this.blockDepth * 16 + SPACING;
                default -> this.blockDepth * 16 + this.blockHeight * 16 + DOUBLE_SPACING;
            };
        }

        public SideSize getSideSize(Direction side) {
            return switch (side) {
                case WEST,EAST -> SideSize.of(this.deep,this.tall);
                case UP,DOWN -> SideSize.of(this.wide,this.deep);
                case SOUTH,NORTH -> SideSize.of(this.wide,this.tall);
            };
        }

        public SizedSprite getSideSprite(Direction side,Supplier<IDirectionalSettingsObject> context) {
            return new SpriteForSide(this.getSideSize(side),side,context);
        }

        public BlockState initializeCornerState(Direction side,BlockState state) {
            if(this.wide)
                state = state.setValue(IWideBlock.ISLEFT,side == Direction.WEST || side == Direction.SOUTH || side == Direction.UP || side == Direction.DOWN);
            if(this.tall)
                state = state.setValue(ITallBlock.ISBOTTOM,side == Direction.DOWN);
            if(this.deep)
                state = state.setValue(IDeepBlock.ISFRONT,side != Direction.WEST && side != Direction.UP);
            return state;
        }

        public ScreenPosition getWideStateOffset(Direction side) {
            return switch (side) {
                case EAST,WEST -> ScreenPosition.ZERO;
                default -> ScreenPosition.of(16,0);
            };
        }

        public ScreenPosition getTallStateOffset(Direction side) {
            return switch (side) {
                case UP,DOWN -> ScreenPosition.ZERO;
                default -> ScreenPosition.of(0,16);
            };
        }

        public ScreenPosition getDepthStateOffset(Direction side) {
            return switch (side) {
                case UP,DOWN -> ScreenPosition.of(0,16);
                case EAST,WEST -> ScreenPosition.of(16,0);
                default -> ScreenPosition.ZERO;
            };
        }

        public Quaternionf getSideRotation(Direction side) {
            return switch (side) {
                case EAST -> new Quaternionf().fromAxisAngleDeg(MathHelper.YP,90f);
                case WEST -> new Quaternionf().fromAxisAngleDeg(MathHelper.YP,-90f);
                case NORTH -> new Quaternionf();
                case SOUTH -> new Quaternionf().fromAxisAngleDeg(MathHelper.YP,180f);
                case DOWN -> new Quaternionf().fromAxisAngleDeg(MathHelper.XP,-90f);
                case UP -> new Quaternionf().fromAxisAngleDeg(MathHelper.XP,90f);
            };
        }

    }

    private record SpriteForSide(SideSize size,Direction side,Supplier<IDirectionalSettingsObject> context) implements SizedSprite {
        @Override
        public Identifier sprite() {
            return this.size.spriteForState(stateForSide(this.context,this.side));
        }
        @Override
        public int width() { return size.width; }
        @Override
        public int height() { return size.height; }
    }

    private static class PendingDrawStates {
        public final List<Pair<ScreenPosition,BlockState>> statesToDraw = new ArrayList<>();

        public void add(int x,int y,BlockState state) { this.add(ScreenPosition.of(x,y),state); }
        public void add(ScreenPosition position,BlockState state) { this.statesToDraw.add(Pair.of(position,state)); }

        public void addFirst(int x,int y,BlockState state) { this.addFirst(ScreenPosition.of(x,y),state); }
        public void addFirst(ScreenPosition position,BlockState state) { this.statesToDraw.addFirst(Pair.of(position,state)); }

        public void addMutation(ScreenPosition offset,BooleanProperty property) { this.addMutation(offset,s -> s.setValue(property,!s.getValue(property))); }
        public void addMutation(ScreenPosition offset,UnaryOperator<BlockState> state) { this.addMutation(p -> p.offset(offset),state); }
        public void addMutation(UnaryOperator<ScreenPosition> position,UnaryOperator<BlockState> state) {
            for(var entry : List.copyOf(this.statesToDraw)) {
                ScreenPosition oldPosition = entry.getFirst();
                ScreenPosition newPosition = position.apply(oldPosition);
                Pair<ScreenPosition,BlockState> newEntry = Pair.of(newPosition,state.apply(entry.getSecond()));
                //If the position is unchanged, then assume this is a background layer and thus it should be drawn first
                if(newPosition.equals(oldPosition))
                    this.statesToDraw.addFirst(newEntry);
                else
                    this.statesToDraw.add(newEntry);
            }
        }

    }

}
