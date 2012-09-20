package org.pa.jmeupdatesite;

import static org.apache.commons.lang3.Validate.notNull;

import java.io.File;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class JarFilesDependencies {

	private final Set<JMLib> availableJarLibs;
	private final JMLib jarLib;

	public JarFilesDependencies(JMLib jarLib, Set<JMLib> availableJarLibs) {
		this.availableJarLibs = notNull(availableJarLibs,
				"available jar libs must not be null");
		this.jarLib = notNull(jarLib, "jarLib must not be null");

		computeDependencies();
	}

	private void computeDependencies() {
		for (String packageDependency : jarLib.getPackageDependencies()) {

			ArrayList<JMLib> providePackageDependencies = new ArrayList<JMLib>();

			for (JMLib other : availableJarLibs) {
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
						+ jarLib.getJarFile().getName() + "::"
						+ packageDependency);
				break;
			case 1:
				System.out.println("RESOLVED:            "
						+ jarLib.getJarFile().getName()
						+ "::"
						+ packageDependency
						+ " --> "
						+ providePackageDependencies.get(0).getJarFile()
								.getName());
				break;

			default:
				System.err.println("RESOLVED MANY TIMES: "
						+ jarLib.getJarFile().getName() + "::"
						+ packageDependency);
				for (JMLib other : providePackageDependencies) {
					System.err.println(" --> " + other.getJarFile().getName());
				}
				break;
			}

		}
	}

	public static void main(String[] args) throws Exception {
		File libDir = new File("/home/palador/jmonkey/lib");
		Set<JMLib> pool = new HashSet<JMLib>();
		for (File f : libDir.listFiles()) {
			if (f.isFile() && f.getName().endsWith(".jar")) {
				System.out.println("ADD TO POOL: " + f);
				pool.add(new JMLib(f));
			}
		}

		for (JMLib lib : pool) {
			System.out.println("---------------------------------------------");
			System.out.println("-- " + lib.getJarFile().getName());
			System.out.println("---------------------------------------------");
			
			JarFilesDependencies dep = new JarFilesDependencies(lib, pool);
		}
	}
}