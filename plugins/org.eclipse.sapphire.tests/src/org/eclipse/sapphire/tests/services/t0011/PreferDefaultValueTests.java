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

package org.eclipse.sapphire.tests.services.t0011;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.eclipse.sapphire.services.FactsAggregationService;
import org.eclipse.sapphire.tests.SapphireTestCase;

/**
 * Tests @PreferDefaultValue feature.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class PreferDefaultValueTests extends SapphireTestCase
{
    private PreferDefaultValueTests( final String name )
    {
        super( name );
    }
    
    public static Test suite()
    {
        final TestSuite suite = new TestSuite();
        
        suite.setName( "PreferDefaultValueTests" );

        suite.addTest( new PreferDefaultValueTests( "testPreferDefaultValueValidation" ) );
        suite.addTest( new PreferDefaultValueTests( "testPreferDefaultValueFacts" ) );
        
        return suite;
    }
    
    public void testPreferDefaultValueValidation() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        assertValidationOk( element.getValue() );
        
        element.setValue( "abc" );
        assertValidationWarning( element.getValue(), "Value should be \"123\"." );
        
        element.setValue( null );
        assertValidationOk( element.getValue() );
    }

    public void testPreferDefaultValueFacts() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        assertEquals
        (
            set( "Default value is \"123\".", "Recommended value is \"123\"." ),
            element.service( TestElement.PROP_VALUE, FactsAggregationService.class ).facts()
        );        
    }

}
