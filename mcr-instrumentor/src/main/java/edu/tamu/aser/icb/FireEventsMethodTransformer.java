package edu.tamu.aser.icb;

import edu.tamu.aser.instrumentation.Instrumentor;
import org.objectweb.asm.MethodVisitor;

/**
 * 
 * @author Qingzhou Luo <qluo2@illinois.edu>
 * 
 */
public class FireEventsMethodTransformer extends LocationAwareLocalVariablesSorter {

    private static final String STRING_VOID = "(L" + String.class.getName().replace(".", "/") + ";)V";
    private static final String FIRE_EVENT_METHOD_NAME = "fireEvent";
    private static final String IMUNIT_CLASS_NAME = "edu/illinois/imunit/IMUnit";
    private static final String SCHEDULER_FIRE_EVENT_METHOD_NAME = "fireIMUnitEvent";
    
    public FireEventsMethodTransformer(int access, String desc, String methodName, String className, MethodVisitor methodVisitor) {
        super(access, desc, methodName, className, methodVisitor);
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        if (name.equals(FIRE_EVENT_METHOD_NAME) && owner.equals(IMUNIT_CLASS_NAME) && opcode == INVOKESTATIC && desc.equals(STRING_VOID)) {
           super.mv.visitMethodInsn(INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, SCHEDULER_FIRE_EVENT_METHOD_NAME, STRING_VOID);
        } else {
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

}
