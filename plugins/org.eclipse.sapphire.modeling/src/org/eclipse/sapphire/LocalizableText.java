/******************************************************************************
 * Copyright (c) 2014 Oracle and Other Contributors
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    IBM - implementation inspired by org.eclipse.osgi.util.NLS
 ******************************************************************************/

package org.eclipse.sapphire;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.eclipse.sapphire.internal.ValueSnapshot;
import org.eclipse.sapphire.services.ValueLabelService;

/**
 * Represents a string that can be localized by substituting a translated version based on
 * the current locale. Instances of LocalizableText are held as static fields in a class and must be
 * initialized by calling the init method on the class.
 * 
 * <p>The text lookup searches resource files from most specific to most generic, based on the 
 * current locale. When looking at the resource files, the key is the field name. If a corresponding
 * resource is not found, the text specified by the @Text annotation is used.</p>
 * 
 * <p>In the following example, two LocalizableText fields are used to implement a simple validator.</p>
 * 
 * <pre><code> public class Validator
 * {
 *     @Text( "Value must be specified." )
 *     private static LocalizableText mustBeSpecifiedMessage;
 *     
 *     @Text( "Value must not be larger than {0}." )
 *     private static LocalizableText mustNotBeLargerThanMessage;
 *     
 *     static
 *     {
 *         LocalizableText.init( ExampleValidationService.class );
 *     }
 *     
 *     public String validate( Integer value, Integer max )
 *     {
 *         if( value == null )
 *         {
 *             return mustBeSpecifiedMessage.text();
 *         }
 *         else if( max != null && value.intValue() > max.intValue() )
 *         {
 *             return mustNobeLargerThanMessage.format( max );
 *         }
 *         
 *         return null;
 *     }
 * }</code></pre>
 * 
 * <p>If the locale is set to en_US, the search order will be as follows:</p>
 * 
 * <ol>
 *   <li>org/eclipse/example/Validator_en_US.properties</li>
 *   <li>org/eclipse/example/Validator_en.properties</li>
 *   <li>org/eclipse/example/Validator.properties</li>
 *   <li>@Text annotation on the field</li>
 * </ol>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class LocalizableText
{
    private static final String EXTENSION = ".properties";
    private static String[] suffixes;

    private final String text;
    
    /**
     * Initializes all LocalizableText static fields present in the specified class. The semantics of this
     * method are equivalent to calling <code>init( clazz, null )</code>.
     * 
     * @param clazz the class that contains static LocalizableText fields to initialize
     * @throws IllegalArgumentException if clazz is null
     */
    

    public static void init( final Class<?> clazz )
    {
        init( clazz, null );
    }

    /**
     * Initializes all LocalizableText static fields present in the specified class.
     * 
     * @param clazz the class that contains static LocalizableText fields to initialize
     * @param resource the qualified base name of a resource file; defaults to class name if not specified
     * @throws IllegalArgumentException if clazz is null
     */
    
    public static void init( final Class<?> clazz, final String resource )
    {
        if( clazz == null )
        {
            throw new IllegalArgumentException();
        }
        
        final String resourceBaseName = ( resource == null ? clazz.getName() : resource );
        
        if( System.getSecurityManager() == null )
        {
            load( clazz, resourceBaseName );
        }
        else
        {
            AccessController.doPrivileged
            (
                new PrivilegedAction<Object>()
                {
                    public Object run()
                    {
                        load( clazz, resourceBaseName );
                        return null;
                    }
                }
            );
        }
    }
    
    private static void load( final Class<?> clazz, final String resource )
    {
        final Map<String,Field> fields = new HashMap<String,Field>();
        
        for( final Field field : clazz.getDeclaredFields() )
        {
            if( ( field.getModifiers() & Modifier.STATIC ) != 0 && field.getType() == LocalizableText.class )
            {
                field.setAccessible( true );
                fields.put( field.getName(), field );
            }
        }
        
        ClassLoader loader = clazz.getClassLoader();
        
        if( loader == null )
        {
            loader = ClassLoader.getSystemClassLoader();
        }
        
        if( suffixes == null )
        {
            // Build an array of the relevant property file suffixes in the order from most specific to most generic.
            // For instance, in FR_fr locale, the array will contain "_FR_fr.properties", then "_fr.properties" and
            // finally ".properties".
            
            String nl = Locale.getDefault().toString();
            final List<String> result = new ArrayList<String>( 4 );
            
            while( true )
            {
                result.add( '_' + nl + EXTENSION );
                
                final int lastSeparator = nl.lastIndexOf( '_' );
                
                if( lastSeparator == -1 )
                {
                    break;
                }
                
                nl = nl.substring(0, lastSeparator);
            }
            
            result.add( EXTENSION );
            
            suffixes = result.toArray( new String[ result.size() ] );
        }
        
        final String root = resource.replace( '.', '/' );
    
        for( final String suffix : suffixes )
        {
            final String variant = root + suffix;
            final InputStream input = loader.getResourceAsStream( variant );
            
            if( input != null )
            {
                Properties properties = null;
                
                try
                {
                    properties = new Properties();
                    properties.load( input );
                }
                catch( IOException e )
                {
                    System.err.println( "Error loading " + variant );
                    e.printStackTrace();
                }
                finally
                {
                    try
                    {
                        input.close();
                    }
                    catch( IOException e ) {}
                }
                
                if( properties != null )
                {
                    for( final Map.Entry<Object,Object> entry : properties.entrySet() )
                    {
                        final String key = (String) entry.getKey();
                        final Field field = fields.get( key );
                        
                        if( field == null )
                        {
                            final String msg = "Unused message: " + key + " in: " + resource;
                            System.err.println( msg );
                        }
                        else
                        {
                            try
                            {
                                if( field.get( null ) == null )
                                {
                                    // Extra care is taken to be sure we create a String with its own backing char[] (bug 287183)
                                    // This is to ensure we do not keep the key chars in memory.
                                    
                                    field.set( null, new LocalizableText( new String( ( (String) entry.getValue() ).toCharArray() ) ) );
                                }
                            }
                            catch( Exception e )
                            {
                                System.err.println( "Exception setting field " + field.getName() );
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        }
        
        for( final Field field : fields.values() )
        {
            try
            {
                if( field.get( null ) == null )
                {
                    final Text annotation = field.getAnnotation( Text.class );
                    final String value;

                    if( annotation != null )
                    {
                        value = annotation.value();
                    }
                    else
                    {
                        value = "Missing message: " + field.getName() + " in: " + resource;
                    }
                    
                    field.set( null, new LocalizableText( value ) );
                }
            }
            catch( Exception e )
            {
                System.err.println( "Exception setting field " + field.getName() );
                e.printStackTrace();
            }
        }
    }
    
    /**
     * The constructor must not be called directly. Instances are created by calling {@link #init(Class)}.
     */

    private LocalizableText( final String text )
    {
        this.text = text;
    }
    
    /**
     * Returns the raw text as it was found during initialization.
     * 
     * @return the raw text
     */
    
    public String text()
    {
        return this.text;
    }
    
    /**
     * Formats a message using this text as a template. The semantics of this method are equivalent
     * to {@link MessageFormat#format( String template, Object... bindings )}.
     * 
     * @param bindings the bindings to substitute into the template
     * @return the formatted message
     */
    
    public String format( final Object... bindings )
    {
        if( bindings == null || bindings.length == 0 )
        {
            return this.text;
        }
        
        boolean foundValueInBindings = false;
        
        for( final Object binding : bindings )
        {
            if( binding instanceof Value || binding instanceof ValueSnapshot )
            {
                foundValueInBindings = true;
                break;
            }
        }
        
        final Object[] objects;
        
        if( foundValueInBindings )
        {
            objects = new Object[ bindings.length ];
            
            for( int i = 0; i < bindings.length; i++ )
            {
                final Object binding = bindings[ i ];
                
                if( binding instanceof Value )
                {
                    final Value<?> value = (Value<?>) binding;
                    objects[ i ] = format( value.definition(), value.text() );
                }
                else if( binding instanceof ValueSnapshot )
                {
                    final ValueSnapshot snapshot = (ValueSnapshot) binding;
                    objects[ i ] = format( snapshot.property(), snapshot.text() );
                }
                else
                {
                    objects[ i ] = binding;
                }
            }
        }
        else
        {
            objects = bindings;
        }
        
        return MessageFormat.format( this.text, objects );
    }
    
    private static String format( final ValueProperty property, final String text ) 
    {
        String formatted = property.service( ValueLabelService.class ).provide( text );
        
        if( ! ( property.isOfType( Byte.class ) ||
                property.isOfType( Short.class ) ||
                property.isOfType( Integer.class ) ||
                property.isOfType( Long.class ) ||
                property.isOfType( Float.class ) ||
                property.isOfType( Double.class ) ||
                property.isOfType( BigInteger.class ) ||
                property.isOfType( BigDecimal.class ) ||
                property.isOfType( Boolean.class ) ) )
        {
            formatted = "\"" + formatted + "\"";
        }
        
        return formatted;
    }
    
    /**
     * Returns the raw text as it was found during initialization. The semantics of this method
     * are equivalent to {@link #text()}.
     * 
     * @return the raw text
     */
    
    @Override
    public String toString()
    {
        return this.text;
    }
    
}