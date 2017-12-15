/******************************************************************************
 * Copyright (c) 2013 Oracle
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
import java.util.Collection;
import java.util.List;
import java.util.Set;

import junit.framework.TestCase;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.modeling.ElementDisposeEvent;
import org.eclipse.sapphire.modeling.ElementValidationEvent;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.PropertyContentEvent;
import org.eclipse.sapphire.modeling.PropertyEnablementEvent;
import org.eclipse.sapphire.modeling.PropertyInitializationEvent;
import org.eclipse.sapphire.modeling.PropertyValidationEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.util.MiscUtil;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireTestCase extends TestCase
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
        assertValidationOk( value.validation() );
    }
    
    protected static final void assertValidationOk( final IModelElement element )
    {
        assertValidationOk( element.validation() );
    }
    
    protected static final void assertValidationOk( final Status status )
    {
        assertEquals( Status.Severity.OK, status.severity() );
    }
    
    protected static final void assertValidationWarning( final Value<?> value,
                                                         final String expectedMessage )
    {
        assertValidationWarning( value.validation(), expectedMessage );
    }

    protected static final void assertValidationWarning( final IModelElement element,
                                                         final String expectedMessage )
    {
        assertValidationWarning( element.validation(), expectedMessage );
    }

    protected static final void assertValidationWarning( final Status status,
                                                         final String expectedMessage )
    {
        assertEquals( Status.Severity.WARNING, status.severity() );
        assertEquals( expectedMessage, status.message() );
    }
    
    protected static final void assertValidationError( final Value<?> value,
                                                       final String expectedMessage )
    {
        assertValidationError( value.validation(), expectedMessage );
    }

    protected static final void assertValidationError( final IModelElement element,
                                                       final String expectedMessage )
    {
        assertValidationError( element.validation(), expectedMessage );
    }

    protected static final void assertValidationError( final Status status,
                                                       final String expectedMessage )
    {
        assertEquals( Status.Severity.ERROR, status.severity() );
        assertEquals( expectedMessage, status.message() );
    }

    protected static void assertElementValidationEvent( final Event event,
                                                        final IModelElement element,
                                                        final Status before,
                                                        final Status after )
    {
        assertInstanceOf( event, ElementValidationEvent.class );
        
        final ElementValidationEvent evt = (ElementValidationEvent) event;
        
        assertSame( element, evt.element() );
        assertEquals( before, evt.before() );
        assertEquals( after, evt.after() );
    }
    
    protected static void assertElementDisposeEvent( final Event event,
                                                     final IModelElement element )
    {
        assertInstanceOf( event, ElementDisposeEvent.class );
        
        final ElementDisposeEvent evt = (ElementDisposeEvent) event;
        
        assertSame( element, evt.element() );
    }

    protected static void assertPropertyInitializationEvent( final Event event,
                                                             final IModelElement element,
                                                             final ModelProperty property )
    {
        assertInstanceOf( event, PropertyInitializationEvent.class );
        
        final PropertyInitializationEvent evt = (PropertyInitializationEvent) event;
        
        assertSame( element, evt.element() );
        assertSame( property, evt.property() );
    }
    
    protected static void assertPropertyContentEvent( final Event event,
                                                      final IModelElement element,
                                                      final ModelProperty property )
    {
        assertInstanceOf( event, PropertyContentEvent.class );
        
        final PropertyContentEvent evt = (PropertyContentEvent) event;
        
        assertSame( element, evt.element() );
        assertSame( property, evt.property() );
    }
    
    protected static void assertPropertyValidationEvent( final Event event,
                                                         final IModelElement element,
                                                         final ModelProperty property,
                                                         final Status before,
                                                         final Status after )
    {
        assertInstanceOf( event, PropertyValidationEvent.class );
        
        final PropertyValidationEvent evt = (PropertyValidationEvent) event;
        
        assertSame( element, evt.element() );
        assertSame( property, evt.property() );
        assertEquals( before, evt.before() );
        assertEquals( after, evt.after() );
    }

    protected static void assertPropertyEnablementEvent( final Event event,
                                                         final IModelElement element,
                                                         final ModelProperty property,
                                                         final boolean before,
                                                         final boolean after )
    {
        assertInstanceOf( event, PropertyEnablementEvent.class );
        
        final PropertyEnablementEvent evt = (PropertyEnablementEvent) event;
        
        assertSame( element, evt.element() );
        assertSame( property, evt.property() );
        assertEquals( before, evt.before() );
        assertEquals( after, evt.after() );
    }
    
    protected static void assertContainsInstanceOf( final Collection<?> collection,
                                                    final Class<?> type )
    {
        boolean found = false;
        
        for( Object obj : collection )
        {
            if( type.isInstance( obj ) )
            {
                found = true;
                break;
            }
        }
        
        if( ! found )
        {
            fail( "Collection does not contain instance of " + type.getName() + " type." );
        }
    }
    
    protected static void assertInstanceOf( final Object object,
                                            final Class<?> type )
    {
        if( ! type.isInstance( object ) )
        {
            fail( "Expected " + type.getSimpleName() + ". Found " + object.getClass().getSimpleName() + "." );
        }
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
