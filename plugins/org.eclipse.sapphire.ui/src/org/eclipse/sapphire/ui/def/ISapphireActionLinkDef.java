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
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "action link" )
@GenerateImpl

public interface ISapphireActionLinkDef

    extends ISapphirePartDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireActionLinkDef.class );
 
    // *** Label ***
    
    @Label( standard = "label" )
    @NonNullValue
    @Localizable
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String value );
    
    // *** ShowImage ***
    
    @Type( base = Boolean.class )
    @Label( standard = "show image" )
    @DefaultValue( text = "false" )
    @XmlValueBinding( path = "show-image", mapExistanceToValue = "true;false" )
    
    ValueProperty PROP_SHOW_IMAGE = new ValueProperty( TYPE, "ShowImage" );
    
    Value<Boolean> getShowImage();
    void setShowImage( String value );
    void setShowImage( Boolean value );
    
    // *** ActionId ***
    
    @Label( standard = "action id" )
    @NonNullValue
    @XmlBinding( path = "action-id" )
    
    ValueProperty PROP_ACTION_ID = new ValueProperty( TYPE, "ActionId" );
    
    Value<String> getActionId();
    void setActionId( String value );
    
    // *** ActionHandlerId ***
    
    @Label( standard = "action handler id" )
    @XmlBinding( path = "action-handler-id" )
    
    ValueProperty PROP_ACTION_HANDLER_ID = new ValueProperty( TYPE, "ActionHandlerId" );
    
    Value<String> getActionHandlerId();
    void setActionHandlerId( String value );

}
