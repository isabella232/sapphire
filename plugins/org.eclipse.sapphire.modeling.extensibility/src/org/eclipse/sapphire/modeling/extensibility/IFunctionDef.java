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

@Label( standard = "function" )
@GenerateImpl

public interface IFunctionDef

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IFunctionDef.class );
    
    // *** Name ***
    
    @Label( standard = "name" )
    @NonNullValue
    @XmlBinding( path = "name" )
    
    @Documentation( content = "The name by which function will be referenced in the expression language. Functions " +
                              "can be placed in a namespace by using \"[namespace]:[name]\" syntax. Hierarchical " +
                              "namespaces are also alowed. Each segment in the function name must follow Java identifier rules." )
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String value );
    
    // *** Description ***
    
    @LongString
    @Label( standard = "description" )
    @Localizable
    @XmlValueBinding( path = "description", collapseWhitespace = true )
    
    @Documentation( content = "Provides information about the value serialization service. The " +
                              "description should be in the form of properly capitalized and punctuated sentences." )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** ImplClass ***
    
    @Reference( target = Class.class, service = ClassReferenceService.class )
    @Label( standard = "implementation class" )
    @NonNullValue
    @JavaTypeConstraints( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.modeling.el.Function" )
    @MustExist
    @XmlBinding( path = "impl" )
    
    @Documentation( content = "The function implementation. Must extend Function." )

    ValueProperty PROP_IMPL_CLASS = new ValueProperty( TYPE, "ImplClass" );
    
    ReferenceValue<Class<?>> getImplClass();
    void setImplClass( String value );
    
}
