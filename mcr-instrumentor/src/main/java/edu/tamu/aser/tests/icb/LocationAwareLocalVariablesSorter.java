package edu.tamu.aser.icb;

import edu.tamu.aser.instrumentation.Instrumentor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.commons.AdviceAdapter;

/**
 * 
 * @author Vilas Jagannath <vbangal2@illinois.edu>
 * 
 */
public class LocationAwareLocalVariablesSorter extends AdviceAdapter {

    private static final String UPDATE_THREAD_LOCATION = "updateThreadLocation";

    private static final String STRINGS_INT_VOID = "(Ljava/lang/String;Ljava/lang/String;I)V";

    protected final String methodName;
    protected final String className;

    protected int lineNumber;

    public LocationAwareLocalVariablesSorter(int access, String desc, String methodName, String className, MethodVisitor mv) {
        super(Opcodes.ASM5, mv, access, methodName, desc);
        this.methodName = methodName;
        this.className = className;
        this.lineNumber = -1;
    }

    @Override
    public void visitLineNumber(int line, Label start) {
        super.visitLineNumber(line, start);
        this.lineNumber = line;
    }

    protected void updateThreadLocation() {
        super.mv.visitLdcInsn(this.className);
        super.mv.visitLdcInsn(this.methodName);
        super.mv.visitLdcInsn(this.lineNumber);
        super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, UPDATE_THREAD_LOCATION, STRINGS_INT_VOID);
    }
}
