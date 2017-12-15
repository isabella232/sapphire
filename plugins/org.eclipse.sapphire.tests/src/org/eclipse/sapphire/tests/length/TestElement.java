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

package org.eclipse.sapphire.tests.length;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Length;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** MinValue ***
    
    @Length( min = 8 )
    
    ValueProperty PROP_MIN_VALUE = new ValueProperty( TYPE, "MinValue" );
    
    Value<String> getMinValue();
    void setMinValue( String value );
    
    // *** MaxValue ***
    
    @Length( max = 50 )
    
    ValueProperty PROP_MAX_VALUE = new ValueProperty( TYPE, "MaxValue" );
    
    Value<String> getMaxValue();
    void setMaxValue( String value );
    
    // *** MinMaxValue ***
    
    @Length( min = 17, max = 297 )
    
    ValueProperty PROP_MIN_MAX_VALUE = new ValueProperty( TYPE, "MinMaxValue" );
    
    Value<String> getMinMaxValue();
    void setMinMaxValue( String value );
    
    // *** MinOneList ***
    
    @Type( base = Element.class )
    @Length( min = 1 )
    
    ListProperty PROP_MIN_ONE_LIST = new ListProperty( TYPE, "MinOneList" );
    
    ElementList<Element> getMinOneList();
    
    // *** MinTwoList ***
    
    @Type( base = Element.class )
    @Length( min = 2 )
    
    ListProperty PROP_MIN_TWO_LIST = new ListProperty( TYPE, "MinTwoList" );
    
    ElementList<Element> getMinTwoList();
    
    // *** MaxList ***
    
    @Type( base = Element.class )
    @Length( max = 12 )
    
    ListProperty PROP_MAX_LIST = new ListProperty( TYPE, "MaxList" );
    
    ElementList<Element> getMaxList();
    
    // *** MinMaxList ***
    
    @Type( base = Element.class )
    @Length( min = 1, max = 15 )
    
    ListProperty PROP_MIN_MAX_LIST = new ListProperty( TYPE, "MinMaxList" );
    
    ElementList<Element> getMinMaxList();
    
}
