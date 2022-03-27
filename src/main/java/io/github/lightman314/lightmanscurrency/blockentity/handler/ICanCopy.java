package io.github.lightman314.lightmanscurrency.blockentity.handler;

public interface ICanCopy<T extends ICanCopy<T>> {

	public T copy();
	
}
