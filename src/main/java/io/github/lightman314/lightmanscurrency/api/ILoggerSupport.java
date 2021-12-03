package io.github.lightman314.lightmanscurrency.api;

public interface ILoggerSupport<T extends TextLogger> {

	public T getLogger();
	public void clearLogger();
	public void markLoggerDirty();
	
}
