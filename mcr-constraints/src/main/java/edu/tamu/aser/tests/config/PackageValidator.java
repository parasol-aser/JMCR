package edu.tamu.aser.config;

import com.beust.jcommander.IParameterValidator;
import com.beust.jcommander.ParameterException;

/**
* Created by Traian on 06.08.2014.
*/
public class PackageValidator implements IParameterValidator {

    @Override
    public void validate(String name, String value) throws ParameterException {
        String[] packages = value.split(",");
        for (String pkg : packages) {
            if (pkg.isEmpty()) {
                throw new ParameterException("Empty package specified for the " + name + " option.");
            }
            String[] pkgParts = pkg.replace('.','/').replace('\\', '/').split("/");
            for (String pkgPart : pkgParts) {
                if (pkgPart.isEmpty()) {
                    throw new ParameterException("Empty package part in " + pkg +
                            " specified as part of the " + name + " option.");
                }
                char[] chars = pkgPart.toCharArray();
                if (!Character.isJavaIdentifierStart(chars[0])) {
                    throw new ParameterException("Incorrect package component " + pkgPart +
                            " specified as part of the " + name + " option.");
                }
                for (char c : chars) {
                    if (!Character.isJavaIdentifierPart(chars[0])) {
                        throw new ParameterException("Incorrect package component " + pkgPart +
                                " specified as part of the " + name + " option.");
                    }

                }
            }
        }

    }
}
