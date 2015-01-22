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

package org.eclipse.sapphire.tests.index;

import java.util.Set;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.Index;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.tests.EventLog;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.eclipse.sapphire.tests.index.TestElement.ListEntry;
import org.eclipse.sapphire.tests.index.TestElement.ListEntry.NestedListEntry;
import org.eclipse.sapphire.util.Comparators;
import org.junit.Test;

/**
 * Tests the index feature.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class IndexTests extends SapphireTestCase
{
    @Test
    
    public void testSingleIndex()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final ElementList<ListEntry> list = element.getList();
            
            for( int i = 0; i < 100; i++ )
            {
                list.insert().setStringValue( String.valueOf( i ) );
            }
            
            final Index<ListEntry> index = list.index( ListEntry.PROP_STRING_VALUE );
            
            assertSame( list, index.list() );
            assertSame( ListEntry.PROP_STRING_VALUE, index.property() );
            assertSame( index, list.index( ListEntry.PROP_STRING_VALUE ) );
            
            testIndexLookup( index, "20", 1 );
            testIndexLookup( index, "97", 1 );
            testIndexLookup( index, "137", 0 );
            testIndexLookup( index, null, 0 );

            for( int i = 100; i < 200; i++ )
            {
                list.insert().setStringValue( String.valueOf( i ) );
            }
            
            list.insert();
            
            testIndexLookup( index, "137", 1 );
            testIndexLookup( index, null, 1 );

            for( int i = 100; i < 200; i++ )
            {
                list.insert().setStringValue( String.valueOf( i ) );
            }
            
            list.insert();
            
            testIndexLookup( index, "20", 1 );
            testIndexLookup( index, "97", 1 );
            testIndexLookup( index, "137", 2 );
            testIndexLookup( index, "182", 2 );
            testIndexLookup( index, "213", 0 );
            testIndexLookup( index, null, 2 );
        }
        finally
        {
            element.dispose();
        }
    }
    
    @Test
    
    public void testMultipleIndexes()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final ElementList<ListEntry> list = element.getList();
            
            for( int i = 0; i < 100; i++ )
            {
                final ListEntry entry = list.insert();
                
                entry.setStringValue( "s" + String.valueOf( i ) );
                entry.setIntegerValue( i );
            }
            
            final Index<ListEntry> stringValueIndex = list.index( ListEntry.PROP_STRING_VALUE );
            
            assertSame( list, stringValueIndex.list() );
            assertSame( ListEntry.PROP_STRING_VALUE, stringValueIndex.property() );
            assertSame( stringValueIndex, list.index( ListEntry.PROP_STRING_VALUE ) );

            final Index<ListEntry> integerValueIndex = list.index( ListEntry.PROP_INTEGER_VALUE );
            
            assertSame( list, integerValueIndex.list() );
            assertSame( ListEntry.PROP_INTEGER_VALUE, integerValueIndex.property() );
            assertSame( integerValueIndex, list.index( ListEntry.PROP_INTEGER_VALUE ) );
            
            assertSame( stringValueIndex.element( "s97" ), integerValueIndex.element( "97" ) );

            for( int i = 100; i < 200; i++ )
            {
                list.insert().setStringValue( String.valueOf( i ) );
            }
            
            assertSame( stringValueIndex.element( "s137" ), integerValueIndex.element( "137" ) );
        }
        finally
        {
            element.dispose();
        }
    }
    
    @Test
    
    public void testIndexCaseSensitivity()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final ElementList<ListEntry> list = element.getList();
            
            for( int i = 0; i < 100; i++ )
            {
                list.insert().setStringValue( "abc" + String.valueOf( i ) );
            }
            
            final Index<ListEntry> respectCaseIndex = list.index( ListEntry.PROP_STRING_VALUE );
            
            assertSame( respectCaseIndex, list.index( ListEntry.PROP_STRING_VALUE ) );
            
            final Index<ListEntry> ignoreCaseIndex = list.index( ListEntry.PROP_STRING_VALUE, Comparators.createIgnoreCaseComparator() );
            
            assertSame( ignoreCaseIndex, list.index( ListEntry.PROP_STRING_VALUE, Comparators.createIgnoreCaseComparator() ) );
            assertNotSame( ignoreCaseIndex, respectCaseIndex );
            
            assertEquals( 1, respectCaseIndex.elements( "abc97" ).size() );
            assertEquals( 0, respectCaseIndex.elements( "AbC97" ).size() );
            
            assertEquals( 1, ignoreCaseIndex.elements( "abc97" ).size() );
            assertEquals( 1, ignoreCaseIndex.elements( "AbC97" ).size() );
            assertEquals( ignoreCaseIndex.elements( "abc97" ), ignoreCaseIndex.elements( "AbC97" ) );

            for( int i = 0; i < 100; i++ )
            {
                list.insert().setStringValue( "AbC" + String.valueOf( i ) );
            }
            
            assertEquals( 1, respectCaseIndex.elements( "abc97" ).size() );
            assertEquals( 1, respectCaseIndex.elements( "AbC97" ).size() );
            assertEquals( 0, respectCaseIndex.elements( "ABC97" ).size() );
            assertFalse( respectCaseIndex.elements( "abc97" ).equals( respectCaseIndex.elements( "AbC97" ) ) );
            
            assertEquals( 2, ignoreCaseIndex.elements( "abc97" ).size() );
            assertEquals( 2, ignoreCaseIndex.elements( "AbC97" ).size() );
            assertEquals( 2, ignoreCaseIndex.elements( "ABC97" ).size() );
            assertEquals( ignoreCaseIndex.elements( "abc97" ), ignoreCaseIndex.elements( "AbC97" ) );
            assertEquals( ignoreCaseIndex.elements( "abc97" ), ignoreCaseIndex.elements( "ABC97" ) );
        }
        finally
        {
            element.dispose();
        }
    }
    
    @Test
    
    public void testIndexEvents()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final ElementList<ListEntry> list = element.getList();
            
            for( int i = 0; i < 100; i++ )
            {
                final ListEntry entry = list.insert();
                
                entry.setStringValue( String.valueOf( i ) );
                entry.setIntegerValue( i );
            }
            
            final Index<ListEntry> index = list.index( ListEntry.PROP_STRING_VALUE );
            final EventLog log = new EventLog();
            
            index.attach( log );
            
            // Initialize the index by accessing it.
            
            index.element( "1" );
            
            assertEquals( 0, log.size() );
            
            // Remove item from the list.
            
            list.remove( 0 );
            
            assertEquals( 1, log.size() );
            log.clear();
            
            // Change an indexed value.
            
            list.get( 10 ).setStringValue( "aa" );
            
            assertEquals( 1, log.size() );
            log.clear();
            
            // Change an unrelated value.
            
            list.get( 10 ).setIntegerValue( 9999 );
            
            assertEquals( 0, log.size() );
            
            // Move list items.
            
            list.moveUp( list.get( 10 ) );
            
            assertEquals( 0, log.size() );
            
            list.moveDown( list.get( 20 ) );
            
            assertEquals( 0, log.size() );
            
            // Detach the listener.
            
            index.detach( log );
            list.remove( 0 );
            
            assertEquals( 0, log.size() );
        }
        finally
        {
            element.dispose();
        }
    }
    
    /**
     * Test index behavior with list elements that have a custom EqualityService.
     */
    
    @Test
    
    public void testIndexWithEqualityService()
    {
        final TestElementWithEqualityService element = TestElementWithEqualityService.TYPE.instantiate();
        
        try
        {
            final ElementList<TestElementWithEqualityService.ListEntry> list = element.getList();
            
            list.insert().setValue( "a" );
            list.insert().setValue( "b" );
            list.insert().setValue( "a" );
            
            final Index<TestElementWithEqualityService.ListEntry> index = list.index( TestElementWithEqualityService.ListEntry.PROP_VALUE );
            
            assertEquals( 2, index.elements( "a" ).size() );
            assertEquals( 1, index.elements( "b" ).size() );
            
            list.insert().setValue( "b" );
            
            assertEquals( 2, index.elements( "b" ).size() );
        }
        finally
        {
            element.dispose();
        }
    }

    /**
     * Test lookup of the index when the property is inherited into list entry type.
     */
    
    @Test
    
    public void testIndexWithInheritedProperty()
    {
        final TestElementWithInheritedProperty element = TestElementWithInheritedProperty.TYPE.instantiate();
        
        try
        {
            final ElementList<TestElementWithInheritedProperty.ListEntry> list = element.getList();
            
            list.insert().setValue( "a" );
            list.insert().setValue( "b" );
            
            final Index<TestElementWithInheritedProperty.ListEntry> index = list.index( TestElementWithInheritedProperty.ListEntry.PROP_VALUE );
            
            assertNotNull( index.element( "a" ) );
            assertNotNull( index.element( "b" ) );
        }
        finally
        {
            element.dispose();
        }
    }

    /**
     * Test {@link ElementList#index(ValueProperty)} with a null.
     */
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testException_ElementList_Index_ValueProperty_Null()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            element.getList().index( (ValueProperty) null );
        }
        finally
        {
            element.dispose();
        }
    }

    /**
     * Test {@link ElementList#index(ValueProperty)} with a foreign property.
     */
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testException_ElementList_Index_ValueProperty_Foreign_1()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            element.getList().index( TestElement.PROP_VALUE );
        }
        finally
        {
            element.dispose();
        }
    }
    
    /**
     * Test {@link ElementList#index(ValueProperty)} with a foreign property.
     */
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testException_ElementList_Index_ValueProperty_Foreign_2()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            element.getList().index( NestedListEntry.PROP_VALUE );
        }
        finally
        {
            element.dispose();
        }
    }
    
    /**
     * Test {@link ElementList#index(ValueProperty)} when the list is disposed.
     */
    
    @Test( expected = IllegalStateException.class )
    
    public void testException_ElementList_Index_ValueProperty_Disposed()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final ElementList<ListEntry> list;
        
        try
        {
            list = element.getList();
        }
        finally
        {
            element.dispose();
        }

        list.index( ListEntry.PROP_STRING_VALUE );
    }
    
    /**
     * Test {@link ElementList#index(String)} with a null.
     */
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testException_ElementList_Index_String_Null()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            element.getList().index( (String) null );
        }
        finally
        {
            element.dispose();
        }
    }
    
    /**
     * Test {@link ElementList#index(String)} with an unknown property.
     */
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testException_ElementList_Index_String_Unknown()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            element.getList().index( "Value" );
        }
        finally
        {
            element.dispose();
        }
    }

    /**
     * Test {@link ElementList#index(String)} with a path.
     */
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testException_ElementList_Index_String_Path()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            element.getList().index( "List/Value" );
        }
        finally
        {
            element.dispose();
        }
    }
    
    /**
     * Test {@link ElementList#index(String)} with a list property.
     */
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testException_ElementList_Index_String_List()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            element.getList().index( "List" );
        }
        finally
        {
            element.dispose();
        }
    }
    
    /**
     * Test {@link ElementList#index(String)} when the list is disposed.
     */
    
    @Test( expected = IllegalStateException.class )
    
    public void testException_ElementList_Index_String_Disposed()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final ElementList<ListEntry> list;
        
        try
        {
            list = element.getList();
        }
        finally
        {
            element.dispose();
        }

        list.index( ListEntry.PROP_STRING_VALUE );
    }
    
    /**
     * Test {@link Index#element(String)} when the list is disposed.
     */
    
    @Test( expected = IllegalStateException.class )
    
    public void testException_Index_Element_Disposed()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final Index<ListEntry> index;
        
        try
        {
            index = element.getList().index( ListEntry.PROP_STRING_VALUE );
        }
        finally
        {
            element.dispose();
        }

        index.element( "a" );
    }
    
    /**
     * Test {@link Index#elements(String)} when the list is disposed.
     */
    
    @Test( expected = IllegalStateException.class )
    
    public void testException_Index_Elements_Disposed()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final Index<ListEntry> index;
        
        try
        {
            index = element.getList().index( ListEntry.PROP_STRING_VALUE );
        }
        finally
        {
            element.dispose();
        }

        index.elements( "a" );
    }
    
    /**
     * Test {@link Index#attach(org.eclipse.sapphire.Listener)} with a null.
     */
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testException_Index_Attach_Null()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            element.getList().index( ListEntry.PROP_STRING_VALUE ).attach( null );
        }
        finally
        {
            element.dispose();
        }
    }
    
    /**
     * Test {@link Index#attach(org.eclipse.sapphire.Listener)} when the list is disposed.
     */
    
    @Test( expected = IllegalStateException.class )
    
    public void testException_Index_Attach_Disposed()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        final Index<ListEntry> index;
        
        try
        {
            index = element.getList().index( ListEntry.PROP_STRING_VALUE );
        }
        finally
        {
            element.dispose();
        }

        index.attach( new EventLog() );
    }
    
    /**
     * Test {@link Index#detach(org.eclipse.sapphire.Listener)} with a null.
     */
    
    @Test( expected = IllegalArgumentException.class )
    
    public void testException_Index_Detach_Null()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            element.getList().index( ListEntry.PROP_STRING_VALUE ).detach( null );
        }
        finally
        {
            element.dispose();
        }
    }
    
    private void testIndexLookup( final Index<ListEntry> index, final String key, final int count )
    {
        if( count == 0 )
        {
            assertNull( index.element( key ) );
            
            final Set<ListEntry> elements = index.elements( key );
            assertNotNull( elements );
            assertEquals( 0, elements.size() );
        }
        else
        {
            final ListEntry element = index.element( key );
            assertNotNull( element );
            assertEquals( key, element.property( index.property() ).text() );

            final Set<ListEntry> elements = index.elements( key );
            assertNotNull( elements );
            assertEquals( count, elements.size() );
            
            for( final ListEntry entry : elements )
            {
                assertEquals( key, entry.property( index.property() ).text() );
            }
        }
    }

}
