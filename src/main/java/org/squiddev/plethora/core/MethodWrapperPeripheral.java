package org.squiddev.plethora.core;

import dan200.computercraft.api.lua.ILuaContext;
import dan200.computercraft.api.lua.LuaException;
import dan200.computercraft.api.peripheral.IComputerAccess;
import dan200.computercraft.api.peripheral.IPeripheral;
import org.apache.commons.lang3.tuple.Pair;
import org.squiddev.plethora.api.method.ContextKeys;
import org.squiddev.plethora.api.method.IMethod;
import org.squiddev.plethora.api.method.IResultExecutor;
import org.squiddev.plethora.api.method.MethodResult;
import org.squiddev.plethora.core.executor.IExecutorFactory;

import javax.annotation.Nonnull;
import java.util.List;

/**
 * Handles integration with a {@link IPeripheral}
 */
public class MethodWrapperPeripheral extends MethodWrapper implements IPeripheral {
	private final Object owner;
	private final String type;
	private final IExecutorFactory factory;

	private Object delegate;

	public MethodWrapperPeripheral(String name, Object owner, List<IMethod<?>> methods, List<UnbakedContext<?>> contexts, IExecutorFactory factory) {
		super(methods, contexts);
		this.owner = owner;
		this.type = name;
		this.factory = factory;
	}

	public MethodWrapperPeripheral(String name, Object owner, Pair<List<IMethod<?>>, List<UnbakedContext<?>>> methods, IExecutorFactory factory) {
		this(name, owner, methods.getLeft(), methods.getRight(), factory);
	}

	@Nonnull
	@Override
	public String getType() {
		return type;
	}

	@Override
	public Object[] callMethod(@Nonnull IComputerAccess access, @Nonnull ILuaContext luaContext, int method, @Nonnull final Object[] args) throws LuaException, InterruptedException {
		IResultExecutor executor = factory.createExecutor(access);

		UnbakedContext<?> context = getContext(method);
		Object[] extraRef = getReferences(access, luaContext);

		int totalSize = context.keys.length + extraRef.length;
		String[] keys = new String[totalSize];
		Object[] references = new Object[totalSize];
		System.arraycopy(context.keys, 0, keys, 0, context.keys.length);
		System.arraycopy(context.references, 0, references, 0, context.references.length);

		for (int i = 0; i < extraRef.length; i++) {
			keys[context.keys.length + i] = ContextKeys.COMPUTER;
			references[context.keys.length + i] = extraRef[i];
		}

		UnbakedContext<?> full = new UnbakedContext<>(
			context.target, keys, references, context.handler, context.modules, context.executor
		);

		MethodResult result = doCallMethod(getMethod(method), full, args);
		return executor.execute(result, luaContext);
	}

	@Override
	public void attach(@Nonnull IComputerAccess access) {
	}

	@Override
	public void detach(@Nonnull IComputerAccess access) {
	}

	/**
	 * Technically overrides, but only on CC:Tweaked
	 *
	 * @return The current object's owner
	 */
	@Nonnull
	public Object getTarget() {
		return owner;
	}

	protected IExecutorFactory getExecutorFactory() {
		return factory;
	}

	@Override
	public boolean equals(IPeripheral other) {
		if (this == other) return true;
		if (other == null || !(other instanceof MethodWrapperPeripheral)) return false;
		if (!getType().equals(other.getType())) return false;

		MethodWrapperPeripheral otherP = (MethodWrapperPeripheral) other;
		return owner == otherP.owner && equalMethods(otherP);
	}
}
