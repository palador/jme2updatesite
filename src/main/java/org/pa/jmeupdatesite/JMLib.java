package org.pa.jmeupdatesite;

import static java.util.Collections.list;
import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.google.common.collect.Sets;

public class JMLib {

	private final File jarFile;
	private final ZipFile jarZip;

	public JMLib(File jarFile) throws IOException {
		this.jarFile = notNull(jarFile);
		this.jarZip = new ZipFile(jarFile);
	}
	
	public File getJarFile() {
		return jarFile;
	}

	public Set<String> getPackages() {
		HashSet<String> result = new HashSet<String>();

		for (String directory : getDirectories()) {
			if (hasClassFile(directory)) {
				String packageName = directory.substring(0,
						directory.length() - 1).replace('/', '.');
				result.add(packageName);
			}
		}

		return result;
	}

	public Set<String> getReferencedClassNames() {
		HashSet<String> result = new HashSet<String>();

		for (ZipEntry entry : list(jarZip.entries())) {
			String name = entry.getName();
			if (name.endsWith(".class")) {
				try {
					result.addAll(ClassBytesUtil
							.findUsedSimpleClassNames(jarZip
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

		return result;
	}

	public Set<String> getReferencedPackageNames() {
		HashSet<String> result = new HashSet<String>();

		for (ZipEntry entry : list(jarZip.entries())) {
			String name = entry.getName();
			if (name.endsWith(".class")) {
				try {
					result.addAll(ClassBytesUtil
							.findPackageNamesOfUsedClasses(jarZip
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

		return result;
	}
	
	public Set<String> getPackageDependencies() {
		Set<String> requiredButNotContainedPackages =  Sets.difference(getReferencedPackageNames(), getPackages());
		return Sets.filter(requiredButNotContainedPackages, ClassNameFilters.NOT_JRE_CLASS_NAME);
	}

	private List<String> getDirectories() {
		ArrayList<String> result = new ArrayList<String>();
		for (ZipEntry entry : list(jarZip.entries())) {
			String name = entry.getName();
			if (name.endsWith("/")) {
				result.add(name);
			}
		}
		return result;
	}

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

}
