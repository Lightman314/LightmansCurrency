package io.github.lightman314.lightmanscurrency.datagen.common.tags;

import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.DeferredHolderBundle2;
import io.github.lightman314.lightmanscurrency.api.helpers.registry.IOptionalKey;
import net.minecraft.core.Holder;
import net.minecraft.data.tags.TagAppender;
import net.minecraft.tags.TagEntry;
import net.minecraft.tags.TagKey;

import java.util.Collection;
import java.util.function.Function;
import java.util.stream.Stream;

public interface LCTagAppender<E,T> extends TagAppender<E,T> {

    @Override
    LCTagAppender<E, T> add(E element);

    @Override
    default LCTagAppender<E, T> add(E... elements) {
        TagAppender.super.add(elements);
        return this;
    }

    default LCTagAppender<E,T> add(Holder<E> holder) {
        this.add(holder.value());
        return this;
    }

    default LCTagAppender<E,T> add(Holder<E>... holders) {
        for(Holder<E> holder : holders)
            this.add(holder);
        return this;
    }

    @Override
    default LCTagAppender<E, T> addAll(Collection<E> elements) {
        TagAppender.super.addAll(elements);
        return this;
    }

    @Override
    default LCTagAppender<E, T> addAll(Stream<E> elements) {
        TagAppender.super.addAll(elements);
        return this;
    }

    @Override
    LCTagAppender<E, T> addOptional(E element);

    @Override
    LCTagAppender<E, T> addTag(TagKey<T> tag);

    @Override
    LCTagAppender<E, T> addOptionalTag(TagKey<T> tag);

    @Override
    default <U> LCTagAppender<U,T> map(Function<U, E> converter) {
        return new Wrapper<>(TagAppender.super.map(converter));
    }

    @Override
    default LCTagAppender<E, T> addTags(TagKey<T>... values) {
        TagAppender.super.addTags(values);
        return this;
    }

    @Override
    default LCTagAppender<E, T> addOptionalTags(TagKey<T>... values) {
        TagAppender.super.addOptionalTags(values);
        return this;
    }

    @Override
    LCTagAppender<E, T> add(TagEntry entry);

    @Override
    default LCTagAppender<E, T> replace() {
        TagAppender.super.replace();
        return this;
    }

    @Override
    LCTagAppender<E, T> replace(boolean value);

    @Override
    LCTagAppender<E, T> remove(E e);

    @Override
    default LCTagAppender<E, T> remove(E firstE, E... es) {
        TagAppender.super.remove(firstE, es);
        return this;
    }

    @Override
    LCTagAppender<E, T> remove(TagKey<T> tag);

    @Override
    default LCTagAppender<E, T> remove(TagKey<T> first, TagKey<T>... tags) {
        TagAppender.super.remove(first, tags);
        return this;
    }

    default LCTagAppender<E,T> addBundle(DeferredHolderBundle<?,? extends E,? extends E> bundle) { return this.addBundle(bundle,Function.identity()); }
    default <X> LCTagAppender<E,T> addBundle(DeferredHolderBundle<?,? extends X,? extends X> bundle,Function<X,E> mapper) {
        bundle.forEach((key,value) -> {
            E element = mapper.apply(value);
            if(IOptionalKey.isModdedKey(key))
                this.addOptional(element);
            else
                this.add(element);
        });
        return this;
    }

    default LCTagAppender<E,T> addBundle(DeferredHolderBundle2<?,?,? extends E,? extends E> bundle) { return this.addBundle(bundle,Function.identity()); }
    default <X> LCTagAppender<E,T> addBundle(DeferredHolderBundle2<?,?,? extends X,? extends X> bundle,Function<X,E> mapper) {
        bundle.forEach((k1,k2,value) -> {
            E element = mapper.apply(value);
            if(IOptionalKey.isModdedKey(k1) || IOptionalKey.isModdedKey(k2))
                this.addOptional(element);
            else
                this.add(element);
        });
        return this;
    }

    record Wrapper<E,T>(TagAppender<E,T> appender) implements LCTagAppender<E,T> {

        @Override
        public LCTagAppender<E, T> add(E element) {
            this.appender.add(element);
            return this;
        }

        @Override
        public LCTagAppender<E, T> addOptional(E element) {
            this.appender.addOptional(element);
            return this;
        }

        @Override
        public LCTagAppender<E, T> addTag(TagKey<T> tag) {
            this.appender.addTag(tag);
            return this;
        }

        @Override
        public LCTagAppender<E, T> addOptionalTag(TagKey<T> tag) {
            this.appender.addOptionalTag(tag);
            return this;
        }

        @Override
        public LCTagAppender<E, T> add(TagEntry entry) {
            this.appender.add(entry);
            return this;
        }

        @Override
        public LCTagAppender<E, T> replace(boolean value) {
            this.appender.replace(value);
            return this;
        }

        @Override
        public LCTagAppender<E, T> remove(E e) {
            this.appender.remove(e);
            return this;
        }

        @Override
        public LCTagAppender<E, T> remove(TagKey<T> tag) {
            this.appender.remove(tag);
            return this;
        }
    }

}