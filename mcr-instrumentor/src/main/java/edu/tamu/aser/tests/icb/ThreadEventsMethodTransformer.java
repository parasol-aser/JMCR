package edu.tamu.aser.icb;

import edu.tamu.aser.instrumentation.Instrumentor;
import edu.tamu.aser.instrumentation.RVGlobalStateForInstrumentation;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * 
 * @author Vilas Jagannath <vbangal2@illinois.edu>
 * 
 */
public class ThreadEventsMethodTransformer extends LocationAwareLocalVariablesSorter {

    private static final String RUNNABLE_CLASS_NAME = "java/lang/Runnable";
    private static final String YIELD_METHOD_NAME = "yield";
    private static final String SLEEP_METHOD_NAME = "sleep";
    private static final String INT = "I";
    private static final String LONG = "J";
    public static final String THREAD_CLASS_NAME = Thread.class.getName().replace(".", "/");
    private static final String THREAD_VOID = "(L" + THREAD_CLASS_NAME + ";)V";
    private static final String OBJECT_VOID = "(L" + Object.class.getName().replace(".", "/") + ";)V";
    private static final String OBJECT_LONG = "(L" + Object.class.getName().replace(".", "/") + ";)J";
    private static final String LONG_INT_VOID = "(JI)V";
    private static final String LONG_VOID = "(J)V";
    private static final String NO_ARGS_VOID = "()V";
    private static final String WAIT_METHOD_NAME = "wait";
    private static final String NOTIFY_METHOD_NAME = "notify";
    private static final String NOTIFY_ALL_METHOD_NAME = "notifyAll";
    private static final String JOIN_METHOD_NAME = "join";
    private static final String START_METHOD_NAME = "start";

    private final boolean possibleRunMethod;

    public ThreadEventsMethodTransformer(int access, String desc, String methodName, String className, MethodVisitor methodVisitor,
            boolean possibleRunMethod) {
        super(access, desc, methodName, className, methodVisitor);
        this.possibleRunMethod = possibleRunMethod;
    }

    @Override
    protected void onMethodEnter() {
        if (this.possibleRunMethod) {
            visitThreadBeginEnd(true);
        }
    }

