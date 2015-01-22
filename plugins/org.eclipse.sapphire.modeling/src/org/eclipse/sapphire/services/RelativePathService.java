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

package org.eclipse.sapphire.services;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.util.MiscUtil;

/**
 * Service for value properties of type Path that enables relative path support. Typically attached to
 * the property using the @Service annotation. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class RelativePathService extends Service
{
    /**
     * Returns the absolute paths to folders that should be used as relativization roots.
     * 
     * @return the absolute paths to folders that should be used as relativization roots
     */
    
    public abstract List<Path> roots();
    
    /**
     * Determines whether relative paths should be confined to paths beneath roots and
     * precluded from using "../" parent navigation. The default implementation returns true.
     * 
     * @return true if relative paths should be confined to paths beneath roots 
     */
    
    public boolean enclosed()
    {
        return true;
    }
    
    /**
     * Converts the given absolute path to a relative path. The default implementation 
     * logic is defined as follows:
     * 
     * <pre><code> if( path != null )
     * {
     *     if( enclosed() )
     *     {
     *         for( Path root : roots() )
     *         {
     *             if( root is prefix of path )
     *             {
     *                 return path relative to root
     *             }
     *         }
     *     }
     *     else
     *     {
     *         for( Path root : roots() )
     *         {
     *             if( root device matches path device )
     *             {
     *                 return path relative to root (possibly with parent navigations)
     *             }
     *         }
     *     }
     * }
     * 
     * return null</code></pre>
     * 
     * <p>Can be overridden to customize conversion logic. The implementation must
     * return null if path parameter is null or if relative path could not be computed
     * for whatever reason. Exceptions must not be used to signal failure to convert.</p>
     * 
     * @param path the absolute path that should be converted to relative
     * @return the relative path or null if a relative path could not be computed
     */
    
    public Path convertToRelative( final Path path )
    {
        if( path != null )
        {
            if( enclosed() )
            {
                for( Path root : roots() )
                {
                    if( root.isPrefixOf( path ) )
                    {
                        return path.makeRelativeTo( root );
                    }
                }
            }
            else
            {
                final String pathDevice = path.getDevice();
                
                for( Path root : roots() )
                {
                    if( MiscUtil.equal( pathDevice, root.getDevice() ) )
                    {
                        return path.makeRelativeTo( root );
                    }
                }
            }
        }
        
        return null;
    }
    
    public final Path convertToRelative( final String path )
    {
        if( path != null )
        {
            return convertToRelative( new Path( path ) );
        }
        
        return null;
    }
    
    /**
     * Converts the given relative path to an absolute path. The default implementation 
     * logic is defined as follows:
     * 
     * <pre><code> if( path != null )
     * {
     *     if( enclosed() and path starts with ".." )
     *     {
     *         return null
     *     }
     *     
     *     absolute = null
     *     
     *     for( Path root : roots()
     *     {
     *         absolute = canonicalize( root + path )
     *         
     *         if( absolute path exists on the file system )
     *         {
     *             break
     *         }
     *     }
     *     
     *     return absolute
     * }
     * 
     * return null</code></pre>
     * 
     * <p>Can be overridden to customize conversion logic. The implementation must
     * return null if path parameter is null or if absolute path could not be computed
     * for whatever reason. Exceptions must not be used to signal failure to convert.</p>
     * 
     * @param path the relative path that should be converted to absolute
     * @return the absolute path or null if an absolute path could not be computed
     */
    
    public Path convertToAbsolute( final Path path )
    {
        if( path != null )
        {
            if( enclosed() && path.segmentCount() > 0 && path.segment( 0 ).equals( ".." ) )
            {
                return null;
            }
            
            Path absolute = null;
            
            for( Path root : roots() )
            {
                try
                {
                    final File file = root.append( path ).toFile().getCanonicalFile();
                    absolute = new Path( file.getPath() );
                    
                    if( file.exists() )
                    {
                        break;
                    }
                }
                catch( IOException e )
                {
                    // Intentionally ignoring to continue to the next root. If none of the roots
                    // produce a viable absolute path, a null return from this method signifies
                    // being unable to convert the relative path. That is sufficient.
                }
            }
            
            return absolute;
        }
        
        return null;
    }

    public final Path convertToAbsolute( final String path )
    {
        if( path != null )
        {
            return convertToAbsolute( new Path( path ) );
        }
        
        return null;
    }
    
}
