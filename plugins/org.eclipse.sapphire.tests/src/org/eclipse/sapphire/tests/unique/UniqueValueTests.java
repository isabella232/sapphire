/******************************************************************************
 * Copyright (c) 2014 Oracle
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
        
        assertValidationOk( element );
        
        element.getList().get( 0 ).setValue( "9" );
        assertValidationError( element.getList().get( 0 ), "Unique value required. Another occurrence of \"9\" was found" );
        assertValidationError( element.getList().get( 9 ), "Unique value required. Another occurrence of \"9\" was found" );
        assertValidationOk( element.getList().get( 1 ) );
        
        element.getList().get( 0 ).setValue( "0" );
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
