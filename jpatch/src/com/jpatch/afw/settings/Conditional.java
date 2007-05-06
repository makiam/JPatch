package com.jpatch.afw.settings;

import java.lang.annotation.*;

@Retention(value=RetentionPolicy.RUNTIME)
public @interface Conditional {
	String dependsOn();
	String value();
}
