/*
 * $Id: ControlPointBrowser.java,v 1.7 2006/02/01 21:11:28 sascha_l Exp $
 *
 * Copyright (c) 2005 Sascha Ledinsky
 *
 * This file is part of JPatch.
 *
 * JPatch is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPatch is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JPatch; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package jpatch.boundary;

import java.awt.event.*;
import java.util.*;
import javax.swing.*;
import jpatch.entity.*;

/**
 * @author sascha
 * For debugging:
 * Allows to browse through all controlpoints while JPatch is running.
 */
public class ControlPointBrowser extends JDialog implements ActionListener {
	private static final long serialVersionUID = 1L;
	private ControlPoint cp;
	private ControlPoint cpOld;
	private JButton buttonBack = new JButton();
	private JButton buttonNext = new JButton();
	private JButton buttonPrev = new JButton();
	private JButton buttonNextAttached = new JButton();
	private JButton buttonPrevAttached = new JButton();
	private JButton buttonChildHook = new JButton();
	private JButton buttonParentHook = new JButton();
	private JButton buttonRefresh = new JButton("refresh");
	private JButton buttonSync = new JButton("sync");
	private JTextField textLoop = new JTextField();
	private JTextField textDeleted = new JTextField();
	private JTextField textId = new JTextField();
	private JTextField textHookPos = new JTextField();
	private JTextField textGoto = new JTextField();
	
