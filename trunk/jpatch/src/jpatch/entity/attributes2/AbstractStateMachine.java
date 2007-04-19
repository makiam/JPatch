package jpatch.entity.attributes2;

import java.util.*;

public class AbstractStateMachine<T> extends AbstractAttribute {
	/**
	 * A list of possible states of this AbstractStateMachine
	 */
	protected final List<T> states = new ArrayList<T>();
	
	/**
	 * An unmodifyable view of the list of possible states
	 */
	private final List<T> clonedStates = Collections.unmodifiableList(states);
	
	/**
	 * The current state of this state machine
	 */
	protected T currentState;

	/**
	 * Creates a new StateMachine for the specified states and sets the initial state to <i>null</i>.
	 * @param states an array containing the possible states for this StateMachine
	 * @throws NullPointerException if states is <i>null</i>
	 * @throws IllegalArgumentException if states does not contain <i>null</i> or if <i>performStateTransition(null)</i> returns false
	 */
	protected AbstractStateMachine(T[] states) {
		this(states, null);
	}
	
	/**
	 * Creates a new StateMachine for the specified states and sets the initial state to the specified argument.
	 * @param states an array containing the possible states for this StateMachine
	 * @param state the initial state
	 * @throws NullPointerException if states is <i>null</i>
	 * @throws IllegalArgumentException if states does not contain initialState or if <i>performStateTransition(initialState)</i> returns false
	 */
	protected AbstractStateMachine(T[] states, T initialState) {
		for (T s : states) {
			this.states.add(s);
		}
		if (!checkForDuplicates()) {
			throw new IllegalArgumentException("State list " + this.states + " contains duplicate entries");
		}
		if (!switchState(initialState)) {
			throw new IllegalArgumentException("Can't initialize state-machine. Unable to switch state to " + initialState);
		}
	}
	
	/**
	 * Creates a new StateMachine for the specified states and sets the initial state to <i>null</i>.
	 * @param states a list of possible states for this StateMachine
	 * @throws NullPointerException if states is <i>null</i>
	 * @throws IllegalArgumentException if states does not contain <i>null</i> of if <i>performStateTransition(null)</i> returns false
	 */
	protected AbstractStateMachine(Class<? extends Enum> states) {
		this(states, null);
	}
	
	/**
	 * Creates a new StateMachine for the specified states and sets the initial state to the specified argument.
	 * @param states a list of possible states for this StateMachine
	 * @param state the initial state
	 * @throws NullPointerException if states is <i>null</i>
	 * @throws IllegalArgumentException if states does not contain initialState or if <i>performStateTransition(initialState)</i> returns false
	 */
	@SuppressWarnings("unchecked")
	protected AbstractStateMachine(Class<? extends Enum> states, T initialState) {
		for (Enum s : states.getEnumConstants()) {
			this.states.add((T) s);
		}
		if (!checkForDuplicates()) {
			throw new IllegalArgumentException("State list " + this.states + " contains duplicate entries");
		}
		if (!switchState(initialState)) {
			throw new IllegalArgumentException("Can't initialize state-machine. Unable to switch state to " + initialState);
		}
	}
	
	/**
	 * Returns an (unmodifyable) List of possible states of this StateMachine
	 * @return an (unmodifyable) List of possible states of this StateMachine
	 */
	public List<T> getStates() {
		return clonedStates;
	}

	/**
	 * Returns the current state of this StateMachine
	 * @return the current state of this StateMachine
	 */
	public final T getCurrentState() {
		return currentState;
	}

	/**
	 * Causes this StateMachine to transition to <i>newState</i>.
	 * This implementation will check if <i>newState</i> equals the current state and returns false if this is the case.
	 * If <i>newState</i> is a valid state it will call the performStateTransition(<i>newState</i>) method. If performStateTransition(<i>newState</i>)
	 * returned true, it will set <i>currentState</i> to <i>newState</i>, notify registered AttributeListeners by calling the
	 * fireAttributeChanged() method and finally return true.
	 * Note that this method is declared final. Subclasses wishing to implement custom state-switching behavior must override
	 * the performStateTransition(T) method.
	 * @param newState the new state to switch to
	 * @return true if the state transition was successful, false otherwise
	 * @throws IllegalArgumentException if <i>newState</i> is not a legal state of this statemachine
	 */
	public final boolean switchState(T newState) {
		if (newState == currentState) {
			return false;
		}
		if (!states.contains(newState)) {
			throw new IllegalArgumentException(newState + " is not a legal state of this statemachine (" + this + ")");
		}
		if (!performStateTransition(newState)) {
			return false;
		}
		currentState = newState;
		fireAttributeChanged();
		return true;
	}

	/**
	 * Adds <i>state</i> to the list of possible states of this StateMachine
	 * @param state the state to add
	 * @throws IllegalArgumentException if the list of possible states already contained the specified state.
	 */
	public final void addState(T state) {
		if (states.contains(state)) {
			throw new IllegalArgumentException(state + " is already a state of this statemachine (" + this + ")");
		}
		states.add(state);
	}

	/**
	 * Removes <i>state</i> from the list of possible states.
	 * If the state to be removed is the current state, this StateMachine will try to switch to another state, starting with the first state
	 * in the list of possible states. If it can't switch to any other state, an IllegalStateException is thrown.
	 * @param state the state to remove
	 * @throws IllegalStateException if the state to be removed is the current state and this StateMachine is unable to switch to any other state
	 */
	public final void removeState(T state) {
		if (state == currentState) {
			for (T s : states) {
				if (s == state) {
					continue;
				}
				if (switchState(s)) {
					break;
				}
			}
			if (state == currentState) {
				throw new IllegalStateException("can't remove state " + state + " - unable to switch state");
			}
		}
		states.remove(state);
	}

	/**
	 * Checks for duplicates in <i>states</i> (the list of possible states)
	 * @return true, if <i>states</i> is free of duplicates, false otherwise
	 */
	private boolean checkForDuplicates() {
		HashSet<T> set = new HashSet<T>(states);
		return (set.size() == states.size());
	}
	
	/**
	 * Performs the state transition.
	 * This implementation does nothing at all, and returns true. It is intended to be overriden by subclasses.
	 * @param newState the state to switch to
	 * @return true if the state transition was successful, false otherwise
	 */
	protected boolean performStateTransition(T newState) {
		return true;
	}
}
