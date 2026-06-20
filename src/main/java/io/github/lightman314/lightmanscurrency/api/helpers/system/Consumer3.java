package io.github.lightman314.lightmanscurrency.api.helpers.system;

@FunctionalInterface
public interface Consumer3<A,B,C> {
    void accept(A a,B b,C c);
}
