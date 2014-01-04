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

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.MarginPresentation;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "composite" )
@Image( path = "CompositeDef.png" )
@XmlBinding( path = "composite" )

public interface CompositeDef extends FormDef, MarginPresentation
{
    ElementType TYPE = new ElementType( CompositeDef.class );
    
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
    
    // *** Margin ***
    
    @DefaultValue( text = "${ ScrollVertically || ScrollHorizontally ? 10 : 0 }")
    
    ValueProperty PROP_MARGIN = new ValueProperty( TYPE, MarginPresentation.PROP_MARGIN );
    
}
