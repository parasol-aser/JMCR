package edu.tamu.aser.icb;

import java.util.HashMap;
import java.util.Set;

import edu.tamu.aser.instrumentation.Instrumentor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * ReEx instrumentor for shared field read/writes.
 * 
 * @author Vilas Jagannath <vbangal2@illinois.edu>
 * 
 */
public class SharedAccessEventsMethodTransformer extends LocationAwareLocalVariablesSorter {

    private static final String PERFORM_UNLOCK = "performUnlock";
    private static final String PERFORM_LOCK = "performLock";
    private static final String AFTER_FIELD_ACCESS = "afterFieldAccess";
    private static final String BEFORE_FIELD_ACCESS = "beforeFieldAccess";
    private static final String AFTER_ARRAY_ACCESS = "afterArrayAccess";
    private static final String BEFORE_ARRAY_ACCESS = "beforeArrayAccess";
    private static final String OBJECT_VOID = "(Ljava/lang/Object;)V";
    private static final String BOOL_3STRINGS_VOID = "(ZLjava/lang/String;Ljava/lang/String;Ljava/lang/String;)V";
    private static final String INT_OBJECT_INT_BOOLEAN_VOID = "(ILjava/lang/Object;IZ)V";
    private static final String BOOL_VOID = "(Z)V";
    
    private final boolean insertSyncBlock;
    private final boolean staticSyncBlock;
    String methodSignature;
    public static HashMap<Integer, String> variableIdSigMap = new HashMap<Integer, String>();
    public static HashMap<Integer, String> stmtIdSigMap = new HashMap<Integer, String>();
    private int lockVariable;
    private final Set<String> nonArrayfinalFields;
    private int line_cur;
    private int maxindex_cur;// current max index of local variables

    public SharedAccessEventsMethodTransformer(int access, String desc, String methodName, String className, MethodVisitor methodVisitor,
            boolean insertSyncBlock, boolean staticSyncBlock, Set<String> nonArrayFinalFields) {
        super(access, desc, methodName, className, methodVisitor);
        this.insertSyncBlock = insertSyncBlock;
        this.staticSyncBlock = staticSyncBlock;
        this.nonArrayfinalFields = nonArrayFinalFields;
        
        this.methodSignature = methodName + desc;

        this.maxindex_cur = Type.getArgumentsAndReturnSizes(desc) + 1;
    }

