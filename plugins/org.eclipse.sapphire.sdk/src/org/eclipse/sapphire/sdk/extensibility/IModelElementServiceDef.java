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
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
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
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "model element service" )
@GenerateImpl

public interface IModelElementServiceDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IModelElementServiceDef.class );
    
    // *** Id ***
    
    @Label( standard = "ID" )
    @Required
    @XmlBinding( path = "id" )
    
    @Documentation( content = "Uniquely identifies this model element service to the system and other services." )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String value );
    
    // *** Description ***
    
    @LongString
    @Label( standard = "description" )
    @Localizable
    @Whitespace( collapse = true )
    @XmlValueBinding( path = "description" )
    
    @Documentation( content = "Provides information about the model element service. The " +
                              "description should be in the form of properly capitalized and punctuated sentences." )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** TypeClass ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "service type class" )
    @Required
    @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS, JavaTypeKind.INTERFACE }, type = "org.eclipse.sapphire.modeling.ModelElementService" )
    @MustExist
    @XmlBinding( path = "type" )
    
    @Documentation( content = "The type of service that the factory can create. Must extend ModelElementService." )

    ValueProperty PROP_TYPE_CLASS = new ValueProperty( TYPE, "TypeClass" );
    
    ReferenceValue<JavaTypeName,JavaType> getTypeClass();
    void setTypeClass( String value );
    void setTypeClass( JavaTypeName value );
    
    // *** FactoryClass ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "service factory class" )
    @Required
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.modeling.ModelElementServiceFactory" )
    @MustExist
    @XmlBinding( path = "factory" )
    
    @Documentation( content = "The factory that can create a service of the specified type. Must extend ModelElementServiceFactory." )

    ValueProperty PROP_FACTORY_CLASS = new ValueProperty( TYPE, "FactoryClass" );
    
    ReferenceValue<JavaTypeName,JavaType> getFactoryClass();
    void setFactoryClass( String value );
    void setFactoryClass( JavaTypeName value );
    
    // *** Overrides ***
    
    @Type( base = IModelElementServiceRef.class )
    @Label( standard = "overrides" )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "overrides", type = IModelElementServiceRef.class ) )
    
    @Documentation( content = "When multiple service implementations activate for a given context, overrides can be used " +
                              "to control which implementation is used." )
    
    ListProperty PROP_OVERRIDES = new ListProperty( TYPE, "Overrides" );
    
    ModelElementList<IModelElementServiceRef> getOverrides();
    
}
