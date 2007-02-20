package jpatch.entity.attributes;

public abstract class IntConstraint extends Constraint {

	public IntConstraint(int priority) {
		super(priority);
	}

	public abstract int constrainedValue(IntAttr attribute, int oldValue, int newValue);
}
