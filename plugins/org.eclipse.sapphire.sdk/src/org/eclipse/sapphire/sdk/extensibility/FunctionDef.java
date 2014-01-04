/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.sdk.extensibility;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Whitespace;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlElementBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "function" )
@Image( path = "FunctionDef.png" )

public interface FunctionDef extends Element
{
    ElementType TYPE = new ElementType( FunctionDef.class );
    
    // *** Name ***
    
    @Label( standard = "name" )
    @Required
    @XmlBinding( path = "name" )
    
    @Documentation( content = "The name by which function will be referenced in the expression language. Functions " +
                              "can be placed in a namespace by using \"[namespace]:[name]\" syntax. Hierarchical " +
                              "namespaces are also alowed. Each segment in the function name must follow Java identifier rules." )
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String value );
    
    // *** Signature ***
    
    @Label( standard = "signature" )
    @XmlBinding( path = "signature" )
    
    interface Signature extends Element
    {
        ElementType TYPE = new ElementType( Signature.class );
        
        // *** Parameters ***
        
        @Label( standard = "parameter" )
        @XmlBinding( path = "parameter" )
        
        interface Parameter extends Element
        {
            ElementType TYPE = new ElementType( Parameter.class );
            
            // *** Type ***
            
            @Type( base = JavaTypeName.class )
            @Reference( target = JavaType.class )
            @Label( standard = "parameter type" )
            @Required
            @MustExist
            @XmlBinding( path = "" )
            
            @Documentation( content = "The function parameter type." )

            ValueProperty PROP_TYPE = new ValueProperty( TYPE, "Type" );
            
            ReferenceValue<JavaTypeName,JavaType> getType();
            void setType( String value );
            void setType( JavaTypeName value );
        }
        
        @Type( base = Parameter.class )
        @Label( standard = "parameters" )
        @XmlListBinding( path = "" )
        
        ListProperty PROP_PARAMETERS = new ListProperty( TYPE, "Parameters" );
        
        ElementList<Parameter> getParameters();
    }
    
    @Type( base = Signature.class )
    @Label( standard = "signature" )
    @XmlElementBinding( path = "" )
    
    @Documentation
    (
        content = "A function can be restricted to apply to a specific parameter signature. If not restricted, the function will " +
                  "apply for any number of parameters. The function is responsible for throwing a FunctionExeption during evaluation " +
                  "if a problem with the parameters is detected."
    )
    
    ElementProperty PROP_SIGNATURE = new ElementProperty( TYPE, "Signature" );
    
    ElementHandle<Signature> getSignature();
    
    // *** Description ***
    
    @LongString
    @Label( standard = "description" )
    @Localizable
    @Whitespace( collapse = true )
    @XmlBinding( path = "description" )
    
    @Documentation( content = "Provides detailed information about the function. The " +
                              "description should be in the form of properly capitalized and punctuated sentences." )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** ImplClass ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "implementation class" )
    @Required
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.modeling.el.Function" )
    @MustExist
    @XmlBinding( path = "impl" )
    
    @Documentation( content = "The function implementation. Must extend Function." )

    ValueProperty PROP_IMPL_CLASS = new ValueProperty( TYPE, "ImplClass" );
    
    ReferenceValue<JavaTypeName,JavaType> getImplClass();
    void setImplClass( String value );
    void setImplClass( JavaTypeName value );
    
}
