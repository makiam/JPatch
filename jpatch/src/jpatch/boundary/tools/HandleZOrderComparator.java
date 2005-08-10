package jpatch.boundary.tools;

import java.util.*;
import javax.vecmath.*;

public class HandleZOrderComparator
implements Comparator {
	
	private Point3f p3a = new Point3f();
	private Point3f p3b = new Point3f();
	private Matrix4f m4View;

	public void setMatrix(Matrix4f matrix) {
		m4View = matrix;
	}

	public int compare(Object o1, Object o2) {
		p3a.set(((Handle)o1).getPosition());
		p3b.set(((Handle)o2).getPosition());
		m4View.transform(p3a);
		m4View.transform(p3b);
		return Float.compare(p3a.z, p3b.z);
	}
}