	/**
	 * @param cp The initial controlpoint to be shown in the browser
	 * Creates a new ControlPointBrowser
	 */
	public ControlPointBrowser(ControlPoint cp) {
		super(MainFrame.getInstance(), "Controlpoint browser");	// create new JDialog
		this.cp = cp;
		
		/*
		 * create new form and add contents
		 */
		JPatchForm form = new JPatchForm();
		form.addEntry("Controlpoint ID:", textId);
		form.addEntry("deleted:", textDeleted);
		form.addEntry("loop flag:", textLoop);
		form.addEntry("next:", buttonNext);
		form.addEntry("prev:", buttonPrev);
		form.addEntry("next attached:", buttonNextAttached);
		form.addEntry("prev attached:", buttonPrevAttached);
		form.addEntry("child hook:", buttonChildHook);
		form.addEntry("parent hook:", buttonParentHook);
		form.addEntry("hook pos:", textHookPos);
		form.addEntry("sync:", buttonSync);
		form.addEntry("refresh:", buttonRefresh);
		form.addEntry("back:", buttonBack);
		form.addEntry("goto:", textGoto);
		form.populate();										// add and layout form contents
		
		/*
		 * make textfields readonly
		 */
		textId.setEditable(false);								
		textLoop.setEditable(false);
		textDeleted.setEditable(false);
		textHookPos.setEditable(false);
		
		/*
		 * set alignment of textfields to right
		 */
		textId.setHorizontalAlignment(JTextField.RIGHT);
		textLoop.setHorizontalAlignment(JTextField.RIGHT);
		textDeleted.setHorizontalAlignment(JTextField.RIGHT);
		textHookPos.setHorizontalAlignment(JTextField.RIGHT);
		
		/*
		 * add actionlisteners to buttons 
		 */
		buttonNext.addActionListener(this);						
		buttonPrev.addActionListener(this);
		buttonNextAttached.addActionListener(this);
		buttonPrevAttached.addActionListener(this);
		buttonChildHook.addActionListener(this);
		buttonParentHook.addActionListener(this);
		buttonBack.addActionListener(this);
		buttonRefresh.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				initFields();
				MainFrame.getInstance().setSelection(new Selection(getCp()));
				MainFrame.getInstance().getJPatchScreen().update_all();
			}
		});
		buttonSync.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				Selection selection = MainFrame.getInstance().getSelection();
				if (selection != null && selection.isSingle() && (selection.getHotObject() instanceof ControlPoint)) {
					setCp((ControlPoint) selection.getHotObject());
				}
			}
		});
		
		textGoto.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				try {
					int num = Integer.parseInt(textGoto.getText());
					for (Iterator it = MainFrame.getInstance().getModel().getCurveSet().iterator(); it.hasNext(); ) {
						for (ControlPoint cp = (ControlPoint) it.next(); cp != null; cp = cp.getNextCheckNextLoop()) {
							if (cp.number() == num) {
								setCp(cp);
								MainFrame.getInstance().setSelection(new Selection(cp));
								MainFrame.getInstance().getJPatchScreen().update_all();
								return;
							}
						}
					}
				} catch (NumberFormatException exception) {
					;
				}
			}
		});
		
		/*
		 * set tooltips
		 */
		buttonNext.setToolTipText("go to the next controlpoint");
		buttonPrev.setToolTipText("go to the previous controlpoint");
		buttonNextAttached.setToolTipText("go to the next attached controlpoint");
		buttonPrevAttached.setToolTipText("go to the previous attached controlpoint");
		buttonChildHook.setToolTipText("go to the child hook controlpoint");
		buttonParentHook.setToolTipText("go to the parent hook controlpoint");
		buttonSync.setToolTipText("set the browser to the currently selected controlpoint");
		buttonRefresh.setToolTipText("refresh all values and highlight the browsed controlpoint");
		buttonBack.setToolTipText("go back to the previous controlpoint");
		
		/*
		 * add winowlistener to support windowclosing
		 */
		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent event) {
				ControlPointBrowser.this.dispose();
			}
		});
		initFields();					// initialize buttons and textfields
		getContentPane().add(form);		// add the form to the dialog
		pack();							// layout
		setResizable(false);			// make not resizable
		setVisible(true);				// show
	}
	
	/**
	 * initialize all button lables and textfield contents
	 */
	private void initFields() {
		initButton(buttonBack, cpOld);
		initButton(buttonNext, cp.getNext());
		initButton(buttonPrev, cp.getPrev());
		initButton(buttonNextAttached, cp.getNextAttached());
		initButton(buttonPrevAttached, cp.getPrevAttached());
		initButton(buttonChildHook, cp.getChildHook());
		initButton(buttonParentHook, cp.getParentHook());
		textLoop.setText(cp.getLoop() ? "true" : "false");
		textDeleted.setText(cp.isDeleted() ? "true" : "false");
		textId.setText(cp.toString());
		textHookPos.setText(Float.toString(cp.getHookPos()));
	}
	
	/**
	 * Set the controlpoint to show
	 * @param cp
	 */
	private void setCp(ControlPoint cp) {
		cpOld = this.cp;
		this.cp = cp;
		initFields();
	}
	
	/**
	* @return The currently shown controlpoint
	 */
	private ControlPoint getCp() {
		return cp;
	}
	
	/**
	 * initialize a link button to a controlpoint
	 * @param button the button to inizialize
	 * @param cp to controlpoint to link to
	 */
	private void initButton(JButton button, ControlPoint cp) {
		if (cp != null) {
			button.setText(cp.toString());
			button.setEnabled(true);
		} else {
			button.setText("null");
			button.setEnabled(false);
		}
	}
	
	/**
	 * actionlistener implementation common to all link buttons
	 */
	public void actionPerformed(ActionEvent event) {
		/*
		 * check which link button was pressed and link to the corresponding controlpoint
		 */
		if (event.getSource() == buttonNext) setCp(cp.getNext());
		else if (event.getSource() == buttonPrev) setCp(cp.getPrev());
		else if (event.getSource() == buttonNextAttached) setCp(cp.getNextAttached());
		else if (event.getSource() == buttonPrevAttached) setCp(cp.getPrevAttached());
		else if (event.getSource() == buttonChildHook) setCp(cp.getChildHook());
		else if (event.getSource() == buttonParentHook) setCp(cp.getParentHook());
		else if (event.getSource() == buttonBack) setCp(cpOld);
		
		MainFrame.getInstance().setSelection(new Selection(cp));	// create a new pointselection
		MainFrame.getInstance().getJPatchScreen().update_all();			// update all viewports
	}
}
