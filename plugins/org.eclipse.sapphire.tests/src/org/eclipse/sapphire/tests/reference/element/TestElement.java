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

package org.eclipse.sapphire.tests.reference.element;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementReference;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    public interface Item extends Element
    {
        ElementType TYPE = new ElementType( Item.class );
        
        // *** Name ***
        
        ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
        
        Value<String> getName();
        void setName( String value );
        
        // *** Value ***
        
        ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
        
        Value<String> getValue();
        void setValue( String value );
    }
    
    // *** ItemList1 ***
    
    @Type( base = Item.class )
    
    ListProperty PROP_ITEM_LIST_1 = new ListProperty( TYPE, "ItemList1" );
    
    ElementList<Item> getItemList1();
    
    // *** ItemList2 ***
    
    @Type( base = Item.class )
    
    ListProperty PROP_ITEM_LIST_2 = new ListProperty( TYPE, "ItemList2" );
    
    ElementList<Item> getItemList2();
    
    // *** UseItemList2 ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_USE_ITEM_LIST_2 = new ValueProperty( TYPE, "UseItemList2" );
    
    Value<Boolean> getUseItemList2();
    void setUseItemList2( String value );
    void setUseItemList2( Boolean value );
    
    // *** UseValueAsKey ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "false" )

    ValueProperty PROP_USE_VALUE_AS_KEY = new ValueProperty( TYPE, "UseValueAsKey" );
    
    Value<Boolean> getUseValueAsKey();
    void setUseValueAsKey( String value );
    void setUseValueAsKey( Boolean value );
    
    // *** DeclarativeReference ***
    
    @Reference( target = Item.class )
    @ElementReference( list = "/ItemList1", key = "Name" )
    @MustExist
    
    ValueProperty PROP_DECLARATIVE_REFERENCE = new ValueProperty( TYPE, "DeclarativeReference" );
    
    ReferenceValue<String,Item> getDeclarativeReference();
    void setDeclarativeReference( String value );

    // *** CustomReference ***
    
    @Reference( target = Item.class )
    @Service( impl = CustomElementReferenceService.class )
    @MustExist
    
    ValueProperty PROP_CUSTOM_REFERENCE = new ValueProperty( TYPE, "CustomReference" );
    
    ReferenceValue<String,Item> getCustomReference();
    void setCustomReference( String value );

    // *** ExternalReference ***
    
    @Reference( target = Item.class )
    @Service( impl = ExternalElementReferenceService.class )
    @MustExist
    
    ValueProperty PROP_EXTERNAL_REFERENCE = new ValueProperty( TYPE, "ExternalReference" );
    
    ReferenceValue<String,Item> getExternalReference();
    void setExternalReference( String value );

}