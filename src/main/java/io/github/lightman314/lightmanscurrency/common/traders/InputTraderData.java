package io.github.lightman314.lightmanscurrency.common.traders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

import io.github.lightman314.lightmanscurrency.LCText;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

public abstract class InputTraderData extends TraderData {

	public static MutableComponent getFacingName(Direction side) { return LCText.GUI_INPUT_SIDES.get(side).get(); }
	
	public final ImmutableList<Direction> ignoreSides;
	private final Map<Direction,Boolean> inputSides = new HashMap<>();
	private final Map<Direction,Boolean> outputSides = new HashMap<>();
	
	@Override
	protected void modifyDefaultAllyPermissions(Map<String,Integer> defaultValues) {
		defaultValues.put(Permissions.InputTrader.EXTERNAL_INPUTS, 1);
	}
	
	protected InputTraderData(@Nonnull TraderType<?> type) { this(type, ImmutableList.of()); }
	protected InputTraderData(@Nonnull TraderType<?> type, @Nonnull ImmutableList<Direction> ignoreSides) { super(type); this.ignoreSides = ignoreSides; }
	protected InputTraderData(@Nonnull TraderType<?> type, @Nonnull Level level, @Nonnull BlockPos pos) { this(type, level, pos, ImmutableList.of()); }
	protected InputTraderData(@Nonnull TraderType<?> type, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull ImmutableList<Direction> ignoreSides) {
		super(type, level, pos);
		this.ignoreSides = ignoreSides;
	}

	public boolean allowInputs() { return true; }
	
	public boolean allowInputSide(Direction side) {
		if(!this.allowInputs())
			return false;
		if(this.ignoreSides.contains(side))
			return false;
		return this.inputSides.getOrDefault(side, false);
	}
	
	public final boolean hasInputSide() {
		for(Direction side : Direction.values())
		{
			if(this.allowInputSide(side))
				return true;
		}
		return false;
	}

	public boolean allowOutputs() { return true; }
	
	public boolean allowOutputSide(Direction side) {
		if(!this.allowOutputs())
			return false;
		if(this.ignoreSides.contains(side))
			return false;
		return this.outputSides.getOrDefault(side, false);
	}
	
	public final boolean hasOutputSide() {
		for(Direction side : Direction.values())
		{
			if(this.allowOutputSide(side))
				return true;
		}
		return false;
	}
	
	public void setInputSide(Player player, Direction side, boolean value) {
		if(!this.allowInputs())
			return;
		if(this.hasPermission(player, Permissions.InputTrader.EXTERNAL_INPUTS) && value != this.allowInputSide(side))
		{
			if(this.ignoreSides.contains(side))
				return;
			this.inputSides.put(side, value);
			this.markDirty(this::saveInputSides);
			
			if(player != null)
				this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "Input-" + getFacingName(side).getString(), String.valueOf(this.allowInputSide(side))));
		}
	}
	
	public void setOutputSide(Player player, Direction side, boolean value) {
		if(!this.allowOutputs())
			return;
		if(this.hasPermission(player, Permissions.InputTrader.EXTERNAL_INPUTS) && value != this.allowOutputSide(side))
		{
			if(this.ignoreSides.contains(side))
				return;
			this.outputSides.put(side, value);
			this.markDirty(this::saveOutputSides);
			
			if(player != null)
				this.pushLocalNotification(new ChangeSettingNotification.Simple(PlayerReference.of(player), "Output-" + getFacingName(side).getString(), String.valueOf(this.allowOutputSide(side))));
		}
	}
	
	@Override
	protected void saveAdditional(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		this.saveInputSides(compound);
		this.saveOutputSides(compound);
	}
	
	protected final void saveInputSides(CompoundTag compound) {
		if(!this.allowInputs())
			return;
		CompoundTag tag = new CompoundTag();
		for(Direction side : Direction.values())
		{
			if(this.ignoreSides.contains(side))
				continue;
			tag.putBoolean(side.toString(), this.allowInputSide(side));
		}
		compound.put("InputSides", tag);
	}
	
	protected final void saveOutputSides(CompoundTag compound) {
		if(!this.allowOutputs())
			return;
		CompoundTag tag = new CompoundTag();
		for(Direction side : Direction.values())
		{
			if(this.ignoreSides.contains(side))
				continue;
			tag.putBoolean(side.toString(), this.allowOutputSide(side));
		}
		compound.put("OutputSides", tag);
	}
	
	@Override
	protected void loadAdditional(CompoundTag compound,@Nonnull HolderLookup.Provider lookup) {
		if(compound.contains("InputSides") && this.allowInputs())
		{
			this.inputSides.clear();
			CompoundTag tag = compound.getCompound("InputSides");
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				if(tag.contains(side.toString()))
					this.inputSides.put(side, tag.getBoolean(side.toString()));
			}
		}
		
		if(compound.contains("OutputSides") && this.allowOutputs())
		{
			this.outputSides.clear();
			CompoundTag tag = compound.getCompound("OutputSides");
			for(Direction side : Direction.values())
			{
				if(this.ignoreSides.contains(side))
					continue;
				if(tag.contains(side.toString()))
					this.outputSides.put(side, tag.getBoolean(side.toString()));
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
	public void handleSettingsChange(@Nonnull Player player, @Nonnull LazyPacketData message) {
		super.handleSettingsChange(player, message);

		if(message.contains("SetInputSide"))
		{
			boolean newValue = message.getBoolean("SetInputSide");
			Direction side = Direction.from3DDataValue(message.getInt("Side"));
			this.setInputSide(player, side, newValue);
		}
		if(message.contains("SetOutputSide"))
		{
			boolean newValue = message.getBoolean("SetOutputSide");
			Direction side = Direction.from3DDataValue(message.getInt("Side"));
			this.setOutputSide(player, side, newValue);
		}
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addSettingsTabs(@Nonnull TraderSettingsClientTab tab, @Nonnull List<SettingsSubTab> tabs) { tabs.add(new InputTab(tab)); }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addPermissionOptions(List<PermissionOption> options) { options.add(BooleanPermission.of(Permissions.InputTrader.EXTERNAL_INPUTS)); }

	@Deprecated
	protected final void loadOldInputSides(CompoundTag compound) {
		this.inputSides.clear();
		for(Direction side : Direction.values())
		{
			if(this.ignoreSides.contains(side))
				continue;
			if(compound.contains(side.toString()))
				this.inputSides.put(side, compound.getBoolean(side.toString()));
		}
	}
	
	@Deprecated
	protected final void loadOldOutputSides(CompoundTag compound) {
		this.outputSides.clear();
		for(Direction side : Direction.values())
		{
			if(this.ignoreSides.contains(side))
				continue;
			if(compound.contains(side.toString()))
				this.outputSides.put(side, compound.getBoolean(side.toString()));
		}
	}
	
}
