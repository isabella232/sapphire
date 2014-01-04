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
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "split form section" )
@Image( path = "SplitFormSectionDef.png" )

public interface SplitFormSectionDef extends CompositeDef
{
    ElementType TYPE = new ElementType( SplitFormSectionDef.class );
    
    // *** Weight ***
    
    @Type( base = Integer.class )
    @Label( standard = "weight" )
    @DefaultValue( text = "1" )
    @NumericRange( min = "1" )
    @XmlBinding( path = "weight" )
    
    ValueProperty PROP_WEIGHT = new ValueProperty( TYPE, "Weight" );
    
    Value<Integer> getWeight();
    void setWeight( String value );
    void setWeight( Integer value );
    
}
