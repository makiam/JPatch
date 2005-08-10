package jpatch.boundary.selection;

import java.util.*;
import jpatch.entity.*;

public class PointWeightSelection extends PointSelection{
	private HashMap mapWeight;
	
	public PointWeightSelection() {
		super();
		mapWeight = new HashMap();
	}
	
	public void addControlPoint(ControlPoint controlPoint) {
		super.addControlPoint(controlPoint);
		mapWeight.put(controlPoint,new Float(1));
	}
	
	public void addControlPoint(ControlPoint controlPoint, float weight) {
		super.addControlPoint(controlPoint);
		mapWeight.put(controlPoint,new Float(weight));
	}
	
	public float getWeight(ControlPoint cp) {
		return ((Float)mapWeight.get(cp)).floatValue();
	}
}

