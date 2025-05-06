package io.github.lightman314.lightmanscurrency.common.traders;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettings;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.DirectionalSettingsState;
import io.github.lightman314.lightmanscurrency.api.misc.settings.directional.IDirectionalSettingsObject;
import io.github.lightman314.lightmanscurrency.api.traders.TraderData;
import io.github.lightman314.lightmanscurrency.api.traders.TraderType;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.SettingsSubTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.TraderSettingsClientTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTab;
import io.github.lightman314.lightmanscurrency.client.gui.screen.inventory.traderstorage.settings.input.InputTabAddon;
import io.github.lightman314.lightmanscurrency.common.util.IconData;
import io.github.lightman314.lightmanscurrency.common.notifications.types.settings.ChangeSettingNotification;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import io.github.lightman314.lightmanscurrency.common.traders.permissions.Permissions;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.BooleanPermission;
import io.github.lightman314.lightmanscurrency.api.traders.permissions.PermissionOption;
import io.github.lightman314.lightmanscurrency.api.network.LazyPacketData;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public abstract class InputTraderData extends TraderData implements IDirectionalSettingsObject {

	public static MutableComponent getFacingName(Direction side) { return LCText.GUI_INPUT_SIDES.get(side).get(); }
	
	public final ImmutableList<Direction> ignoreSides;
	private final DirectionalSettings directionalSettings = new DirectionalSettings(this);
	
	@Override
	protected void modifyDefaultAllyPermissions(Map<String,Integer> defaultValues) {
		defaultValues.put(Permissions.InputTrader.EXTERNAL_INPUTS, 1);
	}
	
	protected InputTraderData(TraderType<?> type) { this(type, ImmutableList.of()); }
	protected InputTraderData(TraderType<?> type, ImmutableList<Direction> ignoreSides) { super(type); this.ignoreSides = ignoreSides; }
	protected InputTraderData(TraderType<?> type, Level level, BlockPos pos) { this(type, level, pos, ImmutableList.of()); }
	protected InputTraderData(TraderType<?> type, Level level, BlockPos pos, ImmutableList<Direction> ignoreSides) {
		super(type, level, pos);
		this.ignoreSides = ignoreSides;
	}

	@Nullable
	@Override
	public Block getDisplayBlock() {
		if(this.getTraderBlock() instanceof BlockItem block)
			return block.getBlock();
		return null;
	}

	@Nullable
	@Override
	public ResourceLocation getVariant() { return this.getTraderBlockVariant(); }

	@Override
	public final List<Direction> getIgnoredSides() { return this.ignoreSides; }

	@Override
	public final DirectionalSettingsState getSidedState(Direction side) { return this.directionalSettings.getState(side); }

	public void setDirectionalState(Player player, Direction side, DirectionalSettingsState state) {
		if(this.hasPermission(player,Permissions.InputTrader.EXTERNAL_INPUTS) && state != this.getSidedState(side))
		{
			if(this.ignoreSides.contains(side))
				return;
			this.directionalSettings.setState(side,state);
			this.markDirty(this::saveDirectionalSettings);

			if(player != null)
				this.pushLocalNotification(ChangeSettingNotification.simple(PlayerReference.of(player), EasyText.empty().append(LCText.DATA_ENTRY_INPUT_OUTPUT_SIDES.get()).append(getFacingName(side)), state.getText()));
		}
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound,HolderLookup.Provider lookup) {
		this.saveDirectionalSettings(compound);
	}
	
	protected final void saveDirectionalSettings(CompoundTag compound) {
		this.directionalSettings.save(compound,"InputOutputState");
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound,HolderLookup.Provider lookup) {

		this.directionalSettings.load(compound,"InputOutputState");

		//Load deprecated input/output sides
		if(compound.contains("InputSides") && this.allowInputs())
		{
			CompoundTag tag = compound.getCompound("InputSides");
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				if(tag.contains(side.toString()) && tag.getBoolean(side.toString()))
				{
					this.directionalSettings.setState(side,DirectionalSettingsState.INPUT);
				}
			}
		}
		
		if(compound.contains("OutputSides") && this.allowOutputs())
		{
			CompoundTag tag = compound.getCompound("OutputSides");
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				if(tag.contains(side.toString()) && tag.getBoolean(side.toString()))
				{
					DirectionalSettingsState state = this.directionalSettings.getState(side);
					if(state.allowsInputs())
						this.directionalSettings.setState(side,DirectionalSettingsState.INPUT_AND_OUTPUT);
					else
						this.directionalSettings.setState(side,DirectionalSettingsState.OUTPUT);
				}
			}
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public abstract IconData inputSettingsTabIcon();
	@OnlyIn(Dist.CLIENT)
	public abstract MutableComponent inputSettingsTabTooltip();
	@OnlyIn(Dist.CLIENT)
	public List<? extends InputTabAddon> inputSettingsAddons() { return ImmutableList.of(); }

	@Override
	public void handleSettingsChange(Player player, LazyPacketData message) {
		super.handleSettingsChange(player, message);

		if(message.contains("SetDirectionalState"))
		{
			DirectionalSettingsState state = DirectionalSettingsState.parse(message.getString("SetDirectionalState"));
			Direction side = Direction.from3DDataValue(message.getInt("Side"));
			this.setDirectionalState(player,side,state);
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addSettingsTabs(TraderSettingsClientTab tab, List<SettingsSubTab> tabs) { tabs.add(new InputTab(tab)); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addPermissionOptions(List<PermissionOption> options) { options.add(BooleanPermission.of(Permissions.InputTrader.EXTERNAL_INPUTS)); }
	
}
