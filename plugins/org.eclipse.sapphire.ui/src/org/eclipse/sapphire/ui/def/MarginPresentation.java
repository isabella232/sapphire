/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface MarginPresentation extends Element 
{
	ElementType TYPE = new ElementType( MarginPresentation.class );
	
    // *** Margin ***
    
    @Type( base = Integer.class )
    @Label( standard = "margin" )
    @XmlBinding( path = "margin" )
    @DefaultValue( text = "0" )
    
    ValueProperty PROP_MARGIN = new ValueProperty( TYPE, "Margin" );
    
    Value<Integer> getMargin();
    void setMargin( String value );
    void setMargin( Integer value );
    
    // *** MarginVertical ***
    
    @Type( base = Integer.class )
    @Label( standard = "vertical margin" )
    @XmlBinding( path = "margin-vertical" )
    @DefaultValue( text = "${ Margin }" )
    
    ValueProperty PROP_MARGIN_VERTICAL = new ValueProperty( TYPE, "MarginVertical" );
    
    Value<Integer> getMarginVertical();
    void setMarginVertical( String value );
    void setMarginVertical( Integer value );

    // *** MarginHorizontal ***
    
    @Type( base = Integer.class )
    @Label( standard = "horizontal margin" )
    @XmlBinding( path = "margin-horizontal" )
    @DefaultValue( text = "${ Margin }" )
    
    ValueProperty PROP_MARGIN_HORIZONTAL = new ValueProperty( TYPE, "MarginHorizontal" );
    
    Value<Integer> getMarginHorizontal();
    void setMarginHorizontal( String value );
    void setMarginHorizontal( Integer value );

    // *** MarginTop ***
    
    @Type( base = Integer.class )
    @Label( standard = "top margin" )
    @XmlBinding( path = "margin-top" )
    @DefaultValue( text = "${ MarginVertical }" )
    
    ValueProperty PROP_MARGIN_TOP = new ValueProperty( TYPE, "MarginTop" );
    
    Value<Integer> getMarginTop();
    void setMarginTop( String value );
    void setMarginTop( Integer value );
    
    // *** MarginBottom ***
    
    @Type( base = Integer.class )
    @Label( standard = "bottom margin" )
    @XmlBinding( path = "margin-bottom" )
    @DefaultValue( text = "${ MarginVertical }" )
    
    ValueProperty PROP_MARGIN_BOTTOM = new ValueProperty( TYPE, "MarginBottom" );
    
    Value<Integer> getMarginBottom();
    void setMarginBottom( String value );
    void setMarginBottom( Integer value );

    // *** MarginLeft ***
    
    @Type( base = Integer.class )
    @Label( standard = "left margin" )
    @XmlBinding( path = "margin-left" )
    @DefaultValue( text = "${ MarginHorizontal }" )
    
    ValueProperty PROP_MARGIN_LEFT = new ValueProperty( TYPE, "MarginLeft" );
    
    Value<Integer> getMarginLeft();
    void setMarginLeft( String value );
    void setMarginLeft( Integer value );
    
    // *** MarginRight ***
    
    @Type( base = Integer.class )
    @Label( standard = "right margin" )
    @XmlBinding( path = "margin-right" )
    @DefaultValue( text = "${ MarginHorizontal }" )
    
    ValueProperty PROP_MARGIN_RIGHT = new ValueProperty( TYPE, "MarginRight" );
    
    Value<Integer> getMarginRight();
    void setMarginRight( String value );
    void setMarginRight( Integer value );
    
}
