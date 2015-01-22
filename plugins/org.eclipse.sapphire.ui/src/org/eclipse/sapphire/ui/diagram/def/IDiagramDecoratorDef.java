/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - [342897] Integrate with properties view
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.HorizontalAlignment;
import org.eclipse.sapphire.ui.def.VerticalAlignment;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public interface IDiagramDecoratorDef extends Element 
{
    ElementType TYPE = new ElementType( IDiagramDecoratorDef.class );
    
    // *** DecoratorPlacement ***
    
    @Type( base = DecoratorPlacement.class )
    @Label( standard = "placement")
    @DefaultValue( text = "image" )
    @XmlBinding( path = "decorator-placement" )
    
    ValueProperty PROP_DECORATOR_PLACEMENT = new ValueProperty( TYPE, "DecoratorPlacement" );
    
    Value<DecoratorPlacement> getDecoratorPlacement();
    void setDecoratorPlacement( String value );
    void setDecoratorPlacement( DecoratorPlacement value ) ;

    // *** HorizontalAlignment ***
    
    @Type( base = HorizontalAlignment.class )
    @Label( standard = "horizontal alignment")
    @DefaultValue( text = "left" )
    @XmlBinding( path = "horizontal-align" )
    
    ValueProperty PROP_HORIZONTAL_ALIGNMENT = new ValueProperty( TYPE, "HorizontalAlignment" );
    
    Value<HorizontalAlignment> getHorizontalAlignment();
    void setHorizontalAlignment( String value );
    void setHorizontalAlignment( HorizontalAlignment value ) ;
    
    // *** VerticalAlignment ***
    
    @Type( base = VerticalAlignment.class )
    @Label( standard = "vertical alignment")
    @DefaultValue( text = "bottom" )
    @XmlBinding( path = "vertical-align" )
    
    ValueProperty PROP_VERTICAL_ALIGNMENT = new ValueProperty( TYPE, "VerticalAlignment" );
    
    Value<VerticalAlignment> getVerticalAlignment();
    void setVerticalAlignment( String value );
    void setVerticalAlignment( VerticalAlignment value ) ;
    
    // *** VerticalMargin ***
    
    @Type( base = Integer.class )
    @Label( standard = "vertical margin" )
    @DefaultValue( text = "0" )
    @XmlBinding( path = "vertical-margin" )
    
    ValueProperty PROP_VERTICAL_MARGIN = new ValueProperty( TYPE, "VerticalMargin" );
    
    Value<Integer> getVerticalMargin();
    void setVerticalMargin( String value );
    void setVerticalMargin( Integer value );
    
    // *** HorizontalMargin ***
    
    @Type( base = Integer.class )
    @Label( standard = "horizontal margin" )
    @DefaultValue( text = "0" )
    @XmlBinding( path = "horizontal-margin" )
    
    ValueProperty PROP_HORIZONTAL_MARGIN = new ValueProperty( TYPE, "HorizontalMargin" );
    
    Value<Integer> getHorizontalMargin();
    void setHorizontalMargin( String value );
    void setHorizontalMargin( Integer value );
}
