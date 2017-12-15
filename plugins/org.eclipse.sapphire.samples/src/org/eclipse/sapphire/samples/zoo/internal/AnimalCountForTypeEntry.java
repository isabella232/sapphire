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

package org.eclipse.sapphire.samples.zoo.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.xml.ModelElementForXml;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.samples.zoo.IAnimalCountForTypeEntry;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class AnimalCountForTypeEntry

    extends ModelElementForXml
    implements IAnimalCountForTypeEntry
    
{
    private final Value<String> type;
    private final Value<Integer> count;
    
    public AnimalCountForTypeEntry( final IModelElement parentElement,
                                    final ModelProperty parentProperty,
                                    final String type,
                                    final int count )
    {
        super( TYPE, parentElement, parentProperty, (XmlElement) null );
        
        this.type = new Value<String>( this, PROP_TYPE, type );
        this.count = new Value<Integer>( this, PROP_COUNT, String.valueOf( count ) );
    }

    public Value<String> getType()
    {
        return this.type;
    }
    
    public Value<Integer> getCount()
    {
        return this.count;
    }

}
