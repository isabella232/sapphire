/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def;

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.CountConstraint;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "split form" )
@GenerateImpl

public interface SplitFormDef extends FormPartDef
{
    ModelElementType TYPE = new ModelElementType( SplitFormDef.class );
    
    // *** Orientation ***
    
    @Type( base = Orientation.class )
    @Label( standard = "orientation" )
    @DefaultValue( text = "horizontal" )
    @XmlBinding( path = "orientation" )
    
    ValueProperty PROP_ORIENTATION = new ValueProperty( TYPE, "Orientation" );
    
    Value<Orientation> getOrientation();
    void setOrientation( String value );
    void setOrientation( Orientation value );
    
    // *** Blocks ***
    
    @Type( base = SplitFormBlockDef.class )
    @Label( standard = "blocks" )
    @CountConstraint( min = 2 )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "block", type = SplitFormBlockDef.class ) )
    
    ListProperty PROP_BLOCKS = new ListProperty( TYPE, "Blocks" );
    
    ModelElementList<SplitFormBlockDef> getBlocks();
    
}
