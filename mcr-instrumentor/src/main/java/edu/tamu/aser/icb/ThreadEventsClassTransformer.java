package edu.tamu.aser.icb;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.JSRInlinerAdapter;

/**
 * 
 * @author Vilas Jagannath <vbangal2@illinois.edu>
 * 
 */
public class ThreadEventsClassTransformer extends ClassVisitor {

    private static final String RUN_METHOD_NAME = "run";
    private static final String THREAD_CLASS_NAME = Thread.class.getName().replace(".", "/");

    private String className;
    private boolean isThreadClass;

    public ThreadEventsClassTransformer(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        if (version < 49) {
            version = 49;
        }
        super.visit(version, access, name, signature, superName, interfaces);

        this.className = name;
        this.isThreadClass = name.equals(ThreadEventsClassTransformer.THREAD_CLASS_NAME);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        methodVisitor = new JSRInlinerAdapter(methodVisitor, access, name, desc, signature, exceptions);

        boolean possibleRunMethod = name.equals(RUN_METHOD_NAME) && (access & Opcodes.ACC_STATIC) == 0
                && (Type.getArgumentsAndReturnSizes(desc) >> 2) == 1 && Type.getReturnType(desc).equals(Type.VOID_TYPE) && !isThreadClass;

        //possibleRunMethod = false;
        
        return new ThreadEventsMethodTransformer(access, desc, name, this.className, methodVisitor, possibleRunMethod);
    }

}
