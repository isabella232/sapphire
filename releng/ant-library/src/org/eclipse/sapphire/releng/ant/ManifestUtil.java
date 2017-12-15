/******************************************************************************
 * Copyright (c) 2010 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.releng.ant;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * @author <a href="konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ManifestUtil 
{
    public static final String MANIFEST_FILE_NAME = "MANIFEST.MF";
    public static final String MANIFEST_PATH = "META-INF/" + MANIFEST_FILE_NAME;
    
    public static final String ATTR_CLASSPATH = "Class-Path";
    public static final String ATTR_IMPLEMENTATION_VERSION = "Implementation-Version";
    
    private ManifestUtil() {}
    
    public static File findManifestFile( final File baseDir )
    {
        return new File( baseDir, MANIFEST_PATH );
    }

    public static Map<String,String> readManifest( final File location )
    
        throws IOException
        
    {
        if( location.isFile() )
        {
            final ZipFile zip = ZipUtil.open( location );
            
            try
            {
                return readManifest( zip );
            }
            finally
            {
                try
                {
                    zip.close();
                }
                catch( IOException e ) {}
            }
        }
        else
        {
            final File manifestFile = new File( location, MANIFEST_PATH );
            
            if( manifestFile.exists() )
            {
                final InputStream in = new FileInputStream( manifestFile );
                
                try
                {
                    return readManifest( new BufferedInputStream( in ) );
                }
                finally
                {
                    try
                    {
                        in.close();
                    }
                    catch( IOException e ) {}
                }
            }
            else
            {
                return Collections.emptyMap();
            }
        }
    }
    
    public static String readManifestEntry( final File location,
                                            final String key )
    
        throws IOException
        
    {
        final Map<String,String> manifest = readManifest( location );
        
        if( manifest != null )
        {
            return manifest.get( key );
        }

        return null;
    }

    public static Map<String,String> readManifest( final ZipFile zip )
    
        throws IOException
        
    {
        final ZipEntry zipentry = ZipUtil.getZipEntry( zip, MANIFEST_PATH );
        
        if( zipentry != null )
        {
            final InputStream in = zip.getInputStream( zipentry );
            
            try
            {
                return readManifest( in );
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch( IOException e ) {}
            }
        }
        else
        {
            return Collections.emptyMap();
        }
    }
    
    public static Map<String,String> readManifest( final InputStream stream )
    
        throws IOException
        
    {
        final Map<String,String> entries = new HashMap<String,String>();
        final Manifest manifest = new Manifest();
        
        manifest.read( stream );
        
        for( Map.Entry<Object,Object> entry 
             : manifest.getMainAttributes().entrySet() )
        {
            final Attributes.Name name = (Attributes.Name) entry.getKey();
            entries.put( name.toString(), (String) entry.getValue() );
        }
        
        return entries;
    }
    
    public static List<File> readManifestClasspath( final File archive )
    
        throws IOException
        
    {
        final String manifestClasspathEntry = readManifestEntry( archive, ATTR_CLASSPATH );
        
        if( manifestClasspathEntry == null )
        {
            return Collections.emptyList();
        }
        
        final List<File> manifestClasspath = new ArrayList<File>();
        final File baseDir = archive.getParentFile();
        
        for( String entry : manifestClasspathEntry.split( " " ) )
        {
            final String trimmedEntry = entry.trim();
            
            if( trimmedEntry.length() > 0 )
            {
                final File f = new File( baseDir, trimmedEntry );
                manifestClasspath.add( f );
            }
        }
        
        return manifestClasspath;
    }

    public static void setManifestEntry( final File manifestFile,
                                         final String entryKey,
                                         final String entryValue )
    
        throws IOException
        
    {
        setManifestEntries( manifestFile, Collections.singletonMap( entryKey, entryValue ) );
    }
    
    public static void setManifestEntries( final File manifestFile,
                                           final Map<String,String> entries )
    
        throws IOException
        
    {
        final Manifest manifest = new Manifest();
        
        if( manifestFile.exists() )
        {
            final InputStream in = new FileInputStream( manifestFile );
            
            try
            {
                manifest.read( new BufferedInputStream( in ) );
            }
            finally
            {
                try
                {
                    in.close();
                }
                catch( IOException e ) {}
            }
        }
        
        final Attributes mainAttributes = manifest.getMainAttributes();
        
        for( Map.Entry<String,String> entry : entries.entrySet() )
        {
            mainAttributes.putValue( entry.getKey(), entry.getValue() );
        }
        
        final OutputStream out = new FileOutputStream( manifestFile );
        
        try
        {
            final BufferedOutputStream bout = new BufferedOutputStream( out );
            manifest.write( bout );
            bout.flush();
        }
        finally
        {
            try
            {
                out.close();
            }
            catch( IOException e ) {}
        }
    }
    
}
