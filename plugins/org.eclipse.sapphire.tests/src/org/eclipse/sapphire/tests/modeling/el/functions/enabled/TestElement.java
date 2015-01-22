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

package org.eclipse.sapphire.tests.modeling.el.functions.enabled;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ImpliedElementProperty;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** Enable ***
    
    @Type( base = Boolean.class )

    ValueProperty PROP_ENABLE = new ValueProperty( TYPE, "Enable" );
    
    Value<Boolean> getEnable();
    void setEnable( String value );
    void setEnable( Boolean value );
    
    // *** Value ***
    
    @Enablement( expr = "${ Enable }" )
    
    ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
    
    Value<String> getValue();
    void setValue( String value );
    
    // *** List ***
    
    @Type( base = Element.class )
    @Enablement( expr = "${ Enable }" )
    
    ListProperty PROP_LIST = new ListProperty( TYPE, "List" );
    
    ElementList<Element> getList();
    
    // *** Element ***
    
    @Type( base = Element.class )
    @Enablement( expr = "${ Enable }" )
    
    ElementProperty PROP_ELEMENT = new ElementProperty( TYPE, "Element" );
    
    ElementHandle<Element> getElement();
    
    // *** ElementImplied ***
    
    @Type( base = Element.class )
    @Enablement( expr = "${ Enable }" )
    
    ImpliedElementProperty PROP_ELEMENT_IMPLIED = new ImpliedElementProperty( TYPE, "ElementImplied" );
    
    Element getElementImplied();

}
