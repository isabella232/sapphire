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

package org.eclipse.sapphire.tests.services.t0013;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    interface Child extends Element
    {
        ElementType TYPE = new ElementType( Child.class );
    }
    
    // *** Required ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "false" )
    
    ValueProperty PROP_REQUIRED = new ValueProperty( TYPE, "Required" );
    
    Value<Boolean> getRequired();
    void setRequired( String value );
    void setRequired( Boolean value );
    
    // *** Value ***
    
    @Label( standard = "value" )
    
    ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
    
    Value<String> getValue();
    void setValue( String value );
    
    // *** ValueRequired ***
    
    @Label( standard = "value" )
    @Required
    
    ValueProperty PROP_VALUE_REQUIRED = new ValueProperty( TYPE, "ValueRequired" );
    
    Value<String> getValueRequired();
    void setValueRequired( String value );
    
    // *** ValueRequiredExpr ***
    
    @Label( standard = "value" )
    @Required( "${ Required }" )
    
    ValueProperty PROP_VALUE_REQUIRED_EXPR = new ValueProperty( TYPE, "ValueRequiredExpr" );
    
    Value<String> getValueRequiredExpr();
    void setValueRequiredExpr( String value );
    
    // *** Element ***
    
    @Type( base = Child.class )
    @Label( standard = "element" )
    
    ElementProperty PROP_ELEMENT = new ElementProperty( TYPE, "Element" );
    
    ElementHandle<Child> getElement();
    
    // *** ElementRequired ***
    
    @Type( base = Child.class )
    @Label( standard = "element" )
    @Required
    
    ElementProperty PROP_ELEMENT_REQUIRED = new ElementProperty( TYPE, "ElementRequired" );
    
    ElementHandle<Child> getElementRequired();
    
    // *** ElementRequiredExpr ***
    
    @Type( base = Child.class )
    @Label( standard = "element" )
    @Required( "${ Required }" )

    ElementProperty PROP_ELEMENT_REQUIRED_EXPR = new ElementProperty( TYPE, "ElementRequiredExpr" );
    
    ElementHandle<Child> getElementRequiredExpr();
    
}
