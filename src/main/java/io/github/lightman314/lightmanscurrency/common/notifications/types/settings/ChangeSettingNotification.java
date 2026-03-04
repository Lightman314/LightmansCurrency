package io.github.lightman314.lightmanscurrency.common.notifications.types.settings;

import com.mojang.datafixers.Products;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.lightman314.lightmanscurrency.LCText;
import io.github.lightman314.lightmanscurrency.api.codecs.StreamHelper;
import io.github.lightman314.lightmanscurrency.api.codecs.partial.SPart3;
import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.api.notifications.*;
import io.github.lightman314.lightmanscurrency.common.notifications.categories.NullCategory;
import io.github.lightman314.lightmanscurrency.api.misc.player.PlayerReference;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.StreamCodec;

import javax.annotation.Nullable;
public abstract class ChangeSettingNotification extends SingleLineNotification {

    public static final NotificationType<Dumb> DUMB_TYPE = new DumbType();
    public static final NotificationType<Simple> SIMPLE_TYPE = new SimpleType();
	public static final NotificationType<Advanced> ADVANCED_TYPE = new AdvancedType();

	protected PlayerReference player = PlayerReference.NULL;
	protected Component setting = EasyText.empty();

    protected ChangeSettingNotification() {}
    protected ChangeSettingNotification(PlayerReference player,Component setting,CommonData data) {
        super(data);
        this.player = player;
        this.setting = setting;
    }
	protected ChangeSettingNotification(PlayerReference player, Component setting) { this.player = player; this.setting = setting; }

	@Nullable
	public static ChangeSettingNotification dumb(@Nullable PlayerReference player, Component setting) { return player == null ? null : new Dumb(player,setting); }
	@Nullable
	public static ChangeSettingNotification simple(@Nullable PlayerReference player, Component setting, int newValue) { return simple(player,setting,String.valueOf(newValue)); }
	@Nullable
	public static ChangeSettingNotification simple(@Nullable PlayerReference player, Component setting, boolean newValue) { return simple(player,setting,LCText.GUI_SETTINGS_VALUE_TRUE_FALSE.get(newValue).get()); }
	@Nullable
	public static ChangeSettingNotification simple(@Nullable PlayerReference player, Component setting, String newValue) { return simple(player,setting,EasyText.literal(newValue)); }
	@Nullable
	public static ChangeSettingNotification simple(@Nullable PlayerReference player, Component setting, Component newValue) { return player == null ? null : new Simple(player,setting,newValue); }

	@Nullable
	public static ChangeSettingNotification advanced(@Nullable PlayerReference player, Component setting, int newValue, int oldValue) { return advanced(player,setting,String.valueOf(newValue),String.valueOf(oldValue)); }
	public static ChangeSettingNotification advanced(@Nullable PlayerReference player, Component setting, String newValue, String oldValue) { return advanced(player,setting,EasyText.literal(newValue),EasyText.literal(oldValue)); }
	public static ChangeSettingNotification advanced(@Nullable PlayerReference player, Component setting, Component newValue, Component oldValue) { return player == null ? null : new Advanced(player,setting,newValue,oldValue); }

	@Override
	public NotificationCategory getCategory() { return NullCategory.INSTANCE; }
	
