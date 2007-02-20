package jpatch.entity.attributes;

public abstract class DoubleConstraint extends Constraint {

	public DoubleConstraint(int priority) {
		super(priority);
	}

	public abstract double enforce(DoubleAttr attribute, double oldValue, double newValue);
}
