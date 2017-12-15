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

import java.util.Collections;

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.xml.IModelElementForXml;
import org.eclipse.sapphire.modeling.xml.ModelElementListControllerForXml;
import org.eclipse.sapphire.modeling.xml.XmlElement;
import org.eclipse.sapphire.samples.zoo.IAnimalEnclosure;
import org.eclipse.sapphire.samples.zoo.IRestroomBuilding;
import org.eclipse.sapphire.samples.zoo.IStructure;
import org.eclipse.sapphire.samples.zoo.IZooModel;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class StructuresModelElementListController

    extends ModelElementListControllerForXml<IStructure>
{
    private static final String EL_STRUCTURES = "structures";
    private static final String EL_STRUCTURE = "structure";
    private static final String EL_ANIMAL_ENCLOSURE = "animal-enclosure";
    private static final String EL_RESTROOM_BUILDING = "restroom-building";
    
    public StructuresModelElementListController()
    {
        super( Collections.singleton( EL_STRUCTURE ) );
    }

    public IStructure createNewElement( final ModelElementType type )
    {
        validateEdit();

        final XmlElement element = getParentXmlElement( true ).addChildElement( EL_STRUCTURE );
        
        if( type == IAnimalEnclosure.TYPE )
        {
            element.addChildElement( EL_ANIMAL_ENCLOSURE );
        }
        else if( type == IRestroomBuilding.TYPE )
        {
            element.addChildElement( EL_RESTROOM_BUILDING );
        }
        else
        {
            throw new IllegalArgumentException();
        }
        
        return wrap( element );
    }
    
    @Override
    protected IStructure wrap( final XmlElement element )
    {
        if( element.getChildElement( EL_ANIMAL_ENCLOSURE, false ) != null )
        {
            return new AnimalEnclosure( getList(), IZooModel.PROP_STRUCTURES, element );
        }
        else if( element.getChildElement( EL_RESTROOM_BUILDING, false ) != null )
        {
            return new RestroomBuilding( getList(), IZooModel.PROP_STRUCTURES, element );
        }
        else
        {
            return new AnimalEnclosure( getList(), IZooModel.PROP_STRUCTURES, element );
        }
    }
    
    @Override
    protected XmlElement getParentXmlElement( final boolean createIfNecessary )
    {
        final IModelElementForXml listParent = (IModelElementForXml) getModelElement();
        XmlElement parent = listParent.getXmlElement( createIfNecessary );
        
        if( parent != null )
        {
            parent = parent.getChildElement( EL_STRUCTURES, createIfNecessary );
        }
        
        return parent;
    }

}
