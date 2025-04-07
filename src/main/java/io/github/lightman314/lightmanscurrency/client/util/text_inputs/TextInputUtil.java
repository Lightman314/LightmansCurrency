package io.github.lightman314.lightmanscurrency.client.util.text_inputs;

import io.github.lightman314.lightmanscurrency.api.misc.EasyText;
import io.github.lightman314.lightmanscurrency.client.util.ScreenArea;
import io.github.lightman314.lightmanscurrency.client.util.ScreenPosition;
import io.github.lightman314.lightmanscurrency.util.NumberUtil;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class TextInputUtil {

	public static Builder<String> stringBuilder() { return new Builder<>(s -> s); }
	public static Builder<Integer> intBuilder() { return new Builder<>(IntParser.DEFAULT).filter(NumberUtil::IsIntegerOrEmpty); }
	public static Builder<Long> longBuilder() { return new Builder<>(LongParser.DEFAULT).filter(NumberUtil::IsLongOrEmpty); }
	public static Builder<Float> floatBuilder() { return new Builder<>(FloatParser.DEFAULT).filter(NumberUtil::IsFloatOrEmpty); }
	public static Builder<Double> doubleBuilder() { return new Builder<>(DoubleParser.DEFAULT).filter(NumberUtil::IsDoubleOrEmpty); }

	public static class Builder<T>
	{

		private Builder(Function<String,T> parser) { this.parser = parser; }

		private Font font = Minecraft.getInstance().font;
		private ScreenArea area = ScreenArea.of(0,0,100,20);
		private Consumer<T> handler = v -> {};
		@Nullable
		private Predicate<String> filter = null;
		private Function<String,T> parser;
		String startingValue = "";
		private int maxLength = 32;
		private Component message = EasyText.empty();

		public Builder<T> font(Font font) { this.font = font; return this; }
		public Builder<T> startingString(String value) { this.startingValue = value; return this; }
		public Builder<T> startingValue(T value) { return this.startingString(String.valueOf(value)); }
		public Builder<T> maxLength(int maxLength) { this.maxLength = maxLength; return this; }
		public Builder<T> message(Component message) { this.message = Objects.requireNonNull(message); return this; }

		public Builder<T> position(int x, int y) { this.area = area.atPosition(x,y); return this; }
		public Builder<T> position(ScreenPosition pos) { this.area = area.atPosition(pos); return this; }

		public Builder<T> width(int width) { this.area = this.area.ofSize(width,this.area.height); return this; }
		public Builder<T> height(int height) { this.area = this.area.ofSize(this.area.width,height); return this; }
		public Builder<T> size(int width, int height) { this.area = this.area.ofSize(width,height); return this; }

		public Builder<T> area(ScreenArea area) { this.area = Objects.requireNonNull(area); return this; }

		public Builder<T> handler(Consumer<T> handler) { this.handler = handler; return this; }
		public Builder<T> filter(Predicate<String> filter) { this.filter = filter; return this; }
		public Builder<T> parser(Function<String,T> parser) { this.parser = parser; return this; }

		public Builder<T> apply(Consumer<Builder<T>> application) { application.accept(this); return this; }

		public EditBox build() {
			EditBox box = new EditBox(this.font, this.area.x,this.area.y,this.area.width,this.area.height,this.message);
			box.setValue(this.startingValue);
			if(this.filter != null)
				box.setFilter(this.filter);
			box.setResponder(s -> this.handler.accept(this.parser.apply(s)));
			box.setMaxLength(this.maxLength);
			return box;
		}

	}
	
}
