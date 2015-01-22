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

import org.eclipse.sapphire.Color;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.PartDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface FormComponentDef extends PartDef
{
    ElementType TYPE = new ElementType( FormComponentDef.class );
    
    // *** ScaleVertically ***
    
    @Type( base = Boolean.class )
    @Label( standard = "scale vertically" )
    @DefaultValue( text = "false" )
    @XmlBinding( path = "scale-vertically" )
    
    ValueProperty PROP_SCALE_VERTICALLY = new ValueProperty( TYPE, "ScaleVertically" );
    
    Value<Boolean> getScaleVertically();
    void setScaleVertically( String value );
    void setScaleVertically( Boolean value );
    
    // *** BackgroundColor ***
    
    @Type( base = Color.class )
    @Label( standard = "background color" )
    @XmlBinding( path = "background-color" )
    
    ValueProperty PROP_BACKGROUND_COLOR = new ValueProperty( TYPE, "BackgroundColor" );
    
    Value<Color> getBackgroundColor();
    void setBackgroundColor( String value );
    void setBackgroundColor( Color value );
    
}
