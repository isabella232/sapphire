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
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.CompositeMarginLeftRightDefaultValueProvider;
import org.eclipse.sapphire.ui.def.internal.CompositeMarginTopBottomDefaultValueProvider;
import org.eclipse.sapphire.ui.def.internal.CompositeMarginWidthHeightDefaultValueProvider;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "composite" )
@GenerateImpl

public interface ISapphireCompositeDef

    extends IFormDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireCompositeDef.class );
    
    // *** Indent ***
    
    @Type( base = Boolean.class )
    @Label( standard = "indent" )
    @DefaultValue( text = "false" )
    @XmlBinding( path = "indent" )
    
    ValueProperty PROP_INDENT = new ValueProperty( TYPE, "Indent" );
    
    Value<Boolean> getIndent();
    void setIndent( String value );
    void setIndent( Boolean value );
    
    // *** ScrollVertically ***
    
    @Type( base = Boolean.class )
    @Label( standard = "scroll vertically" )
    @DefaultValue( text = "false" )
    @XmlBinding( path = "scroll-vertically" )
    
    ValueProperty PROP_SCROLL_VERTICALLY = new ValueProperty( TYPE, "ScrollVertically" );
    
    Value<Boolean> getScrollVertically();
    void setScrollVertically( String value );
    void setScrollVertically( Boolean value );
    
    // *** ScrollHorizontally ***
    
    @Type( base = Boolean.class )
    @Label( standard = "scroll horizontally" )
    @DefaultValue( text = "false" )
    @XmlBinding( path = "scroll-horizontally" )

    ValueProperty PROP_SCROLL_HORIZONTALLY = new ValueProperty( TYPE, "ScrollHorizontally" );
    
    Value<Boolean> getScrollHorizontally();
    void setScrollHorizontally( String value );
    void setScrollHorizontally( Boolean value );
    
    // *** MarginLeft ***
    
    @Type( base = Integer.class )
    @Label( standard = "left margin" )
    @XmlBinding( path = "margin-left" )
    @Service( impl = CompositeMarginLeftRightDefaultValueProvider.class )
    @DependsOn( "MarginWidth" )
    
    ValueProperty PROP_MARGIN_LEFT = new ValueProperty( TYPE, "MarginLeft" );
    
    Value<Integer> getMarginLeft();
    void setMarginLeft( String value );
    void setMarginLeft( Integer value );
    
    // *** MarginRight ***
    
    @Type( base = Integer.class )
    @Label( standard = "right margin" )
    @XmlBinding( path = "margin-right" )
    @Service( impl = CompositeMarginLeftRightDefaultValueProvider.class )
    @DependsOn( "MarginWidth" )
    
    ValueProperty PROP_MARGIN_RIGHT = new ValueProperty( TYPE, "MarginRight" );
    
    Value<Integer> getMarginRight();
    void setMarginRight( String value );
    void setMarginRight( Integer value );
    
    // *** MarginTop ***
    
    @Type( base = Integer.class )
    @Label( standard = "top margin" )
    @XmlBinding( path = "margin-top" )
    @Service( impl = CompositeMarginTopBottomDefaultValueProvider.class )
    @DependsOn( "MarginHeight" )
    
    ValueProperty PROP_MARGIN_TOP = new ValueProperty( TYPE, "MarginTop" );
    
    Value<Integer> getMarginTop();
    void setMarginTop( String value );
    void setMarginTop( Integer value );
    
    // *** MarginBottom ***
    
    @Type( base = Integer.class )
    @Label( standard = "bottom margin" )
    @XmlBinding( path = "margin-bottom" )
    @Service( impl = CompositeMarginTopBottomDefaultValueProvider.class )
    @DependsOn( "MarginHeight" )
    
    ValueProperty PROP_MARGIN_BOTTOM = new ValueProperty( TYPE, "MarginBottom" );
    
    Value<Integer> getMarginBottom();
    void setMarginBottom( String value );
    void setMarginBottom( Integer value );
    
    // *** MarginWidth ***
    
    @Type( base = Integer.class )
    @Label( standard = "margin width" )
    @XmlBinding( path = "margin-width" )
    @Service( impl = CompositeMarginWidthHeightDefaultValueProvider.class )
    @DependsOn( { "ScrollVertically", "ScrollHorizontally" } )
    
    ValueProperty PROP_MARGIN_WIDTH = new ValueProperty( TYPE, "MarginWidth" );
    
    Value<Integer> getMarginWidth();
    void setMarginWidth( String value );
    void setMarginWidth( Integer value );
    
    // *** MarginHeight ***
    
    @Type( base = Integer.class )
    @Label( standard = "margin height" )
    @XmlBinding( path = "margin-height" )
    @Service( impl = CompositeMarginWidthHeightDefaultValueProvider.class )
    @DependsOn( { "ScrollVertically", "ScrollHorizontally" } )
    
    ValueProperty PROP_MARGIN_HEIGHT = new ValueProperty( TYPE, "MarginHeight" );
    
    Value<Integer> getMarginHeight();
    void setMarginHeight( String value );
    void setMarginHeight( Integer value );

}
