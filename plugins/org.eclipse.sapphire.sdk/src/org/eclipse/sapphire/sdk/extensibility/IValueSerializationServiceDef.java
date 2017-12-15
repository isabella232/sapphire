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

package org.eclipse.sapphire.sdk.extensibility;

import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Whitespace;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "value serialization service" )
@GenerateImpl

public interface IValueSerializationServiceDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IValueSerializationServiceDef.class );
    
    // *** Description ***
    
    @LongString
    @Label( standard = "description" )
    @Localizable
    @Whitespace( collapse = true )
    @XmlValueBinding( path = "description" )
    
    @Documentation( content = "Provides information about the value serialization service. The " +
                              "description should be in the form of properly capitalized and punctuated sentences." )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** TypeClass ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "type class" )
    @Required
    @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS, JavaTypeKind.INTERFACE } )
    @MustExist
    @XmlBinding( path = "type" )
    
    @Documentation( content = "The type that the value serialization service can handler." )

    ValueProperty PROP_TYPE_CLASS = new ValueProperty( TYPE, "TypeClass" );
    
    ReferenceValue<JavaTypeName,JavaType> getTypeClass();
    void setTypeClass( String value );
    void setTypeClass( JavaTypeName value );
    
    // *** ImplClass ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "implementation class" )
    @Required
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.modeling.serialization.ValueSerializationService" )
    @MustExist
    @XmlBinding( path = "impl" )
    
    @Documentation( content = "The value serialization service implementation. Must extend ValueSerializationService." )

    ValueProperty PROP_IMPL_CLASS = new ValueProperty( TYPE, "ImplClass" );
    
    ReferenceValue<JavaTypeName,JavaType> getImplClass();
    void setImplClass( String value );
    void setImplClass( JavaTypeName value );
    
}
