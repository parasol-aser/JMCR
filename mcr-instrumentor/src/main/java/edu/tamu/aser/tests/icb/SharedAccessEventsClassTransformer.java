package edu.tamu.aser.icb;

import java.util.HashSet;
import java.util.Set;

import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 
 * @author Vilas Jagannath <vbangal2@illinois.edu>
 * 
 */
public class SharedAccessEventsClassTransformer extends ClassVisitor {

    private static final String ARRAY_DESC = "[";
    private String className;
    private Set<String> nonArrayFinalFields;

    public SharedAccessEventsClassTransformer(ClassVisitor classVisitor) {
        super(Opcodes.ASM5, classVisitor);
        nonArrayFinalFields = new HashSet<String>();
    }

    @Override
    public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
        super.visit(version, access, name, signature, superName, interfaces);
        this.className = name;
    }

    @Override
    public FieldVisitor visitField(int access, String name, String desc, String signature, Object value) {
        if ((access & Opcodes.ACC_FINAL) != 0 && !desc.startsWith(ARRAY_DESC)) {
            nonArrayFinalFields.add(name);
        }
        return super.visitField(access, name, desc, signature, value);
    }

    @Override
    public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
        boolean insertSyncBlock = false;
        boolean staticSyncBlock = false;
        if ((access & Opcodes.ACC_SYNCHRONIZED) != 0) {
            access = access ^ Opcodes.ACC_SYNCHRONIZED;
            insertSyncBlock = true;
            if ((access & Opcodes.ACC_STATIC) != 0) {
                staticSyncBlock = true;
            }
        }
        MethodVisitor methodVisitor = super.visitMethod(access, name, desc, signature, exceptions);
        return new SharedAccessEventsMethodTransformer(access, desc, name, this.className, methodVisitor, insertSyncBlock, staticSyncBlock,
                this.nonArrayFinalFields);
    }

}
