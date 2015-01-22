/******************************************************************************
 * Copyright (c) 2015 Oracle
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
import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.ReferenceValue;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Whitespace;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "service" )
@Image( path = "ServiceDef.png" )

public interface ServiceDef extends Element
{
    ElementType TYPE = new ElementType( ServiceDef.class );
    
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
    
    // *** Implementation ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "implementation" )
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.services.Service" )
    @MustExist
    @XmlBinding( path = "implementation" )
    
    @Documentation
    (
        content = "The implementation of the service."
    )

    ValueProperty PROP_IMPLEMENTATION = new ValueProperty( TYPE, "Implementation" );
    
    ReferenceValue<JavaTypeName,JavaType> getImplementation();
    void setImplementation( String value );
    void setImplementation( JavaTypeName value );
    
    // *** Condition ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "condition" )
    @JavaTypeConstraint( kind = JavaTypeKind.CLASS, type = "org.eclipse.sapphire.services.ServiceCondition" )
    @MustExist
    @XmlBinding( path = "condition" )
    
    @Documentation
    (
        content = "The condition that determines when the service active."
    )

    ValueProperty PROP_CONDITION = new ValueProperty( TYPE, "Condition" );
    
    ReferenceValue<JavaTypeName,JavaType> getCondition();
    void setCondition( String value );
    void setCondition( JavaTypeName value );
    
    // *** Contexts ***

    @Type( base = ServiceContextRef.class )
    @Label( standard = "contexts" )
    @CountConstraint( min = 1 )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "context", type = ServiceContextRef.class ) )
    
    ListProperty PROP_CONTEXTS = new ListProperty(TYPE, "Contexts");

    ElementList<ServiceContextRef> getContexts();
    
    // *** Overrides ***
    
    @Label( standard = "service override" )

    public interface Override extends Element
    {
        ElementType TYPE = new ElementType( Override.class );
        
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
    
    ElementList<Override> getOverrides();
    
}
