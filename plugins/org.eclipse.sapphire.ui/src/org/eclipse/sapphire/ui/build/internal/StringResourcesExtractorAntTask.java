/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.build.internal;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.HashSet;
import java.util.Set;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StringResourcesExtractorAntTask

	extends Task
	
{
	private File src = null;
	private File dest = null;
	
	public void setSrc( final File src )
	{
		this.src = src;
	}
	
	public void setDest( final File dest )
	{
		this.dest = dest;
	}
	
	public void execute()
	{
		if( this.src == null )
		{
			throw new BuildException( "Attribute src not set!" );
		}
		
		if( this.dest == null )
		{
			throw new BuildException( "Attribute dest not set!" );
		}
		
		final Set<File> files = new HashSet<File>();
		gatherSourceFiles( this.src, files );

        for( File sourceFile : files )
        {
            try
            {
            	final Reader reader = new FileReader( sourceFile );
            	final String resourceFileContent;
            	
            	try
            	{
            		resourceFileContent = StringResourcesExtractor.extract( reader );
            	}
            	finally
            	{
            		try
            		{
            			reader.close();
            		}
            		catch( IOException e ) {}
            	}
            	
            	if( resourceFileContent != null )
            	{
	                File resourceFile = new File( this.dest, getRelativePath( sourceFile.getParentFile(), this.src ) );
	                resourceFile = new File( resourceFile, getNameWithoutExtension( sourceFile ) + ".properties" );
	                
	                resourceFile.getParentFile().mkdirs();
	                
	                final OutputStream out = new FileOutputStream( resourceFile );
	                
	                try
	                {
	                	final Writer writer = new OutputStreamWriter( out );
	                	writer.write( resourceFileContent );
	                	writer.flush();
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
            catch( Exception e )
            {
            	throw new BuildException( e );
            }
        }
	}
	
    private static void gatherSourceFiles( final File directory,
                                           final Set<File> files )
    {
        for( File child : directory.listFiles() )
        {
            if( child.isDirectory() )
            {
                gatherSourceFiles( child, files );
            }
            else if( child.getName().toLowerCase().endsWith( ".sdef" ))
            {
                files.add( child );
            }
        }
    }
    
    private static String getRelativePath( final File path,
                                           final File base )
    {
        final String[] splitPath
            = path.getAbsolutePath().split( "[/\\\\]" );
        
        final String[] splitBase
            = base.getAbsolutePath().split( "[/\\\\]" );
        
        int i;
        
        for( i = 0; i < splitPath.length && i < splitBase.length && 
             splitPath[ i ].equals( splitBase[ i ] ); i++ ) {} 
        
        if( i != splitBase.length )
        {
            throw new IllegalArgumentException( "path not beneath base" );
        }
        
        final StringBuilder buf = new StringBuilder();
        
        for( ; i < splitPath.length; i++ )
        {
            if( buf.length() > 0 ) buf.append( '/' );
            buf.append( splitPath[ i ] );
        }
        
        return buf.toString();
    }

    private static String getNameWithoutExtension( final File f )
    {
        final String fname = f.getName();
        final int lastdot = fname.lastIndexOf( '.' );
        
        if( lastdot == -1 )
        {
            return fname;
        }
        else
        {
            return fname.substring( 0, lastdot );
        }
    }
    
}
