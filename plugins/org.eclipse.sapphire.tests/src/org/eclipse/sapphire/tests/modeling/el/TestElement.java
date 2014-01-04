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

package org.eclipse.sapphire.tests.modeling.el;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** StringProp ***
    
    ValueProperty PROP_STRING_PROP = new ValueProperty( TYPE, "StringProp" );
    
    Value<String> getStringProp();
    void setStringProp( String value );

    // *** IntegerProp ***
    
    @Type( base = Integer.class )
    
    ValueProperty PROP_INTEGER_PROP = new ValueProperty( TYPE, "IntegerProp" );
    
    Value<Integer> getIntegerProp();
    void setIntegerProp( String value );
    void setIntegerProp( Integer value );
    
    // *** FooBar ***
    
    @Type( base = TestElement.class )

    ElementProperty PROP_FOO_BAR = new ElementProperty( TYPE, "FooBar" );
    
    ElementHandle<TestElement> getFooBar();

}
