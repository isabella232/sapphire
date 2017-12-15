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

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.BooleanPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.ClassReferenceResolver;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "action link" )
@GenerateXmlBinding

public interface ISapphireActionLinkDef

    extends ISapphirePartDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireActionLinkDef.class );
 
    // *** Label ***
    
    @Label( standard = "label" )
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String label );
    
    // *** ShowImage ***
    
    @Type( base = Boolean.class )
    @Label( standard = "show image" )
    @DefaultValue( "false" )
    @BooleanPropertyXmlBinding( path = "show-image", treatExistenceAsValue = true, valueWhenPresent = true )
    
    ValueProperty PROP_SHOW_IMAGE = new ValueProperty( TYPE, "ShowImage" );
    
    Value<Boolean> getShowImage();
    void setShowImage( String showImage );
    void setShowImage( Boolean showImage );
    
    // *** ActionId ***
    
    @Label( standard = "action id" )
    @XmlBinding( path = "action-id" )
    
    ValueProperty PROP_ACTION_ID = new ValueProperty( TYPE, "ActionId" );
    
    Value<String> getActionId();
    void setActionId( String actionId );
    
    // *** ImplClass ***
    
    @Reference( target = Class.class, resolver = ClassReferenceResolver.class )
    @Label( standard = "implementation class" )
    @XmlBinding( path = "action-class" )
    
    ValueProperty PROP_IMPL_CLASS = new ValueProperty( TYPE, "ImplClass" );
    
    ReferenceValue<Class<?>> getImplClass();
    void setImplClass( String implClass );
    
}
