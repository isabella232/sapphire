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
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.PossibleTypesService;
import org.eclipse.sapphire.Type;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.tests.SapphireTestCase;
import org.junit.Test;

/**
 * Tests the ability of a list property to hold multiple entry types.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ListPropertyTests extends SapphireTestCase
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
        
        // *** ListWithUnorderedPossibleTypes ***
        
        @Type( base = Child.class, possible = { Child2.class, Child1.class, Child3.class } )
        
        ListProperty PROP_LIST_WITH_UNORDERED_POSSIBLE_TYPES = new ListProperty( TYPE, "ListWithUnorderedPossibleTypes" );
        
        ElementList<Child> getListWithUnorderedPossibleTypes();

        // *** ListWithOrderedPossibleTypes ***
        
        @Type( base = Child.class, possible = { Child2.class, Child1.class, Child3.class }, ordered = true )
        
        ListProperty PROP_LIST_WITH_ORDERED_POSSIBLE_TYPES = new ListProperty( TYPE, "ListWithOrderedPossibleTypes" );
        
        ElementList<Child> getListWithOrderedPossibleTypes();
    }
    
    @Test
    
    public void OrderedPossibleTypes() throws Exception
    {
        try( OrderedPossibleTypesTestElement a = OrderedPossibleTypesTestElement.TYPE.instantiate() )
        {
            final PossibleTypesService unorderedPossibleTypesService = a.getListWithUnorderedPossibleTypes().service( PossibleTypesService.class );
            
            assertEquals( false, unorderedPossibleTypesService.ordered() );
            assertSetOrder( unorderedPossibleTypesService.types(), OrderedPossibleTypesTestElement.Child1.TYPE, OrderedPossibleTypesTestElement.Child2.TYPE, OrderedPossibleTypesTestElement.Child3.TYPE );
            
            final PossibleTypesService orderedPossibleTypesService = a.getListWithOrderedPossibleTypes().service( PossibleTypesService.class );
            
            assertEquals( true, orderedPossibleTypesService.ordered() );
            assertSetOrder( orderedPossibleTypesService.types(), OrderedPossibleTypesTestElement.Child2.TYPE, OrderedPossibleTypesTestElement.Child1.TYPE, OrderedPossibleTypesTestElement.Child3.TYPE );
        }
    }
    
}
