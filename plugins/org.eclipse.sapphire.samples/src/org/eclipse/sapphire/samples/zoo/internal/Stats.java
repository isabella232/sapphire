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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementListController;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.xml.ModelElementForXml;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.samples.zoo.IAnimal;
import org.eclipse.sapphire.samples.zoo.IAnimalCountForTypeEntry;
import org.eclipse.sapphire.samples.zoo.IStats;
import org.eclipse.sapphire.samples.zoo.IZooModel;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Stats

    extends ModelElementForXml
    implements IStats
    
{
    public ModelElementList<IAnimalCountForTypeEntry> animalsCountByType;
    
    public Stats( final IModelElement parentElement,
                  final ModelProperty parentProperty )
    {
        super( TYPE, parentElement, parentProperty, (XmlElement) null );
    }

    public Value<Integer> getAnimalsCount()
    {
        final int count = ( (IZooModel) this.model ).getAnimals().size();
        return new Value<Integer>( this, PROP_ANIMALS_COUNT, String.valueOf( count ) );
    }

    public ModelElementList<IAnimalCountForTypeEntry> getAnimalsCountByType()
    {
        synchronized( this.model )
        {
            if( this.animalsCountByType == null )
            {
                final ModelElementListController<IAnimalCountForTypeEntry> controller = new ModelElementListController<IAnimalCountForTypeEntry>()
                {
                    @Override
                    public List<IAnimalCountForTypeEntry> refresh( final List<IAnimalCountForTypeEntry> contents )
                    {
                        synchronized( getModel() )
                        {
                            final Map<String,Integer> counts = new TreeMap<String,Integer>();
                            
                            for( IAnimal animal : ( (IZooModel) getModel() ).getAnimals() )
                            {
                                final String type = animal.getModelElementType().getLabel( false, CapitalizationType.FIRST_WORD_ONLY, false );
                                final Integer c = counts.get( type );
                                counts.put( type, ( c == null ? 1 : c + 1 ) );
                            }
                            
                            final List<IAnimalCountForTypeEntry> newContents = new ArrayList<IAnimalCountForTypeEntry>();
                            
                            for( Map.Entry<String,Integer> entry : counts.entrySet() )
                            {
                                final String type = entry.getKey();
                                final Integer count = entry.getValue();
                                
                                IAnimalCountForTypeEntry x = null;
                                
                                for( IAnimalCountForTypeEntry y : contents )
                                {
                                    if( type.equals( y.getType().getContent() ) && count.equals( y.getCount().getContent() ) )
                                    {
                                        x = y;
                                    }
                                }
                                
                                if( x == null )
                                {
                                    x = new AnimalCountForTypeEntry( Stats.this, PROP_ANIMALS_COUNT_BY_TYPE, type, count );
                                }
                                
                                newContents.add( x );
                            }
                            
                            return newContents;
                        }
                    }
                };

                this.animalsCountByType = new ModelElementList<IAnimalCountForTypeEntry>( this, PROP_ANIMALS_COUNT_BY_TYPE );

                controller.init( this, PROP_ANIMALS_COUNT_BY_TYPE, this.animalsCountByType, new String[ 0 ] );
                this.animalsCountByType.init( controller );
                
                return this.animalsCountByType;
            }
            
            this.animalsCountByType.refresh();
            
            return this.animalsCountByType;
        }
    }
    
}
