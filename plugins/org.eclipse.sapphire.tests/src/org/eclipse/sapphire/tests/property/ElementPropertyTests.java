/******************************************************************************
 * Copyright (c) 2016 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.property;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.PossibleTypesService;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests element properties.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ElementPropertyTests extends SapphireTestCase
{
    public interface OrderedPossibleTypesTestElement extends Element
    {
        ElementType TYPE = new ElementType( OrderedPossibleTypesTestElement.class );
        
        interface Child extends Element
        {
            ElementType TYPE = new ElementType( Child.class );
            
            // *** Value ***
            
            ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
            
            Value<String> getValue();
            void setValue( String value );
        }
        
        interface Child1 extends Child
        {
            ElementType TYPE = new ElementType( Child1.class );
        }
        
        interface Child2 extends Child
        {
            ElementType TYPE = new ElementType( Child2.class );
        }
        
        interface Child3 extends Child
        {
            ElementType TYPE = new ElementType( Child3.class );
        }
        
        // *** ElementWithUnorderedPossibleTypes ***
        
        @Type( base = Child.class, possible = { Child2.class, Child1.class, Child3.class } )
        
        ElementProperty PROP_ELEMENT_WITH_UNORDERED_POSSIBLE_TYPES = new ElementProperty( TYPE, "ElementWithUnorderedPossibleTypes" );
        
        ElementHandle<Child> getElementWithUnorderedPossibleTypes();

        // *** ElementWithOrderedPossibleTypes ***
        
        @Type( base = Child.class, possible = { Child2.class, Child1.class, Child3.class }, ordered = true )
        
        ElementProperty PROP_ELEMENT_WITH_ORDERED_POSSIBLE_TYPES = new ElementProperty( TYPE, "ElementWithOrderedPossibleTypes" );
        
        ElementHandle<Child> getElementWithOrderedPossibleTypes();
    }
    
    @Test
    
    public void OrderedPossibleTypes() throws Exception
    {
        try( OrderedPossibleTypesTestElement a = OrderedPossibleTypesTestElement.TYPE.instantiate() )
        {
            final PossibleTypesService unorderedPossibleTypesService = a.getElementWithUnorderedPossibleTypes().service( PossibleTypesService.class );
            
            assertEquals( false, unorderedPossibleTypesService.ordered() );
            assertSetOrder( unorderedPossibleTypesService.types(), OrderedPossibleTypesTestElement.Child1.TYPE, OrderedPossibleTypesTestElement.Child2.TYPE, OrderedPossibleTypesTestElement.Child3.TYPE );
            
            final PossibleTypesService orderedPossibleTypesService = a.getElementWithOrderedPossibleTypes().service( PossibleTypesService.class );
            
            assertEquals( true, orderedPossibleTypesService.ordered() );
            assertSetOrder( orderedPossibleTypesService.types(), OrderedPossibleTypesTestElement.Child2.TYPE, OrderedPossibleTypesTestElement.Child1.TYPE, OrderedPossibleTypesTestElement.Child3.TYPE );
        }
    }
    
}