	@Override
	protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
		this.player = PlayerReference.load(compound.getCompound("Player"));
		this.setting = EasyText.loadComponentOrString(compound.getString("Setting"),lookup);
	}

	private static Advanced createAdvanced() { return new Advanced(); }
	private static Simple createSimple() { return new Simple(); }
	private static Dumb createDumb() { return new Dumb(); }

	public static class Advanced extends ChangeSettingNotification
	{

		Component newValue = EasyText.empty();
		Component oldValue = EasyText.empty();

		private Advanced() { }
        private Advanced(Component newValue,Component oldValue,PlayerReference player,Component setting,CommonData data) {
            super(player,setting,data);
            this.newValue = newValue;
            this.oldValue = oldValue;
        }
		private Advanced(PlayerReference player, Component setting, Component newValue, Component oldValue) { super(player, setting); this.newValue = newValue; this.oldValue = oldValue; }
		
        @Override
		public NotificationType<Advanced> getType() { return ADVANCED_TYPE; }

		@Override
		public Component getMessage() { return LCText.NOTIFICATION_SETTINGS_CHANGE_ADVANCED.get(this.player.getName(this.isClient()), this.setting, this.oldValue, this.newValue); }
		
		@Override
		protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
			super.loadAdditional(compound,lookup);
			this.newValue = EasyText.loadComponentOrString(compound.getString("NewValue"),lookup);
			this.oldValue = EasyText.loadComponentOrString(compound.getString("OldValue"),lookup);
		}
		
		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Advanced n)
			{
				return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue) && n.oldValue.equals(this.oldValue);
			}
			return false;
		}
		
	}
	
	public static class Simple extends ChangeSettingNotification
	{

		Component newValue = EasyText.empty();

		private Simple() {}
        private Simple(Component newValue,PlayerReference player,Component setting,CommonData data) {
            super(player,setting,data);
            this.newValue = newValue;
        }
		private Simple(PlayerReference player, Component setting, Component newValue) { super(player, setting); this.newValue = newValue; }
		
        @Override
		public NotificationType<Simple> getType() { return SIMPLE_TYPE; }

		
		@Override
		public Component getMessage() {
			return LCText.NOTIFICATION_SETTINGS_CHANGE_SIMPLE.get(this.player.getName(this.isClient()), this.setting, this.newValue);
		}
		
		@Override
		protected void loadAdditional(CompoundTag compound, HolderLookup.Provider lookup) {
			super.loadAdditional(compound,lookup);
			this.newValue = EasyText.loadComponentOrString(compound.getString("NewValue"),lookup);
		}

		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Simple n)
			{
				return n.player.is(this.player) && n.setting.equals(this.setting) && n.newValue.equals(this.newValue);
			}
			return false;
		}
		
	}


	public static class Dumb extends ChangeSettingNotification
	{

		private Dumb() {}
        private Dumb(PlayerReference player, Component setting, CommonData data) { super(player,setting,data); }
		private Dumb(PlayerReference player, Component setting) { super(player,setting); }

		@Override
		protected Component getMessage() { return LCText.NOTIFICATION_SETTINGS_CHANGE_DUMB.get(this.player.getName(this.isClient()),this.setting); }

		@Override
		public NotificationType<?> getType() { return DUMB_TYPE; }

		@Override
		protected boolean canMerge(Notification other) {
			if(other instanceof Dumb d)
				return d.player.equals(this.player) && d.setting.equals(this.setting);
			return false;
		}
	}

    protected static <T extends ChangeSettingNotification> Products.P3<RecordCodecBuilder.Mu<T>,PlayerReference,Component,CommonData> settingFields(RecordCodecBuilder.Instance<T> builder) {
        return builder.group(PlayerReference.CODEC.fieldOf("player").forGetter(n -> n.player),
                ComponentSerialization.CODEC.fieldOf("setting").forGetter(n -> n.setting),
                baseFields());
    }

    protected static <T extends ChangeSettingNotification> SPart3<RegistryFriendlyByteBuf,T,PlayerReference,Component,CommonData> settingStreamFields(Class<T> clazz) { return settingStreamFields(); }
    protected static <T extends ChangeSettingNotification> SPart3<RegistryFriendlyByteBuf,T,PlayerReference,Component,CommonData> settingStreamFields()
    {
        return SPart3.of(baseStreamFields(),
                PlayerReference.STREAM_CODEC,n -> n.player,
                ComponentSerialization.STREAM_CODEC,n -> n.setting);
    }

    private static class DumbType extends NotificationType<Dumb>
    {
        private static final MapCodec<Dumb> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> settingFields(builder)
                        .apply(builder,Dumb::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,Dumb> STREAM_CODEC = settingStreamFields(Dumb.class)
                .assemble(Dumb::new);

        @Override
        protected Dumb createNew() { return new Dumb(); }
        @Override
        public MapCodec<Dumb> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Dumb> streamCodec() { return STREAM_CODEC; }

    }

    private static class SimpleType extends NotificationType<Simple>
    {
        private static final MapCodec<Simple> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ComponentSerialization.CODEC.fieldOf("value").forGetter(n -> n.newValue)
        ).and(settingFields(builder)).apply(builder,Simple::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,Simple> STREAM_CODEC = StreamHelper.combine(settingStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.newValue,
                Simple::new);

        @Override
        protected Simple createNew() { return new Simple(); }
        @Override
        public MapCodec<Simple> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Simple> streamCodec() { return STREAM_CODEC; }
    }

    private static class AdvancedType extends NotificationType<Advanced>
    {
        private static final MapCodec<Advanced> MAP_CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
                ComponentSerialization.CODEC.fieldOf("value").forGetter(n -> n.newValue),
                ComponentSerialization.CODEC.fieldOf("old").forGetter(n -> n.oldValue)
        ).and(settingFields(builder)).apply(builder,Advanced::new));

        private static final StreamCodec<RegistryFriendlyByteBuf,Advanced> STREAM_CODEC = StreamHelper.combine(settingStreamFields(),
                ComponentSerialization.STREAM_CODEC,n -> n.newValue,
                ComponentSerialization.STREAM_CODEC,n -> n.oldValue,
                Advanced::new);

        @Override
        protected Advanced createNew() { return new Advanced(); }
        @Override
        public MapCodec<Advanced> codec() { return MAP_CODEC; }
        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Advanced> streamCodec() { return STREAM_CODEC; }
    }
	
}
