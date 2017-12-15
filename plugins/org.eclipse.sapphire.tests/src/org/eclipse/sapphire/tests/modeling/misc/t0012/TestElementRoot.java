/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.misc.t0012;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Transient;
import org.eclipse.sapphire.modeling.TransientProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestElementRoot extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestElementRoot.class );
    
    // *** ValueProperty1 ***
    
    ValueProperty PROP_VALUE_PROPERTY_1 = new ValueProperty( TYPE, "ValueProperty1" );
    
    Value<String> getValueProperty1();
    void setValueProperty1( String value );
    
    // *** ValueProperty2 ***
    
    @Type( base = Integer.class )
    
    ValueProperty PROP_VALUE_PROPERTY_2 = new ValueProperty( TYPE, "ValueProperty2" );
    
    Value<Integer> getValueProperty2();
    void setValueProperty2( String value );
    void setValueProperty2( Integer value );
    
    // *** ValueProperty3 ***
    
    @Type( base = Integer.class )
    @DefaultValue( text = "5" )

    ValueProperty PROP_VALUE_PROPERTY_3 = new ValueProperty( TYPE, "ValueProperty3" );
    
    Value<Integer> getValueProperty3();
    void setValueProperty3( String value );
    void setValueProperty3( Integer value );
    
    // *** ImpliedElementProperty1 ***
    
    @Type( base = TestElementChild.class )
    
    ImpliedElementProperty PROP_IMPLIED_ELEMENT_PROPERTY_1 = new ImpliedElementProperty( TYPE, "ImpliedElementProperty1" );
    
    TestElementChild getImpliedElementProperty1();
    
    // *** ImpliedElementProperty2 ***
    
    @Type( base = TestElementChildEx.class )
    
    ImpliedElementProperty PROP_IMPLIED_ELEMENT_PROPERTY_2 = new ImpliedElementProperty( TYPE, "ImpliedElementProperty2" );
    
    TestElementChildEx getImpliedElementProperty2();
    
    // *** ElementProperty1 ***
    
    @Type( base = TestElementChild.class )
    
    ElementProperty PROP_ELEMENT_PROPERTY_1 = new ElementProperty( TYPE, "ElementProperty1" );
    
    ModelElementHandle<TestElementChild> getElementProperty1();
    
    // *** ElementProperty2 ***
    
    @Type( base = TestElementChild.class, possible = { TestElementChild.class, TestElementChildEx.class } )
    
    ElementProperty PROP_ELEMENT_PROPERTY_2 = new ElementProperty( TYPE, "ElementProperty2" );
    
    ModelElementHandle<TestElementChild> getElementProperty2();
    
    // *** ListProperty1 ***
    
    @Type( base = TestElementChild.class )
    
    ListProperty PROP_LIST_PROPERTY_1 = new ListProperty( TYPE, "ListProperty1" );
    
    ModelElementList<TestElementChild> getListProperty1();
    
    // *** ListProperty2 ***
    
    @Type( base = TestElementChild.class, possible = { TestElementChild.class, TestElementChildEx.class } )
    
    ListProperty PROP_LIST_PROPERTY_2 = new ListProperty( TYPE, "ListProperty2" );
    
    ModelElementList<TestElementChild> getListProperty2();
    
    // *** TransientProperty ***
    
    @Type( base = Object.class )
    
    TransientProperty PROP_TRANSIENT_PROPERTY = new TransientProperty( TYPE, "TransientProperty" );
    
    Transient<Object> getTransientProperty();
    void setTransientProperty( Object value );

}