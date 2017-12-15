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

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "composite" )
@GenerateImpl

public interface CompositeDef extends FormDef
{
    ModelElementType TYPE = new ModelElementType( CompositeDef.class );
    
    // *** Indent ***
    
    @Type( base = Boolean.class )
    @Label( standard = "indent" )
    @DefaultValue( text = "false" )
    @XmlBinding( path = "indent" )
    
    ValueProperty PROP_INDENT = new ValueProperty( TYPE, "Indent" );
    
    Value<Boolean> getIndent();
    void setIndent( String value );
    void setIndent( Boolean value );
    
    // *** Width ***
    
    @Type( base = Integer.class )
    @Label( standard = "width" )
    @XmlBinding( path = "width" )
    
    @Documentation
    (
        content = "Specifies the preferred width (in pixels) for the composite. The width preference " +
                  "will be respected to the extent that it is feasible."
    )
    
    ValueProperty PROP_WIDTH = new ValueProperty( TYPE, "Width" );
    
    Value<Integer> getWidth();
    void setWidth( String value );
    void setWidth( Integer value );
    
    // *** Height ***
    
    @Type( base = Integer.class )
    @Label( standard = "height" )
    @XmlBinding( path = "height" )
    
    @Documentation
    (
        content = "Specifies the preferred height (in pixels) for the composite. The height preference " +
                  "will be respected to the extent that it is feasible."
    )
    
    ValueProperty PROP_HEIGHT = new ValueProperty( TYPE, "Height" );
    
    Value<Integer> getHeight();
    void setHeight( String value );
    void setHeight( Integer value );

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
    @DefaultValue( text = "${ MarginWidth }" )
    
    ValueProperty PROP_MARGIN_LEFT = new ValueProperty( TYPE, "MarginLeft" );
    
    Value<Integer> getMarginLeft();
    void setMarginLeft( String value );
    void setMarginLeft( Integer value );
    
    // *** MarginRight ***
    
    @Type( base = Integer.class )
    @Label( standard = "right margin" )
    @XmlBinding( path = "margin-right" )
    @DefaultValue( text = "${ MarginWidth }" )
    
    ValueProperty PROP_MARGIN_RIGHT = new ValueProperty( TYPE, "MarginRight" );
    
    Value<Integer> getMarginRight();
    void setMarginRight( String value );
    void setMarginRight( Integer value );
    
    // *** MarginTop ***
    
    @Type( base = Integer.class )
    @Label( standard = "top margin" )
    @XmlBinding( path = "margin-top" )
    @DefaultValue( text = "${ MarginHeight }" )
    
    ValueProperty PROP_MARGIN_TOP = new ValueProperty( TYPE, "MarginTop" );
    
    Value<Integer> getMarginTop();
    void setMarginTop( String value );
    void setMarginTop( Integer value );
    
    // *** MarginBottom ***
    
    @Type( base = Integer.class )
    @Label( standard = "bottom margin" )
    @XmlBinding( path = "margin-bottom" )
    @DefaultValue( text = "${ MarginHeight }" )
    
    ValueProperty PROP_MARGIN_BOTTOM = new ValueProperty( TYPE, "MarginBottom" );
    
    Value<Integer> getMarginBottom();
    void setMarginBottom( String value );
    void setMarginBottom( Integer value );
    
    // *** MarginWidth ***
    
    @Type( base = Integer.class )
    @Label( standard = "margin width" )
    @XmlBinding( path = "margin-width" )
    @DefaultValue( text = "${ ScrollVertically || ScrollHorizontally ? 10 : 0 }")
    
    ValueProperty PROP_MARGIN_WIDTH = new ValueProperty( TYPE, "MarginWidth" );
    
    Value<Integer> getMarginWidth();
    void setMarginWidth( String value );
    void setMarginWidth( Integer value );
    
    // *** MarginHeight ***
    
    @Type( base = Integer.class )
    @Label( standard = "margin height" )
    @XmlBinding( path = "margin-height" )
    @DefaultValue( text = "${ ScrollVertically || ScrollHorizontally ? 10 : 0 }")
    
    ValueProperty PROP_MARGIN_HEIGHT = new ValueProperty( TYPE, "MarginHeight" );
    
    Value<Integer> getMarginHeight();
    void setMarginHeight( String value );
    void setMarginHeight( Integer value );

}
