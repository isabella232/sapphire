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
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface FormPartDef extends PartDef
{
    ModelElementType TYPE = new ModelElementType( FormPartDef.class );
    
    // *** ScaleVertically ***
    
    @Type( base = Boolean.class )
    @Label( standard = "scale vertically" )
    @DefaultValue( text = "false" )
    @XmlBinding( path = "scale-vertically" )
    
    ValueProperty PROP_SCALE_VERTICALLY = new ValueProperty( TYPE, "ScaleVertically" );
    
    Value<Boolean> getScaleVertically();
    void setScaleVertically( String value );
    void setScaleVertically( Boolean value );
    
}
