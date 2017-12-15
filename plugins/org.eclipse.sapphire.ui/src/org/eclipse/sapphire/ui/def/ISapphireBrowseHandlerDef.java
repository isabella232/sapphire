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

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.IRemovable;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.ClassReferenceResolver;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "browse handler" )
@GenerateXmlBinding

public interface ISapphireBrowseHandlerDef

    extends IModelElementForXml, IRemovable
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireBrowseHandlerDef.class );
 
    // *** ImplClass ***
    
    @Reference( target = Class.class, resolver = ClassReferenceResolver.class )
    @Label( standard = "class" )
    @XmlBinding( path = "class" )
    
    ValueProperty PROP_IMPL_CLASS = new ValueProperty( TYPE, "ImplClass" );
    
    ReferenceValue<Class<?>> getImplClass();
    void setImplClass( String implClass );
    
    // *** Params ***
    
    @Type( base = ISapphireParam.class )
    @Label( standard = "params" )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "param", type = ISapphireParam.class ) } )
    
    ListProperty PROP_PARAMS = new ListProperty( TYPE, "Params" );
    
    ModelElementList<ISapphireParam> getParams();
    
}
