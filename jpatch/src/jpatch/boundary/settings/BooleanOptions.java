package jpatch.boundary.settings;

import java.lang.annotation.*;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface BooleanOptions {
	String trueOption() default "true";
	String falseOption() default "false";
}
