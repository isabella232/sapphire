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

package org.eclipse.sapphire.tests.unique;

import org.eclipse.sapphire.Counter;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.internal.UniqueValueValidationService;
import org.eclipse.sapphire.modeling.ByteArrayResourceStore;
import org.eclipse.sapphire.modeling.xml.RootXmlResource;
import org.eclipse.sapphire.modeling.xml.XmlResourceStore;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Tests the unique value feature.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class UniqueValueTests extends SapphireTestCase
{
    @Test
    
    public void testUniqueValue() throws Exception
    {
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore( generateTestData( 10 ) );
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );
        final TestElement element = TestElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        final ElementList<TestElement.ListEntry> list = element.getList(); 
        
        assertValidationOk( element );
        
        list.get( 0 ).setValue( "9" );
        assertValidationError( list.get( 0 ), "Multiple occurrence of \"9\" were found" );
        assertValidationError( list.get( 9 ), "Multiple occurrence of \"9\" were found" );
        assertValidationOk( list.get( 1 ) );
        
        list.insert();
        list.insert();

        assertValidationError( list.get( 0 ), "Multiple occurrence of \"9\" were found" );
        assertValidationError( list.get( 9 ), "Multiple occurrence of \"9\" were found" );
        assertValidationOk( list.get( 1 ) );
        assertValidationOk( list.get( 10 ) );
        assertValidationOk( list.get( 11 ) );
        
        list.get( 0 ).setValue( "0" );
        
        assertValidationOk( element );
    }

    @Test
    
    public void testUniqueValue_ValidateNull() throws Exception
    {
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore( generateTestData( 10 ) );
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );
        final TestElementValidateNull element = TestElementValidateNull.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        final ElementList<TestElementValidateNull.ListEntry> list = element.getList(); 
        
        assertValidationOk( element );
        
        list.get( 0 ).setValue( "9" );
        assertValidationError( list.get( 0 ), "Multiple occurrence of \"9\" were found" );
        assertValidationError( list.get( 9 ), "Multiple occurrence of \"9\" were found" );
        assertValidationOk( list.get( 1 ) );
        
        list.insert();
        list.insert();

        assertValidationError( list.get( 0 ), "Multiple occurrence of \"9\" were found" );
        assertValidationError( list.get( 9 ), "Multiple occurrence of \"9\" were found" );
        assertValidationOk( list.get( 1 ) );
        assertValidationError( list.get( 10 ), "Multiple occurrence of a missing value were found" );
        assertValidationError( list.get( 11 ), "Multiple occurrence of a missing value were found" );
        
        list.get( 0 ).setValue( "0" );
        list.remove( list.get( 10 ) );
        list.remove( list.get( 10 ) );
        
        assertValidationOk( element );
    }

    @Test
    
    public void testUniqueValueCustomCollation() throws Exception
    {
        final TestElementCustomCollation element = TestElementCustomCollation.TYPE.instantiate();
        final ElementList<TestElementCustomCollation.ListEntry> list = element.getList();
        
        assertValidationOk( element );
        
        list.insert().setValue( "a" );
        list.insert().setValue( "b" );
        list.insert().setValue( "c" );
        
        assertValidationOk( element );
        
        list.insert().setValue( "A" );
        
        assertValidationOk( element );
        
        element.setIgnoreCaseDifferences( true );
        
        assertValidationError( element.getList().get( 0 ), "Multiple occurrence of \"a\" were found" );
        assertValidationError( element.getList().get( 3 ), "Multiple occurrence of \"A\" were found" );
        
        element.setIgnoreCaseDifferences( false );
        
        assertValidationOk( element );
    }

    @Test
    
    public void testUniqueValuePerformance10() throws Exception
    {
        testUniqueValuePerformance( 10 );
    }

    @Test
    
    public void testUniqueValuePerformance100() throws Exception
    {
        testUniqueValuePerformance( 100 );
    }

    @Test
    
    public void testUniqueValuePerformance1000() throws Exception
    {
        testUniqueValuePerformance( 1000 );
    }
    
    @Test
    
    public void testUniqueValuePerformance10000() throws Exception
    {
        testUniqueValuePerformance( 10000 );
    }
    
    @Test
    @Ignore // Issues with exceeding GC overhead limit on some configurations.
    
    public void testUniqueValuePerformance100000() throws Exception
    {
        testUniqueValuePerformance( 100000 );
    }
    
    private void testUniqueValuePerformance( final int entries ) throws Exception
    {
        final Counter counter = Counter.find( UniqueValueValidationService.class );
        
        final ByteArrayResourceStore byteArrayResourceStore = new ByteArrayResourceStore( generateTestData( entries ) );
        final XmlResourceStore xmlResourceStore = new XmlResourceStore( byteArrayResourceStore );
        
        counter.reset();
        
        final TestElement element = TestElement.TYPE.instantiate( new RootXmlResource( xmlResourceStore ) );
        element.validation();
        assertEquals( entries, counter.read() );
        
        counter.reset();
        
        element.getList().get( 0 ).setValue( "a" );
        element.validation();
        assertEquals( entries + 1, counter.read() );
        
        counter.reset();
        
        element.getList().get( 0 ).setValue( "9" );
        element.validation();
        assertEquals( entries + 1, counter.read() );
    }
    
    private String generateTestData( final int entries )
    {
        final StringBuilder content = new StringBuilder();
        
        content.append( "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" );
        content.append( "<root>" );
        
        for( int i = 0; i < entries; i++ )
        {
            content.append( "<entry>" );
            content.append( i );
            content.append( "</entry>" );
        }
        
        content.append( "</root>" );
        
        return content.toString();
    }

}
