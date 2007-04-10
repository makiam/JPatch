package jpatch.entity;

import java.util.*;

public class Selection implements Collection {
	private Map<Object, Double> selectedObjects = new HashMap<Object, Double>();
	private Iterable<Object> unmodifyableSelectedObjects = Collections.unmodifiableCollection(selectedObjects.keySet());
	
	public Selection() {
		;
	}
	
	public Selection(Selection selection) {
		this();
		copy(selection);
	}
	
	public void copy(Selection selection) {
		clear();
		selectedObjects.putAll(selection.selectedObjects);
	}
	
	public boolean add(Object object) {
		add(object, 1.0);
		return true;
	}
	
	public void add(Object object, double weight) {
		selectedObjects.put(object, weight);
	}
	
	public boolean remove(Object object) {
		selectedObjects.remove(object);
		return true;
	}
	
	public double getWeight(Object object) {
		return selectedObjects.get(object);
	}
	
	public static void main(String[] args) {
		Selection s = new Selection();
		s.add("Test", 1.0);
		
	}

	public boolean addAll(Collection c) {
		for (Object o : c) {
			add(o);
		}
		return true;
	}

	public void clear() {
		selectedObjects.clear();
	}

	public boolean contains(Object o) {
		return selectedObjects.containsKey(o);
	}

	public boolean containsAll(Collection c) {
		return selectedObjects.keySet().containsAll(c);
	}

	public boolean isEmpty() {
		return selectedObjects.isEmpty();
	}

	public Iterator iterator() {
		return unmodifyableSelectedObjects.iterator();
	}

	public Iterator iterator(final Class type) {
		return new Iterator<Object>() {
			private Iterator<Object> it = selectedObjects.keySet().iterator();
			private Object next = advance();
			
			public boolean hasNext() {
				return next != null;
			}

			public Object next() {
				Object tmp = next;
				advance();
				return tmp;
			}

			public void remove() {
				throw new UnsupportedOperationException();
			}
			
			@SuppressWarnings("unchecked")
			private Object advance() {
				while (it.hasNext()) {
					Object object = it.next();
					if (type.isAssignableFrom(object.getClass())) {
						return object;
					}
				}
				return null;
			}
		};
	}
	
	public Iterable filtered(final Class type) {
		return new Iterable() {
			public Iterator iterator() {
				return Selection.this.iterator(type);
			}
		};
	}
	
	public boolean removeAll(Collection c) {
		return selectedObjects.keySet().removeAll(c);
	}

	public boolean retainAll(Collection c) {
		return selectedObjects.keySet().retainAll(c);
	}

	public int size() {
		return selectedObjects.size();
	}

	public Object[] toArray() {
		return selectedObjects.keySet().toArray();
	}

	@SuppressWarnings("unchecked")
	public Object[] toArray(Object[] a) {
		return selectedObjects.keySet().toArray(a);
	}
}
