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

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** Entries ***
    
    @Type( base = ListEntry.class )
    
    ListProperty PROP_ENTRIES = new ListProperty( TYPE, "Entries" );
    
    ElementList<ListEntry> getEntries();
    
    // *** ValueWithStaticPossibles ***
    
    @PossibleValues( values = { "a", "b", "c" } )
    
    ValueProperty PROP_VALUE_WITH_STATIC_POSSIBLES = new ValueProperty( TYPE, "ValueWithStaticPossibles" );
    
    Value<String> getValueWithStaticPossibles();
    void setValueWithStaticPossibles( String value );
    
    // *** ValueWithModelPossibles ***
    
    @PossibleValues( property = "/Entries/Value" )
    
    ValueProperty PROP_VALUE_WITH_MODEL_POSSIBLES = new ValueProperty( TYPE, "ValueWithModelPossibles" );
    
    Value<String> getValueWithModelPossibles();
    void setValueWithModelPossibles( String value );
    
    // *** ListWithStaticPossibles ***
    
    @Type( base = ListEntry.class )
    @PossibleValues( values = { "a", "b", "c" } )
    
    ListProperty PROP_LIST_WITH_STATIC_POSSIBLES = new ListProperty( TYPE, "ListWithStaticPossibles" );
    
    ElementList<ListEntry> getListWithStaticPossibles();

    // *** ListWithModelPossibles ***
    
    @Type( base = ListEntry.class )
    @PossibleValues( property = "/Entries/Value" )
    
    ListProperty PROP_LIST_WITH_MODEL_POSSIBLES = new ListProperty( TYPE, "ListWithModelPossibles" );
    
    ElementList<ListEntry> getListWithModelPossibles();

}