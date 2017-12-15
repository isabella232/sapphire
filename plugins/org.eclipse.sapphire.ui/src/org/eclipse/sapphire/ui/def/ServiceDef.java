/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

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
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "service" )
@GenerateImpl

public interface ServiceDef extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ServiceDef.class );
    
    // *** Implementation ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "implementation" )
    @Required
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
