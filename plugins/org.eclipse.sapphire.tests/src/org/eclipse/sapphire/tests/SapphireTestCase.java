/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.util.MiscUtil;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireTestCase

    extends TestCase
    
{
    private IProject project;
    
    protected SapphireTestCase( final String name )
    {
        super( name );
    }
    
    protected final IProject project() throws Exception
    {
        if( this.project == null )
        {
            final String name = getClass().getName() + "." + getName();
            this.project = ResourcesPlugin.getWorkspace().getRoot().getProject( name );
            this.project.create( null );
            this.project.open( null );
        }
        
        return this.project;
    }
    
    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
        
        if( this.project != null )
        {
            this.project.delete( true, null );
        }
    }
    
    protected final InputStream loadResourceAsStream( final String name )
    {
        final InputStream in = getClass().getResourceAsStream( name );
        
        if( in == null )
        {
            throw new IllegalArgumentException( name );
        }
        
        return in;
    }
    
    protected final String loadResource( final String name )
    
        throws Exception
        
    {
        final InputStream in = loadResourceAsStream( name );
        
        try
        {
            final BufferedReader r = new BufferedReader( new InputStreamReader( in ) );
            final char[] chars = new char[ 1024 ];
            final StringBuilder buf = new StringBuilder();
            
            for( int i = r.read( chars ); i != -1; i = r.read( chars ) )
            {
                buf.append( chars, 0, i );
            }
            
            return buf.toString();
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
    
    protected static final void assertEqualsIgnoreNewLineDiffs( final String expected, 
                                                                final String actual ) 
    {
        assertEquals( expected.trim().replace( "\r", "" ), actual.trim().replace( "\r", "" ) );
    }
    
    protected static final void assertValidationOk( final Value<?> value )
    {
        assertEquals( Status.Severity.OK, value.validate().severity() );
    }
    
    protected static final void assertValidationError( final Value<?> value,
                                                       final String expectedMessage )
    {
        final Status st = value.validate();
        
        assertEquals( Status.Severity.ERROR, st.severity() );
        assertEquals( expectedMessage, st.message() );
    }
    
    protected static <T> List<T> list( final T... items )
    {
        return MiscUtil.list( items );
    }
    
    protected static <T> Set<T> set( final T... items )
    {
        return MiscUtil.set( items );
    }

}
