/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IDefaultXmlBindingTestModelChildA

    extends IDefaultXmlBindingTestModelChild
    
{
    ModelElementType TYPE = new ModelElementType( IDefaultXmlBindingTestModelChildA.class );

    // *** ValuePropertyA ***
    
    ValueProperty PROP_VALUE_PROPERTY_A = new ValueProperty( TYPE, "ValuePropertyA" );

    Value<String> getValuePropertyA();
    void setValuePropertyA( String value );

}
