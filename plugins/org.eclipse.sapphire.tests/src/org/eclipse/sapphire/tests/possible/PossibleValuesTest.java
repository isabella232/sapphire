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

package org.eclipse.sapphire.tests.possible;

import org.eclipse.sapphire.services.PossibleValuesService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests service lookup while in presence of concurrent model access that utilizes the same service context. Of particular
 * note is the case where service initialization involves model access. 
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class PossibleValuesTest extends SapphireTestCase
{
    @Test
    
    public void testValueWithStaticPossibles()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final PossibleValuesService service = element.getValueWithStaticPossibles().service( PossibleValuesService.class );
            
            assertNotNull( service );
            assertEquals( set( "a", "b", "c" ), service.values() );
            
            assertValidationOk( element.getValueWithStaticPossibles() );
            
            element.setValueWithStaticPossibles( "a" );
            assertValidationOk( element.getValueWithStaticPossibles() );
            
            element.setValueWithStaticPossibles( "d" );
            assertValidationError( element.getValueWithStaticPossibles(), "\"d\" is not among possible values" );
            
            element.setValueWithStaticPossibles( "b" );
            assertValidationOk( element.getValueWithStaticPossibles() );
        }
        finally
        {
            element.dispose();
        }
    }

    @Test
    
    public void testValueWithModelPossibles()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final ListEntry a = element.getEntries().insert();
            a.setValue( "a" );
            
            final ListEntry b = element.getEntries().insert();
            b.setValue( "b" );

            final ListEntry c = element.getEntries().insert();
            c.setValue( "c" );
            
            final PossibleValuesService service = element.getValueWithModelPossibles().service( PossibleValuesService.class );
            
            assertNotNull( service );
            assertEquals( set( "a", "b", "c" ), service.values() );
            
            assertValidationOk( element.getValueWithModelPossibles() );
            
            element.setValueWithModelPossibles( "a" );
            assertValidationOk( element.getValueWithModelPossibles() );
            
            element.setValueWithModelPossibles( "d" );
            assertValidationError( element.getValueWithModelPossibles(), "\"d\" is not among possible values" );
            
            element.setValueWithModelPossibles( "b" );
            assertValidationOk( element.getValueWithModelPossibles() );
            
            b.setValue( "d" );
            assertValidationError( element.getValueWithModelPossibles(), "\"b\" is not among possible values" );
            
            element.getEntries().insert().setValue( "b" );
            assertValidationOk( element.getValueWithModelPossibles() );
            
            assertEquals( set( "a", "b", "c", "d" ), service.values() );
        }
        finally
        {
            element.dispose();
        }
    }

    @Test
    
    public void testListWithStaticPossibles()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final PossibleValuesService service = element.getListWithStaticPossibles().service( PossibleValuesService.class );
            
            assertNotNull( service );
            assertEquals( set( "a", "b", "c" ), service.values() );
            
            final ListEntry entry = element.getListWithStaticPossibles().insert();
            
            assertValidationOk( entry.getValue() );
            
            entry.setValue( "a" );
            assertValidationOk( entry.getValue() );
            
            entry.setValue( "d" );
            assertValidationError( entry.getValue(), "\"d\" is not among possible values" );
            
            entry.setValue( "b" );
            assertValidationOk( entry.getValue() );
        }
        finally
        {
            element.dispose();
        }
    }

    @Test
    
    public void testListWithModelPossibles()
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            final ListEntry a = element.getEntries().insert();
            a.setValue( "a" );
            
            final ListEntry b = element.getEntries().insert();
            b.setValue( "b" );

            final ListEntry c = element.getEntries().insert();
            c.setValue( "c" );
            
            final PossibleValuesService service = element.getListWithModelPossibles().service( PossibleValuesService.class );
            
            assertNotNull( service );
            assertEquals( set( "a", "b", "c" ), service.values() );
            
            final ListEntry entry = element.getListWithModelPossibles().insert();
            
            assertValidationOk( entry.getValue() );
            
            entry.setValue( "a" );
            assertValidationOk( entry.getValue() );
            
            entry.setValue( "d" );
            assertValidationError( entry.getValue(), "\"d\" is not among possible values" );
            
            entry.setValue( "b" );
            assertValidationOk( entry.getValue() );
            
            b.setValue( "d" );
            assertValidationError( entry.getValue(), "\"b\" is not among possible values" );
            
            element.getEntries().insert().setValue( "b" );
            assertValidationOk( entry.getValue() );
            
            assertEquals( set( "a", "b", "c", "d" ), service.values() );
        }
        finally
        {
            element.dispose();
        }
    }

}
