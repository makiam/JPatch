package jpatch.entity;

import java.io.*;
import java.util.zip.*;

import jpatch.boundary.settings.Settings;

import org.xml.sax.SAXException;

public abstract class JPatchFile extends AbstractJPatchObject {
	private static XmlLoader xmlLoader;
	
	private final static String SUFFIX = ".xml";
	public Attribute.Name name = new Attribute.Name(this);
	File file;
	private JPatchDirectory directory;
	private boolean rename = false;
	
	static {
		try {
			xmlLoader = new XmlLoader();
		} catch (SAXException e) {
			e.printStackTrace();
		}
	}
	
	JPatchFile(File f) {
		this.file = f;
		name.set(removeSuffix(file.getName()));
		name.addAttributeListener(new AttributeListener() {
			public void attributeChanged(Attribute attribute) {
				if (!rename && !file.renameTo(new File(file.getParentFile(), addSuffix(name.get())))) {
					rename = true;
					name.set(removeSuffix(file.getName()));
					rename = false;
				}
			}
		});
	}
	
	public String getName() {
		return file.getName();
	}

	public void setParent(JPatchObject parent) {
		directory = (JPatchDirectory) parent;
	}
	
	private String addSuffix(String string) {
		return string + SUFFIX;
	}
	
	private String removeSuffix(String string) {
		return string.substring(0, string.length() - SUFFIX.length());
	}
	
	void updateHistory() throws IOException {
		/*
		 * cycle history files
		 */
		File historyDir = new File(file.getParentFile(), "history");
		for (int i = Settings.getInstance().historyDepth - 2; i >= 0; i--) {
			File oldFile = new File(historyDir, getName() + "." + i + ".xml.gz");
			File newFile = new File(historyDir, getName() + "." + (i + 1) + ".xml.gz");
			newFile.delete();
			oldFile.renameTo(newFile);
		}
		
		/*
		 * create new history entry
		 */
		FileInputStream input = new FileInputStream(file);
		File historyFile = new File(historyDir, getName() + ".0.xml.gz");
		GZIPOutputStream output = new GZIPOutputStream(new FileOutputStream(historyFile));
		byte[] buffer = new byte[65536];
		int bytesRead = 0;
		while ((bytesRead = input.read(buffer)) > 0) {
			output.write(buffer, 0, bytesRead);
		}
		input.close();
		output.close();
	}
	
	public static class ModelFile extends JPatchFile {
		private Model model;
		
		public ModelFile(File f) {
			super(f);
		}
		
		public Model loadModel() throws FileNotFoundException, IOException, SAXException {
			return (Model) xmlLoader.parse(new BufferedReader(new FileReader(file)));
		}
		
		public void open() throws FileNotFoundException, IOException, SAXException {
			assert model == null : "Model " + getName() + " is already open.";
			model = loadModel();
		}
		
		public void close() {
			model = null;
		}
		
		public void save() throws IOException {
			/*
			 * Serialize model into ByteArray
			 */
			ByteArrayOutputStream byteArray = new ByteArrayOutputStream();
			model.xml(new PrintStream(byteArray), "");
			
			/*
			 * update history files
			 */
			if (file.exists()) {
				updateHistory();
			}
			
			/*
			 * write ByteArray to file
			 */
			FileOutputStream out = new FileOutputStream(file);
			byteArray.writeTo(out);
			out.close();
			byteArray.close();
		}
	}
	
	public static class ChoreographyFile extends JPatchFile {
		public ChoreographyFile(File f) {
			super(f);
		}
	}
}
