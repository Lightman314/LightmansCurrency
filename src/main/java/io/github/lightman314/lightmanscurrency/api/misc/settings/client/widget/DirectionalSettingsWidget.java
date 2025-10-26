package io.github.lightman314.lightmanscurrency.api.misc.settings.client.widget;

import java.util.*;
import java.util.function.*;

import com.google.common.collect.ImmutableList;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IDeepBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IRotatableBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.ITallBlock;
import io.github.lightman314.lightmanscurrency.api.misc.blocks.IWideBlock;
import io.github.lightman314.lightmanscurrency.api.misc.client.rendering.EasyGuiGraphics;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.FixedSizeSprite;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.SpriteSource;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.api.misc.client.sprites.builtin.NormalSprite;
import io.github.lightman314.lightmanscurrency.client.gui.widget.button.PlainButton;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyAddonHelper;
import io.github.lightman314.lightmanscurrency.client.gui.widget.easy.EasyWidgetWithChildren;
import io.github.lightman314.lightmanscurrency.client.model.VariantBlockModel;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.ModelVariantDataManager;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.data.ModelVariant;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.VariantProperties;
import io.github.lightman314.lightmanscurrency.client.resourcepacks.data.model_variants.properties.builtin.InputDisplayOffset;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.MathUtil;
import io.github.lightman314.lightmanscurrency.util.VersionUtil;
import net.minecraft.FieldsAreNonnullByDefault;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class DirectionalSettingsWidget extends EasyWidgetWithChildren {

	private static final Map<Direction,Map<DirectionalSettingsState, FixedSizeSprite>> SIDED_SPRITE_CACHE = new HashMap<>();
	private static final Map<SideSize,Map<DirectionalSettingsState, FixedSizeSprite>> SIZED_SPRITE_CACHE = new HashMap<>();

	public static final ResourceLocation BLOCK_SIDE_TEXTURE = VersionUtil.lcResource("textures/gui/blocksides.png");
	
	private static final List<Direction> DIRECTIONS = ImmutableList.of(Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST);
	//Seperate list for rendering for easy debugging purposes
	private static final List<Direction> RENDER_DIRECTIONS = DIRECTIONS;

	private static final int SPACING = 4;
	
	private final Supplier<IDirectionalSettingsObject> objectSource;
	private final Consumer<Direction> onPress;
	private final Consumer<Direction> onAltPress;
	
	public boolean visible = true;

	enum SideSize {
		//Naming format = WIDTH_BY_HEIGHT
		ONE_BY_ONE(0,16,16),
		TWO_BY_ONE(16,32,16),
		ONE_BY_TWO(48,16,32),
		TWO_BY_TWO(64,32,32);
		private final int uPos;
		private final int uSize;
		private final int vSize;
		SideSize(int uPos,int uSize,int vSize) { this.uPos = uPos; this.uSize = uSize; this.vSize = vSize; }
		private FixedSizeSprite spriteForState(DirectionalSettingsState state) {
			return new NormalSprite(new SpriteSource(BLOCK_SIDE_TEXTURE,this.uPos,this.vSize * state.ordinal(),this.uSize,this.vSize));
		}
		static SideSize of(boolean wide,boolean tall) { return wide ? (tall ? TWO_BY_TWO : TWO_BY_ONE) : (tall ? ONE_BY_TWO : ONE_BY_ONE); }
	}

	private DirectionalSettingsWidget(Builder builder)
	{
		super(builder);
		this.objectSource = builder.objectSource;
		this.onPress = builder.handler;
		this.onAltPress = builder.altHandler;
	}

	@Override
	public void addChildren(ScreenArea area) {
		//Get the attributes
		DisplayBlockAttributes attributes = DisplayBlockAttributes.of(this.objectSource);
		//Recalculate the size
		area = this.recalculateSize(attributes);
		//Generate buttons that only display the frame as the actual block will be drawn below them
		for(Direction side : DIRECTIONS)
		{
			this.addChild(buttonForSide(
					area.pos.offset(attributes.getSidePos(side)),
					spriteForSize(attributes.getSideSize(side),() -> this.stateForSide(side)),
					side));
		}
	}

	private ScreenArea recalculateSize(DisplayBlockAttributes attributes)
	{
		int width = 2 * SPACING;
		//First column width
		width += attributes.deep ? 32 : 16;
		//Second column width
		width += attributes.wide ? 32 : 16;
		//Third column width
		width += attributes.deep || attributes.wide ? 32 : 16;
		this.setWidth(width);

		int height = 2 * SPACING;
		//First row height
		height += attributes.deep ? 32 : 16;
		//Second row height
		height += attributes.tall ? 32 : 16;
		//Third row height
		height += attributes.tall || attributes.deep ? 32 : 16;
		this.setHeight(height);

		this.setX(this.getX() - (width / 2));
		return this.getArea();
	}

	private PlainButton buttonForSide(ScreenPosition position, Supplier<FixedSizeSprite> sprite, Direction side) {
		return PlainButton.builder()
				.position(position)
				.pressAction(() -> this.onButtonPress(side))
				.altPressAction(() -> this.onAltButtonPress(side))
				.sprite(sprite)
				.drawInForeground()
				.addon(EasyAddonHelper.tooltips(tooltipForSide(side)))
				.addon(EasyAddonHelper.visibleCheck(() -> this.isSideVisible(side)))
				.build();
	}

	private Supplier<List<Component>> tooltipForSide(Direction side)
	{
		return () -> {
			List<Component> list = new ArrayList<>();
			list.add(LCText.GUI_INPUT_SIDES.get(side).get());
			DirectionalSettingsState state = this.stateForSide(side);
			if(state != DirectionalSettingsState.NONE)
				list.add(state.getText());
			return list;
		};
	}

	private DirectionalSettingsState stateForSide(Direction side) {
		IDirectionalSettingsObject parent = this.objectSource.get();
		return parent == null ? DirectionalSettingsState.NONE : parent.getSidedState(side);
	}

	private boolean isSideVisible(Direction side) {
		IDirectionalSettingsObject parent = this.objectSource.get();
		return this.isVisible() && parent != null && !parent.getIgnoredSides().contains(side);
	}

	@Override
	protected void renderWidget(EasyGuiGraphics gui) {
		gui.pushOffset(this);
		DisplayBlockAttributes attributes = DisplayBlockAttributes.of(this.objectSource);
		if(attributes.isNull())
		{
			//Render generic background
			for(Direction side : DIRECTIONS)
			{
				if(!this.isSideVisible(side))
					continue;
				gui.blit(BLOCK_SIDE_TEXTURE,attributes.getSidePos(side),96 + 16 * DIRECTIONS.indexOf(side),0,16,16);
			}
		}
		else
		{
			ItemStack item = new ItemStack(attributes.displayBlock.asItem());
			PoseStack pose = gui.getPose();
			BlockRenderDispatcher blockRenderer = Minecraft.getInstance().getBlockRenderer();
			gui.pushOffset(ScreenPosition.ZERO);

			InputDisplayOffset offset = null;
			if(attributes.variant != null)
			{
				ModelVariant variant = ModelVariantDataManager.getVariant(attributes.variant);
				if(variant != null && variant.has(VariantProperties.INPUT_DISPLAY_OFFSET))
					offset = variant.get(VariantProperties.INPUT_DISPLAY_OFFSET);
			}
			for(Direction side : RENDER_DIRECTIONS)
			{
				if(!this.isSideVisible(side))
					continue;
				//Cache rotation for this side
				Quaternionf rotation = attributes.getSideRotation(side);
				//Collect block states for the side
				BlockState defaultState = attributes.displayBlock.defaultBlockState();
				if(attributes.displayBlock instanceof IRotatableBlock)
					defaultState.setValue(IRotatableBlock.FACING,Direction.NORTH);
				PendingDrawStates statesToDraw = new PendingDrawStates();
				switch (side) {
					case WEST : {
						//Only draw the left side of wide blocks
						if(attributes.wide)
							defaultState = defaultState.setValue(IWideBlock.ISLEFT,true);
						if(attributes.tall)
						{
							//Bottom States
							BlockState bottomState = defaultState.setValue(ITallBlock.ISBOTTOM,true);
							BlockState topState = defaultState.setValue(ITallBlock.ISBOTTOM,false);
							if(attributes.deep)
							{
								attributes.addDepthStates(statesToDraw,ScreenPosition.of(16,0),ScreenPosition.of(0,0),topState);
								attributes.addDepthStates(statesToDraw,ScreenPosition.of(16,16),ScreenPosition.of(0,16),bottomState);
							}
							else
							{
								statesToDraw.add(0,0,topState);
								statesToDraw.add(0,16,bottomState);
							}
						}
						else if(attributes.deep)
						{
							attributes.addDepthStates(statesToDraw,ScreenPosition.of(16,0),ScreenPosition.of(0,0),defaultState);
						}
						else 
							statesToDraw.add(0,0,defaultState);
						break;
					}
					case EAST : {
						//Only draw the right side of wide blocks
						if(attributes.wide)
							defaultState = defaultState.setValue(IWideBlock.ISLEFT,false);
						if(attributes.tall)
						{
							//Bottom States
							BlockState bottomState = defaultState.setValue(ITallBlock.ISBOTTOM,true);
							BlockState topState = defaultState.setValue(ITallBlock.ISBOTTOM,false);
							if(attributes.deep)
							{
								attributes.addDepthStates(statesToDraw,ScreenPosition.of(0,0),ScreenPosition.of(16,0),topState);
								attributes.addDepthStates(statesToDraw,ScreenPosition.of(0,16),ScreenPosition.of(16,16),bottomState);
							}
							else
							{
								statesToDraw.add(0,0,topState);
								statesToDraw.add(0,16,bottomState);
							}
						}
						else if(attributes.deep)
						{
							attributes.addDepthStates(statesToDraw,ScreenPosition.of(0,0),ScreenPosition.of(16,0),defaultState);
						}
						else
							statesToDraw.add(0,0,defaultState);
						break;
					}
					case SOUTH: {
						//Only draw the front of the block
						if(attributes.deep)
							defaultState = defaultState.setValue(IDeepBlock.IS_FRONT,true);
						if(attributes.tall)
						{
							//Bottom States
							BlockState topState = defaultState.setValue(ITallBlock.ISBOTTOM,false);
							BlockState bottomState = defaultState.setValue(ITallBlock.ISBOTTOM,true);
							if(attributes.wide)
							{
								attributes.addWidthStates(statesToDraw,ScreenPosition.of(0,0),ScreenPosition.of(16,0),topState);
								attributes.addWidthStates(statesToDraw,ScreenPosition.of(0,16),ScreenPosition.of(16,16),bottomState);
							}
							else
							{
								statesToDraw.add(0,0,topState);
								statesToDraw.add(0,16,bottomState);
							}
						}
						else if(attributes.wide)
						{
							attributes.addWidthStates(statesToDraw,ScreenPosition.of(0,0),ScreenPosition.of(16,0),defaultState);
						}
						else
							statesToDraw.add(0,0,defaultState);
						break;
					}
					case NORTH: {
						//Only draw the back of the block
						if(attributes.deep)
							defaultState = defaultState.setValue(IDeepBlock.IS_FRONT,false);
						if(attributes.tall)
						{
							//Bottom States
							BlockState topState = defaultState.setValue(ITallBlock.ISBOTTOM,false);
							BlockState bottomState = defaultState.setValue(ITallBlock.ISBOTTOM,true);
							if(attributes.wide)
							{
								attributes.addWidthStates(statesToDraw,ScreenPosition.of(16,0),ScreenPosition.of(0,0),topState);
								attributes.addWidthStates(statesToDraw,ScreenPosition.of(16,16),ScreenPosition.of(0,16),bottomState);
							}
							else
							{
								statesToDraw.add(0,0,topState);
								statesToDraw.add(0,16,bottomState);
							}
						}
						else if(attributes.wide)
						{
							attributes.addWidthStates(statesToDraw,ScreenPosition.of(16,0),ScreenPosition.of(0,0),defaultState);
						}
						else
							statesToDraw.add(0,0,defaultState);
						break;
					}
					case UP: {
						//Only draw the top of the block
						if(attributes.tall)
							defaultState = defaultState.setValue(ITallBlock.ISBOTTOM,false);
						if(attributes.deep)
						{
							BlockState frontState = defaultState.setValue(IDeepBlock.IS_FRONT,true);
							BlockState backState = defaultState.setValue(IDeepBlock.IS_FRONT,false);
							if(attributes.wide)
							{
								attributes.addWidthStates(statesToDraw,ScreenPosition.of(0,0),ScreenPosition.of(16,0),backState);
								attributes.addWidthStates(statesToDraw,ScreenPosition.of(0,16),ScreenPosition.of(16,16),frontState);
							}
							else
							{
								statesToDraw.add(0,0,backState);
								statesToDraw.add(0,16,frontState);
							}
						}
						else if(attributes.wide)
						{
							attributes.addWidthStates(statesToDraw,ScreenPosition.of(0,0),ScreenPosition.of(16,0),defaultState);
						}
						else
							statesToDraw.add(0,0,defaultState);
						break;
					}
					case DOWN: {
						//Only draw the bottom of the block
						if(attributes.tall)
							defaultState = defaultState.setValue(ITallBlock.ISBOTTOM,true);
						if(attributes.deep)
						{
							BlockState frontState = defaultState.setValue(IDeepBlock.IS_FRONT,true);
							BlockState backState = defaultState.setValue(IDeepBlock.IS_FRONT,false);
							if(attributes.wide)
							{
								attributes.addWidthStates(statesToDraw,ScreenPosition.of(0,0),ScreenPosition.of(16,0),frontState);
								attributes.addWidthStates(statesToDraw,ScreenPosition.of(0,16),ScreenPosition.of(16,16),backState);
							}
							else
							{
								statesToDraw.add(0,0,frontState);
								statesToDraw.add(0,16,backState);
							}
						}
						else if(attributes.wide)
						{
							attributes.addWidthStates(statesToDraw,ScreenPosition.of(0,0),ScreenPosition.of(16,0),defaultState);
						}
						else
							statesToDraw.add(0,0,defaultState);
						break;
					}
				}

				//Offset to position
				ScreenPosition position = attributes.getSideRenderPos(side).offset(this.getPosition());

				//Enable blending and set up matrix
				RenderSystem.enableBlend();

				for(var stateToDraw : statesToDraw.statesToDraw)
				{
					pose.pushPose();

					//Translate to position
					pose.translate(position.x,position.y,32);
					pose.translate(stateToDraw.getFirst().x,stateToDraw.getFirst().y,0);
					//Offset based on defined variant property
					if(offset != null)
					{
						ScreenPosition temp = offset.getOffset(side);
						pose.translate(temp.x,temp.y,0);
					}

					//Rotate & scale
					pose.mulPose(rotation);
					pose.scale(16,-16,16);

					ModelData modelData = ModelData.EMPTY;
					if(attributes.variant != null)
					{
						modelData = ModelData.builder()
								.with(VariantBlockModel.VARIANT,attributes.variant)
								.with(VariantBlockModel.STATE,stateToDraw.getSecond())
								.build();

					}

					//Render
					blockRenderer.renderSingleBlock(stateToDraw.getSecond(),pose,gui.getGui().bufferSource(), LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, modelData, RenderType.CUTOUT);

					pose.popPose();
				}

				RenderSystem.disableBlend();
			}
			gui.popOffset();
		}
		gui.popOffset();
	}

	
	private static Supplier<FixedSizeSprite> spriteForSide(Direction side, Supplier<DirectionalSettingsState> value) { return () -> getSprite(side,value.get()); }

	
	private static FixedSizeSprite getSprite(Direction side, DirectionalSettingsState state) {
		if(!SIDED_SPRITE_CACHE.containsKey(side))
			SIDED_SPRITE_CACHE.put(side,new HashMap<>());
		Map<DirectionalSettingsState, FixedSizeSprite> stateMap = SIDED_SPRITE_CACHE.get(side);
		if(!stateMap.containsKey(state))
			stateMap.put(state, new NormalSprite(new SpriteSource(BLOCK_SIDE_TEXTURE, getSideU(side), state.ordinal() * 16, 16, 16)));
		return stateMap.get(state);
	}

	private static Supplier<FixedSizeSprite> spriteForSize(SideSize size, Supplier<DirectionalSettingsState> value) { return () -> getSprite(size,value.get()); }
	private static FixedSizeSprite getSprite(SideSize size, DirectionalSettingsState state) {
		if(!SIZED_SPRITE_CACHE.containsKey(size))
			SIZED_SPRITE_CACHE.put(size,new HashMap<>());
		Map<DirectionalSettingsState, FixedSizeSprite> stateMap = SIZED_SPRITE_CACHE.get(size);
		if(!stateMap.containsKey(state))
			stateMap.put(state,size.spriteForState(state));
		return stateMap.get(state);
	}
	
	private int getSidePosX(Direction side)
	{
		return switch (side) {
			case UP, SOUTH, DOWN -> 16 + SPACING;
			case EAST, NORTH -> 2 * (16 + SPACING);
			default -> 0;
		};
	}
	
	private int getSidePosY(Direction side)
	{
		return switch (side) {
			case WEST, SOUTH, EAST -> 16 + SPACING;
			case DOWN, NORTH -> 2 * (16 + SPACING);
			default -> 0;
		};
	}
	
	private static int getSideU(Direction side) { return side.get3DDataValue() * 16; }
	
	private void onButtonPress(Direction side) { this.onPress.accept(side); }

	private void onAltButtonPress(Direction side) { this.onAltPress.accept(side); }

	@Override
	public boolean hideFromMouse() { return true; }

	public static Builder builder() { return new Builder(); }

	@FieldsAreNonnullByDefault
	public static class Builder extends EasyBuilder<Builder>
	{
		private Builder() { super(0,0); }
		@Override
		protected Builder getSelf() { return this; }

		private Supplier<IDirectionalSettingsObject> objectSource = () -> null;
		private Consumer<Direction> handler = d -> {};
		private Consumer<Direction> altHandler = d -> {};

		public Builder object(Supplier<IDirectionalSettingsObject> objectSource) { this.objectSource = objectSource; return this; }
		public Builder handler(Consumer<Direction> handler) { this.handler = handler; return this; }
		public Builder altHandler(Consumer<Direction> handler) { this.altHandler = handler; return this; }
		public Builder handlers(BiConsumer<Direction,Boolean> handler) {
			this.handler = s -> handler.accept(s,false);
			this.altHandler = s -> handler.accept(s,true);
			return this;
		}

		public DirectionalSettingsWidget build() { return new DirectionalSettingsWidget(this); }

	}

	private static class DisplayBlockAttributes
	{

		public static final DisplayBlockAttributes NULL = new DisplayBlockAttributes(null,null);

		final boolean tall;
		final int blockHeight;
		final boolean wide;
		final int blockWidth;
		final boolean deep;
		final int blockDepth;
		final Block displayBlock;
		final ResourceLocation variant;
		public final boolean isNull() { return this.displayBlock == null; }
		private DisplayBlockAttributes(@Nullable Block displayBlock,@Nullable ResourceLocation variant)
		{
			this.displayBlock = displayBlock;
			this.variant = variant;
			this.tall = this.displayBlock instanceof ITallBlock;
			this.blockHeight = this.tall ? 2 : 1;
			this.wide = this.displayBlock instanceof IWideBlock;
			this.blockWidth = this.wide ? 2 : 1;
			this.deep = this.displayBlock instanceof IDeepBlock;
			this.blockDepth = this.deep ? 2 : 1;
		}

		public static DisplayBlockAttributes of(Supplier<IDirectionalSettingsObject> objectSource) {
			IDirectionalSettingsObject parent = objectSource.get();
			Block displayBlock = parent == null ? null : parent.getDisplayBlock();
			if(displayBlock != null)
				return new DisplayBlockAttributes(displayBlock,parent.getVariant());
			return NULL;
		}

		public ScreenPosition getSidePos(Direction side) { return ScreenPosition.of(getSidePosX(side),getSidePosY(side)); }

		public int getSidePosX(Direction side) {
			return switch (side) {
				case WEST -> 0;
				case UP,SOUTH,DOWN -> this.blockDepth * 16 + SPACING;
				default -> this.blockDepth * 16 + this.blockWidth * 16 + SPACING + SPACING;
			};
		}

		public int getSidePosY(Direction side) {
			return switch (side) {
				case UP -> 0;
				case WEST,SOUTH,EAST -> this.blockDepth * 16 + SPACING;
				default -> this.blockDepth * 16 + this.blockHeight * 16 + SPACING + SPACING;
			};
		}

		public ScreenPosition getSideRenderPos(Direction side) {
			ScreenPosition pos = this.getSidePos(side).offset(0,16);
			return switch (side) {
				//case WEST,SOUTH -> pos.offset(0,16);
				//case EAST -> pos.offset(16,16);
				case EAST,NORTH -> pos.offset(16,0);
				case UP -> pos.offset(0,-16);
				default -> pos;
			};
		}

		public SideSize getSideSize(Direction side) {
			return switch (side) {
				case WEST,EAST -> SideSize.of(this.deep,this.tall);
				case UP,DOWN -> SideSize.of(this.wide,this.deep);
				case SOUTH,NORTH -> SideSize.of(this.wide,this.tall);
			};
		}

		public Quaternionf getSideRotation(Direction side) {
			return switch (side) {
				case WEST -> MathUtil.fromAxisAngleDegree(MathUtil.getYP(),90f);
				case EAST -> MathUtil.fromAxisAngleDegree(MathUtil.getYP(),-90f);
				case SOUTH -> new Quaternionf();
				case NORTH -> MathUtil.fromAxisAngleDegree(MathUtil.getYP(),180f);
				case UP -> MathUtil.fromAxisAngleDegree(MathUtil.getXP(),-90f);
				case DOWN -> MathUtil.fromAxisAngleDegree(MathUtil.getXP(),90f);
			};
		}

		public void addWidthStates(PendingDrawStates list, ScreenPosition leftPos, ScreenPosition rightPos, BlockState defaultState)
		{
			BlockState temp = defaultState.setValue(IWideBlock.ISLEFT,true);
			list.add(leftPos,temp);
			temp = defaultState.setValue(IWideBlock.ISLEFT,false);
			list.add(rightPos,temp);
		}

		public void addDepthStates(PendingDrawStates list, ScreenPosition frontPos, ScreenPosition backPos, BlockState defaultState)
		{
			BlockState temp = defaultState.setValue(IDeepBlock.IS_FRONT,true);
			list.add(frontPos,temp);
			temp = defaultState.setValue(IDeepBlock.IS_FRONT,false);
			list.add(backPos,temp);
		}

	}

	private static class PendingDrawStates
	{
		private final List<Pair<ScreenPosition,BlockState>> statesToDraw = new ArrayList<>();

		public void add(int x, int y,BlockState state) { this.statesToDraw.add(Pair.of(ScreenPosition.of(x,y),state));}
		public void add(ScreenPosition position,BlockState state) { this.statesToDraw.add(Pair.of(position,state));}

	}
	
}
