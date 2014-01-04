/******************************************************************************
 * Copyright (c) 2014 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.xml;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a>
 */

@XmlBinding( path = "test-root" )

public interface XmlBindingTestModel extends Element
{
    ElementType TYPE = new ElementType( XmlBindingTestModel.class );
    
    // *** ValuePropertyA ***
    
    @XmlBinding( path = "value-prop-a" )
    @Label( standard = "value property A" )

    ValueProperty PROP_VALUE_PROPERTY_A = new ValueProperty( TYPE, "ValuePropertyA" );

    Value<String> getValuePropertyA();
    void setValuePropertyA( String value );

    // *** ValuePropertyB ***
    
    @XmlBinding( path = "x/y/z/value-prop-b" )
    @Label( standard = "value property B" )

    ValueProperty PROP_VALUE_PROPERTY_B = new ValueProperty( TYPE, "ValuePropertyB" );

    Value<String> getValuePropertyB();
    void setValuePropertyB( String value );

    // *** ValuePropertyC ***
    
    @XmlBinding( path = "@value-prop-c" )
    @Label( standard = "value property C" )

    ValueProperty PROP_VALUE_PROPERTY_C = new ValueProperty( TYPE, "ValuePropertyC" );

    Value<String> getValuePropertyC();
    void setValuePropertyC( String value );

    // *** ValuePropertyD ***
    
    @XmlBinding( path = "x/y/z/@value-prop-d" )
    @Label( standard = "value property D" )

    ValueProperty PROP_VALUE_PROPERTY_D = new ValueProperty( TYPE, "ValuePropertyD" );

    Value<String> getValuePropertyD();
    void setValuePropertyD( String value );
    
    // *** ValuePropertyE ***
    
    @XmlBinding( path = "%value-prop-e" )
    @Label( standard = "value property E" )

    ValueProperty PROP_VALUE_PROPERTY_E = new ValueProperty( TYPE, "ValuePropertyE" );

    Value<String> getValuePropertyE();
    void setValuePropertyE( String value );

    // *** ValuePropertyF ***
    
    @XmlBinding( path = "x/y/z/%value-prop-f" )
    @Label( standard = "value property F" )

    ValueProperty PROP_VALUE_PROPERTY_F = new ValueProperty( TYPE, "ValuePropertyF" );

    Value<String> getValuePropertyF();
    void setValuePropertyF( String value );

}
