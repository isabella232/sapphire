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

package org.eclipse.sapphire.modeling.extensibility;

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
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.extensibility.internal.ClassReferenceService;
import org.eclipse.sapphire.modeling.java.JavaTypeConstraints;
import org.eclipse.sapphire.modeling.java.JavaTypeKind;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "model property service" )
@GenerateImpl

public interface IModelPropertyServiceDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IModelPropertyServiceDef.class );
    
    // *** Description ***
    
    @LongString
    @Label( standard = "description" )
    @Localizable
    @XmlValueBinding( path = "description", collapseWhitespace = true )
    
    @Documentation( content = "Provides information about the model property service. The " +
                              "description should be in the form of properly capitalized and punctuated sentences." )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** TypeClass ***
    
    @Reference( target = Class.class, service = ClassReferenceService.class )
    @Label( standard = "service type class" )
    @NonNullValue
    @JavaTypeConstraints( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS, JavaTypeKind.INTERFACE }, type = "org.eclipse.sapphire.modeling.ModelPropertyService" )
    @MustExist
    @XmlBinding( path = "type" )
    
    @Documentation( content = "The type of service that the factory can create. Must extend ModelPropertyService." )

    ValueProperty PROP_TYPE_CLASS = new ValueProperty( TYPE, "TypeClass" );
    
    ReferenceValue<Class<?>> getTypeClass();
    void setTypeClass( String value );
    
    // *** FactoryClass ***
    
    @Reference( target = Class.class, service = ClassReferenceService.class )
    @Label( standard = "service factory class" )
    @NonNullValue
    @JavaTypeConstraints( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.modeling.ModelPropertyServiceFactory" )
    @MustExist
    @XmlBinding( path = "factory" )
    
    @Documentation( content = "The factory that can create a service of the specified type. Must extend ModelPropertyServiceFactory." )

    ValueProperty PROP_FACTORY_CLASS = new ValueProperty( TYPE, "FactoryClass" );
    
    ReferenceValue<Class<?>> getFactoryClass();
    void setFactoryClass( String value );
    
}
