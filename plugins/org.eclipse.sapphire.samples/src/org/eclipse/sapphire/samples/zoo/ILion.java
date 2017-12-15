/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.samples.zoo;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.EnabledByBooleanProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "lion" )
@Image( small = "org.eclipse.sapphire.samples/images/lion.png" )
@GenerateXmlBinding

public interface ILion

    extends IAnimal
    
{
    ModelElementType TYPE = new ModelElementType( ILion.class );
    
    // *** Name ***

    @XmlBinding( path = "lion-name" )

    ValueProperty PROP_NAME = new ValueProperty( TYPE, IAnimal.PROP_NAME );

    // *** ManePresent ***

    @Type( base = Boolean.class )
    @Label( standard = "mane is present" )
    @XmlBinding( path = "mane-present" )

    ValueProperty PROP_MANE_PRESENT = new ValueProperty( TYPE, "ManePresent" );

    Value<Boolean> isManePresent();
    void setManePresent( String manePresent );
    void setManePresent( Boolean manePresent );

    // *** ManeColor ***

    @Label( standard = "color of mane" )
    @EnabledByBooleanProperty( "ManePresent" )
    @XmlBinding( path = "mane-color" )
    
    ValueProperty PROP_MANE_COLOR = new ValueProperty( TYPE, "ManeColor" );
    
    Value<String> getManeColor();
    void setManeColor( String maneColor );
}
