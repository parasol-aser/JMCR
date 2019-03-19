package edu.tamu.aser.icb;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * 
 * @author Qingzhou Luo <qluo2@illinois.edu>
 * 
 */
public class FireEventsClassTransformer extends ClassVisitor {
    
    private String className;

    public FireEventsClassTransformer(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (version < 49) {
            version = 49;
        }
        super.visit(version, access, name, signature, superName, interfaces);

        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        methodVisitor = new JSRInlinerAdapter(methodVisitor, access, name, desc, signature, exceptions);

        return new FireEventsMethodTransformer(access, desc, name, this.className, methodVisitor);
    }

}
