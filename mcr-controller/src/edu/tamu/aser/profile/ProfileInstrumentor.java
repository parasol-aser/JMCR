package edu.tamu.aser.profile;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;

import edu.tamu.aser.profile.ProfileClassTransformer;
import edu.tamu.aser.profile.ProfileRunTime;
import edu.tamu.aser.reexecution.Scheduler;
import edu.tamu.aser.rvinstrumentation.ExtendedClassWriter;
import edu.tamu.aser.rvinstrumentation.MCRProperties;

public class ProfileInstrumentor {
    private static final String SLASH = "/";
    private static final String DOT = ".";
    private static final String SEMICOLON = ";";
    
    public static String logClass = "edu/tamu/aser/profile/ProfileRunTime";

    
    private static final String JUC_DOTS = "java.util.concurrent";

    private static final String INSTUMENTATION_DEBUG_DEFAULT = "false";
    private static final String INSTRUMENTATION_PACKAGES_DEFAULT = "default";

    public static final String INSTR_EVENTS_RECEIVER = Scheduler.class.getName().replace(DOT, SLASH);

    public static Set<String> packagesThatWereInstrumented = new HashSet<String>();
    public static Set<String> packagesThatWereNOTInstrumented = new HashSet<String>();

    private static final Set<String> pckgPrefixesToIgnore = new HashSet<String>();
    private static final Set<String> pckgsToIgnore = new HashSet<String>();
    private static final Set<String> classPrefixesToIgnore = new HashSet<String>();
    private static final Set<String> classesToIgnore = new HashSet<String>();
    private static final Set<String> pckgPrefixesToAllow = new HashSet<String>();
    private static final Set<String> pckgsToAllow = new HashSet<String>();
    private static final Set<String> classPrefixesToAllow = new HashSet<String>();
    private static final Set<String> classesToAllow = new HashSet<String>();

    private static void storePropertyValues(String values, Set<String> toSet) {
        if (values != null) {
            String[] split = values.split(SEMICOLON);
            for (String val : split) {
                val = val.replace(DOT, SLASH).trim();
                if (!val.isEmpty()) {
                    toSet.add(val);
                }
            }
        }
    }

    private static boolean shouldInstrumentClass(String name) {

        String pckgName = INSTRUMENTATION_PACKAGES_DEFAULT;
        int lastSlashIndex = name.lastIndexOf(SLASH);
        // Non-default package
        if (lastSlashIndex != -1) {
            pckgName = name.substring(0, lastSlashIndex);
        }

        // Phase 1 - check if explicitly allowed
        if (classesToAllow.contains(name)) {
            packagesThatWereInstrumented.add(pckgName);
            return true;
        }

        // Phase 2 - check if prefix is allowed
        for (String classPrefix : classPrefixesToAllow) {
            if (name.startsWith(classPrefix)) {
                packagesThatWereInstrumented.add(pckgName);
                return true;
            }
        }

        // Phase 3 - check if package is allowed
        if (pckgsToAllow.contains(pckgName)) {
            packagesThatWereInstrumented.add(pckgName);
            return true;
        }

        // Phase 4 - check if package is allowed via prefix matching
        for (String pckgPrefix : pckgPrefixesToAllow) {
            if (pckgName.startsWith(pckgPrefix)) {
                packagesThatWereInstrumented.add(pckgName);
                return true;
            }
        }

        // Phase 5 - check for any ignores
        if (classesToIgnore.contains(name)) {
            packagesThatWereNOTInstrumented.add(pckgName);
            return false;
        }
        if (pckgsToIgnore.contains(pckgName)) {
            packagesThatWereNOTInstrumented.add(pckgName);
            return false;
        }
        for (String classPrefix : classPrefixesToIgnore) {
            if (name.startsWith(classPrefix)) {
                packagesThatWereNOTInstrumented.add(pckgName);
                return false;
            }
        }
        for (String pckgPrefix : pckgPrefixesToIgnore) {
            //System.out.println(pckgPrefix);
            if (pckgName.startsWith(pckgPrefix)) {
                packagesThatWereNOTInstrumented.add(pckgName);
                return false;
            }
        }

        // Otherwise instrument by default
        packagesThatWereInstrumented.add(pckgName);
        return true;
    }

    public static void premain(String agentArgs, Instrumentation inst) {
        MCRProperties mcrProps = MCRProperties.getInstance();
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_PACKAGES_IGNORE_PREFIXES_KEY), pckgPrefixesToIgnore);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_PACKAGES_IGNORE_KEY), pckgsToIgnore);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_CLASSES_IGNORE_PREFIXES_KEY), classPrefixesToIgnore);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_CLASSES_IGNORE_KEY), classesToIgnore);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_PACKAGES_ALLOW_PREFIXES_KEY), pckgPrefixesToAllow);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_PACKAGES_ALLOW_KEY), pckgsToAllow);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_CLASSES_ALLOW_PREFIXES_KEY), classPrefixesToAllow);
        storePropertyValues(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_CLASSES_ALLOW_KEY), classesToAllow);

        /* Should instrumentation debug messages be enabled? */
        final boolean debugIntrumentation = Boolean.parseBoolean(mcrProps.getProperty(MCRProperties.INSTRUMENTATION_DEBUG_KEY,
                INSTUMENTATION_DEBUG_DEFAULT));

        ProfileRunTime.init();
        
        inst.addTransformer(new ClassFileTransformer() {
            public byte[] transform(ClassLoader l, String name, Class<?> c, ProtectionDomain d, byte[] bytes) throws IllegalClassFormatException {
                try {
                    /*
                     * If the package is included in the packages to instrument,
                     * or the class is included in the classes to instrument,
                     * instrument the class
                     */
                    if (shouldInstrumentClass(name)) {
                        if (debugIntrumentation)
                        {
                            System.out.println("Instrumenting " + name);
                        }

                        ClassReader classReader = new ClassReader(bytes);
                        ClassWriter classWriter = new ExtendedClassWriter(classReader, ClassWriter.COMPUTE_FRAMES);

                            ProfileClassTransformer transformer = new ProfileClassTransformer(classWriter);
                            classReader.accept(transformer, ClassReader.EXPAND_FRAMES);
                            bytes = classWriter.toByteArray();
                     
                        
                    }
                } catch (Throwable th) {
                    th.printStackTrace();
                }
                return bytes;
            }
        }, true);

        /* Re-transform already loaded java.util.concurrent classes */
        try {
            List<Class<?>> classesToReTransform = new ArrayList<Class<?>>();
            for (Class<?> loadedClass : inst.getAllLoadedClasses()) {
                if (inst.isModifiableClass(loadedClass) && loadedClass.getPackage().getName().startsWith(JUC_DOTS)) {
                    classesToReTransform.add(loadedClass);
                }
            }
            inst.retransformClasses(classesToReTransform.toArray(new Class<?>[classesToReTransform.size()]));
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
            System.err.println("Unable to modify a pre-loaded java.util.concurrent class!");
            System.exit(2);
        }
    }
}
