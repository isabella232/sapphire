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

@Label( standard = "type cast" )
@GenerateImpl

public interface ITypeCastDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ITypeCastDef.class );
    
    // *** Description ***
    
    @LongString
    @Label( standard = "description" )
    @Localizable
    @Whitespace( collapse = true )
    @XmlValueBinding( path = "description" )
    
    @Documentation( content = "Provides information about the type cast. The description should be " +
                              "in the form of properly capitalized and punctuated sentences." )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** SourceType ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "source type" )
    @Required
    @MustExist
    @XmlBinding( path = "source" )
    
    @Documentation( content = "The input type for this type cast." )

    ValueProperty PROP_SOURCE_TYPE = new ValueProperty( TYPE, "SourceType" );
    
    ReferenceValue<JavaTypeName,JavaType> getSourceType();
    void setSourceType( String value );
    void setSourceType( JavaTypeName value );

    // *** TargetType ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "target type" )
    @Required
    @MustExist
    @XmlBinding( path = "target" )
    
    @Documentation( content = "The output type for this type cast." )

    ValueProperty PROP_TARGET_TYPE = new ValueProperty( TYPE, "TargetType" );
    
    ReferenceValue<JavaTypeName,JavaType> getTargetType();
    void setTargetType( String value );
    void setTargetType( JavaTypeName value );

    // *** Implementation ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "implementation" )
    @Required
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.modeling.el.TypeCast" )
    @MustExist
    @XmlBinding( path = "impl" )
    
    @Documentation( content = "The function implementation. Must extend Function." )

    ValueProperty PROP_IMPLEMENTATION = new ValueProperty( TYPE, "Implementation" );
    
    ReferenceValue<JavaTypeName,JavaType> getImplementation();
    void setImplementation( String value );
    void setImplementation( JavaTypeName value );
    
}
