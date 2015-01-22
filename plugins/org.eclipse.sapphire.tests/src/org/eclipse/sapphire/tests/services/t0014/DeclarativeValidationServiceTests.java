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

package org.eclipse.sapphire.tests.services.t0014;

import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests for @Validation, @Validations and DeclarativeValidationService.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class DeclarativeValidationServiceTests extends SapphireTestCase
{
    @Test
    
    public void testDeclarativeValidationService() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
     
        try
        {
            assertValidationOk( element.getMin() );
            assertValidationOk( element.getMax() );
            
            element.setMin( 30 );
            element.setMax( 20 );
            
            assertValidationError( element.getMin(), "Must not be larger than max" );
            assertValidationError( element.getMax(), "Must not be smaller than min" );
            
            element.setMax( 200 );
            
            assertValidationOk( element.getMin() );
            assertValidationWarning( element.getMax(), "Must be less than or equal to 100" );
            
            element.setMax( 50 );
            
            assertValidationOk( element.getMin() );
            assertValidationOk( element.getMax() );
        }
        finally
        {
            element.dispose();
        }
    }
    
    @Test
    
    public void testDeclarativeValidationService_MessageFunction() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            assertValidationOk( element.getPath() );
            
            element.setPath( "abc" );
            
            assertValidationError( element.getPath(), "Path \"abc\" must start with a slash" );
            
            element.setPath( "/abc" );
            
            assertValidationOk( element.getPath() );
        }
        finally
        {
            element.dispose();
        }
    }

}
