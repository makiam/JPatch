package jpatch.entity.attributes;

public abstract class IntLimitConstraint extends IntConstraint {
	public final IntAttr limit;
	public final IntAttr constrainedAttribute;
	
	private IntLimitConstraint(IntAttr constrainedAttribute, int priority, int limit) {
		super(priority);
		this.limit = new IntAttr(limit);
		this.constrainedAttribute = constrainedAttribute;
	}
	
	public static class Min extends IntLimitConstraint {
		public Min(IntAttr constrainedAttribute, int priority, int limit) {
			super(constrainedAttribute, priority, limit);
		}
		@Override
		public int constrainedValue(IntAttr attribute, int oldValue, int newValue) {
			return Math.max(newValue, limit.getInt());
		}
	}
	
	public static class Max extends IntLimitConstraint {
		public Max(IntAttr constrainedAttribute, int priority, int limit) {
			super(constrainedAttribute, priority, limit);
		}
		@Override
		public int constrainedValue(IntAttr attribute, int oldValue, int newValue) {
			return Math.min(newValue, limit.getInt());
		}
	}
}
