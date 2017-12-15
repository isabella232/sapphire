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

package org.eclipse.sapphire.tests.modeling.misc.t0013;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestChildElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestChildElement.class );
    
    // *** DefaultIntegerValue ***
    
    @Type( base = Integer.class )

    ValueProperty PROP_DEFAULT_INTEGER_VALUE = new ValueProperty( TYPE, "DefaultIntegerValue" );
    
    Value<Integer> getDefaultIntegerValue();
    void setDefaultIntegerValue( String value );
    void setDefaultIntegerValue( Integer value );
    
    // *** IntegerValue ***

    @Type( base = Integer.class )
    @DependsOn( "/Child/DefaultIntegerValue" )
    @Service( impl = TestDefaultValueService.class )

    ValueProperty PROP_INTEGER_VALUE = new ValueProperty( TYPE, "IntegerValue" );

    Value<Integer> getIntegerValue();
    void setIntegerValue(String value);
    void setIntegerValue(Integer value);

}