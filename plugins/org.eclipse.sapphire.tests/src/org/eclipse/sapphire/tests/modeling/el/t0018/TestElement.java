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

package org.eclipse.sapphire.tests.modeling.el.t0018;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** BooleanValue ***
    
    @Type( base = Boolean.class )
    
    ValueProperty PROP_BOOLEAN_VALUE = new ValueProperty( TYPE, "BooleanValue" );
    
    Value<Boolean> getBooleanValue();
    void setBooleanValue( String value );
    void setBooleanValue( Boolean value );
    
    // *** BooleanValueWithDefault ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "true" )
    
    ValueProperty PROP_BOOLEAN_VALUE_WITH_DEFAULT = new ValueProperty( TYPE, "BooleanValueWithDefault" );
    
    Value<Boolean> getBooleanValueWithDefault();
    void setBooleanValueWithDefault( String value );
    void setBooleanValueWithDefault( Boolean value );
    
    // *** IntegerValue ***
    
    @Type( base = Integer.class )

    ValueProperty PROP_INTEGER_VALUE = new ValueProperty( TYPE, "IntegerValue" );
    
    Value<Integer> getIntegerValue();
    void setIntegerValue( String value );
    void setIntegerValue( Integer value );
    
    // *** IntegerValueWithDefault ***
    
    @Type( base = Integer.class )
    @DefaultValue( text = "1" )

    ValueProperty PROP_INTEGER_VALUE_WITH_DEFAULT = new ValueProperty( TYPE, "IntegerValueWithDefault" );
    
    Value<Integer> getIntegerValueWithDefault();
    void setIntegerValueWithDefault( String value );
    void setIntegerValueWithDefault( Integer value );
    
    // *** EnumValue ***
    
    enum EnumType
    {
        A,
        B,
        C
    }
    
    @Type( base = EnumType.class )
    
    ValueProperty PROP_ENUM_VALUE = new ValueProperty( TYPE, "EnumValue" );
    
    Value<EnumType> getEnumValue();
    void setEnumValue( String value );
    void setEnumValue( EnumType value );
    
    // *** EnumValueWithDefault ***
    
    @Type( base = EnumType.class )
    @DefaultValue( text = "A" )

    ValueProperty PROP_ENUM_VALUE_WITH_DEFAULT = new ValueProperty( TYPE, "EnumValueWithDefault" );
    
    Value<EnumType> getEnumValueWithDefault();
    void setEnumValueWithDefault( String value );
    void setEnumValueWithDefault( EnumType value );
    
}
