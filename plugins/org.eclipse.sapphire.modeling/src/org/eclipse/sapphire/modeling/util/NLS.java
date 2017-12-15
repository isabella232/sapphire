/*******************************************************************************
 * Copyright (c) 2005, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *    IBM - Initial API and implementation
 *    Konstantin Komissarchik - Adaptation for Sapphire and Java 5
 *******************************************************************************/

package org.eclipse.sapphire.modeling.util;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

/**
 * Common superclass for all message bundle classes.  Provides convenience
 * methods for manipulating messages.
 * <p>
 * The <code>#bind</code> methods perform string substitution and should be considered a
 * convenience and <em>not</em> a full substitute replacement for <code>MessageFormat#format</code>
 * method calls. 
 * </p>
 * <p>
 * Text appearing within curly braces in the given message, will be interpreted
 * as a numeric index to the corresponding substitution object in the given array. Calling
 * the <code>#bind</code> methods with text that does not map to an integer will result in an
 * {@link IllegalArgumentException}.
 * </p>
 * <p>
 * Text appearing within single quotes is treated as a literal. A single quote is escaped by
 * a preceeding single quote.
 * </p>
 * <p>
 * Clients who wish to use the full substitution power of the <code>MessageFormat</code> class should
 * call that class directly and not use these <code>#bind</code> methods.
 * </p>
 *
 * @author IBM
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class NLS {

    private static final Object[] EMPTY_ARGS = new Object[0];
    private static final String EXTENSION = ".properties";
    private static String[] nlSuffixes;

    /*
     * This object is assigned to the value of a field map to indicate
     * that a translated message has already been assigned to that field.
     */
    static final Object ASSIGNED = new Object();

    /**
     * Creates a new NLS instance.
     */
    protected NLS() {
        super();
    }

    /**
     * Bind the given message's substitution locations with the given string values.
     * 
     * @param message the message to be manipulated
     * @param bindings An array of objects to be inserted into the message
     * @return the manipulated String
     * @throws IllegalArgumentException if the text appearing within curly braces in the given message does not map to an integer
     */
    public static String bind(String message, Object... bindings) {
        if (message == null)
            return "No message available.";
        if (bindings == null || bindings.length == 0)
            bindings = EMPTY_ARGS;
        
        int length = message.length();
        //estimate correct size of string buffer to avoid growth
        int bufLen = length + (bindings.length * 5);
        StringBuffer buffer = new StringBuffer(bufLen < 0 ? 0 : bufLen);
        for (int i = 0; i < length; i++) {
            char c = message.charAt(i);
            switch (c) {
                case '{' :
                    int index = message.indexOf('}', i);
                    // if we don't have a matching closing brace then...
                    if (index == -1) {
                        buffer.append(c);
                        break;
                    }
                    i++;
                    if (i >= length) {
                        buffer.append(c);
                        break;
                    }
                    // look for a substitution
                    int number = -1;
                    try {
                        number = Integer.parseInt(message.substring(i, index));
                    } catch (NumberFormatException e) {
                        throw (IllegalArgumentException) new IllegalArgumentException().initCause(e);
                    }
                    if (number >= bindings.length || number < 0) {
                        buffer.append("<missing argument>");
                        i = index;
                        break;
                    }
                    buffer.append(bindings[number]);
                    i = index;
                    break;
                case '\'' :
                    // if a single quote is the last char on the line then skip it
                    int nextIndex = i + 1;
                    if (nextIndex >= length) {
                        buffer.append(c);
                        break;
                    }
                    char next = message.charAt(nextIndex);
                    // if the next char is another single quote then write out one
                    if (next == '\'') {
                        i++;
                        buffer.append(c);
                        break;
                    }
                    // otherwise we want to read until we get to the next single quote
                    index = message.indexOf('\'', nextIndex);
                    // if there are no more in the string, then skip it
                    if (index == -1) {
                        buffer.append(c);
                        break;
                    }
                    // otherwise write out the chars inside the quotes
                    buffer.append(message.substring(nextIndex, index));
                    i = index;
                    break;
                default :
                    buffer.append(c);
            }
        }
        return buffer.toString();
    }

    /**
     * Initialize the given class with the values from the message properties specified by the
     * base name.  The base name specifies a fully qualified base name to a message properties file,
     * including the package where the message properties file is located.  The class loader of the 
     * specified class will be used to load the message properties resources.
     * <p>
     * For example, if the locale is set to en_US and <code>org.eclipse.example.nls.messages</code>
     * is used as the base name then the following resources will be searched using the class
     * loader of the specified class:
     * <pre>
     *   org/eclipse/example/nls/messages_en_US.properties
     *   org/eclipse/example/nls/messages_en.properties
     *   org/eclipse/example/nls/messages.properties
     * </pre>
     * </p>
     * 
     * @param baseName the base name of a fully qualified message properties file.
     * @param clazz the class where the constants will exist
     */
    public static void initializeMessages(final String baseName, final Class<?> clazz) {
        if (System.getSecurityManager() == null) {
            load(baseName, clazz);
            return;
        }
        AccessController.doPrivileged(new PrivilegedAction<Object>() {
            public Object run() {
                load(baseName, clazz);
                return null;
            }
        });
    }

    /*
     * Build an array of property files to search.  The returned array contains
     * the property fields in order from most specific to most generic.
     * So, in the FR_fr locale, it will return file_fr_FR.properties, then
     * file_fr.properties, and finally file.properties.
     */
    private static String[] buildVariants(String root) {
        if (nlSuffixes == null) {
            //build list of suffixes for loading resource bundles
            String nl = Locale.getDefault().toString();
            List<String> result = new ArrayList<String>(4);
            int lastSeparator;
            while (true) {
                result.add('_' + nl + EXTENSION);
                lastSeparator = nl.lastIndexOf('_');
                if (lastSeparator == -1)
                    break;
                nl = nl.substring(0, lastSeparator);
            }
            //add the empty suffix last (most general)
            result.add(EXTENSION);
            nlSuffixes = result.toArray(new String[result.size()]);
        }
        root = root.replace('.', '/');
        String[] variants = new String[nlSuffixes.length];
        for (int i = 0; i < variants.length; i++)
            variants[i] = root + nlSuffixes[i];
        return variants;
    }

    private static void computeMissingMessages(String bundleName, Class<?> clazz, Map<Object, Object> fieldMap, Field[] fieldArray, boolean isAccessible) {
        // iterate over the fields in the class to make sure that there aren't any empty ones
        final int MOD_EXPECTED = Modifier.PUBLIC | Modifier.STATIC;
        final int MOD_MASK = MOD_EXPECTED | Modifier.FINAL;
        final int numFields = fieldArray.length;
        for (int i = 0; i < numFields; i++) {
            Field field = fieldArray[i];
            if ((field.getModifiers() & MOD_MASK) != MOD_EXPECTED)
                continue;
            //if the field has a a value assigned, there is nothing to do
            if (fieldMap.get(field.getName()) == ASSIGNED)
                continue;
            try {
                // Set a value for this empty field. We should never get an exception here because
                // we know we have a public static non-final field. If we do get an exception, silently
                // log it and continue. This means that the field will (most likely) be un-initialized and
                // will fail later in the code and if so then we will see both the NPE and this error.
                String value = "NLS missing message: " + field.getName() + " in: " + bundleName;
                if (!isAccessible)
                    field.setAccessible(true);
                field.set(null, value);
            } catch (Exception e) {
                System.err.println( "Error setting the missing message value for: " + field.getName() );
                e.printStackTrace();
            }
        }
    }

    /*
     * Load the given resource bundle using the specified class loader.
     */
    static void load(final String bundleName, Class<?> clazz) {
        final Field[] fieldArray = clazz.getDeclaredFields();
        ClassLoader loader = clazz.getClassLoader();

        boolean isAccessible = (clazz.getModifiers() & Modifier.PUBLIC) != 0;

        //build a map of field names to Field objects
        final int len = fieldArray.length;
        Map<Object, Object> fields = new HashMap<Object, Object>(len * 2);
        for (int i = 0; i < len; i++)
            fields.put(fieldArray[i].getName(), fieldArray[i]);

        // search the variants from most specific to most general, since
        // the MessagesProperties.put method will mark assigned fields
        // to prevent them from being assigned twice
        final String[] variants = buildVariants(bundleName);
        for (int i = 0; i < variants.length; i++) {
            // loader==null if we're launched off the Java boot classpath
            final InputStream input = loader == null ? ClassLoader.getSystemResourceAsStream(variants[i]) : loader.getResourceAsStream(variants[i]);
            if (input == null)
                continue;
            try {
                final MessagesProperties properties = new MessagesProperties(fields, bundleName, isAccessible);
                properties.load(input);
            } catch (IOException e) {
                System.err.println( "Error loading " + variants[i] );
                e.printStackTrace();
            } finally {
                try {
                    input.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
        computeMissingMessages(bundleName, clazz, fields, fieldArray, isAccessible);
    }

    /*
     * Class which sub-classes java.util.Properties and uses the #put method
     * to set field values rather than storing the values in the table.
     */
    private static class MessagesProperties extends Properties {

        private static final int MOD_EXPECTED = Modifier.PUBLIC | Modifier.STATIC;
        private static final int MOD_MASK = MOD_EXPECTED | Modifier.FINAL;
        private static final long serialVersionUID = 1L;

        private final String bundleName;
        private final Map<Object, Object> fields;
        private final boolean isAccessible;

        public MessagesProperties(Map<Object, Object> fieldMap, String bundleName, boolean isAccessible) {
            super();
            this.fields = fieldMap;
            this.bundleName = bundleName;
            this.isAccessible = isAccessible;
        }

        /* (non-Javadoc)
         * @see java.util.Hashtable#put(java.lang.Object, java.lang.Object)
         */
        @Override
        public synchronized Object put(Object key, Object value) {
            Object fieldObject = this.fields.put(key, ASSIGNED);
            // if already assigned, there is nothing to do
            if (fieldObject == ASSIGNED)
                return null;
            if (fieldObject == null) {
                final String msg = "NLS unused message: " + key + " in: " + this.bundleName;
                System.err.println( msg );
                return null;
            }
            final Field field = (Field) fieldObject;
            //can only set value of public static non-final fields
            if ((field.getModifiers() & MOD_MASK) != MOD_EXPECTED)
                return null;
            try {
                // Check to see if we are allowed to modify the field. If we aren't (for instance 
                // if the class is not public) then change the accessible attribute of the field
                // before trying to set the value.
                if (!this.isAccessible)
                    field.setAccessible(true);
                // Set the value into the field. We should never get an exception here because
                // we know we have a public static non-final field. If we do get an exception, silently
                // log it and continue. This means that the field will (most likely) be un-initialized and
                // will fail later in the code and if so then we will see both the NPE and this error.

                // Extra care is taken to be sure we create a String with its own backing char[] (bug 287183)
                // This is to ensure we do not keep the key chars in memory.
                field.set(null, new String(((String) value).toCharArray()));
            } catch (Exception e) {
                System.err.println( "Exception setting field value." );
                e.printStackTrace();
            }
            return null;
        }
    }
}