package edu.tamu.aser.mcr;

import edu.tamu.aser.instrumentation.RVGlobalStateForInstrumentation;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;


public class ClassAdapter extends ClassVisitor {

    private String classname;
	private String source;
	private boolean isThreadClass;
	private boolean isRunnableClass;

    private static final String RUN_METHOD_NAME = "run";

	public ClassAdapter(ClassVisitor classVisitor) {
		super(Opcodes.ASM5, classVisitor);
	}

	@Override
	public void visit(int version,
					  int access, String name,
                      String signature,
                      String superName,
                      String[] interfaces) {
		super.visit(version, access, name, signature, superName, interfaces);
		this.classname = name;
		isThreadClass = RVGlobalStateForInstrumentation.instance.isThreadClass(classname);
	    isRunnableClass = RVGlobalStateForInstrumentation.instance.isRunnableClass(classname);
	}
	
	@Override
    public void visitSource(String source, String debug) {
        this.source = source;
        if (cv != null) {
            cv.visitSource(source, debug);
        }
    }
	
	public FieldVisitor visitField(int access, String name, String desc,
            String signature, Object value) 
	{
        String sig_var = (classname+"."+name).replace("/", ".");
        RVGlobalStateForInstrumentation.instance.getVariableId(sig_var);
        if((access & Opcodes.ACC_VOLATILE)!=0)
        {
            RVGlobalStateForInstrumentation.instance.addVolatileVariable(sig_var);
        }
        
        if (cv != null) {
            return cv.visitField(access, name, desc, signature, value);
        }
        return null;
    }

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions) {       

	    MethodVisitor mv = cv.visitMethod(access&(~Opcodes.ACC_SYNCHRONIZED),
                name, desc, signature, exceptions);

		if (mv != null) {
            boolean isSynchronized = false;
            boolean isStatic = false;

            if((access & Opcodes.ACC_SYNCHRONIZED)!=0)
                isSynchronized = true;
            if((access & Opcodes.ACC_STATIC)!=0)
                isStatic = true;
           
            boolean possibleRunMethod = name.equals(RUN_METHOD_NAME) && !isStatic
                    && (Type.getArgumentsAndReturnSizes(desc) >> 2) == 1 &&
                    Type.getReturnType(desc).equals(Type.VOID_TYPE) &&
                    (isThreadClass||isRunnableClass);

            mv = new MethodAdapter(mv,
			        source, 
			        access, 
			        desc, 
			        classname,
			        name,name+desc,name.equals("<init>")||name.equals("<clinit>"), 
			        isSynchronized,
			        isStatic,
			        possibleRunMethod);
		}
		return mv;
	}
}
