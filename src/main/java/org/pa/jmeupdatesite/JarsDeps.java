package org.pa.jmeupdatesite;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.pa.jmeupdatesite.JarFileDependencies.Dependency;

public class JarsDeps {

	private static void printUsage() {
		System.out
				.println("prints the dependencies of jars in a directory on each other");
		System.out
				.println("usage: one arg - the path to a directory containing many jar files");
	}

	public static void main(String[] args) throws Exception {

		if (args.length != 1) {
			printUsage();
			System.exit(1);
		}

		File libDir = new File(args[0]);
		if (!libDir.isDirectory()) {
			System.out.println(args[0] + " is not a directory");
			System.exit(1);
		}
		TreeSet<File> poolFiles = new TreeSet<File>(Arrays.asList(libDir
				.listFiles(new java.io.FileFilter() {

					public boolean accept(File pathname) {
						return pathname.getName().endsWith(".jar");
					}
				})));
		if (poolFiles.isEmpty()) {
			System.out.println("no jars found");
			System.exit(0);
		}

		System.out.println("-- POOL --");
		for (File f : poolFiles) {
			System.out.println(f.getName());
		}
		System.out.println();
		System.out.println();

		TreeSet<JarFileDescription> pool = new TreeSet<JarFileDescription>();
		for (File poolFile : poolFiles) {
			pool.add(new JarFileDescription(poolFile));
		}

		TreeMap<String, ArrayList<JarFileDescription>> allUnresolvedPckgToJars = new TreeMap<String, ArrayList<JarFileDescription>>();

		for (JarFileDescription jfd : pool) {
			JarFileDependencies deps = new JarFileDependencies(jfd, pool);
			System.out.println("- " + jfd.getFile().getName() + "-");

			if (deps.getUnresolvedPackages().isEmpty()
					&& deps.getOneToManyDependencies().isEmpty()
					&& deps.getOneToOneDependencies().isEmpty()) {
				System.out.println(" nothing to report");
			} else {
				Set<Dependency> depSet = new HashSet<JarFileDependencies.Dependency>(
						deps.getOneToOneDependencies());

				if (!depSet.isEmpty()) {
					System.out.println("one-to-one:");
					for (Dependency dep : depSet) {
						System.out.println(" jar: "
								+ dep.getSingleToDesc().getFile().getName());
						for (String p : dep.getPackages()) {
							System.out.println("  pck:" + p);
						}
					}
				}
				depSet = new HashSet<JarFileDependencies.Dependency>(
						deps.getOneToManyDependencies());
				if (!depSet.isEmpty()) {
					System.out.println("one-to-many (bad):");
					for (Dependency dep : depSet) {
						System.out.println(" pck: "
								+ dep.getPackages().iterator().next());
						for (File to : new TreeSet<File>(dep.getToSet())) {
							System.out.println("  jar: " + to.getName());
						}
					}
				}

				Set<String> unresolved = new TreeSet<String>(
						deps.getUnresolvedPackages());
				if (!unresolved.isEmpty()) {
					System.out.println("unresolved (bad):");
					for (String p : unresolved) {
						System.out.println(" pck: " + p);
						ArrayList<JarFileDescription> pckgList = allUnresolvedPckgToJars
								.get(p);
						if (pckgList == null) {
							pckgList = new ArrayList<JarFileDescription>();
							allUnresolvedPckgToJars.put(p, pckgList);
						}
						pckgList.add(jfd);
					}
				}
			}

			System.out.println();
		}

		System.out.println("-- ALL UNRESOLVED PACKAGES --");
		if (allUnresolvedPckgToJars.isEmpty()) {
			System.out.println("nothing");
		} else {
			for (String pck : allUnresolvedPckgToJars.keySet()) {
				System.out.println(" " + pck);
				for (JarFileDescription jfd : allUnresolvedPckgToJars.get(pck)) {
					System.out.println("  " + jfd.getFile().getName());
				}

			}
		}
	}
}
