package jpatch.entity.attributes;

public abstract class Constraint implements AttributeListener, Comparable<Constraint> {
	public final IntAttr priority;
	
	public Constraint(int priority) {
		this.priority = new IntAttr(priority);
	}

	public int compareTo(Constraint constraint) {
		return priority.compareTo(constraint.priority);
	}

	public boolean attributeChanged(AbstractAttribute source) {
		// TODO Auto-generated method stub
		return false;
	}
}