    @Override
    protected void onMethodEnter() {
        if (this.insertSyncBlock) {
            Type classType = Type.getType("L" + this.className + ";");
            if (this.staticSyncBlock) {
                super.mv.visitLdcInsn(classType);
                this.lockVariable = newLocal(classType);
            } else {
                super.mv.visitVarInsn(Opcodes.ALOAD, 0);
                this.lockVariable = newLocal(classType);
            }
            super.mv.visitInsn(Opcodes.DUP);
            super.mv.visitVarInsn(Opcodes.ASTORE, this.lockVariable);
            visitInsn(Opcodes.MONITORENTER);
        }
    }
//    @Override
//    public void visitVarInsn(int opcode, int var) {
//        if (var > maxindex_cur) {
//            maxindex_cur = var;
//        }
//
//        switch (opcode) {
//        case LSTORE:
//        case DSTORE:
//        case LLOAD:
//        case DLOAD:
//            if (var == maxindex_cur) {
//                maxindex_cur = var + 1;
//            }
//            mv.visitVarInsn(opcode, var);
//            break;
//        case ISTORE:
//        case FSTORE:
//        case ASTORE:
//        case ILOAD:
//        case FLOAD:
//        case ALOAD:
//        case RET:
//            mv.visitVarInsn(opcode, var);
//            break;
//        default:
//            System.err.println("Unknown var instruction opcode " + opcode);
//            System.exit(1);
//        }
//    }

//    @Override
//    public void visitIincInsn(int var, int increment) {
//        if (var > maxindex_cur) {
//            maxindex_cur = var;
//        }
//        mv.visitIincInsn(var, increment);
//    } vb 
    @Override
    protected void onMethodExit(int opcode) {
        if (this.insertSyncBlock) {
            super.mv.visitVarInsn(Opcodes.ALOAD, this.lockVariable);
            visitInsn(Opcodes.MONITOREXIT);
        }
    }
    @Override
    public void visitLineNumber(int line, Label start) {
        line_cur = line;
        mv.visitLineNumber(line, start);
    }
    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        /*
         * Final field access is not a scheduling event (only detecting local
         * (same class) non array final fields atm). System.{err,out}.* is not
         * schedule relevant either.
         */
        if ((this.nonArrayfinalFields.contains(name) && owner.equals(this.className)) || owner.equals("java/lang/System")) {
            super.visitFieldInsn(opcode, owner, name, desc);
        } else {
            boolean isRead = false;
            if (opcode == Opcodes.GETFIELD || opcode == Opcodes.GETSTATIC) {
                isRead = true;
            }
            String sig_var = (owner + "." + name).replace("/", ".");

//            if(RVGlobalStateForInstrumentation.instance.isVariableShared(sig_var))
//             instrumentFieldAccess(opcode,owner,name,desc);
            
            super.updateThreadLocation();
            this.informSchedulerAboutFieldAccess(true, isRead, owner, name, desc);
            super.visitFieldInsn(opcode, owner, name, desc);
            this.informSchedulerAboutFieldAccess(false, isRead, owner, name, desc);
        }
    }

    public int getVariableId(String sig) {
        if (!variableIdSigMap.containsValue(sig)) {
            variableIdSigMap.put(variableIdSigMap.size() + 1, sig);
        }

        for (Integer key : variableIdSigMap.keySet()) {
            if (variableIdSigMap.get(key).equals(sig)) {
                return key;
            }
        }
        return 0;
    }

    public int getLocationId(String sig) {

        if (!stmtIdSigMap.containsValue(sig)) {
            stmtIdSigMap.put(stmtIdSigMap.size() + 1, sig);
        }

        for (Integer key : stmtIdSigMap.keySet()) {
            if (stmtIdSigMap.get(key).equals(sig)) {
                return key;
            }
        }
        return 0;
    }

    private void addBipushInsn(MethodVisitor mv, int val) {
        switch (val) {
        case 0:
            mv.visitInsn(ICONST_0);
            break;
        case 1:
            mv.visitInsn(ICONST_1);
            break;
        case 2:
            mv.visitInsn(ICONST_2);
            break;
        case 3:
            mv.visitInsn(ICONST_3);
            break;
        case 4:
            mv.visitInsn(ICONST_4);
            break;
        case 5:
            mv.visitInsn(ICONST_5);
            break;
        default:
            mv.visitLdcInsn(new Integer(val));
            break;
        }
    }

    private void instrumentFieldAccess(int opcode,String owner, String name,
            String desc)
    {

        String sig_var = (owner + "." + name).replace("/", ".");
        String sig_loc = (owner + "|" + methodSignature + "|" + sig_var + "|" + line_cur)
                .replace("/", ".");
        int SID = getVariableId(sig_var);
        int ID = getLocationId(sig_loc);
        
        switch (opcode) {
        case GETSTATIC:
                addBipushInsn(mv, ID);
                mv.visitInsn(ACONST_NULL);
                addBipushInsn(mv, SID);
                addBipushInsn(mv, 1);
                super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, BEFORE_FIELD_ACCESS, INT_OBJECT_INT_BOOLEAN_VOID);
            break;

        case PUTSTATIC:
            addBipushInsn(mv, ID);
            mv.visitInsn(ACONST_NULL);
            addBipushInsn(mv, SID);
            addBipushInsn(mv, 0);
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, BEFORE_FIELD_ACCESS, INT_OBJECT_INT_BOOLEAN_VOID);
            break;

        case GETFIELD:
        {
                maxindex_cur++;
                int index1 = maxindex_cur;
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, index1);

                addBipushInsn(mv, ID);
                mv.visitVarInsn(ALOAD, index1);
                addBipushInsn(mv, SID);
                addBipushInsn(mv, 1);
                super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, BEFORE_FIELD_ACCESS, INT_OBJECT_INT_BOOLEAN_VOID);
 
            
            break;
        }
        case PUTFIELD:
        {
            if (name.startsWith("this$")
                    || (className.contains("$") && name.startsWith("val$"))) {
                // inner class or strange class
                break;
            }

            maxindex_cur++;
            int index1 = maxindex_cur;
            int index2;
            if (desc.startsWith("D")) {
                mv.visitVarInsn(DSTORE, index1);
                maxindex_cur++;// double
                maxindex_cur++;
                index2 = maxindex_cur;
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, index2);
                mv.visitVarInsn(DLOAD, index1);
            } else if (desc.startsWith("J")) {
                mv.visitVarInsn(LSTORE, index1);
                maxindex_cur++;// long
                maxindex_cur++;
                index2 = maxindex_cur;
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, index2);
                mv.visitVarInsn(LLOAD, index1);
            } else if (desc.startsWith("F")) {
                mv.visitVarInsn(FSTORE, index1);
                maxindex_cur++;// float
                index2 = maxindex_cur;
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, index2);
                mv.visitVarInsn(FLOAD, index1);
            } else if (desc.startsWith("[")) {
                mv.visitVarInsn(ASTORE, index1);
                maxindex_cur++;// ref or array
                index2 = maxindex_cur;
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, index2);
                mv.visitVarInsn(ALOAD, index1);
            } else if (desc.startsWith("L")) {
                mv.visitVarInsn(ASTORE, index1);
                maxindex_cur++;// ref or array
                index2 = maxindex_cur;
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, index2);
                mv.visitVarInsn(ALOAD, index1);
            } else {
                // integer,char,short,boolean
                mv.visitVarInsn(ISTORE, index1);
                maxindex_cur++;
                index2 = maxindex_cur;
                mv.visitInsn(DUP);
                mv.visitVarInsn(ASTORE, index2);
                mv.visitVarInsn(ILOAD, index1);
            }
            
            addBipushInsn(mv, ID);
            mv.visitVarInsn(ALOAD, index2);
            addBipushInsn(mv, SID);
            addBipushInsn(mv, 0);
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, BEFORE_FIELD_ACCESS, INT_OBJECT_INT_BOOLEAN_VOID);


            break;
        }
        default:
            System.err.println("Unknown field access opcode " + opcode);
            System.exit(1);
        }
    }
    private int arrayStoreOpcode(int opcode)
    {
        switch (opcode) {
        case AASTORE:return ASTORE;
        case FASTORE:return FSTORE;
        case DASTORE:return DSTORE;
        case LASTORE:return LSTORE;
        case BASTORE:
        case CASTORE:
        case SASTORE:
        case IASTORE:
        default: return ISTORE; 
        }
    }

    private int arrayLoadOpcode(int opcode)
    {
        switch (opcode) {
        case AASTORE:return ALOAD;
        case FASTORE:return FLOAD;
        case DASTORE:return DLOAD;
        case LASTORE:return LLOAD;
        case BASTORE:
        case CASTORE:
        case SASTORE:
        case IASTORE:
        default: return ILOAD; 
        }
    }

    private void instrumentArrayAccess(int opcode, boolean isLoad)
    {
        String sig_loc = (className + "|" + methodSignature + "|" + line_cur)
                .replace("/", ".");
        int ID = getLocationId(sig_loc);
        
        if (isLoad) {
                mv.visitInsn(DUP2);
                maxindex_cur++;
                int index1 = maxindex_cur;
                mv.visitVarInsn(ISTORE, index1);
                maxindex_cur++;
                int index2 = maxindex_cur;
                mv.visitVarInsn(ASTORE, index2);
                

                addBipushInsn(mv, ID);
                mv.visitVarInsn(ALOAD, index2);
                mv.visitVarInsn(ILOAD, index1);

                addBipushInsn(mv, 1);

                super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, BEFORE_ARRAY_ACCESS, INT_OBJECT_INT_BOOLEAN_VOID);

        }
        else
        {
            maxindex_cur++;
            int index1 = maxindex_cur;
            mv.visitVarInsn(arrayStoreOpcode(opcode), index1);
            maxindex_cur++;
            int index2 = maxindex_cur;
            mv.visitVarInsn(ISTORE, index2);

            mv.visitInsn(DUP);
            maxindex_cur++;
            int index3 = maxindex_cur;
            mv.visitVarInsn(ASTORE, index3);// arrayref


            addBipushInsn(mv, ID);
            mv.visitVarInsn(ALOAD, index3);
            mv.visitVarInsn(ILOAD, index2);

            addBipushInsn(mv, 0);

            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, BEFORE_ARRAY_ACCESS, INT_OBJECT_INT_BOOLEAN_VOID);
            
            mv.visitVarInsn(ILOAD, index2);// index
            mv.visitVarInsn(arrayLoadOpcode(opcode), index1);// value
         
        }
    }

    @Override
    public void visitInsn(int opcode) {
        boolean arrayRead = false;
        boolean arrayWrite = false;
        // Array access here
        if (opcode == Opcodes.AALOAD || opcode == Opcodes.IALOAD || opcode == Opcodes.LALOAD 
                || opcode == Opcodes.SALOAD || opcode == Opcodes.CALOAD || opcode == Opcodes.DALOAD 
                || opcode == Opcodes.FALOAD || opcode == Opcodes.BALOAD) {
            arrayRead = true;
            
            String sig_loc = (className + "|" + methodSignature + "|" + line_cur)
                    .replace("/", ".");
//            if(RVGlobalStateForInstrumentation.instance.shouldInstrumentArray(sig_loc))
//                    instrumentArrayAccess(opcode,true);
            
        } else if (opcode == Opcodes.AASTORE || opcode == Opcodes.IASTORE || opcode == Opcodes.LASTORE 
                || opcode == Opcodes.SASTORE || opcode == Opcodes.CASTORE || opcode == Opcodes.DASTORE 
                || opcode == Opcodes.FASTORE || opcode == Opcodes.BASTORE) {
            arrayWrite = true;
            
            String sig_loc = (className + "|" + methodSignature + "|" + line_cur)
                    .replace("/", ".");
//            if(RVGlobalStateForInstrumentation.instance.shouldInstrumentArray(sig_loc))
//                    instrumentArrayAccess(opcode,false);

        }
        boolean arrayAccess = (arrayRead || arrayWrite);
        
        if (opcode == Opcodes.MONITORENTER) {
            this.performLockEventUsingScheduler(true);
        } else if (opcode == Opcodes.MONITOREXIT) {
            this.performLockEventUsingScheduler(false);
        } else {
            if (arrayAccess) {

                super.updateThreadLocation();
                this.informSchedulerAboutArrayAccess(true, arrayRead);
            }
            super.visitInsn(opcode);
            if (arrayAccess) {
                this.informSchedulerAboutArrayAccess(false, arrayRead);
            }
        }
    }

    private void informSchedulerAboutFieldAccess(boolean isBefore, boolean isRead, String fieldOwner, String fieldName, String fieldDesc) {
        super.mv.visitInsn(isRead ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        super.mv.visitLdcInsn(fieldOwner);
        super.mv.visitLdcInsn(fieldName);
        super.mv.visitLdcInsn(fieldDesc);
        if (isBefore) {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, BEFORE_FIELD_ACCESS, BOOL_3STRINGS_VOID);
        } else {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, AFTER_FIELD_ACCESS, BOOL_3STRINGS_VOID);
        }
    }
    
    private void informSchedulerAboutArrayAccess(boolean isBefore, boolean isRead) {
        super.mv.visitInsn(isRead ? Opcodes.ICONST_1 : Opcodes.ICONST_0);
        if (isBefore) {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, BEFORE_ARRAY_ACCESS, BOOL_VOID);
        } else {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, AFTER_ARRAY_ACCESS, BOOL_VOID);
        }
    }

    private void performLockEventUsingScheduler(boolean isLock) {
        super.updateThreadLocation();
        if (isLock) {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, PERFORM_LOCK, OBJECT_VOID);
        } else {
            super.mv.visitMethodInsn(Opcodes.INVOKESTATIC, Instrumentor.INSTR_EVENTS_RECEIVER, PERFORM_UNLOCK, OBJECT_VOID);
        }
    }

}
