package jpatch.entity.attributes;

public abstract class LimitConstraint extends Constraint {
	public final AbstractAttribute limit;
	
	private LimitConstraint(int priority, AbstractAttribute limit) {
		super(priority);
		this.limit = limit;
	}
	
	public static class Minimum extends LimitConstraint {
		public Minimum(int priority, AbstractAttribute limit) {
			super(priority, limit);
		}
		@Override
		public void enforceOn(AbstractAttribute attribute) {
			if (attribute.compareTo(limit) < 0) {
				attribute.overrideValue(limit);
			}
		}
	}
	
	public static class Maximum extends LimitConstraint {
		public Maximum(int priority, AbstractAttribute limit) {
			super(priority, limit);
		}
		@Override
		public void enforceOn(AbstractAttribute attribute) {
			if (attribute.compareTo(limit) > 0) {
				attribute.overrideValue(limit);
			}
		}
	}
}
