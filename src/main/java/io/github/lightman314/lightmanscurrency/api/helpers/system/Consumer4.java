package io.github.lightman314.lightmanscurrency.api.helpers.system;

@FunctionalInterface
public interface Consumer4<A,B,C,D> {
    void accept(A a,B b,C c,D d);
}
