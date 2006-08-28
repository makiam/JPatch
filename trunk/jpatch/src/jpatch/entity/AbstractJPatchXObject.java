package jpatch.entity;

import java.lang.reflect.Field;
import java.util.Iterator;

public abstract class AbstractJPatchXObject implements JPatchObject {
	
	private Iterable<Attribute> attributes = new Iterable<Attribute>() {
		public Iterator<Attribute> iterator() {
			return createAttributeIterator();
		}
	};
	
	private Iterable<Attribute> channels = new Iterable<Attribute>() {
		public Iterator<Attribute> iterator() {
			return createChannelIterator();
		}
	};
	
	public Iterable<Attribute> getAttributes() {
		return attributes;
	}
	
	public Iterable<Attribute> getChannels() {
		return channels;
	}
	
	public Attribute getAttribute(int index) {
		int i = 0;
		for (Field field : getClass().getFields()) {
			if (Attribute.class.isAssignableFrom(field.getType())) {
				if (i == index) {
					try {
						return (Attribute) field.get(this);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				i++;
			}
		}
		return null;
	}

	private Iterator<Attribute> createAttributeIterator() {
		return new Iterator<Attribute>() {
			private int index = 0;
			
			public boolean hasNext() {
				return getAttribute(index + 1) != null;
			}

			public Attribute next() {
				return getAttribute(index++);
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
		};
	}
	
	private Iterator<Attribute> createChannelIterator() {
		return new Iterator<Attribute>() {
			private int index = searchNextChannel();
			
			public boolean hasNext() {
				return getAttribute(index + 1) != null;
			}

			public Attribute next() {
				Attribute a = getAttribute(index++);
				searchNextChannel();
				return a;
			}
			
			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			private int searchNextChannel() {
				Attribute a;
				for (a = getAttribute(index); a != null; index++)
					if (a.isKeyed())
						break;
				if (a != null)
					index--;
				return index;
			}
		};
	}
}
