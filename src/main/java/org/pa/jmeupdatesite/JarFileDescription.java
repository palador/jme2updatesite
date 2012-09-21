package org.pa.jmeupdatesite;

import static java.util.Collections.list;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Sets;

/**
 * Provides information about classes in a jar-file. This includes dependencies
 * and package names.
 */
public class JarFileDescription {

	private final File jarFile; // the file
	private final ZipFile jarZip; // the unzip object

	/*
	 * Result cache.
	 */
	private Set<String> packageDependencies;
	private Set<String> providedPackages;
	private Set<String> referencedClassNames;
	private Set<String> referencedPackageNames;
	private Set<String> zipDirectories;

	/**
	 * Creates a jar file description.
	 * 
	 * @param jarFile
	 *            the jar-file to describe, must not ne <code>null</code> and a
	 *            valid jar-file.
	 * @throws IOException
	 *             if an I/O error has occured
	 */
	public JarFileDescription(File jarFile) throws IOException {
		this.jarFile = notNull(jarFile);
		this.jarZip = new ZipFile(jarFile);
	}

	/**
	 * Returns the jar-file as specified in the constructor.
	 * 
	 * @return the jar-file, will never be <code>null</code>
	 */
	public File getFile() {
		return jarFile;
	}

	/**
	 * Returns the names of the packages contained in the jar-file.
	 * 
	 * @return a unmodifiable set of package names
	 */
	public Set<String> getProvidedPackages() {
		if (providedPackages == null) {
			providedPackages = new HashSet<String>();
			for (String directory : getDirectories()) {
				if (hasClassFile(directory)) {
					String packageName = directory.substring(0,
							directory.length() - 1).replace('/', '.');
					providedPackages.add(packageName);
				}
			}
		}
		return Collections.unmodifiableSet(providedPackages);
	}

	/**
	 * Returns a set of canonical class names contained in the jar-file.
	 * 
	 * @return a unmodifiable set of canonical class names.
	 */
	public Set<String> getClasseNames() {
		if (referencedClassNames == null) {
			referencedClassNames = new HashSet<String>();

			for (ZipEntry entry : list(jarZip.entries())) {
				String name = entry.getName();
				if (name.endsWith(".class")) {
					try {
						referencedClassNames.addAll(ClassBytesUtil
								.findClassNames(jarZip
										.getInputStream(entry)));
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		return Collections.unmodifiableSet(referencedClassNames);
	}

	/**
	 * Returns a set of package names referenced by classes in this jar-file.
	 * This include the packages contained in this jar-file as well as foreign
	 * packages.
	 * 
	 * @return a unmodifiable set of package names
	 */
	public Set<String> getReferencedPackageNames() {
		if (referencedPackageNames == null) {
			referencedPackageNames = new HashSet<String>();

			for (ZipEntry entry : list(jarZip.entries())) {
				String name = entry.getName();
				if (name.endsWith(".class")) {
					try {
						referencedPackageNames.addAll(ClassBytesUtil
								.findPackageNames(jarZip
										.getInputStream(entry)));
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}

		return Collections.unmodifiableSet(referencedPackageNames);
	}

	/**
	 * Returns a set of package names referenced by classes in this jar-file,
	 * but not contained in it. These are called foreign packages, too.
	 * 
	 * @return a unmodifiable set of packages
	 */
	public Set<String> getPackageDependencies() {
		if (packageDependencies == null) {
			packageDependencies = Sets.difference(getReferencedPackageNames(),
					getProvidedPackages());
			packageDependencies = Sets.filter(packageDependencies, ClassNameFilters.NOT_JRE_CLASS_NAME);
		}
		return Collections.unmodifiableSet(packageDependencies);
	}

	/**
	 * @return a unmodifiable of all directories in this jar-file
	 */
	private Set<String> getDirectories() {
		if (zipDirectories == null) {
			zipDirectories = new HashSet<String>();
			for (ZipEntry entry : list(jarZip.entries())) {
				String name = entry.getName();
				if (name.endsWith("/")) {
					zipDirectories.add(name);
				}
			}
		}
		return Collections.unmodifiableSet(zipDirectories);
	}

	/**
	 * @param the
	 *            name of a directory in this jar-file
	 * @return whether the specified directory contains at least one class file
	 *         or not
	 */
	private boolean hasClassFile(String directory) {
		for (ZipEntry entry : list(jarZip.entries())) {
			String name = entry.getName();
			if (name.startsWith(directory)) {
				String fileName = name.substring(directory.length());
				if (fileName.indexOf('/') == -1 && fileName.endsWith(".class")) {
					return true;
				}
			}
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return getFile().hashCode();
	}
	
	
	@Override
	public boolean equals(Object obj) {
		return obj != null && obj.getClass() == JarFileDescription.class && ((JarFileDescription)obj).getFile().equals(getFile());
	}

}
