package edu.tamu.aser.icb;

import edu.tamu.aser.instrumentation.Instrumentor;
import org.objectweb.asm.MethodVisitor;

/**
 * 
 * @author Vilas Jagannath <vbangal2@illinois.edu>
 * @author Matt Kirn <kirn1@illinois.edu>
 * 
 */
public class JUCEventsMethodTransformer extends LocationAwareLocalVariablesSorter {

    private static final String UNSAFE_CLASS_NAME = "sun/misc/Unsafe";
    private static final String BOOL_LONG_VOID = "(ZJ)V";
    private static final String PARK_METHOD_NAME = "park";
    private static final String UNPARK_METHOD_NAME = "unpark";
    private static final String COMPARE_AND_SWAP_METHOD_PREFIX = "compareAndSwap";
    private static final String OBJECT_VOID = "(L" + Object.class.getName().replace(".", "/") + ";)V";
    private static final String AFTER_FIELD_ACCESS = "afterFieldAccess";
    private static final String BEFORE_FIELD_ACCESS = "beforeFieldAccess";
    private static final String BOOL_3STRINGS_VOID = "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";

    public JUCEventsMethodTransformer(int access, String desc, String methodName, String className, MethodVisitor methodVisitor) {
        super(access, desc, methodName, className, methodVisitor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (owner.equals(UNSAFE_CLASS_NAME) && opcode == INVOKEVIRTUAL && !(name.equals("getUnsafe"))) {
            if (name.equals(PARK_METHOD_NAME) && desc.equals(BOOL_LONG_VOID)) {
                visitParkMethod(opcode, owner, name, desc);
                return;
            } else if (name.equals(UNPARK_METHOD_NAME) && desc.equals(OBJECT_VOID)) {
                visitUnparkMethod(opcode, owner, name, desc);
                return;
            } else if (name.startsWith(COMPARE_AND_SWAP_METHOD_PREFIX) || name.startsWith("put")) {
                super.updateThreadLocation();
                visitUnsafeScheduleRelevantMethod(true, false, opcode, owner, name, desc);
                super.visitMethodInsn(opcode, owner, name, desc);
                visitUnsafeScheduleRelevantMethod(false, false, opcode, owner, name, desc);
                return;
            } else if (name.equals("objectFieldOffset") || name.startsWith("get")) {
                super.updateThreadLocation();
                visitUnsafeScheduleRelevantMethod(true, true, opcode, owner, name, desc);
                super.visitMethodInsn(opcode, owner, name, desc);
                visitUnsafeScheduleRelevantMethod(false, true, opcode, owner, name, desc);
                return;
            } else {
                visitUnsafeOther(opcode, owner, name, desc);
            }
        }
        // visit original method by default
        super.visitMethodInsn(opcode, owner, name, desc);
    }

    private void visitUnsafeOther(int opcode, String owner, String name, String desc) {
        super.mv.visitLdcInsn(owner);
        super.mv.visitLdcInsn(name);
        super.mv.visitLdcInsn(desc);
        super.mv.visitMethodInsn(INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "beforeUnsafeOther",
                "(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)V");
    }

    private void visitUnsafeScheduleRelevantMethod(boolean isBefore, boolean isRead, int opcode, String owner, String name, String desc) {
        super.mv.visitInsn(isRead ? ICONST_1 : ICONST_0);
        super.mv.visitLdcInsn(owner);
        super.mv.visitLdcInsn(name);
        super.mv.visitLdcInsn(desc);
        if (isBefore) {
            super.mv.visitMethodInsn(INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, BEFORE_FIELD_ACCESS, BOOL_3STRINGS_VOID);
        } else {
            super.mv.visitMethodInsn(INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, AFTER_FIELD_ACCESS, BOOL_3STRINGS_VOID);
        }
    }

    private void visitUnparkMethod(int opcode, String owner, String name, String desc) {
        super.updateThreadLocation();
        super.mv.visitMethodInsn(INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performUnpark", OBJECT_VOID);
        super.mv.visitInsn(POP);
    }

    private void visitParkMethod(int opcode, String owner, String name, String desc) {
        super.updateThreadLocation();
        super.mv.visitMethodInsn(INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performPark", BOOL_LONG_VOID);
        super.mv.visitInsn(POP);
    }

}
