package com.jpatch.afw.attributes;

import java.util.*;

/**
 * An Attribute that acts as a state-machine. Pre- and post-change listeners can be added that will be notified
 * before and after a state-transition occured. Subclasses may implement more specialized state-transition behavior.
 * Furthermore, the addStateSetChangeListener() and removeStateSetChangeListener() methods can be used to add or remove listeners
 * respectively that will be notified whenever states were added or removed from this StateMachine.
 * @param <T> The type of possible states
 */
public class StateMachine<T> extends GenericAttr<T> {
	/**
	 * A list of possible states of this StateMachine
	 */
	protected final CollectionAttr<T> states;
	
	/**
	 * The default state of this state machine (may be <i>null</i>)
	 */
	protected T defaultState;
	
	/**
	 * Wheter or not this StateMachine should revert to the default state
	 */
	protected boolean revertToDefault;
	
	/**
	 * Creates a new StateMachine for the specified states and sets the initial state to <i>null</i>.
	 * @param states an array containing the possible states for this StateMachine
	 * @throws NullPointerException if states is <i>null</i>
	 * @throws IllegalArgumentException if states does not contain <i>null</i> or if <i>performStateTransition(null)</i> returns false
	 */
	public StateMachine(T[] states) {
		this(states, null);
	}
	
	/**
	 * Creates a new StateMachine for the specified states and sets the initial state to the specified argument.
	 * @param states an array containing the possible states for this StateMachine
	 * @param state the initial state
	 * @throws NullPointerException if states is <i>null</i>
	 * @throws IllegalArgumentException if states does not contain initialState or if <i>performStateTransition(initialState)</i> returns false
	 */
	public StateMachine(T[] states, T initialState) {
		this.states = new CollectionAttr<T>(LinkedHashSet.class);
		for (T s : states) {
			this.states.add(s);
		}
		if (setValue(initialState) != initialState) {
			throw new IllegalArgumentException("Can't initialize state-machine. Unable to switch state to " + initialState);
		}
	}
	
	/**
	 * Creates a new StateMachine for the specified states and sets the initial state to <i>null</i>.
	 * @param states a list of possible states for this StateMachine
	 * @throws NullPointerException if states is <i>null</i>
	 * @throws IllegalArgumentException if states does not contain <i>null</i> of if <i>performStateTransition(null)</i> returns false
	 */
	public StateMachine(Class<? extends Enum> states) {
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
	public StateMachine(Class<? extends Enum> states, T initialState) {
		this.states = new CollectionAttr<T>(LinkedHashSet.class);
		for (Enum s : states.getEnumConstants()) {
			this.states.add((T) s);
		}
		if (setValue(initialState) != initialState) {
			throw new IllegalArgumentException("Can't initialize state-machine. Unable to switch state to " + initialState);
		}
	}
	
	/**
	 * Creates a new StateMachine for the specified states and sets the initial state to the specified argument.
	 * @param states
	 * @param initialState
	 * @throws NullPointerException if states is <i>null</i>
	 * @throws IllegalArgumentException if states does not contain initialState or if <i>performStateTransition(initialState)</i> returns false
	 */
	public StateMachine(CollectionAttr<T> states, T initialState) {
		this.states = states;
		if (setValue(initialState) != initialState) {
			throw new IllegalArgumentException("Can't initialize state-machine. Unable to switch state to " + initialState);
		}
	}
	
	/**
	 * Returns the default state of this StateMachine
	 * @return the default state of this StateMachine
	 */
	public T getDefaultState() {
		return defaultState;
	}

	/**
	 * Sets the default state of this StateMachine to the specified state and sets the revertToDefault flag to true.
	 * @param defaultState the default state of this StateMachine
	 * @throws IllegalArgumentException if states does not contain defaultState
	 */
	public void setDefaultState(T defaultState) {
		if (!states.contains(defaultState)) {
			throw new IllegalArgumentException(defaultState + " is not a legal state of this statemachine (" + this + ")");
		}
		this.defaultState = defaultState;
		revertToDefault = true;
	}

	/**
	 * Returns the revertToDefault flag
	 * @return true the revertToDefault flag
	 */
	public boolean isRevertToDefault() {
		return revertToDefault;
	}

	/**
	 * Sets the revertToDefault flag
	 * @param revertToDefault
	 */
	public void setRevertToDefault(boolean revertToDefault) {
		this.revertToDefault = revertToDefault;
	}

	/**
	 * Returns an Iterable that iterates over the possible states of this StateMachine.
	 * The order of states is preserved.
	 * @return an Iterable that iterates over the possible states of this StateMachine
	 */
	public Iterable<T> getStates() {
		return states.getElements();
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
	 * @return the state after the state change
	 * @throws IllegalArgumentException if <i>newState</i> is not a legal state of this statemachine
	 */
	@Override
	public final T setValue(T newState) {
		if (!states.contains(newState)) {
			throw new IllegalArgumentException(newState + " is not a legal state of this statemachine (" + this + ")");
		}
		if (newState != value) {
			newState = fireAttributeWillChange(newState);
			value = performStateTransition(newState);
			fireAttributeHasChanged();
		}
		return value;
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
	 * If the state to be removed is the current state, this StateMachine will try to switch to another state. It first tries to switch
	 * to the default state if the revertToDefault flag is set. If the revertToDefault flag is not set or it can't switch to the default
	 * state, it tries to switch to any other state, starting with the first state
	 * in the list of possible states. If it can't switch to any other state, an IllegalStateException is thrown.
	 * @param state the state to remove
	 * @throws IllegalStateException if the state to be removed is the current state and this StateMachine is unable to switch to any other state
	 */
	public final void removeState(T state) {
		if (state == value) {
			if (!revertToDefault()) {
				for (T s : states.getElements()) {
					if (s == state) {
						continue;
					}
					if (setValue(s) == s) {
						break;
					}
				}
			}
			if (state == value) {
				throw new IllegalStateException("can't remove state " + state + " - unable to switch state");
			}
		}
		states.remove(state);
	}

	/**
	 * If the revertToDefault flag is set, this method will try to switch the state to the default state and returns the result
	 * of the <i>setState(defaultState)</i> method. Otherwise it will do nothing and return false.
	 * @return whether the current state has been changed (true if the revertToDefault flag is set and <i>setState(defaultState)</i> returned true, false otherwise)
	 */
	public boolean revertToDefault() {
		if (revertToDefault) {
			return setValue(defaultState) == defaultState;
		} else {
			return false;
		}
	}
	
	/**
	 * Returns the CollectionAttr that contains the states of this StateMachine
	 * @return the CollectionAttr that contains the states of this StateMachine
	 */
	public CollectionAttr<T> getStateSet() {
		return states;
	}
	
	/**
	 * Removes the specified AttributePostChangeListener
	 * @param the listener to be removed
	 * @see addStateSetChangeListener(AttributePostChangeListener) 
	 */
	public void removeStateSetChangeListener(AttributePostChangeListener listener) {
		states.addAttributePostChangeListener(listener);
	}
	
	/**
	 * Performs the state transition.
	 * @param newState the state to switch to
	 * @return the state after the state transition
	 */
	protected T performStateTransition(T newState) {
		return newState;
	}
}