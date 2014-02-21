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

package org.eclipse.sapphire.tests.collation;

import java.util.Comparator;

import org.eclipse.sapphire.CollationService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests the unique value feature.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CollationTests extends SapphireTestCase
{
    @Test
    
    public void NoCollationSpecified()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final CollationService serviceFromInstanceContext = element.getNoCollationSpecified().service( CollationService.class );
            
            assertNotNull( serviceFromInstanceContext );
            
            final CollationService serviceFromGlobalContext = TestElement.PROP_NO_COLLATION_SPECIFIED.service( CollationService.class );
            
            assertNotNull( serviceFromGlobalContext );
            assertSame( serviceFromGlobalContext, serviceFromInstanceContext );
            
            final Comparator<String> comparator = serviceFromInstanceContext.comparator();
            
            assertTrue( comparator.compare( "a", "b" ) < 0 );
            assertTrue( comparator.compare( "b", "a" ) > 0 );
            assertTrue( comparator.compare( "A", "a" ) != 0 );
        }
        finally
        {
            element.dispose();
        }
    }

    @Test
    
    public void IgnoreCaseLiteralFalse()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final CollationService serviceFromInstanceContext = element.getIgnoreCaseLiteralFalse().service( CollationService.class );
            
            assertNotNull( serviceFromInstanceContext );
            
            final CollationService serviceFromGlobalContext = TestElement.PROP_IGNORE_CASE_LITERAL_FALSE.service( CollationService.class );
            
            assertNotNull( serviceFromGlobalContext );
            assertSame( serviceFromGlobalContext, serviceFromInstanceContext );
            
            final Comparator<String> comparator = serviceFromInstanceContext.comparator();
            
            assertTrue( comparator.compare( "a", "b" ) < 0 );
            assertTrue( comparator.compare( "b", "a" ) > 0 );
            assertTrue( comparator.compare( "A", "a" ) != 0 );
        }
        finally
        {
            element.dispose();
        }
    }

    @Test
    
    public void IgnoreCaseLiteralTrue()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final CollationService serviceFromInstanceContext = element.getIgnoreCaseLiteralTrue().service( CollationService.class );
            
            assertNotNull( serviceFromInstanceContext );
            
            final CollationService serviceFromGlobalContext = TestElement.PROP_IGNORE_CASE_LITERAL_TRUE.service( CollationService.class );
            
            assertNotNull( serviceFromGlobalContext );
            assertSame( serviceFromGlobalContext, serviceFromInstanceContext );
            
            final Comparator<String> comparator = serviceFromInstanceContext.comparator();
            
            assertTrue( comparator.compare( "a", "b" ) < 0 );
            assertTrue( comparator.compare( "b", "a" ) > 0 );
            assertTrue( comparator.compare( "A", "a" ) == 0 );
        }
        finally
        {
            element.dispose();
        }
    }

    @Test
    
    public void IgnoreCaseDynamic()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final CollationService serviceFromInstanceContext = element.getIgnoreCaseDynamic().service( CollationService.class );
            
            assertNotNull( serviceFromInstanceContext );
            
            final CollationService serviceFromGlobalContext = TestElement.PROP_IGNORE_CASE_DYNAMIC.service( CollationService.class );
            
            assertNotNull( serviceFromGlobalContext );
            assertNotSame( serviceFromGlobalContext, serviceFromInstanceContext );
            
            final Comparator<String> comparator1 = serviceFromInstanceContext.comparator();
            
            assertTrue( comparator1.compare( "a", "b" ) < 0 );
            assertTrue( comparator1.compare( "b", "a" ) > 0 );
            assertTrue( comparator1.compare( "A", "a" ) != 0 );
            
            element.setIgnoreCase( true );
            
            final Comparator<String> comparator2 = serviceFromInstanceContext.comparator();
            
            assertNotSame( comparator2, comparator1 );
            assertTrue( comparator2.compare( "a", "b" ) < 0 );
            assertTrue( comparator2.compare( "b", "a" ) > 0 );
            assertTrue( comparator2.compare( "A", "a" ) == 0 );
        }
        finally
        {
            element.dispose();
        }
    }

    @Test
    
    public void IgnoreCaseDynamicGlobal()
    {
        Sapphire.global().put( "TestIgnoreCase", false );
        
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final CollationService serviceFromInstanceContext = element.getIgnoreCaseDynamicGlobal().service( CollationService.class );
            
            assertNotNull( serviceFromInstanceContext );
            
            final CollationService serviceFromGlobalContext = TestElement.PROP_IGNORE_CASE_DYNAMIC_GLOBAL.service( CollationService.class );
            
            assertNotNull( serviceFromGlobalContext );
            assertSame( serviceFromGlobalContext, serviceFromInstanceContext );
            
            final Comparator<String> comparator1 = serviceFromInstanceContext.comparator();
            
            assertTrue( comparator1.compare( "a", "b" ) < 0 );
            assertTrue( comparator1.compare( "b", "a" ) > 0 );
            assertTrue( comparator1.compare( "A", "a" ) != 0 );
            
            Sapphire.global().put( "TestIgnoreCase", true );
            
            final Comparator<String> comparator2 = serviceFromInstanceContext.comparator();
            
            assertNotSame( comparator2, comparator1 );
            assertTrue( comparator2.compare( "a", "b" ) < 0 );
            assertTrue( comparator2.compare( "b", "a" ) > 0 );
            assertTrue( comparator2.compare( "A", "a" ) == 0 );
        }
        finally
        {
            element.dispose();
        }
    }

}
