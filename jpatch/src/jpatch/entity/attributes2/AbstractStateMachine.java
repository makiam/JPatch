package jpatch.entity.attributes2;

import java.util.*;

public abstract class AbstractStateMachine<T> extends AbstractAttribute {
	protected final List<T> states = new ArrayList<T>();
	private final List<T> clonedStates = Collections.unmodifiableList(states);
	
	protected T currentState;

	protected AbstractStateMachine() { }
	
	protected AbstractStateMachine(T[] states) {
		this(states, null);
	}
	
	protected AbstractStateMachine(T[] states, T state) {
		for (T s : states) {
			this.states.add(s);
		}
		enterState(state);
		currentState = state;
	}
	
	protected AbstractStateMachine(Class<? extends Enum> states) {
		this(states, null);
	}
	
	@SuppressWarnings("unchecked")
	protected AbstractStateMachine(Class<? extends Enum> states, T state) {
		for (Enum s : states.getEnumConstants()) {
			this.states.add((T) s);
		}
		enterState(state);
		currentState = state;
	}
	
	public List<T> getStates() {
		return clonedStates;
	}

	public T getCurrentState() {
		return currentState;
	}

	public boolean switchState(T state) {
		if (state == currentState) {
			return false;
		}
		if (!states.contains(state)) {
			throw new IllegalArgumentException(state + " is not a legal state of this statemachine");
		}
		exitState(state);
		enterState(state);
		currentState = state;
		fireAttributeChanged();
		return true;
	}

	public void addState(T state) {
		states.add(state);
	}

	public void removeState(T state) {
		if (state == currentState) {
			switchState(states.get(0));
		}
		states.remove(state);
	}

	protected abstract void enterState(T newState);

	protected abstract void exitState(T newState);
}
