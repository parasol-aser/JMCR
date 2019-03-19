package edu.tamu.aser.profile;

import edu.tamu.aser.instrumentation.RVConfig;
import edu.tamu.aser.instrumentation.RVGlobalStateForInstrumentation;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

public class ProfileMethodTransformer extends MethodVisitor implements Opcodes {

    final static String logClass = "edu/tamu/aser/profile/ProfileRunTime";

	final static String CLASS_INTEGER = "java/lang/Integer";
	final static String CLASS_BOOLEAN = "java/lang/Boolean";
	final static String CLASS_CHAR = "java/lang/Character";
	final static String CLASS_SHORT = "java/lang/Short";
	final static String CLASS_BYTE = "java/lang/Byte";
	final static String CLASS_LONG = "java/lang/Long";
	final static String CLASS_FLOAT = "java/lang/Float";
	final static String CLASS_DOUBLE = "java/lang/Double";

	final static String METHOD_VALUEOF = "valueOf";
	final static String DESC_INTEGER_VALUEOF = "(I)Ljava/lang/Integer;";
	final static String DESC_BOOLEAN_VALUEOF = "(Z)Ljava/lang/Boolean;";
	final static String DESC_BYTE_VALUEOF = "(B)Ljava/lang/Byte;";
	final static String DESC_SHORT_VALUEOF = "(S)Ljava/lang/Short;";
	final static String DESC_CHAR_VALUEOF = "(C)Ljava/lang/Character;";
	final static String DESC_LONG_VALUEOF = "(J)Ljava/lang/Long;";
	final static String DESC_FLOAT_VALUEOF = "(F)Ljava/lang/Float;";
	final static String DESC_DOUBLE_VALUEOF = "(D)Ljava/lang/Double;";

    boolean isInit, isSynchronized, isStatic, staticLock;
	String methodSignature;
	String className;
	   String methodName;

	private int maxindex_cur;// current max index of local variables
	private int line_cur;

