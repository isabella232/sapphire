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

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.ClassReferenceResolver;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface ISapphirePropertyMetadata

    extends IModelElementForXml
    
{
    ModelElementType TYPE = new ModelElementType( ISapphirePropertyMetadata.class );
    
    // *** BrowseHandlers ***
    
    @Type( base = ISapphireBrowseHandlerDef.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "browse-handler", type = ISapphireBrowseHandlerDef.class ) } )
    @Label( standard = "browse handlers" )
    
    ListProperty PROP_BROWSE_HANDLERS = new ListProperty( TYPE, "BrowseHandlers" );
    
    ModelElementList<ISapphireBrowseHandlerDef> getBrowseHandlers();
    
    // *** JumpHandler ***
    
    @Reference( target = Class.class, resolver = ClassReferenceResolver.class )
    @Label( standard = "jump handler" )
    @XmlBinding( path = "jump-handler" )
    
    ValueProperty PROP_JUMP_HANDLER = new ValueProperty( TYPE, "JumpHandler" );
    
    ReferenceValue<Class<?>> getJumpHandler();
    void setJumpHandler( String value );
    
}
