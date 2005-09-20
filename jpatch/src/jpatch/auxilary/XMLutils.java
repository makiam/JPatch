package jpatch.auxilary;

public class XMLutils {
//	public static StringBuffer indent(int tabs) {
//		StringBuffer sb = new StringBuffer();
//		for (int i = 0; i < tabs; i++) {
//			sb.append("\t");
//		}
//		return sb;
//	}
	
//	public static StringBuffer lineBreak() {
//		return new StringBuffer("\n");
//	}
	
	public static StringBuffer quote(float value) {
		StringBuffer sb = new StringBuffer();
		sb.append("\"").append(value).append("\"");
		return sb;
	}
	
	public static StringBuffer quote(int value) {
		StringBuffer sb = new StringBuffer();
		sb.append("\"").append(value).append("\"");
		return sb;
	}
	
	public static StringBuffer quote(String value) {
		StringBuffer sb = new StringBuffer();
		sb.append("\"").append(value).append("\"");
		return sb;
	}
}
	
