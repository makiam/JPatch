package jpatch.entity.attributes;

public abstract class Constraint implements Comparable<Constraint> {
	public final IntAttr priority;
	
	public Constraint(int priority) {
		this.priority = new IntAttr(priority);
	}

	public abstract void enforce();
	
	public int compareTo(Constraint constraint) {
		int thisPriority = priority.getInt();
		int otherPriority = constraint.priority.getInt();
		return (thisPriority < otherPriority ? -1 : (thisPriority > otherPriority ? 1 : 0));
	}
}
