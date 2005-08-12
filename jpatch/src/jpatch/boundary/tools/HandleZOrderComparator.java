package jpatch.boundary.tools;

import java.util.*;
import javax.vecmath.*;

import jpatch.boundary.*;

public class HandleZOrderComparator
implements Comparator {
	
	private Point3f p3a = new Point3f();
	private Point3f p3b = new Point3f();
	private ViewDefinition viewDef;

//	public void setMatrix(Matrix4f matrix) {
//		m4View = matrix;
//	}
	public void setViewDefinition(ViewDefinition viewDef) {
		this.viewDef = viewDef;
	}
	
	public int compare(Object o1, Object o2) {
		p3a.set(((Handle)o1).getPosition(viewDef));
		p3b.set(((Handle)o2).getPosition(viewDef));
		viewDef.getMatrix().transform(p3a);
		viewDef.getMatrix().transform(p3b);
		return Float.compare(p3a.z, p3b.z);
	}
}

