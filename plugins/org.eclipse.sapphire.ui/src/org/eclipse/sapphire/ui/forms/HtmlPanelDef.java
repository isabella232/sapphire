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

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;
import org.eclipse.sapphire.ui.def.HtmlContentSourceType;
import org.eclipse.sapphire.ui.def.internal.SapphireHtmlPanelDefSourceBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "HTML panel" )
@Image( path = "HtmlPanelDef.png" )
@XmlBinding( path = "html" )

public interface HtmlPanelDef extends FormComponentDef
{
    ElementType TYPE = new ElementType( HtmlPanelDef.class );
    
    // *** ContentSourceType ***
    
    @Type( base = HtmlContentSourceType.class )
    @Label( standard = "content source type" )
    @DefaultValue( text = "EMBEDDED" )
    @CustomXmlValueBinding( impl = SapphireHtmlPanelDefSourceBinding.class )
    
    ValueProperty PROP_CONTENT_SOURCE_TYPE = new ValueProperty( TYPE, "ContentSourceType" );
    
    Value<HtmlContentSourceType> getContentSourceType();
    void setContentSourceType( String value );
    void setContentSourceType( HtmlContentSourceType value );
    
    // *** ContentUrl ***
    
    @Type( base = Function.class )
    @Label( standard = "content URL" )
    @Required
    @Enablement( expr = "${ ContentSourceType == 'REMOTE' }" )
    @XmlValueBinding( path = "url", removeNodeOnSetIfNull = false )
    
    ValueProperty PROP_CONTENT_URL = new ValueProperty( TYPE, "ContentUrl" );
    
    Value<Function> getContentUrl();
    void setContentUrl( String value );
    void setContentUrl( Function value );
    
    // *** Content ***
    
    @Type( base = Function.class )
    @Label( standard = "content" )
    @Required
    @Enablement( expr = "${ ContentSourceType == 'EMBEDDED' }" )
    @LongString
    @XmlValueBinding( path = "content", removeNodeOnSetIfNull = false )
    
    ValueProperty PROP_CONTENT = new ValueProperty( TYPE, "Content" );
    
    Value<Function> getContent();
    void setContent( String value );
    void setContent( Function value );
    
    // *** Fragment ***
    
    @Type( base = Boolean.class )
    @Label( standard = "fragment" )
    @DefaultValue( text = "false" )
    @Enablement( expr = "${ ContentSourceType == 'EMBEDDED' }" )
    @XmlValueBinding( path = "fragment", mapExistanceToValue = "true" )
    
    ValueProperty PROP_FRAGMENT = new ValueProperty( TYPE, "Fragment" );
    
    Value<Boolean> getFragment();
    void setFragment( String value );
    void setFragment( Boolean value );

    // *** ShowBorder ***
    
    @Type( base = Boolean.class )
    @Label( standard = "show border" )
    @DefaultValue( text = "false" )
    @XmlValueBinding( path = "show-border", mapExistanceToValue = "true" )
    
    ValueProperty PROP_SHOW_BORDER = new ValueProperty( TYPE, "ShowBorder" );
    
    Value<Boolean> getShowBorder();
    void setShowBorder( String value );
    void setShowBorder( Boolean value );
    
    // *** Height ***
    
    @Type( base = Integer.class )
    @Label( standard = "height" )
    @DefaultValue( text = "150" )
    @XmlBinding( path = "height" )
    
    ValueProperty PROP_HEIGHT = new ValueProperty( TYPE, "Height" );
    
    Value<Integer> getHeight();
    void setHeight( String value );
    void setHeight( Integer value );
    
}
