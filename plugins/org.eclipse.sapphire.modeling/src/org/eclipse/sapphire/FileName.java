/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire;

import java.util.Set;

import org.eclipse.sapphire.util.SetFactory;

/**
 * Represents the name of a file within a folder.
 * 
 * <p>Ready to be used as a type of a value property. The following services are provided:</p>
 * 
 * <ol>
 * 
 *   <li>ValidationService - Flags invalid file names as errors.</p>
 *   
 *   <li>ValueSerializationService - Only creates FileName objects that are valid for
 *   the current platform.</p>
 *   
 *   <li>ValueNormalizationService</li>
 *   
 *   <ol type="A">
 *   
 *     <li>Leading whitespace is removed.</li>
 *     
 *     <li>Trailing whitespace and dots are removed.</li>
 *     
 *     <li>Extension is added if file name does not have one already and if the property
 *     has a FileExtensionsService (usually via @FileExtensions annotation).</li>
 *   
 *   </ol>
 *   
 * </ol>
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class FileName implements Comparable<FileName>
{
    private static final boolean WINDOWS;
    private static final Set<Character> INVALID_CHARACTERS;
    private static final Set<String> INVALID_BASENAMES;
    private static final Set<String> INVALID_FULLNAMES;

    static
    {
        WINDOWS = System.getProperties().getProperty( "os.name" ).startsWith( "Windows" );
        
        final SetFactory<Character> invalidCharactersSetFactory = SetFactory.start();
        final SetFactory<String> invalidBaseNameSetFactory = SetFactory.start();
        final SetFactory<String> invalidFullNameSetFactory = SetFactory.start();
        
        for( int i = 0; i <= 31; i++ )
        {
            invalidCharactersSetFactory.add( (char) i );
        }
        
        if( WINDOWS )
        {
            invalidCharactersSetFactory.add( '\\' );
            invalidCharactersSetFactory.add( '/' );
            invalidCharactersSetFactory.add( ':' );
            invalidCharactersSetFactory.add( '*' );
            invalidCharactersSetFactory.add( '?' );
            invalidCharactersSetFactory.add( '"' );
            invalidCharactersSetFactory.add( '<' );
            invalidCharactersSetFactory.add( '>' );
            invalidCharactersSetFactory.add( '|' );
            
            invalidBaseNameSetFactory.add( "aux" ); 
            invalidBaseNameSetFactory.add( "com1" ); 
            invalidBaseNameSetFactory.add( "com2" );
            invalidBaseNameSetFactory.add( "com3" );
            invalidBaseNameSetFactory.add( "com4" ); 
            invalidBaseNameSetFactory.add( "com5" );
            invalidBaseNameSetFactory.add( "com6" );
            invalidBaseNameSetFactory.add( "com7" );
            invalidBaseNameSetFactory.add( "com8" );
            invalidBaseNameSetFactory.add( "com9" );
            invalidBaseNameSetFactory.add( "con" );
            invalidBaseNameSetFactory.add( "lpt1" );
            invalidBaseNameSetFactory.add( "lpt2" );
            invalidBaseNameSetFactory.add( "lpt3" );
            invalidBaseNameSetFactory.add( "lpt4" );
            invalidBaseNameSetFactory.add( "lpt5" );
            invalidBaseNameSetFactory.add( "lpt6" );
            invalidBaseNameSetFactory.add( "lpt7" );
            invalidBaseNameSetFactory.add( "lpt8" );
            invalidBaseNameSetFactory.add( "lpt9" );
            invalidBaseNameSetFactory.add( "nul" );
            invalidBaseNameSetFactory.add( "prn" );
            
            invalidFullNameSetFactory.add( "clock$" );
        }
        else
        {
            invalidCharactersSetFactory.add( '/' );
        }
        
        INVALID_CHARACTERS = invalidCharactersSetFactory.result();
        INVALID_BASENAMES = invalidBaseNameSetFactory.result();
        INVALID_FULLNAMES = invalidFullNameSetFactory.result();
    }
    
    private final String full;
    private final String base;
    private final String extension;
    
    /**
     * Constructs a new FileName object.
     * 
     * @param name file name as a string
     * @throws IllegalArgumentException if file name is invalid for the current platform
     */

    public FileName( final String name )
    {
        if( ! valid( name ) )
        {
            throw new IllegalArgumentException( name );
        }
        
        // Note that the rest of the code in the constructor is able to avoid handling various
        // corner cases because they are ruled out by valid() call above.
        
        this.full = name;
        
        int segments = 0;
        
        for( String segment : name.split( "\\." ) )
        {
            if( segment.trim().length() > 0 )
            {
                segments++;
                
                if( segments > 1 )
                {
                    break; // Only need to know if count is anything other than one.
                }
            }
        }
        
        if( segments == 1 )
        {
            this.base = name;
            this.extension = null;
        }
        else
        {
            final int lastDot = name.lastIndexOf( '.' );
            
            this.base = name.substring( 0, lastDot );
            this.extension = name.substring( lastDot + 1 );
        }
    }
    
    /**
     * Returns the full file name, including the base portion and extension.
     * 
     * @return the full file name, including the base portion and extension.
     */
    
    public String full()
    {
        return this.full;
    }
    
    /**
     * Returns the portion of the file name in front of extension.
     * 
     * @return the portion of the file name in front of extension
     */
    
    public String base()
    {
        return this.base;
    }
    
    /**
     * Returns the file extension. A file extension is defined as the last significant segment
     * of a file name that has at least two significant segments. The significant segments are
     * derived by splitting the full file name on dots and discarding all zero-length segments
     * as well as those segments composed entirely of whitespace.
     * 
     * <p>For instance, file name "config.xml" has extension "xml" while file name ".config" 
     * has no extension.</p>
     *  
     * @return the file extension or null
     */
    
    public String extension()
    {
        return this.extension;
    }
    
    @Override
    public int hashCode()
    {
        return this.full.hashCode();
    }

    @Override
    public boolean equals( final Object obj )
    {
        if( obj instanceof FileName )
        {
            return this.full.equals( ( (FileName) obj ).full );
        }
        
        return false;
    }
    
    public int compareTo( final FileName fname )
    {
        return this.full.compareToIgnoreCase( fname.full );
    }

    @Override
    public String toString()
    {
        return this.full;
    }
    
    public static boolean valid( final String name )
    {
        if( name == null || name.length() == 0 )
        {
            return false;
        }

        final int length = name.length();
        
        if( length == 0 )
        {
            return false;
        }
        
        if( name.equals( "." ) || name.equals( ".." ) )
        {
            return false;
        }
        
        for( int i = 0; i < length; i++ )
        {
            if( INVALID_CHARACTERS.contains( name.charAt( i ) ) )
            {
                return false;
            }
        }
        
        if( WINDOWS )
        {
            final char lastChar = name.charAt( name.length() - 1 );
            
            if( lastChar == '.' || Character.isWhitespace( lastChar ) )
            {
                return false;
            }
            
            final int dot = name.indexOf( '.' );
            final String basename = ( dot == -1 ? name : name.substring( 0, dot ) );
            
            if( INVALID_BASENAMES.contains( basename.toLowerCase() ) )
            {
                return false;
            }
            
            if( INVALID_FULLNAMES.contains( name.toLowerCase() ) )
            {
                return false;
            }
        }
        
        return true;
    }

}
