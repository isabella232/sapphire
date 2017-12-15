/******************************************************************************
 * Copyright (c) 2012 Oracle
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
import org.eclipse.sapphire.modeling.Status.Severity;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Whitespace;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.services.ServiceContext;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "service" )
@GenerateImpl

public interface ServiceDef extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ServiceDef.class );
    
    // *** Id ***
    
    @Label( standard = "ID" )
    @Required
    @XmlBinding( path = "id" )
    
    @Documentation
    (
        content = "Uniquely identifies this service to the system and other services."
    )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String value );
    
    // *** Description ***
    
    @LongString
    @Label( standard = "description" )
    @Localizable
    @Whitespace( collapse = true )
    @XmlBinding( path = "description" )
    
    @Documentation
    (
        content = "Provides information about the service. The description should be in the form of " +
    	          "properly capitalized and punctuated sentences."
    )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<String> getDescription();
    void setDescription( String value );
    
    // *** Type ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "service type class" )
    @Required
    @JavaTypeConstraint( kind = { JavaTypeKind.CLASS, JavaTypeKind.ABSTRACT_CLASS }, type = "org.eclipse.sapphire.services.Service" )
    @MustExist
    @XmlBinding( path = "type" )
    
    @Documentation
    (
        content = "The type of service that the factory can create."
    )

    ValueProperty PROP_TYPE = new ValueProperty( TYPE, "Type" );
    
    ReferenceValue<JavaTypeName,JavaType> getType();
    void setType( String value );
    void setType( JavaTypeName value );
    
    // *** Factory ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "service factory class" )
    @Required
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.services.ServiceFactory" )
    @MustExist
    @XmlBinding( path = "factory" )
    
    @Documentation
    (
        content = "The factory that can create a service of the specified type."
    )

    ValueProperty PROP_FACTORY = new ValueProperty( TYPE, "Factory" );
    
    ReferenceValue<JavaTypeName,JavaType> getFactory();
    void setFactory( String value );
    void setFactory( JavaTypeName value );
    
    // *** Context ***
    
    @Label( standard = "context" )
    @Required
    @XmlBinding( path = "context" )
    
    @PossibleValues
    (
        values = 
        {
            ServiceContext.ID_ELEMENT_INSTANCE,
            ServiceContext.ID_ELEMENT_METAMODEL,
            ServiceContext.ID_PROPERTY_INSTANCE,
            ServiceContext.ID_PROPERTY_METAMODEL
        },
        invalidValueSeverity = Severity.OK
    )
    
    ValueProperty PROP_CONTEXT = new ValueProperty( TYPE, "Context" );
    
    Value<String> getContext();
    void setContext( String value );
    
    // *** Overrides ***
    
    @Label( standard = "service override" )
    @GenerateImpl

    public interface Override extends IModelElement
    {
        ModelElementType TYPE = new ModelElementType( Override.class );
        
        // *** Id ***
        
        @Label( standard = "ID" )
        @Required
        @XmlBinding( path = "" )
        
        ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
        
        Value<String> getId();
        void setId( String value );
    }
    
    @Type( base = Override.class )
    @Label( standard = "overrides" )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "overrides", type = Override.class ) )
    
    @Documentation
    (
        content = "When multiple service implementations activate for a given context, overrides can be used " +
                  "to control which implementation is used."
    )
    
    ListProperty PROP_OVERRIDES = new ListProperty( TYPE, "Overrides" );
    
    ModelElementList<Override> getOverrides();
    
}