	public ProfileMethodTransformer(int access, String desc,
			String methodName, String className, MethodVisitor mv) {
	    
        super(Opcodes.ASM5, mv);

		this.methodSignature = methodName + desc;
		this.isInit = (methodName.equals("<init>") || methodName
				.equals("<clinit>"));

        this.maxindex_cur = Type.getArgumentsAndReturnSizes(desc) + 1;
        this.className = className;
        this.methodName = methodName;

    
            
	}
	@Override
    public void visitLineNumber(int line, Label start) {
        line_cur = line;
        mv.visitLineNumber(line, start);
    }
//	@Override
//	   public void visitFieldInsn(int opcode, String owner, String name,
//	            String desc) {
//        mv.visitFieldInsn(opcode, owner, name, desc);
//
//	}
	public void visitFieldInsn(int opcode, String owner, String name,
			String desc) {

		String sig_var = (owner + "." + name).replace("/", ".");
		String sig_loc = (owner + "|" + methodSignature + "|" + sig_var + "|" + line_cur)
				.replace("/", ".");
		int SID = RVGlobalStateForInstrumentation.instance
				.getVariableId(sig_var);
		int ID = RVGlobalStateForInstrumentation.instance
				.getLocationId(sig_loc);

        if (!isInit) {
        
    		switch (opcode) {
    		case GETSTATIC:
    				addBipushInsn(mv, ID);
    				addBipushInsn(mv, SID);
    				addBipushInsn(mv, 0);
    
    				mv.visitMethodInsn(INVOKESTATIC, logClass,
    						RVConfig.instance.LOG_FIELD_ACCESS,
    						RVConfig.instance.DESC_LOG_FIELD_ACCESS_DETECT_SHARING);
    			break;
    
    		case PUTSTATIC:
    
                    addBipushInsn(mv,ID);
                    addBipushInsn(mv,SID);
                
                    addBipushInsn(mv,1);
                    mv.visitMethodInsn(INVOKESTATIC, logClass, RVConfig.instance.LOG_FIELD_ACCESS,
                            RVConfig.instance.DESC_LOG_FIELD_ACCESS_DETECT_SHARING);
    
    			break;
    
    		case GETFIELD:
    
                    addBipushInsn(mv,ID);
                   addBipushInsn(mv,SID);
                   
                   addBipushInsn(mv,0);
    
                   
                   mv.visitMethodInsn(INVOKESTATIC, logClass, RVConfig.instance.LOG_FIELD_ACCESS,
                           RVConfig.instance.DESC_LOG_FIELD_ACCESS_DETECT_SHARING);
    			break;
    
    		case PUTFIELD:
    			if (name.startsWith("this$")
    					|| (className.contains("$") && name.startsWith("val$"))) {
    				// inner class or strange class
    				break;
    			}
    
    			 addBipushInsn(mv,ID);
                 addBipushInsn(mv,SID);
                 addBipushInsn(mv,1); 
                
                mv.visitMethodInsn(INVOKESTATIC, logClass, RVConfig.instance.LOG_FIELD_ACCESS,
                        RVConfig.instance.DESC_LOG_FIELD_ACCESS_DETECT_SHARING);
    			break;
    		default:
    			System.err.println("Unknown field access opcode " + opcode);
    			System.exit(1);
    		}
        }
        mv.visitFieldInsn(opcode, owner, name, desc);

	}
	
	  
    @Override
    public void visitMethodInsn(int opcode, String owner, String name,
            String desc) {
        // maxindex_cur++;
         mv.visitMethodInsn(opcode, owner, name, desc);

//        String sig_loc = (className + "|" + methodSignature + "|" + line_cur)
//                .replace("/", ".");
//        int ID = RVGlobalStateForInstrumentation.instance
//                .getLocationId(sig_loc);
//        switch (opcode) {
//        case INVOKEVIRTUAL:
//            if (RVGlobalStateForInstrumentation.instance.isThreadClass(owner)
//                    && name.equals("start") && desc.equals("()V")) {
//                maxindex_cur++;
//                int index = maxindex_cur;
//
//                mv.visitInsn(DUP);
//                mv.visitVarInsn(ASTORE, index);
//                addBipushInsn(mv, ID);
//                mv.visitVarInsn(ALOAD, index);
//                mv.visitMethodInsn(INVOKESTATIC, ProfileInstrumentor.logClass,
//                        RVConfig.instance.LOG_THREAD_BEFORE_START,
//                        RVConfig.instance.DESC_LOG_THREAD_START);
//
//                mv.visitMethodInsn(opcode, owner, name, desc);
//
//            } else if (RVGlobalStateForInstrumentation.instance
//                    .isThreadClass(owner)
//                    && name.equals("join")
//                    && desc.equals("()V")) {
//                // consider timed join later
//                maxindex_cur++;
//                int index = maxindex_cur;
//
//                // Comment out reason below
//                 //mv.visitInsn(DUP);
//
//                mv.visitVarInsn(ASTORE, index);
//
//                // Comment out join because it will be called inside performJoin
//                // by ReEx
//                 //mv.visitMethodInsn(opcode, owner, name, desc);
//
//                addBipushInsn(mv, ID);
//                mv.visitVarInsn(ALOAD, index);
//                mv.visitMethodInsn(INVOKESTATIC, ProfileInstrumentor.logClass,
//                        RVConfig.instance.LOG_THREAD_JOIN,
//                        RVConfig.instance.DESC_LOG_THREAD_JOIN);
//
//            } else {
//                mv.visitMethodInsn(opcode, owner, name, desc);
//            }
//
//            break;
//        case INVOKESPECIAL:
//        case INVOKESTATIC:
//        case INVOKEINTERFACE:
//            mv.visitMethodInsn(opcode, owner, name, desc);
//            break;
//        default:
//            System.err.println("Unknown method invocation opcode " + opcode);
//            System.exit(1);
//        }
    }
//    public void visitMaxs(int maxStack, int maxLocals) {
//        mv.visitMaxs(maxStack + 5, maxindex_cur+2);//may change to ...
//
//    }
//	@Override
//	public void visitVarInsn(int opcode, int var) {
//		if (var > maxindex_cur) {
//			maxindex_cur = var;
//		}
//
//		switch (opcode) {
//		case LSTORE:
//		case DSTORE:
//		case LLOAD:
//		case DLOAD:
//			if (var == maxindex_cur) {
//				maxindex_cur = var + 1;
//			}
//			mv.visitVarInsn(opcode, var);
//			break;
//		case ISTORE:
//		case FSTORE:
//		case ASTORE:
//		case ILOAD:
//		case FLOAD:
//		case ALOAD:
//		case RET:
//			mv.visitVarInsn(opcode, var);
//			break;
//		default:
//			System.err.println("Unknown var instruction opcode " + opcode);
//			System.exit(1);
//		}
//	}

	
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
    private void instrumentArrayAccess(int opcode, boolean isWrite)
    {
        //super.updateThreadLocation();
        //this.informSchedulerAboutArrayAccess(true, !isWrite);
        
        String sig_loc = (className + "|" + methodSignature + "|" + line_cur)
                .replace("/", ".");
        int ID = RVGlobalStateForInstrumentation.instance
                .getLocationId(sig_loc);
        

        if(isWrite)
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

            addBipushInsn(mv, 1);

            mv.visitMethodInsn(INVOKESTATIC, logClass, RVConfig.instance.LOG_ARRAY_ACCESS,
                    RVConfig.instance.DESC_LOG_ARRAY_ACCESS_DETECT_SHARING);
            
            mv.visitVarInsn(ILOAD, index2);// index
            mv.visitVarInsn(arrayLoadOpcode(opcode), index1);// value
         
        }
        else {
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

            mv.visitMethodInsn(INVOKESTATIC, logClass, RVConfig.instance.LOG_ARRAY_ACCESS,
                    RVConfig.instance.DESC_LOG_ARRAY_ACCESS_DETECT_SHARING);
    }
    }
    public void visitJumpInsn(int opcode, Label label) {
        String sig_loc = (className + "|" + methodSignature + "|" + line_cur)
                .replace("/", ".");
        int ID = RVGlobalStateForInstrumentation.instance
                .getLocationId(sig_loc);

        switch (opcode) {
        case IFEQ:// branch
        case IFNE:
        case IFLT:
        case IFGE:
        case IFGT:
        case IFLE:
        case IF_ICMPEQ:
        case IF_ICMPNE:
        case IF_ICMPLT:
        case IF_ICMPGE:
        case IF_ICMPGT:
        case IF_ICMPLE:
        case IF_ACMPEQ:
        case IF_ACMPNE:
        case IFNULL:
        case IFNONNULL:
//            addBipushInsn(mv, ID);
//            mv.visitMethodInsn(INVOKESTATIC, RVInstrumentor.logClass,
//                    RVConfig.instance.LOG_BRANCH,
//                    RVConfig.instance.DESC_LOG_BRANCH);
        default:
            mv.visitJumpInsn(opcode, label);
            break;
        }
    }
    @Override
    public void visitIincInsn(int var, int increment) {
        if (var > maxindex_cur) {
            maxindex_cur = var;
        }
        mv.visitIincInsn(var, increment);
    }
    public void visitVarInsn(int opcode, int var) {
        if (var > maxindex_cur) {
            maxindex_cur = var;
        }

        switch (opcode) {
        case LSTORE:
        case DSTORE:
        case LLOAD:
        case DLOAD:
            if (var == maxindex_cur) {
                maxindex_cur = var + 1;
            }
            mv.visitVarInsn(opcode, var);
            break;
        case ISTORE:
        case FSTORE:
        case ASTORE:
        case ILOAD:
        case FLOAD:
        case ALOAD:
        case RET:
            mv.visitVarInsn(opcode, var);
            break;
        default:
            System.err.println("Unknown var instruction opcode " + opcode);
            System.exit(1);
        }
    }
    
	//@Override
	public void visitInsn(int opcode) {

	    if(!isInit)
	    {

    
    		// Array access here
    		if (opcode == Opcodes.AALOAD || opcode == Opcodes.IALOAD
    				|| opcode == Opcodes.LALOAD || opcode == Opcodes.SALOAD
    				|| opcode == Opcodes.CALOAD || opcode == Opcodes.DALOAD
    				|| opcode == Opcodes.FALOAD || opcode == Opcodes.BALOAD) {
    		    
                instrumentArrayAccess(opcode,false);     

    
    		} else if (opcode == Opcodes.AASTORE || opcode == Opcodes.IASTORE
    				|| opcode == Opcodes.LASTORE || opcode == Opcodes.SASTORE
    				|| opcode == Opcodes.CASTORE || opcode == Opcodes.DASTORE
    				|| opcode == Opcodes.FASTORE || opcode == Opcodes.BASTORE) {
    		    
                instrumentArrayAccess(opcode,true);            
    
    		}
	    }
	    
         mv.visitInsn(opcode);
     
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
}
