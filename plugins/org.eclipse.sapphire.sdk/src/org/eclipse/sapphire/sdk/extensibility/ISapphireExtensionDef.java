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

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;
import org.eclipse.sapphire.ui.def.ISapphireUiExtensionDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlRootBinding( elementName = "extension", namespace = "http://www.eclipse.org/sapphire/xmlns/extension" )

public interface ISapphireExtensionDef

    extends ISapphireUiExtensionDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireExtensionDef.class );

    // *** ModelElementServices ***
    
    @Type( base = IModelElementServiceDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "model-element-service", type = IModelElementServiceDef.class ) )
    @Label( standard = "model element services" )
    
    ListProperty PROP_MODEL_ELEMENT_SERVICES = new ListProperty( TYPE, "ModelElementServices" );
    
    ModelElementList<IModelElementServiceDef> getModelElementServices();
    
    // *** ModelPropertyServices ***
    
    @Type( base = IModelPropertyServiceDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "model-property-service", type = IModelPropertyServiceDef.class ) )
    @Label( standard = "model property services" )
    
    ListProperty PROP_MODEL_PROPERTY_SERVICES = new ListProperty( TYPE, "ModelPropertyServices" );
    
    ModelElementList<IModelPropertyServiceDef> getModelPropertyServices();
    
    // *** ValueSerializationServices ***
    
    @Type( base = IValueSerializationServiceDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "value-serialization-service", type = IValueSerializationServiceDef.class ) )
    @Label( standard = "value serialization services" )
    
    ListProperty PROP_VALUE_SERIALIZATION_SERVICES = new ListProperty( TYPE, "ValueSerializationServices" );
    
    ModelElementList<IValueSerializationServiceDef> getValueSerializationServices();
    
    // *** Functions ***
    
    @Type( base = IFunctionDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "function", type = IFunctionDef.class ) )
    @Label( standard = "functions" )
    
    ListProperty PROP_FUNCTIONS = new ListProperty( TYPE, "Functions" );
    
    ModelElementList<IFunctionDef> getFunctions();

    // *** TypeCasts ***
    
    @Type( base = ITypeCastDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "type-cast", type = ITypeCastDef.class ) )
    @Label( standard = "type casts" )
    
    ListProperty PROP_TYPE_CASTS = new ListProperty( TYPE, "TypeCasts" );
    
    ModelElementList<ITypeCastDef> getTypeCasts();

}
