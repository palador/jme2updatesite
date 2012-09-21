package org.pa.jmeupdatesite;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Objects;

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
				System.err
						.println("CANNT RESOLVE:       "
								+ jarLib.getFile().getName() + "::"
								+ packageDependency);
				break;
			case 1:
				System.out
						.println("RESOLVED:            "
								+ jarLib.getFile().getName()
								+ "::"
								+ packageDependency
								+ " --> "
								+ providePackageDependencies.get(0).getFile()
										.getName());
				break;

			default:
				System.err
						.println("RESOLVED MANY TIMES: "
								+ jarLib.getFile().getName() + "::"
								+ packageDependency);
				for (JarFileDescription other : providePackageDependencies) {
					System.err.println(" --> " + other.getFile().getName());
				}
				break;
			}

		}
	}

	/**
	 * Describes the package dependencies of one jar-file to another.
	 */
	public static final class Dependency {
		private final JarFileDescription from;
		final Set<JarFileDescription> toSet = new HashSet<JarFileDescription>();
		final Set<String> packages = new HashSet<String>();
		private int lazyHash;

		Dependency(JarFileDescription from) throws IllegalArgumentException {
			this.from = notNull(from, "from must not be null");
		}

		/**
		 * Returns the left jar-file.
		 * 
		 * @return the left jar-file
		 */
		public File getFrom() {
			return from.getFile();
		}

		/**
		 * Returns the jar-files providing the packages.
		 * 
		 * @return the right jar-file, is
		 */
		public Set<File> getToSet() {
			HashSet<File> result = new HashSet<File>();
			for (JarFileDescription jfd : toSet) {
				result.add(jfd.getFile());
			}
			return result;
		}

		/**
		 * A set with package names the <i>from</i> jar requires from the
		 * <i>to</i> jar.
		 * 
		 * @return a set with package names
		 */
		public Set<String> getPackages() {
			return Collections.unmodifiableSet(packages);
		}

		/**
		 * Returns the description of the right jar-file.
		 * 
		 * @return the description of the right jar-file
		 */
		public JarFileDescription getFromDesc() {
			return from;
		}

		/**
		 * Returns the descriptions of the jar-files the <i>from</i> jar-file
		 * depends on.
		 * 
		 * @return the description of the left jar-file
		 */
		public Set<JarFileDescription> getToDescSet() {
			return toSet;
		}

		/**
		 * Returns whether <i>from</i> has any dependencies to <i>to</i>
		 * 
		 * @return
		 */
		public boolean isDependency() {
			return !packages.isEmpty();
		}

		/**
		 * Returns whether the dependencies are resolved by many jar-files.
		 * 
		 * @return
		 */
		public boolean areDependeciesResolvedByMany() {
			return toSet.size() > 1;
		}

		@Override
		public int hashCode() {
			if (lazyHash == 0) {
				lazyHash = Objects.hashCode(from, toSet, packages);
			}
			return lazyHash;
		}

		@Override
		public boolean equals(Object obj) {
			if (obj == null || obj.getClass() != Dependency.class) {
				return false;
			}
			Dependency other = (Dependency) obj;
			return other.from.equals(from) && other.toSet.equals(toSet)
					&& other.packages.equals(packages);
		}

	}
}
