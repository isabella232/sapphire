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
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "giraffe" )
@Image( small = "org.eclipse.sapphire.samples/images/camel.png" )
@GenerateXmlBinding

public interface IGiraffe

    extends IAnimal
    
{
    ModelElementType TYPE = new ModelElementType( IGiraffe.class );
    
    // *** Name ***

    @Label( standard = "giraffe name" )

    ValueProperty PROP_NAME = new ValueProperty( TYPE, IAnimal.PROP_NAME );

    // *** SpotCount ***

    @Type( base = Integer.class )
    @Label( standard = "number of spots" )
    @NumericRange( min = "0" )
    @XmlBinding( path = "spot-count" )

    ValueProperty PROP_SPOT_COUNT = new ValueProperty( TYPE, "SpotCount" );

    Value<Integer> getSpotCount();
    void setSpotCount( String spotCount );
    void setSpotCount( Integer spotCount );
}
