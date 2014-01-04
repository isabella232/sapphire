/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.shape.def;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.MarginPresentation;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@Label( standard = "sequence layout" )

public interface SequenceLayoutDef extends ShapeLayoutDef, MarginPresentation
{
	ElementType TYPE = new ElementType( SequenceLayoutDef.class );
    
	// *** Orientation ***
    
    @Type( base = SequenceLayoutOrientation.class )
    @Label( standard = "orientation" )
    @Localizable
    @XmlBinding( path = "orientation" )
    @DefaultValue( text = "vertical" )
    
    ValueProperty PROP_ORIENTATION = new ValueProperty( TYPE, "Orientation" );
    
    Value<SequenceLayoutOrientation> getOrientation();
    void setOrientation( String value );
    void setOrientation( SequenceLayoutOrientation value );
    
    // *** Spacing ***
        
    @Type( base = Integer.class )
    @Label( standard = "spacing" )
    @XmlBinding( path = "spacing" )
    @DefaultValue( text = "5" )
    @Enablement( expr = "${ Orientation == 'horizontal' || Orientation == 'vertical' }" )
    
    ValueProperty PROP_SPACING = new ValueProperty( TYPE, "Spacing" );
    
    Value<Integer> getSpacing();
    void setSpacing( String value );
    void setSpacing( Integer value );
    
    // *** Margin ***

    @DefaultValue( text = "5" )
    
    ValueProperty PROP_MARGIN = new ValueProperty( TYPE, MarginPresentation.PROP_MARGIN );
    
}
