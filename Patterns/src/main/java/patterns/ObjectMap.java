package patterns;

class ObjectMap {
	class ObjectMapEntry {
		float position;
		Object object;
		
		ObjectMapEntry(float position, Object object) {
			this.position = position;
			this.object = object;
		}
		
		public String toString() {
			return position + "\t" + object;
		}
	}
	static final int EMPTY = 0;
	static final int INCOMPLETE = 1;
	static final int COMPLETE = 2;
	
	ObjectMapEntry[] map = new ObjectMapEntry[0];
	float fPos = 0;
	int iState = EMPTY;
	
	/**
	* The map must be filled in ascending order, starting with an entry at position 0 and ending with an entry
	* at position 1.
	**/
	void addEntry(float position, Object object) {
		if (position < fPos) throw new IllegalArgumentException("position " + position + " < last position " + fPos);
		else if (position > 1) throw new IllegalArgumentException("position " + position + " > 1");
		else if (iState == EMPTY && position != 0) throw new IllegalStateException("First entry must be at position 0, was " + position);
		//else if (iState == COMPLETE) throw new IllegalStateException("This map is already complete (last entry at position 1)");
		else {
			appendEntry(position, object);
			if (position == 0) iState = INCOMPLETE;
			else if (position == 1) iState = COMPLETE;
		}
	}
	
	public String toString() {
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < map.length; sb.append(i + "\t").append(map[i++].toString()).append("\n"));
		return sb.toString();
	}
	
	void appendEntry(float position, Object object) {
		int l = map.length;
		ObjectMapEntry[] newMap = new ObjectMapEntry[l + 1];
		System.arraycopy(map, 0, newMap, 0, l);
		newMap[l] = new ObjectMapEntry(position, object);
		map = newMap;
		fPos = position;
	}
	
	int lowIndex(float position) {
		int min = 0;
		int max = map.length - 1;
		int i = max >> 1;
		while (max > min + 1) {
			if (map[i].position >= position) {
				max = i;
				i -= ((i - min) >> 1);
			} else {
				min = i;
				i += ((max - i) >> 1);
			}
			//System.out.println(min + " " + i + " " + max);
		}
		//System.out.println("index = " + min);
		return min;
	}
}