    @Override
    protected void onMethodExit(int opcode) {
        if (this.possibleRunMethod) {
            visitThreadBeginEnd(false);
        }
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc) {
        
        if(RVGlobalStateForInstrumentation.instance.isThreadClass(owner))
        {
            if (name.equals(START_METHOD_NAME) && opcode != Opcodes.INVOKESTATIC && desc.equals(NO_ARGS_VOID)) {
                visitThreadForking(opcode, owner, name, desc);
            } 
            else if (name.equals(JOIN_METHOD_NAME) && opcode != Opcodes.INVOKESTATIC
                    && (desc.equals(NO_ARGS_VOID) || desc.equals(LONG_VOID) || desc.equals(LONG_INT_VOID))) {
                visitJoinMethod(opcode, owner, name, desc);
            }
            else            super.visitMethodInsn(opcode, owner, name, desc);

        }
        else if (name.equals(WAIT_METHOD_NAME) && opcode != Opcodes.INVOKESTATIC
                && (desc.equals(NO_ARGS_VOID) || desc.equals(LONG_VOID) || desc.equals(LONG_INT_VOID))) {
            visitWaitMethod(desc);
        } else if (name.equals(NOTIFY_METHOD_NAME) && opcode != Opcodes.INVOKESTATIC && desc.equals(NO_ARGS_VOID)) {
            super.updateThreadLocation();
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performNotifyOld", OBJECT_VOID);
        } else if (name.equals(NOTIFY_ALL_METHOD_NAME) && opcode != Opcodes.INVOKESTATIC && desc.equals(NO_ARGS_VOID)) {
            super.updateThreadLocation();
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performNotifyAll", OBJECT_VOID);
        } else if (name.equals(SLEEP_METHOD_NAME) && opcode == Opcodes.INVOKESTATIC && (desc.equals(LONG_VOID) || desc.equals(LONG_INT_VOID))) {
            visitSleepMethod(opcode, owner, name, desc);
        } else if (name.equals(YIELD_METHOD_NAME) && opcode == Opcodes.INVOKESTATIC && desc.equals(NO_ARGS_VOID)) {
            if (!owner.equals(THREAD_CLASS_NAME)) {
                super.visitMethodInsn(opcode, owner, name, desc);
            }
        } 
        else 
        {
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    public void visitSleepMethod(int opcode, String owner, String name, String desc) {
        if (owner.equals(THREAD_CLASS_NAME)) {
            if (desc.contains(INT)) {
                super.mv.visitInsn(Opcodes.POP);
            }
            if (desc.contains(LONG)) {
                super.mv.visitInsn(Opcodes.POP2);
            }
        } else {
            super.visitMethodInsn(opcode, owner, name, desc);
        }
    }

    public void visitWaitMethod(String desc) {
        super.updateThreadLocation();
        if (desc.equals(NO_ARGS_VOID)) {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performWait", OBJECT_VOID);
        } else if (desc.equals("(J)V")) {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performTimedWait", "(L"
                    + Object.class.getName().replace(".", "/") + ";J)V");
        } else if (desc.equals("(JI)V")) {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performTimedWait", "(L"
                    + Object.class.getName().replace(".", "/") + ";JI)V");
        }
    }

    public void visitJoinMethod(int opcode, String owner, String name, String desc) {
        if (desc.equals(NO_ARGS_VOID)) {
            super.mv.visitInsn(Opcodes.DUP);
            super.mv.visitTypeInsn(Opcodes.INSTANCEOF, THREAD_CLASS_NAME);
            Label l1 = new Label();
            super.mv.visitJumpInsn(Opcodes.IFEQ, l1);
            super.updateThreadLocation();
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performJoin", THREAD_VOID);
            Label l2 = new Label();
            super.mv.visitJumpInsn(Opcodes.GOTO, l2);
            super.mv.visitLabel(l1);
            super.visitMethodInsn(opcode, owner, name, desc);
            super.mv.visitLabel(l2);
        } else if (desc.equals("(J)V")) {
            // stack = thread, long2, long1
            super.mv.visitInsn(Opcodes.DUP2_X1); // long2, long1, thread, long2,
                                                 // long1
            super.mv.visitInsn(Opcodes.POP2); // long2, long1, thread
            super.mv.visitInsn(Opcodes.DUP); // long2, long1, thread, thread
            super.mv.visitTypeInsn(Opcodes.INSTANCEOF, THREAD_CLASS_NAME); // long2,
                                                                           // long1,
                                                                           // thread,
                                                                           // [0,1]
            Label l1 = new Label();
            super.mv.visitJumpInsn(Opcodes.IFEQ, l1); // long2, long1, thread
            super.mv.visitInsn(Opcodes.DUP2_X1); // long1, thread, long2, long1,
                                                 // thread
            super.mv.visitInsn(Opcodes.POP2); // long1, thread, long2
            super.mv.visitInsn(Opcodes.DUP2_X1); // thread, long2, long1,
                                                 // thread, long2
            super.mv.visitInsn(Opcodes.POP2); // thread, long2, long1
            super.updateThreadLocation();
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performTimedJoin", "(L" + THREAD_CLASS_NAME + ";J)V");
            Label l2 = new Label();
            super.mv.visitJumpInsn(Opcodes.GOTO, l2);
            super.mv.visitLabel(l1);
            // stack = long2, long1, thread
            super.mv.visitInsn(Opcodes.DUP2_X1); // long1, thread, long2, long1,
                                                 // thread
            super.mv.visitInsn(Opcodes.POP2); // long1, thread, long2
            super.mv.visitInsn(Opcodes.DUP2_X1); // thread, long2, long1,
                                                 // thread, long2
            super.mv.visitInsn(Opcodes.POP2); // thread, long2, long1
            super.visitMethodInsn(opcode, owner, name, desc);
            super.mv.visitLabel(l2);
        } else if (desc.equals("(JI)V")) {
            // stack = thread, long2, long1, int
            super.mv.visitInsn(Opcodes.DUP2_X2); // long1, int, thread, long2,
                                                 // long1, int
            super.mv.visitInsn(Opcodes.POP2); // long1, int, thread, long2
            super.mv.visitInsn(Opcodes.SWAP); // long1, int, long2, thread
            super.mv.visitInsn(Opcodes.DUP); // long1, int, long2, thread,
                                             // thread
            super.mv.visitTypeInsn(Opcodes.INSTANCEOF, THREAD_CLASS_NAME); // long1,
                                                                           // int,
                                                                           // long2,
                                                                           // thread,
                                                                           // [0,1]
            Label l1 = new Label();
            super.mv.visitJumpInsn(Opcodes.IFEQ, l1); // long1, int, long2,
                                                      // thread
            super.mv.visitInsn(Opcodes.SWAP); // long1, int, thread, long2
            super.mv.visitInsn(Opcodes.DUP2_X2); // thread, long2, long1, int,
                                                 // thread, long2
            super.mv.visitInsn(Opcodes.POP2); // thread, long2, long1, int
            super.updateThreadLocation();
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "performTimedJoin", "(L" + THREAD_CLASS_NAME + ";JI)V");
            Label l2 = new Label();
            super.mv.visitJumpInsn(Opcodes.GOTO, l2);
            super.mv.visitLabel(l1);
            // stack = long1, int, long2, thread
            super.mv.visitInsn(Opcodes.SWAP); // long1, int, thread, long2
            super.mv.visitInsn(Opcodes.DUP2_X2); // thread, long2, long1, int,
                                                 // thread, long2
            super.mv.visitInsn(Opcodes.POP2); // thread, long2, long1, int
            super.visitMethodInsn(opcode, owner, name, desc);
            super.mv.visitLabel(l2);
        }
    }

    public void visitThreadForking(int opcode, String owner, String name, String desc) {
        super.mv.visitInsn(Opcodes.DUP);
        super.mv.visitTypeInsn(Opcodes.INSTANCEOF, THREAD_CLASS_NAME);
        Label l1 = new Label();
        super.mv.visitJumpInsn(Opcodes.IFEQ, l1);
        super.mv.visitInsn(Opcodes.DUP);
        super.mv.visitInsn(Opcodes.DUP);
        super.updateThreadLocation();
        super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "beforeForking", THREAD_VOID);
        super.visitMethodInsn(opcode, owner, name, desc);
        super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "afterForking", THREAD_VOID);
        Label l2 = new Label();
        super.mv.visitJumpInsn(Opcodes.GOTO, l2);
        super.mv.visitLabel(l1);
        super.visitMethodInsn(opcode, owner, name, desc);
        super.mv.visitLabel(l2);
    }

    private void visitThreadBeginEnd(boolean threadBegin) {
        super.mv.visitVarInsn(Opcodes.ALOAD, 0);
        super.mv.visitTypeInsn(Opcodes.INSTANCEOF, RUNNABLE_CLASS_NAME);
        Label l1 = new Label();
        super.mv.visitJumpInsn(Opcodes.IFEQ, l1);
        super.updateThreadLocation();
        if (threadBegin) {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "beginThread", NO_ARGS_VOID);
        } else {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, "endThread", NO_ARGS_VOID);
        }
        super.mv.visitLabel(l1);
    }

}
