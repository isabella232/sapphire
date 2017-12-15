/******************************************************************************
 * Copyright (c) 2013 Oracle and Accenture
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Kamesh Sampath - [355751] General improvement of XML root binding API    
 ******************************************************************************/

package org.eclipse.sapphire.tests.modeling.serialization;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:kamesh.sampath@accenture.com">Kamesh Sampath</a> 
 */

@GenerateImpl
@XmlBinding( path = "test-root" )

public interface SerializationTestsModel

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( SerializationTestsModel.class );
    
    // *** EnumProperty1 ***

    @Type( base = ThreeChoiceAnswer.class )
    @Label( standard = "enum property 1" )
    @XmlBinding( path = "enum-prop-1" )

    ValueProperty PROP_ENUM_PROPERTY_1 = new ValueProperty( TYPE, "EnumProperty1" );

    Value<ThreeChoiceAnswer> getEnumProperty1();
    void setEnumProperty1( String value );
    void setEnumProperty1( ThreeChoiceAnswer value );

    // *** EnumProperty2 ***

    @Type( base = ThreeChoiceAnswerCustomized.class )
    @Label( standard = "enum property 2" )
    @XmlBinding( path = "enum-prop-2" )

    ValueProperty PROP_ENUM_PROPERTY_2 = new ValueProperty( TYPE, "EnumProperty2" );

    Value<ThreeChoiceAnswerCustomized> getEnumProperty2();
    void setEnumProperty2( String value );
    void setEnumProperty2( ThreeChoiceAnswerCustomized value );

    // *** EnumProperty3 ***

    @Type( base = ThreeChoiceAnswer.class )
    @Service( impl = ThreeChoiceAnswerCustomSerializationService.class )
    @Label( standard = "enum property 3" )
    @XmlBinding( path = "enum-prop-3" )

    ValueProperty PROP_ENUM_PROPERTY_3 = new ValueProperty( TYPE, "EnumProperty3" );

    Value<ThreeChoiceAnswer> getEnumProperty3();
    void setEnumProperty3( String value );
    void setEnumProperty3( ThreeChoiceAnswer value );
    
}
