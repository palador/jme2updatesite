package org.pa.jmeupdatesite;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

/**
 * Provides some filters for class names.
 */
public class ClassNameFilters {

	/**
	 * Filter witch matches on JRE or JDK class names.
	 */
	public static final Predicate<String> JRE_CLASS_NAME = new Predicate<String>() {
		public boolean apply(String className) {
			return className != null
					&& (className.startsWith("java.")
							|| className.startsWith("javax.")
							|| className.startsWith("sun.") || className
								.startsWith("com.sun."));
		}
	};
	
	/**
	 * Filter which matches on classes witch are NOT provided by the JRE or JDK.
	 */
	public static  final Predicate<String> NOT_JRE_CLASS_NAME = Predicates.not(JRE_CLASS_NAME);

}
