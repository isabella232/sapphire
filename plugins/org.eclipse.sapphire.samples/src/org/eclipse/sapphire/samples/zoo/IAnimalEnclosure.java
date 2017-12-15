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
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NumericRange;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "animal enclosure" )
@Image( small = "org.eclipse.sapphire.samples/images/enclosure.png" )
@GenerateXmlBinding

public interface IAnimalEnclosure

    extends IStructure
    
{
    ModelElementType TYPE = new ModelElementType( IAnimalEnclosure.class );
    
    // *** Type ***

    @Type( base = AnimalEnclosureType.class )
    @Label( standard = "type" )
    @DefaultValue( "OUTDOOR" )
    @XmlBinding( path = "animal-enclosure/type" )

    ValueProperty PROP_TYPE = new ValueProperty( TYPE, "Type" );

    Value<AnimalEnclosureType> getType();
    void setType( String type );
    void setType( AnimalEnclosureType type );

    // *** Size ***

    @Type( base = Integer.class )
    @Label( standard = "size" )
    @NumericRange( min = "1" )
    @XmlBinding( path = "animal-enclosure/size" )

    ValueProperty PROP_SIZE = new ValueProperty( TYPE, "Size" );

    Value<Integer> getSize();
    void setSize( String size );
    void setSize( Integer size );
}
