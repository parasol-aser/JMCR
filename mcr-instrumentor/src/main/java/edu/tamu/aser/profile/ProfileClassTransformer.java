package edu.tamu.aser.profile;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

public class ProfileClassTransformer extends ClassVisitor {

	private String className;	
	public ProfileClassTransformer(ClassVisitor classVisitor) {
		super(Opcodes.ASM5, classVisitor);
	}

	@Override
	public void visit(int version, int access, String name, String signature,
			String superName, String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		this.className = name;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {
        
		MethodVisitor mv = cv.visitMethod(access, name, desc, signature,
				exceptions);
		mv = new JSRInlinerAdapter(mv, access, name, desc, signature, exceptions);

			mv = new ProfileMethodTransformer(access, desc, name,
					this.className, mv);
		return mv;
		// MethodVisitor methodVisitor = super.visitMethod(access, name, desc,
		// signature, exceptions);

	}

}
