package org.pa.jmeupdatesite;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
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

	// dependencies for each single package
	private final Set<Dependency> dependencies = new HashSet<Dependency>();
	private final Set<String> unresolvedPackages = new HashSet<String>();

	/*
	 * cached results
	 */
	private Set<Dependency> oneToOneDependencies;
	private Set<Dependency> oneToManyDependencies;

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
	 * Compute the dependencies. Called once at initialization. Each computed
	 * dependency describes a single package here.
	 */
	private void computeDependencies() {
		for (String packageDependency : jarLib.getPackageDependencies()) {

			Dependency dependency = new Dependency(jarLib);
			dependency.packages.add(packageDependency);

			for (JarFileDescription other : availableJarLibs) {
				// don't compare with self
				if (other.equals(jarLib)) {
					continue;
				}

				if (other.getProvidedPackages().contains(packageDependency)) {
					dependency.toSet.add(other);
				}
			}

			if (dependency.toSet.isEmpty()) { // no jar-file is able to resolve
												// package dependency
				unresolvedPackages.add(packageDependency);
			} else { // resolved
				dependencies.add(dependency);
			}
		}
	}

	/**
	 * Returns a set of dependencies which are resolved by exactly one jar-file.
	 * Each dependency contains at least one package name.
	 * 
	 * @return a unmodifiable set of one to one dependencies
	 */
	public Set<Dependency> getOneToOneDependencies() {
		if (oneToOneDependencies == null) {
			ArrayList<Dependency> oneToOneList = new ArrayList<Dependency>();


			for (Dependency singlePckDep : dependencies) {
				if (singlePckDep.isOneToMany()) {
					continue;
				}
				JarFileDescription to = singlePckDep.getToDescSet().iterator()
						.next();

				Dependency resultDep = findDependencyByToOrCreate(to,
						oneToOneList);
				resultDep.packages.addAll(singlePckDep.getPackages());
			}
			oneToOneDependencies = new HashSet<Dependency>(oneToOneList);
		}

		return oneToOneDependencies;
	}

	/**
	 * Returns a set of dependencies which are resolved by more than one
	 * jar-file. Each dependency contains exactly one package name and mode than
	 * one 'to' jar-files.
	 * 
	 * @return a unmodifiable set of one to one dependencies
	 */
	public Set<Dependency> getOneToManyDependencies() {
		if (oneToManyDependencies == null) {
			ArrayList<Dependency> oneToManyList = new ArrayList<JarFileDependencies.Dependency>();


			for (Dependency singlePckDep : dependencies) {
				if (singlePckDep.isOneToOne()) {
					continue;
				}
				for (JarFileDescription to : singlePckDep.getToDescSet()) {
					String packageName = singlePckDep.packages.iterator()
							.next();

					// find oneToMany with package
					Dependency oneToOneWithPckg = null;
					for (Dependency oneToOne : oneToManyList) {
						if (oneToOne.getPackages().contains(packageName)) {
							oneToOneWithPckg = oneToOne;
							break;
						}
					}
					if (oneToOneWithPckg == null) {
						oneToOneWithPckg = new Dependency(jarLib);
						oneToOneWithPckg.packages.add(packageName);
						oneToManyList.add(oneToOneWithPckg);
					}
					oneToOneWithPckg.toSet.add(to);
				}
			}

			oneToManyDependencies = new HashSet<Dependency>(oneToManyList);
		}

		return oneToManyDependencies;
	}

	/**
	 * Returns a set with all unresolved packages.
	 * 
	 * @return a unmodifiable set with all unresolved packages, may be empty but
	 *         will never be <code>null</code>
	 */
	public Set<String> getUnresolvedPackages() {
		return Collections.unmodifiableSet(unresolvedPackages);
	}

	private Dependency findDependencyByToOrCreate(JarFileDescription to,
			Collection<Dependency> set) {
		Dependency result = null;
		for (Dependency dep : set) {
			if (dep.getToDescSet().contains(to)) {
				result = dep;
				break;
			}
		}
		if (result == null) {
			// create and add if not found
			result = new Dependency(jarLib);
			result.toSet.add(to);
			set.add(result);
		}
		return result;
	}

	/**
	 * Describes the package dependencies of one jar-file to another.
	 */
	public static final class Dependency {
		private final JarFileDescription from;
		private final Set<JarFileDescription> toSet = new HashSet<JarFileDescription>();
		private final Set<String> packages = new HashSet<String>();

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

		public JarFileDescription getSingleToDesc()
				throws IllegalStateException {
			if (isOneToMany()) {
				throw new IllegalStateException(
						"cannot return single dependency resolver, because there are many");
			}
			return toSet.iterator().next();
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
		 * Returns whether this describes a one to one dependency with many
		 * packages.
		 * 
		 * @return
		 */
		public boolean isOneToMany() {
			return toSet.size() > 1;
		}

		/**
		 * Returns whether this describes a one to many dependency with a single
		 * package.
		 * 
		 * @return
		 */
		public boolean isOneToOne() {
			return toSet.size() == 1;
		}

		@Override
		public int hashCode() {
			return Objects.hashCode(from, toSet, packages);
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
