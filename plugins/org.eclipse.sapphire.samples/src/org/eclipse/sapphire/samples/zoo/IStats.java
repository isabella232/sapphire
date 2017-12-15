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

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.ReadOnly;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public interface IStats

    extends IModelElementForXml

{
    ModelElementType TYPE = new ModelElementType( IStats.class );

    // *** AnimalsCount ***
    
    @Type( base = Integer.class )
    @Label( standard = "animals count" )
    @ReadOnly

    ValueProperty PROP_ANIMALS_COUNT = new ValueProperty( TYPE, "AnimalsCount" );

    Value<Integer> getAnimalsCount();
    
    // *** AnimalsCountByType ***
    
    @Label( standard = "animals count by type" )
    @Type( base = IAnimalCountForTypeEntry.class )
    @ReadOnly
    
    ListProperty PROP_ANIMALS_COUNT_BY_TYPE = new ListProperty( TYPE, "AnimalsCountByType" );
    
    ModelElementList<IAnimalCountForTypeEntry> getAnimalsCountByType();
}
