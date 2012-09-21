package org.pa.jmeupdatesite;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class JarFileDependencies {

	private final Set<JarFileDescriptions> availableJarLibs;
	private final JarFileDescriptions jarLib;

	public JarFileDependencies(JarFileDescriptions jarLib, Set<JarFileDescriptions> availableJarLibs) {
		this.availableJarLibs = notNull(availableJarLibs,
				"available jar libs must not be null");
		this.jarLib = notNull(jarLib, "jarLib must not be null");

		computeDependencies();
	}

	private void computeDependencies() {
		for (String packageDependency : jarLib.getPackageDependencies()) {

			ArrayList<JarFileDescriptions> providePackageDependencies = new ArrayList<JarFileDescriptions>();

			for (JarFileDescriptions other : availableJarLibs) {
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
				for (JarFileDescriptions other : providePackageDependencies) {
					System.err.println(" --> " + other.getFile().getName());
				}
				break;
			}

		}
	}

	public static void main(String[] args) throws Exception {
		File libDir = new File("/home/palador/jmonkey/lib");
		Set<JarFileDescriptions> pool = new HashSet<JarFileDescriptions>();
		for (File f : libDir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".jar")) {
				System.out.println("ADD TO POOL: " + f);
				pool.add(new JarFileDescriptions(f));
			}
		}

		for (JarFileDescriptions lib : pool) {
			System.out.println("---------------------------------------------");
			System.out.println("-- " + lib.getFile().getName());
			System.out.println("---------------------------------------------");
			
			JarFileDependencies dep = new JarFileDependencies(lib, pool);
		}
	}
}
