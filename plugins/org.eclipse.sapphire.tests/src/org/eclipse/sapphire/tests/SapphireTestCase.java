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

package org.eclipse.sapphire.tests;

import static org.eclipse.sapphire.util.StringUtil.UTF8;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedSet;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Property;
import org.eclipse.sapphire.PropertyContentEvent;
import org.eclipse.sapphire.PropertyDef;
import org.eclipse.sapphire.PropertyEnablementEvent;
import org.eclipse.sapphire.PropertyValidationEvent;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValuePropertyContentEvent;
import org.eclipse.sapphire.modeling.ElementDisposeEvent;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.util.MiscUtil;
import org.eclipse.sapphire.services.FactsAggregationService;
import org.junit.After;
import org.junit.Assert;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SapphireTestCase extends Assert
{
    private IProject project;
    
    protected final IProject project() throws Exception
    {
        if( this.project == null )
        {
            this.project = ResourcesPlugin.getWorkspace().getRoot().getProject( "test" );
            
            if( this.project.exists() )
            {
                this.project.delete( true, null );
            }
            
            this.project.create( null );
            this.project.open( null );
        }
        
        return this.project;
    }
    
    @After
    
    public void deleteProject() throws Exception
    {
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
            final BufferedReader r = new BufferedReader( new InputStreamReader( in, UTF8 ) );
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
    
    protected static final void assertValidationOk( final ElementHandle<?> handle )
    {
        assertValidationOk( handle.validation() );
    }
    
    protected static final void assertValidationOk( final ElementList<?> list )
    {
        assertValidationOk( list.validation() );
    }
    
    protected static final void assertValidationOk( final Element element )
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

    protected static final void assertValidationWarning( final ElementHandle<?> handle,
                                                         final String expectedMessage )
    {
        assertValidationWarning( handle.validation(), expectedMessage );
    }

    protected static final void assertValidationWarning( final ElementList<?> list,
                                                         final String expectedMessage )
    {
        assertValidationWarning( list.validation(), expectedMessage );
    }

    protected static final void assertValidationWarning( final Element element,
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

    protected static final void assertValidationError( final ElementHandle<?> handle,
                                                       final String expectedMessage )
    {
        assertValidationError( handle.validation(), expectedMessage );
    }

    protected static final void assertValidationError( final ElementList<?> list,
                                                       final String expectedMessage )
    {
        assertValidationError( list.validation(), expectedMessage );
    }

    protected static final void assertValidationError( final Element element,
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

    protected static void assertElementDisposeEvent( final Event event,
                                                     final Element element )
    {
        assertInstanceOf( event, ElementDisposeEvent.class );
        
        final ElementDisposeEvent evt = (ElementDisposeEvent) event;
        
        assertSame( element, evt.element() );
    }

    protected static void assertPropertyContentEvent( final Event event,
                                                      final Property property )
    {
        assertInstanceOf( event, PropertyContentEvent.class );
        
        final PropertyContentEvent evt = (PropertyContentEvent) event;
        
        assertSame( property, evt.property() );
    }
    
    protected static void assertValuePropertyContentEvent( final Event event, final Property property, final String before, final String after )
    {
        assertInstanceOf( event, ValuePropertyContentEvent.class );
        
        final ValuePropertyContentEvent evt = (ValuePropertyContentEvent) event;
        
        assertSame( property, evt.property() );
        assertEquals( before, evt.before() );
        assertEquals( after, evt.after() );
    }
    
    protected static void assertPropertyValidationEvent( final Event event,
                                                         final Property property,
                                                         final Status before,
                                                         final Status after )
    {
        assertInstanceOf( event, PropertyValidationEvent.class );
        
        final PropertyValidationEvent evt = (PropertyValidationEvent) event;
        
        assertSame( property, evt.property() );
        assertEquals( before, evt.before() );
        assertEquals( after, evt.after() );
    }

    protected static void assertPropertyEnablementEvent( final Event event,
                                                         final Property property,
                                                         final boolean before,
                                                         final boolean after )
    {
        assertInstanceOf( event, PropertyEnablementEvent.class );
        
        final PropertyEnablementEvent evt = (PropertyEnablementEvent) event;
        
        assertSame( property, evt.property() );
        assertEquals( before, evt.before() );
        assertEquals( after, evt.after() );
    }
    
    protected static void assertFact( final Property property,
                                      final String fact )
    {
        final SortedSet<String> facts = property.service( FactsAggregationService.class ).facts();
        assertTrue( facts.contains( fact ) );
    }
    
    protected static void assertFact( final Element element,
                                      final PropertyDef property,
                                      final String fact )
    {
        assertFact( element.property( property ), fact );
    }
    
    protected static void assertNoFact( final Property property,
                                        final String fact )
    {
        final SortedSet<String> facts = property.service( FactsAggregationService.class ).facts();
        assertFalse( facts.contains( fact ) );
    }
    
    protected static void assertNoFact( final Element element,
                                        final PropertyDef property,
                                        final String fact )
    {
        assertNoFact( element.property( property ), fact );
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
    
    protected static void assertInstanceOf( final Object object, final Class<?> type )
    {
        if( type == null )
        {
            throw new IllegalArgumentException();
        }
        
        assertNotNull( object );
        
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
    
    protected static <T> T item( final Collection<T> collection,
                                 final int index )
    {
        if( collection instanceof List<?> )
        {
            return (T) ( (List<T>) collection ).get( index );
        }
        else
        {
            final int size = collection.size();
            
            if( index >= 0 && index < size )
            {
                final Iterator<T> itr = collection.iterator();
                
                T item = null;
                
                for( int i = 0; i <= index; i++ )
                {
                    item = itr.next();
                }
                
                return item;
            }
            
            throw new NoSuchElementException();
        }
    }

}
