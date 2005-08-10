package jpatch.boundary;

import java.awt.*;
import java.util.*;
import java.io.*;
import javax.swing.*;
import buoy.widget.*;
import buoy.event.*;

public class BuoyUtils {
	
	public static class FileSelector extends RowContainer {
		private BTextField textField = new BTextField(30);
		private BButton browseButton = new BButton("browse");
		private boolean bFolder;
		private Widget parent;
		private String dir;
		
		public FileSelector(String path, String title, boolean folder, Widget parent) {
			this(path, title, folder, parent, null);
		}
		
		public FileSelector(String path, String title, boolean folder, Widget parent, String dir) {
			bFolder = folder;
			this.parent = parent;
			this.dir = dir;
			//((JButton) browseButton.getComponent()).setPreferredSize(new Dimension(80, 19));
			textField.setText(path);
			add(textField, new LayoutInfo(LayoutInfo.EAST, LayoutInfo.NONE));
			add(browseButton);
			browseButton.addEventLink(CommandEvent.class, this, "browse");
		}
		
		public String getText() {
			return textField.getText();
		}
		
		private void browse() {
			JFileChooser fileChooser = new JFileChooser(dir);
			if (bFolder) fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			else fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			if (!textField.getText().equals(dir + File.separator)) fileChooser.setSelectedFile(new File(textField.getText()));
			if (fileChooser.showDialog(parent.getComponent(), "Select") == JFileChooser.APPROVE_OPTION) {
				textField.setText(fileChooser.getSelectedFile().getPath());
			}
		}
	}
	
	public static class RadioSelector extends RowContainer {
		RadioButtonGroup bg = new RadioButtonGroup();
		private HashMap map = new HashMap();
		
		public RadioSelector(String[] selections, int selectedIndex) {
			for (int i = 0; i < selections.length; i++) {
				BRadioButton button = new BRadioButton(selections[i], i == selectedIndex, bg);
				add(button, new LayoutInfo(LayoutInfo.WEST, LayoutInfo.NONE));
				map.put(button, new Integer(i));
			}
		}
		
		public int getSelectedIndex() {
			return ((Integer) map.get((BRadioButton) bg.getSelection())).intValue();
		}
	}
	
	public static class ColorSelector extends BButton {
		private Color color;
		private Widget parent;
		
		public ColorSelector(Color color, Widget parent) {
			super("             ");
			this.color = color;
			this.parent = parent;
			getComponent().setBackground(color);
			addEventLink(CommandEvent.class, this, "selectColor");
		}
		
		public Color getColor() {
			return color;
		}
		
		private void selectColor() {
			BColorChooser colorChooser = new BColorChooser(color, "select a color");
			colorChooser.showDialog(parent);
			color = colorChooser.getColor();
			getComponent().setBackground(color);
		}
	}
	
	public static BComboBox createComboBox(String[] elements, int selection) {
		BComboBox combo = new BComboBox(elements);
		combo.setSelectedIndex(selection);
		return combo;
	}
}
