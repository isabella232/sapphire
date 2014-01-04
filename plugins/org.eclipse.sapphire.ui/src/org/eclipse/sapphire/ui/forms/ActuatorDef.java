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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.ImageReference;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "actuator" )
@Image( path = "ActuatorDef.png" )
@XmlBinding( path = "actuator" )

@Documentation
(
    content = "An actuator provides means to invoke an action. The action could be drawn from the context where actuator is " +
    		  "placed or provided as part of actuator's definition."
)

public interface ActuatorDef extends FormComponentDef
{
    ElementType TYPE = new ElementType( ActuatorDef.class );
 
    // *** ActionId ***
    
    @Label( standard = "action ID" )
    @Required
    @XmlBinding( path = "action-id" )
    
    ValueProperty PROP_ACTION_ID = new ValueProperty( TYPE, "ActionId" );
    
    Value<String> getActionId();
    void setActionId( String value );
    
    // *** ActionHandlerId ***
    
    @Label( standard = "action handler ID" )
    @XmlBinding( path = "action-handler-id" )
    
    ValueProperty PROP_ACTION_HANDLER_ID = new ValueProperty( TYPE, "ActionHandlerId" );
    
    Value<String> getActionHandlerId();
    void setActionHandlerId( String value );
    
    // *** ShowLabel ***
    
    @Type( base = Boolean.class )
    @Label( standard = "show label" )
    @DefaultValue( text = "true" )
    @XmlBinding( path = "show-label" )
    
    @Documentation
    (
        content = "Indicates whether a label should be shown when presenting the actuator. By default, the label associated " +
                  "with the linked action will be used. Alternatively, a label can be specified explicitly."
    )
    
    ValueProperty PROP_SHOW_LABEL = new ValueProperty( TYPE, "ShowLabel" );
    
    Value<Boolean> getShowLabel();
    void setShowLabel( String value );
    void setShowLabel( Boolean value );
    
    // *** Label ***
    
    @Type( base = Function.class )
    @Label( standard = "label" )
    @Enablement( expr = "${ ShowLabel }" )
    @XmlBinding( path = "label" )
    @Localizable
    
    @Documentation
    (
        content = "The label to use when presenting the actuator. If not specified, the label associated with the " +
                  "linked action will be used."
    )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<Function> getLabel();
    void setLabel( String value );
    void setLabel( Function value );
    
    // *** ShowImage ***
    
    @Type( base = Boolean.class )
    @Label( standard = "show image" )
    @DefaultValue( text = "false" )
    @XmlBinding( path = "show-image" )
    
    @Documentation
    (
        content = "Indicates whether an image should be shown when presenting the actuator. By default, the image associated " +
                  "with the linked action will be used. Alternatively, an image can be specified explicitly."
    )
    
    ValueProperty PROP_SHOW_IMAGE = new ValueProperty( TYPE, "ShowImage" );
    
    Value<Boolean> getShowImage();
    void setShowImage( String value );
    void setShowImage( Boolean value );
    
    // *** Images ***
    
    @Type( base = ImageReference.class )
    @Label( standard = "images" )
    @Enablement( expr = "${ ShowImage }" )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "image", type = ImageReference.class ) )
    
    @Documentation
    (
        content = "The image to use when presenting the actuator. If not specified, the image associated with the " +
                  "linked action will be used."
    )
    
    ListProperty PROP_IMAGES = new ListProperty( TYPE, "Images" );
    
    ElementList<ImageReference> getImages();
    
    // *** Style ***

    @DefaultValue( text = "Sapphire.Actuator.Link" )
    @PossibleValues( values = { "Sapphire.Actuator.Link", "Sapphire.Actuator.Button" } )
    
    ValueProperty PROP_STYLE = new ValueProperty( TYPE, FormComponentDef.PROP_STYLE );
    
    // *** HorizontalAlignment ***
    
    @Type( base = HorizontalAlignment.class )
    @Label( standard = "horizontal alignment")
    @DefaultValue( text = "left" )
    @XmlBinding( path = "horizontal-align" )
    
    ValueProperty PROP_HORIZONTAL_ALIGNMENT = new ValueProperty( TYPE, "HorizontalAlignment" );
    
    Value<HorizontalAlignment> getHorizontalAlignment();
    void setHorizontalAlignment( String value );
    void setHorizontalAlignment( HorizontalAlignment value );
    
    // *** SpanBothColumns ***
    
    @Type( base = Boolean.class )
    @Label( standard = "span both columns" )
    @DefaultValue( text = "true" )
    @XmlBinding( path = "span" )
    
    @Documentation
    (
        content = "Indicates whether the actuator should span both columns. Note that depending on presentation " +
                  "details the actuator may not stretch horizontally, but the choice of whether to span both " +
                  "columns interracts with horizontal alignment selection to determine exactly where the actuator " +
                  "will be placed."
    )
    
    ValueProperty PROP_SPAN_BOTH_COLUMNS = new ValueProperty( TYPE, "SpanBothColumns" );
    
    Value<Boolean> getSpanBothColumns();
    void setSpanBothColumns( String value );
    void setSpanBothColumns( Boolean value );

}
