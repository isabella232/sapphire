/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [44319] Image specification for diagram parts inconsistent with the rest of sdef 
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public interface IDiagramNodeImageDef extends IDiagramDimension 
{
    ElementType TYPE = new ElementType( IDiagramNodeImageDef.class );

    // *** ImagePath ***
    
    @Type( base = Function.class )
    @Label( standard = "image path" )
    @XmlBinding( path = "path" )
    
    ValueProperty PROP_IMAGE = new ValueProperty( TYPE, "Image" );
    
    Value<Function> getImage();
    void setImage( String value );
    void setImage( Function value );

    // *** Placement ***
    
    @Type( base = ImagePlacement.class )
    @Label( standard = "placement")
    @Localizable
    @XmlBinding( path = "placement" )
    @DefaultValue( text = "top" )
    
    ValueProperty PROP_PLACEMENT = new ValueProperty( TYPE, "Placement" );
    
    Value<ImagePlacement> getPlacement();
    void setPlacement( String value );
    void setPlacement( ImagePlacement value );
    
}
