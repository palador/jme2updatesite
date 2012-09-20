package org.pa.jmeupdatesite;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

public class ClassNameFilters {

	public static final Predicate<String> JRE_CLASS_NAME = new Predicate<String>() {
		public boolean apply(String className) {
			return className != null
					&& (className.startsWith("java.")
							|| className.startsWith("javax.")
							|| className.startsWith("sun.") || className
								.startsWith("com.sun."));
		}
	};
	
	public static  final Predicate<String> NOT_JRE_CLASS_NAME = Predicates.not(JRE_CLASS_NAME);

}
