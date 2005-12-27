//package jpatch.boundary;
//
//import java.awt.*;
//import java.awt.event.*;
//import javax.swing.*;
//import java.util.*;
//
//
//public class ColorPreferences extends JDialog implements ActionListener, WindowListener {
//	
//	/**
//	 * 
//	 */
//	private static final long serialVersionUID = 1L;
//	private JButton buttonBackground = new ColorButton();
//	private JButton buttonPoint = new ColorButton();
//	private JButton buttonCurve = new ColorButton();
//	private JButton buttonHeadPoint = new ColorButton();
//	private JButton buttonMultiPoint = new ColorButton();
//	private JButton buttonSelectedPoint = new ColorButton();
//	private JButton buttonHotPoint = new ColorButton();
//	private JButton buttonSelection = new ColorButton();
//	private JButton buttonText = new ColorButton();
//	private JButton buttonGrid = new ColorButton();
//	private JButton buttonMinorGrid = new ColorButton();
//	private JButton buttonXAxis = new ColorButton();
//	private JButton buttonYAxis = new ColorButton();
//	private JButton buttonZAxis = new ColorButton();
//	private JButton buttonTangent = new ColorButton();
//	private JButton buttonBackface = new ColorButton();
//	
//	private JButton buttonOK = new JButton("OK");
//	private JButton buttonDefaults = new JButton("Defaults");
//	private JButton buttonCancel = new JButton("Cancel");
//	
//	private Map mapping = new HashMap();
//	private ArrayList list = new ArrayList();
//	
//	private JPatchColor cBackground = new JPatchColor("Background");
//	private JPatchColor cPoint = new JPatchColor("Unattached points");
//	private JPatchColor cCurve = new JPatchColor("Curves");
//	private JPatchColor cHeadPoint = new JPatchColor("Points");
//	private JPatchColor cMultiPoint = new JPatchColor("Multiattached points");
//	private JPatchColor cSelected = new JPatchColor("Selected points");
//	private JPatchColor cHot = new JPatchColor("Hot point");
//	private JPatchColor cSelection = new JPatchColor("Tools");
//	private JPatchColor cText = new JPatchColor("Text");
//	private JPatchColor cGrid = new JPatchColor("Grid (major)");
//	private JPatchColor cGridMin = new JPatchColor("Grid (minor)");
//	private JPatchColor cX = new JPatchColor("X Axis");
//	private JPatchColor cY = new JPatchColor("Y Axis");
//	private JPatchColor cZ = new JPatchColor("Z Axis");
//	private JPatchColor cTangent = new JPatchColor("Tangents");
//	private JPatchColor cBackface = new JPatchColor("Backfacing patches");
//	
//	private JPanel colorPanel = new JPanel();
//	private JPanel buttonPanel = new JPanel();
//	
//	private Color[] colors = JPatchUserSettings.getInstance().getColors();
//	private boolean bChanged = false;
//	
//	public ColorPreferences(Frame owner) {
//		super(owner,"Color settings",true);
//		
//		setColors(JPatchUserSettings.getInstance().getColors());
//		
//		mapping.put(buttonBackground,cBackground);
//		mapping.put(buttonText,cText);
//		mapping.put(buttonGrid,cGrid);
//		mapping.put(buttonMinorGrid,cGridMin);
//		mapping.put(buttonCurve,cCurve);
//		mapping.put(buttonHeadPoint,cHeadPoint);
//		mapping.put(buttonPoint,cPoint);
//		mapping.put(buttonMultiPoint,cMultiPoint);
//		mapping.put(buttonSelectedPoint,cSelected);
//		mapping.put(buttonHotPoint,cHot);
//		mapping.put(buttonSelection,cSelection);
//		mapping.put(buttonXAxis,cX);
//		mapping.put(buttonYAxis,cY);
//		mapping.put(buttonZAxis,cZ);
//		mapping.put(buttonTangent,cTangent);
//		mapping.put(buttonBackface,cBackface);
//		
//		list.add(buttonBackground);
//		list.add(buttonText);
//		list.add(buttonGrid);
//		list.add(buttonMinorGrid);
//		list.add(buttonCurve);
//		list.add(buttonHeadPoint);
//		list.add(buttonPoint);
//		list.add(buttonMultiPoint);
//		list.add(buttonSelectedPoint);
//		list.add(buttonHotPoint);
//		list.add(buttonSelection);
//		list.add(buttonXAxis);
//		list.add(buttonYAxis);
//		list.add(buttonZAxis);
//		list.add(buttonTangent);
//		list.add(buttonBackface);
//		
//		setButtonColors();
//		
//		GridLayout layout = new GridLayout(list.size(),2);
//		layout.setHgap(20);
//		layout.setVgap(4);
//		colorPanel.setLayout(layout);
//		colorPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
//		//colorPanel.set
//		for (Iterator it = list.iterator(); it.hasNext(); ) {
//			JButton button = (JButton) it.next();
//			JPatchColor jpatchColor = (JPatchColor) mapping.get(button);
//			colorPanel.add(new JLabel(jpatchColor.getName()));
//			colorPanel.add(button);
//			button.addActionListener(this);
//			//button.setContentAreaFilled(true);
//		}
//		
//		buttonPanel.add(buttonOK);
//		buttonPanel.add(buttonDefaults);
//		buttonPanel.add(buttonCancel);
//		
//		buttonOK.addActionListener(this);
//		buttonDefaults.addActionListener(this);
//		buttonCancel.addActionListener(this);
//		
//		getContentPane().setLayout(new BorderLayout());
//		getContentPane().add(colorPanel, BorderLayout.CENTER);
//		getContentPane().add(buttonPanel, BorderLayout.SOUTH);
//		
//		setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
//		addWindowListener(this);
//		pack();
//		setLocationRelativeTo(owner);
//		setVisible(true);
//	}
//	
//	public static void main(String[] args) {
//		new ColorPreferences(null);
//	}
//	
//	private void setButtonColors() {
//		for (Iterator it = mapping.keySet().iterator(); it.hasNext(); ) {
//			JButton button = (JButton) it.next();
//			JPatchColor jpatchColor = (JPatchColor) mapping.get(button);
//			button.setBackground(jpatchColor.getColor());
//		}
//	}
//	
//	private void setColors(Color[] colors) {
//		cBackground.setColor(colors[0]);
//		cPoint.setColor(colors[1]);
//		cCurve.setColor(colors[2]);
//		cHeadPoint.setColor(colors[3]);
//		cMultiPoint.setColor(colors[4]);
//		cSelected.setColor(colors[5]);
//		cHot.setColor(colors[6]);
//		cSelection.setColor(colors[7]);
//		cText.setColor(colors[8]);
//		cGrid.setColor(colors[9]);
//		cGridMin.setColor(colors[10]);
//		cX.setColor(colors[11]);
//		cY.setColor(colors[12]);
//		cZ.setColor(colors[13]);
//		cTangent.setColor(colors[14]);
//		cBackface.setColor(colors[15]);
//	}
//
//	private Color[] getColors() {
//		return new Color[] {
//			cBackground.getColor(),
//			cPoint.getColor(),
//			cCurve.getColor(),
//			cHeadPoint.getColor(),
//			cMultiPoint.getColor(),
//			cSelected.getColor(),
//			cHot.getColor(),
//			cSelection.getColor(),
//			cText.getColor(),
//			cGrid.getColor(),
//			cGridMin.getColor(),
//			cX.getColor(),
//			cY.getColor(),
//			cZ.getColor(),
//			cTangent.getColor(),
//			cBackface.getColor()
//		};
//	}
//	
//	private void ok() {
//		if (!bChanged) {
//			dispose();
//		} else {
//			int option = JOptionPane.showConfirmDialog(this,"Do you want to keep the color changes you've made?", "Are you sure?", JOptionPane.YES_NO_OPTION);
//			if (option == JOptionPane.YES_OPTION) {
//				dispose();
//			}
//		}
//	}
//	
//	private void defaults() {
//		int option = JOptionPane.showConfirmDialog(this,"This will reset all colors to their default values. Proceed?", "Are you sure?", JOptionPane.YES_NO_OPTION);
//		if (option == JOptionPane.YES_OPTION) {
//			setColors(JPatchUserSettings.getInstance().getDefaultColors());
//			JPatchUserSettings.getInstance().setColors(getColors());
//			setButtonColors();
//			MainFrame.getInstance().getJPatchScreen().update_all();
//			bChanged = false;
//			colors = getColors();
//		}
//	}
//	
//	private void cancel() {
//		if (!bChanged) {
//			dispose();
//		} else {
//			int option = JOptionPane.showConfirmDialog(this,"This will revert to the old colors and you'd loose all changes to the colors you've made. Proceed?", "Are you sure?", JOptionPane.YES_NO_OPTION);
//			if (option == JOptionPane.YES_OPTION) {
//				JPatchUserSettings.getInstance().setColors(colors);
//				dispose();
//				MainFrame.getInstance().getJPatchScreen().update_all();
//			}
//		}
//	}
//	
//	private void close() {
//		if (!bChanged) {
//			dispose();
//		} else {
//			int option = JOptionPane.showConfirmDialog(this,"Do you want to keep the color changes you've made?", "Are you sure?", JOptionPane.YES_NO_OPTION);
//			if (option == JOptionPane.NO_OPTION) {
//				JPatchUserSettings.getInstance().setColors(colors);
//				MainFrame.getInstance().getJPatchScreen().update_all();
//			}
//			dispose();
//		}
//	}
//	
//	private class JPatchColor {
//		private int iRGB;
//		private String strName;
//		
//		public JPatchColor(String name) {
//			strName = name;
//		}
//		
//		public void  setColor(Color color) {
//			iRGB = color.getRGB();
//		}
//		
//		public Color getColor() {
//			return new Color(iRGB);
//		}
//		
//		public String getName() {
//			return strName;
//		}
//	}
//	
//	/*
//	* ActionListener implementation
//	*/
//	
//	public void actionPerformed(ActionEvent actionEvent) {
//		JButton button = (JButton) actionEvent.getSource();
//		if (button == buttonCancel) {
//			cancel();
//		} else if (button == buttonOK) {
//			ok();
//		} else if (button == buttonDefaults) {
//			defaults();
//		} else {
//			JPatchColor jpatchColor = (JPatchColor) mapping.get(actionEvent.getSource());
//			Color color = JColorChooser.showDialog(this,jpatchColor.getName(),jpatchColor.getColor());
//			if (color != null) {
//				jpatchColor.setColor(color);
//				JPatchUserSettings.getInstance().setColors(getColors());
//				setButtonColors();
//				MainFrame.getInstance().getJPatchScreen().update_all();
//				bChanged = true;
//			}
//		}
//	}
//	
//	
//	/*
//	* WindowListener implementation
//	*/
//	
//	public void windowActivated(WindowEvent windowEvent) { }
//	public void windowClosed(WindowEvent windowEvent) { }
//	public void windowDeactivated(WindowEvent windowEvent) { }
//	public void windowDeiconified(WindowEvent windowEvent) { }
//	public void windowIconified(WindowEvent windowEvent) { }
//	public void windowOpened(WindowEvent windowEvent) { }
//	public void windowClosing(WindowEvent windowEvent) {
//		close();
//	}
//}
//
