package org.pa.jmeupdatesite;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.Validate;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Type;

/**
 * Some utilities to ease the work with class binaries.
 */
public class ClassBytesUtil {

	/**
	 * Returns a list with all class names of classes referenced in the
	 * specified class file. Inner classes and primitive types are excluded.
	 * Returns the canonical names of the returned classes as defined by the
	 * Java Language Specification.
	 * 
	 * @param is
	 *            The input stream providing class bytes of a single class from
	 *            its beginning to its end. Must not be <code>null</code>.
	 * @return a set with class names
	 * @throws IllegalArgumentException
	 *             if <code>is</code> is <code>null</code>
	 * @throws IOException
	 *             if a problem occurs during reading
	 */
	public static Set<String> findClassNames(final InputStream is)
			throws IllegalArgumentException, IOException {
		Validate.notNull(is, "The input stream must not be null");
		final HashSet<String> result = new HashSet<String>();

		ClassReader classReader = new ClassReader(is);

		// we simply make use of every possible visitor and put every mentioned
		// class name into the result set

		final AnnotationVisitor annotationVisitor = new AnnotationVisitor() {

			public void visitEnum(String name, String desc, String value) {
				addClassFromTypedefToSet(desc, result);
			}

			public void visitEnd() {
			}

			public AnnotationVisitor visitArray(String name) {
				return this;
			}

			public AnnotationVisitor visitAnnotation(String name, String desc) {
				addClassFromTypedefToSet(desc, result);
				return this;
			}

			public void visit(String name, Object value) {
			}
		};

		classReader.accept(new ClassVisitor() {

			public void visitSource(String source, String debug) {
			}

			public void visitOuterClass(String owner, String name, String desc) {
			}

			public MethodVisitor visitMethod(int access, String name,
					String desc, String signature, String[] exceptions) {
				return new MethodVisitor() {

					public void visitVarInsn(int opcode, int var) {
					}

					public void visitTypeInsn(int opcode, String type) {
					}

					public void visitTryCatchBlock(Label start, Label end,
							Label handler, String type) {
					}

					public void visitTableSwitchInsn(int min, int max,
							Label dflt, Label[] labels) {
					}

					public AnnotationVisitor visitParameterAnnotation(
							int parameter, String desc, boolean visible) {
						addClassFromTypedefToSet(desc, result);
						return annotationVisitor;
					}

					public void visitMultiANewArrayInsn(String desc, int dims) {
						addClassFromTypedefToSet(desc, result);
					}

					public void visitMethodInsn(int opcode, String owner,
							String name, String desc) {
						addClassFromTypedefToSet(desc, result);
					}

					public void visitMaxs(int maxStack, int maxLocals) {
					}

					public void visitLookupSwitchInsn(Label dflt, int[] keys,
							Label[] labels) {
					}

					public void visitLocalVariable(String name, String desc,
							String signature, Label start, Label end, int index) {
					}

					public void visitLineNumber(int line, Label start) {
					}

					public void visitLdcInsn(Object cst) {
					}

					public void visitLabel(Label label) {
					}

					public void visitJumpInsn(int opcode, Label label) {
					}

					public void visitIntInsn(int opcode, int operand) {
					}

					public void visitInsn(int opcode) {
					}

					public void visitIincInsn(int var, int increment) {
					}

					public void visitFrame(int type, int nLocal,
							Object[] local, int nStack, Object[] stack) {
					}

					public void visitFieldInsn(int opcode, String owner,
							String name, String desc) {
						addClassFromTypedefToSet(desc, result);
					}

					public void visitEnd() {
					}

					public void visitCode() {
					}

					public void visitAttribute(Attribute attr) {
					}

					public AnnotationVisitor visitAnnotationDefault() {
						return annotationVisitor;
					}

					public AnnotationVisitor visitAnnotation(String desc,
							boolean visible) {
						addClassFromTypedefToSet(desc, result);
						return annotationVisitor;
					}
				};
			}

			public void visitInnerClass(String name, String outerName,
					String innerName, int access) {
			}

			public FieldVisitor visitField(int access, String name,
					String desc, String signature, Object value) {
				addClassFromTypedefToSet(desc, result);
				return new FieldVisitor() {

					public void visitEnd() {
					}

					public void visitAttribute(Attribute attr) {
					}

					public AnnotationVisitor visitAnnotation(String desc,
							boolean visible) {
						addClassFromTypedefToSet(desc, result);
						return annotationVisitor;
					}
				};
			}

			public void visitEnd() {
			}

			public void visitAttribute(Attribute attr) {
			}

			public AnnotationVisitor visitAnnotation(String desc,
					boolean visible) {
				addClassFromTypedefToSet(desc, result);
				return annotationVisitor;
			}

			public void visit(int version, int access, String name,
					String signature, String superName, String[] interfaces) {
			}
		}, 0);

		return result;
	}

	/**
	 * Returns a list with the package names of all referenced classes as
	 * returned by {@link #findClassNames(InputStream)}.
	 * 
	 * @param is
	 *            The input stream providing class bytes of a single class from
	 *            its beginning to its end. Must not be <code>null</code>. *
	 * @return a set with package names
	 * @throws IllegalArgumentException
	 *             if <code>is</code> is <code>null</code>
	 * @throws IOException
	 *             if a problem occurs during reading
	 */
	public static Set<String> findPackageNames(InputStream is)
			throws IllegalArgumentException, IOException {
		HashSet<String> result = new HashSet<String>();
		for (String className : findClassNames(is)) {
			int index = className.lastIndexOf('.');
			if (index != -1) {
				result.add(className.substring(0, index));
			}
		}
		return result;
	}

	/**
	 * Matches class names inside a La.b.c.MyClass; like class name notation.
	 */
	private final static Pattern CLASS_NAME_PATTERN = Pattern
			.compile("(?<=L)[a-z]+/[\\w/]+(?=;)");

	/**
	 * Helper method. Validates and clean up class names before adding them to a
	 * set.
	 * 
	 * @param desc
	 * @param set
	 */
	private static void addClassFromTypedefToSet(String desc, Set<String> set) {
		if (desc == null) {
			return;
		}
		Type type = Type.getObjectType(desc);

		// get type of (multidimensional) arrays
		if (type.getSort() == Type.ARRAY) {
			type = type.getElementType();
		}

		// ignore type which aren't object types
		if (type.getSort() != Type.OBJECT) {
			return;
		}

		String qualifiedClassName = type.getInternalName();

		Matcher matcher = CLASS_NAME_PATTERN.matcher(qualifiedClassName);
		while (matcher.find()) {
			set.add(matcher.group().replace('/', '.'));
		}
	}
}
