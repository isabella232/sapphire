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

package org.eclipse.sapphire.tests.services.t0013;

import org.eclipse.sapphire.RequiredConstraintService;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests for @Required annotation, RequiredConstraintService, DeclarativeRequiredConstraintService,
 * RequiredConstraintValidationService, and RequiredConstraintFactsService.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class RequiredConstraintTests extends SapphireTestCase
{
    @Test
    
    public void testRequiredConstraintValueProperty() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
     
        try
        {
            assertNotNull( element.property( TestElement.PROP_VALUE ).service( RequiredConstraintService.class ) );
            assertNotNull( element.property( TestElement.PROP_VALUE_REQUIRED ).service( RequiredConstraintService.class ) );
            assertNotNull( element.property( TestElement.PROP_VALUE_REQUIRED_EXPR ).service( RequiredConstraintService.class ) );
            
            assertValidationOk( element.getValue() );
            assertValidationError( element.getValueRequired(), "Value must be specified" );
            assertValidationOk( element.getValueRequiredExpr() );
            
            assertNoFact( element.getValue(), "Must be specified" );
            assertFact( element.getValueRequired(), "Must be specified" );
            assertNoFact( element.getValueRequiredExpr(), "Must be specified" );
            
            element.setRequired( true );
            
            assertValidationOk( element.getValue() );
            assertValidationError( element.getValueRequired(), "Value must be specified" );
            assertValidationError( element.getValueRequiredExpr(), "Value must be specified" );
            
            assertNoFact( element.getValue(), "Must be specified" );
            assertFact( element.getValueRequired(), "Must be specified" );
            assertFact( element.getValueRequiredExpr(), "Must be specified" );
            
            element.setValue( "abc" );
            element.setValueRequired( "abc" );
            element.setValueRequiredExpr( "abc" );
            
            assertValidationOk( element.getValue() );
            assertValidationOk( element.getValueRequired() );
            assertValidationOk( element.getValueRequiredExpr() );
            
            assertNoFact( element.getValue(), "Must be specified" );
            assertFact( element.getValueRequired(), "Must be specified" );
            assertFact( element.getValueRequiredExpr(), "Must be specified" );
        }
        finally
        {
            element.dispose();
        }
    }
    
    @Test
    
    public void testRequiredConstraintElementProperty() throws Exception
    {
        final TestElement element = TestElement.TYPE.instantiate();
        
        try
        {
            assertNotNull( element.property( TestElement.PROP_ELEMENT ).service( RequiredConstraintService.class ) );
            assertNotNull( element.property( TestElement.PROP_ELEMENT_REQUIRED ).service( RequiredConstraintService.class ) );
            assertNotNull( element.property( TestElement.PROP_ELEMENT_REQUIRED_EXPR ).service( RequiredConstraintService.class ) );
            
            assertValidationOk( element.getElement() );
            assertValidationError( element.getElementRequired(), "Element must be specified" );
            assertValidationOk( element.getElementRequiredExpr() );
            
            assertNoFact( element.getElement(), "Must be specified" );
            assertFact( element.getElementRequired(), "Must be specified" );
            assertNoFact( element.getElementRequiredExpr(), "Must be specified" );
            
            element.setRequired( true );
            
            assertValidationOk( element.getElement() );
            assertValidationError( element.getElementRequired(), "Element must be specified" );
            assertValidationError( element.getElementRequiredExpr(), "Element must be specified" );
            
            assertNoFact( element.getElement(), "Must be specified" );
            assertFact( element.getElementRequired(), "Must be specified" );
            assertFact( element.getElementRequiredExpr(), "Must be specified" );
            
            element.getElement().content( true );
            element.getElementRequired().content( true );
            element.getElementRequiredExpr().content( true );
            
            assertValidationOk( element.getElement() );
            assertValidationOk( element.getElementRequired() );
            assertValidationOk( element.getElementRequiredExpr() );
            
            assertNoFact( element.getElement(), "Must be specified" );
            assertFact( element.getElementRequired(), "Must be specified" );
            assertFact( element.getElementRequiredExpr(), "Must be specified" );
        }
        finally
        {
            element.dispose();
        }
    }

}
