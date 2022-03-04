package io.github.lightman314.lightmanscurrency.trader.settings;

import java.util.List;
import java.util.function.BiConsumer;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import io.github.lightman314.lightmanscurrency.trader.ITrader;
import io.github.lightman314.lightmanscurrency.trader.permissions.options.PermissionOption;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class Settings {

	private final IMarkDirty dirtyMarker;
	private final BiConsumer<ResourceLocation,CompoundTag> sendToServer;
	private final ResourceLocation type;
	public final ResourceLocation getType() { return this.type; }
	
	protected final ITrader trader;
	
	protected Settings(ITrader trader, IMarkDirty dirtyMarker, BiConsumer<ResourceLocation,CompoundTag> sendToServer, ResourceLocation type) { this.dirtyMarker = dirtyMarker; this.sendToServer = sendToServer; this.type = type; this.trader = trader; }
	
	public final void markDirty() { this.dirtyMarker.markDirty(); }
	
	public final void sendToServer(CompoundTag updateInfo) { if(updateInfo != null) this.sendToServer.accept(this.type, updateInfo); }
	
	public abstract CompoundTag save(CompoundTag compound);
	
	public abstract void load(CompoundTag compound);
	
	public abstract void changeSetting(Player requestor, CompoundTag updateInfo);
	
	public static final CompoundTag initUpdateInfo(String updateType)
	{
		CompoundTag compound = new CompoundTag();
		compound.putString("UpdateType", updateType);
		return compound;
	}
	
	protected final boolean isUpdateType(CompoundTag updateInfo, String updateType)
	{
		return checkUpdateType(updateInfo, updateType);
	}
	
	public static final boolean checkUpdateType(CompoundTag updateInfo, String updateType)
	{
		if(updateInfo.contains("UpdateType",Tag.TAG_STRING))
			return updateInfo.getString("UpdateType").contentEquals(updateType);
		return false;
	}
	
	public interface IMarkDirty{ public void markDirty(); }
	
	public static final void PermissionWarning(Player player, String action, String permission)
	{
		PermissionWarning(player, action, permission, 0, 1);
	}
	
	public static final void PermissionWarning(Player player, String action, String permission, int hasLevel, int requiredLevel)
	{
		LightmansCurrency.LogWarning(player.getName().getString() + " attempted to " + action + " without the appropriate permission level.\nHas " + permission + " level " + hasLevel + ". Level " + requiredLevel + " required.");
	}
	
	//--------Client Only--------
	
	@OnlyIn(Dist.CLIENT)
	public abstract List<SettingsTab> getSettingsTabs();
	
	@OnlyIn(Dist.CLIENT)
	public abstract List<SettingsTab> getBackEndSettingsTabs();
	
	@OnlyIn(Dist.CLIENT)
	public abstract List<PermissionOption> getPermissionOptions();
	
	
}
