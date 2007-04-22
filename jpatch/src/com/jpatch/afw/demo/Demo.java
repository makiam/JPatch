package com.jpatch.afw.demo;

import java.util.Locale;

import com.jpatch.afw.attributes.StateMachine;
import com.jpatch.afw.control.*;

public class Demo {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new Demo();
	}
	
	public Demo() {
		Configuration.getInstance().put("locale", Locale.GERMAN);
		Configuration.getInstance().put("stringResource", "com/jpatch/afw/demo/Strings");
		StateMachine<Integer> stateMachine = new StateMachine<Integer>(new Integer[] { 1, 2, 3 }, 1) {
			@Override
			protected boolean performStateTransition(Integer newState) {
				System.out.println("Switching to state " + newState);
				return true;
			}
		};
		
		SwitchStateAction[] ssa = new SwitchStateAction[] {
				new SwitchStateAction(stateMachine, 1, null, "TEST_1"),
				new SwitchStateAction(stateMachine, 2, null, "TEST_2"),
				new SwitchStateAction(stateMachine, 3, null, "TEST_3")
		};
		
		for (JPatchAction action : ssa) {
			System.out.println(action.getName() + " " + action.getDisplayName());
		}
	}

}
