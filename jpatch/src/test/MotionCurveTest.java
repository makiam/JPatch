package test;

import jpatch.entity.*;

public class MotionCurveTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		MotionCurve motionCurve = MotionCurve.createSizeCurve();
		motionCurve.addKey(new MotionKey.Float(12.0f, 0.0f));
//		motionCurve.addKey(new MotionKey.Float(12.0f, 0.0f));
//		motionCurve.addKey(new MotionKey.Float(24.0f, 0.0f));
//		motionCurve.addKey(new MotionKey.Float(36.0f, 0.0f));
//		motionCurve.addKey(new MotionKey.Float(48.0f, 0.0f));
		
		StringBuffer sb = new StringBuffer();
		motionCurve.xml(sb, "", "");
		System.out.println(sb);
		
		for (int i = -10; i < 50; i++) {
			System.out.println(i+ "\t" + motionCurve.getIndexAt(i));
//			if ( i % 10 == 1)
//				i += 8;
		}
	}

}
