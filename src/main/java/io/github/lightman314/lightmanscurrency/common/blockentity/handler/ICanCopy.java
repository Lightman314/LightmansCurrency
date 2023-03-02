package io.github.lightman314.lightmanscurrency.common.blockentity.handler;

public interface ICanCopy<T extends ICanCopy<T>> {

	T copy();
	
}
