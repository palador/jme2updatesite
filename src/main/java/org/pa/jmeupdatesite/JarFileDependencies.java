package org.pa.jmeupdatesite;

import static org.apache.commons.lang3.Validate.notNull;

import java.util.ArrayList;
import java.util.Set;

/**
 * Provides information about on which jar-files a considered jar-file depend
 * of. Therefore the considered jar-file and all jar-files in the class path are
 * needed.
 */
public class JarFileDependencies {

	private final Set<JarFileDescription> availableJarLibs; //
	private final JarFileDescription jarLib;

	/**
	 * Creates a jar-file dependencies informational instance.
	 * 
	 * @param jarLib
	 *            the considered jar-file, must not be <code>null</code>
	 * @param availableJarLibs
	 *            a set with all jar-files in the class path, must not be
	 *            <code>null</code>l
	 * @throws IllegalArgumentException
	 *             if any argument is <code>null</code>
	 */
	public JarFileDependencies(JarFileDescription jarLib,
			Set<JarFileDescription> availableJarLibs)
			throws IllegalArgumentException {
		this.availableJarLibs = notNull(availableJarLibs,
				"available jar libs must not be null");
		this.jarLib = notNull(jarLib, "jarLib must not be null");

		computeDependencies();
	}

	/**
	 * Compute the dependencies.
	 */
	private void computeDependencies() {
		for (String packageDependency : jarLib.getPackageDependencies()) {

			ArrayList<JarFileDescription> providePackageDependencies = new ArrayList<JarFileDescription>();

			for (JarFileDescription other : availableJarLibs) {
				// don't compare with self
				if (other.equals(jarLib)) {
					continue;
				}

				if (other.getProvidedPackages().contains(packageDependency)) {
					providePackageDependencies.add(other);
				}
			}

			switch (providePackageDependencies.size()) {
			case 0:
				System.err.println("CANNT RESOLVE:       "
						+ jarLib.getFile().getName() + "::"
						+ packageDependency);
				break;
			case 1:
				System.out.println("RESOLVED:            "
						+ jarLib.getFile().getName()
						+ "::"
						+ packageDependency
						+ " --> "
						+ providePackageDependencies.get(0).getFile()
								.getName());
				break;

			default:
				System.err.println("RESOLVED MANY TIMES: "
						+ jarLib.getFile().getName() + "::"
						+ packageDependency);
				for (JarFileDescription other : providePackageDependencies) {
					System.err.println(" --> " + other.getFile().getName());
				}
				break;
			}

		}
	}
}
