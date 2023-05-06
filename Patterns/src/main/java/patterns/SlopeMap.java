package patterns;

public class SlopeMap extends ObjectMap implements Slope {

	public void addEntry(float position, float f) {
		super.addEntry(position, f);
	}
	
        @Override
	public float valueAt(float position) {
		if (iState != COMPLETE) throw new IllegalStateException("This colormap is not complete");
		else if (position <= 0) return ((Float) map[0].object);
		else if (position >= 1) return ((Float) map[map.length - 1].object);
		else {
			int i = lowIndex(position);
			float a = map[i].position;
			float b = map[i + 1].position;
			float s = (position - a) / (b - a);
			//System.out.println("s = " + s);
			float fa = ((Float) map[i].object);
			float fb = ((Float) map[i + 1].object);
			return fa + s * (fb - fa);
		}
	}
}
