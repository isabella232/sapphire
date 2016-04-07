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

package org.eclipse.sapphire.samples.gallery;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface DefaultValueGallery extends Element
{
    ElementType TYPE = new ElementType( DefaultValueGallery.class );
    
    // *** StaticDefaultValue ***
    
    @DefaultValue( text = "abc" )
    
    ValueProperty PROP_STATIC_DEFAULT_VALUE = new ValueProperty( TYPE, "StaticDefaultValue" );
    
    Value<String> getStaticDefaultValue();
    void setStaticDefaultValue( String value );
    
    // *** DefaultValue ***

    ValueProperty PROP_DEFAULT_VALUE = new ValueProperty( TYPE, "DefaultValue" );

    Value<String> getDefaultValue();
    void setDefaultValue(String value);
    
    // *** DynamicDefaultValue ***
    
    @DefaultValue( text = "${ DefaultValue }" )

    ValueProperty PROP_DYNAMIC_DEFAULT_VALUE = new ValueProperty( TYPE, "DynamicDefaultValue" );

    Value<String> getDynamicDefaultValue();
    void setDynamicDefaultValue(String value);

}
