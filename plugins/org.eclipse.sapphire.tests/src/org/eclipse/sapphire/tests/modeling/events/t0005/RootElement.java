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

package org.eclipse.sapphire.tests.modeling.events.t0005;

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface RootElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( RootElement.class );
    
    // *** Enabled ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "true" )
    
    ValueProperty PROP_ENABLED = new ValueProperty( TYPE, "Enabled" );
    
    Value<Boolean> getEnabled();
    void setEnabled( String value );
    void setEnabled( Boolean value );
    
    // *** Value ***
    
    @Enablement( expr = "${ Enabled }" )
    
    ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
    
    Value<String> getValue();
    void setValue( String value );
    
    // *** Element ***
    
    @Type( base = ChildElement.class )
    @Enablement( expr = "${ Enabled }" )

    ElementProperty PROP_ELEMENT = new ElementProperty( TYPE, "Element" );
    
    ModelElementHandle<ChildElement> getElement();
    
    // *** ImpliedElement ***
    
    @Type( base = ChildElement.class )
    @Enablement( expr = "${ Enabled }" )
    
    ImpliedElementProperty PROP_IMPLIED_ELEMENT = new ImpliedElementProperty( TYPE, "ImpliedElement" );
    
    ChildElement getImpliedElement();
    
    // *** List ***
    
    @Type( base = ChildElement.class )
    @Enablement( expr = "${ Enabled }" )
    
    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ModelElementList<ChildElement> getList();

}