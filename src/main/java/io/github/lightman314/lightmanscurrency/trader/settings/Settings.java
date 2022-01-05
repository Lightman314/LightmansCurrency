package io.github.lightman314.lightmanscurrency.trader.settings;

import java.util.List;
import java.util.function.BiConsumer;

import com.google.common.collect.Lists;

import io.github.lightman314.lightmanscurrency.LightmansCurrency;
import io.github.lightman314.lightmanscurrency.client.gui.settings.SettingsTab;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants;

public abstract class Settings {

	private final IMarkDirty dirtyMarker;
	private final BiConsumer<ResourceLocation,CompoundNBT> sendToServer;
	private final ResourceLocation type;
	public final ResourceLocation getType() { return this.type; }
	
	private List<SettingsTab> settingsTabs = null;
	private List<SettingsTab> backEndTabs = Lists.newArrayList();
	
	protected Settings(IMarkDirty dirtyMarker, BiConsumer<ResourceLocation,CompoundNBT> sendToServer, ResourceLocation type) { this.dirtyMarker = dirtyMarker; this.sendToServer = sendToServer; this.type = type; }
	
	public final void markDirty() { this.dirtyMarker.markDirty(); }
	
	public final void sendToServer(CompoundNBT updateMessage) { if(updateMessage != null) this.sendToServer.accept(this.type, updateMessage); }
	
	public abstract CompoundNBT save(CompoundNBT compound);
	
	public abstract void load(CompoundNBT compound);
	
	public abstract void changeSetting(PlayerEntity requestor, CompoundNBT updateInfo);
	
	protected final CompoundNBT initUpdateInfo(String updateType)
	{
		CompoundNBT compound = new CompoundNBT();
		compound.putString("UpdateType", updateType);
		return compound;
	}
	
	protected final boolean isUpdateType(CompoundNBT updateInfo, String updateType)
	{
		if(updateInfo.contains("UpdateType",Constants.NBT.TAG_STRING))
			return updateInfo.getString("UpdateType").contentEquals(updateType);
		return false;
	}
	
	public interface IMarkDirty{ public void markDirty(); }
	
	public static final void PermissionWarning(PlayerEntity player, String action, String permission)
	{
		PermissionWarning(player, action, permission, 0, 1);
	}
	
	public static final void PermissionWarning(PlayerEntity player, String action, String permission, int hasLevel, int requiredLevel)
	{
		LightmansCurrency.LogWarning(player.getName().getString() + " attempted to " + action + " without the appropriate permission level.\nHas " + permission + " level " + hasLevel + ". Level " + requiredLevel + " required.");
	}
	
	@OnlyIn(Dist.CLIENT)
	protected abstract void initSettingsTabs();
	
	protected final Settings addTab(SettingsTab tab)
	{
		if(!this.settingsTabs.contains(tab) && !this.backEndTabs.contains(tab))
			this.settingsTabs.add(tab);
		return this;
	}
	
	protected final Settings addBackEndTab(SettingsTab tab)
	{
		if(!this.settingsTabs.contains(tab) && !this.backEndTabs.contains(tab))
			this.backEndTabs.add(tab);
		return this;
	}
	
	@OnlyIn(Dist.CLIENT)
	public final List<SettingsTab> getSettingsTabs()
	{
		if(this.settingsTabs == null)
		{
			this.settingsTabs = Lists.newArrayList();
			this.initSettingsTabs();
		}
		return this.settingsTabs;
	}
	
	@OnlyIn(Dist.CLIENT)
	public final List<SettingsTab> getBackEndSettingsTabs()
	{
		if(this.settingsTabs == null)
		{
			this.settingsTabs = Lists.newArrayList();
			this.initSettingsTabs();
		}
		return this.backEndTabs;
	}
	
	
}
