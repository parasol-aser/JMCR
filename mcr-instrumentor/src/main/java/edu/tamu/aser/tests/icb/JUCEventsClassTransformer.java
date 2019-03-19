package edu.tamu.aser.icb;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 
 * @author Vilas Jagannath <vbangal2@illinois.edu>
 * 
 */
public class JUCEventsClassTransformer extends ClassVisitor {

    private String className;

    public JUCEventsClassTransformer(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new JUCEventsMethodTransformer(access, desc, name, this.className, methodVisitor);
    }

}
