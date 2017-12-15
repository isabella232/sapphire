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

package org.eclipse.sapphire.samples.gallery;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.PossibleValues;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.gallery.internal.ColorValueImageService;
import org.eclipse.sapphire.samples.gallery.internal.ColorValueLabelService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IMultiSelectListGalleryStringItem

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IMultiSelectListGalleryStringItem.class );
    
    // *** Item ***
    
    @Label( standard = "color" )
    @XmlBinding( path = "" )
    @NoDuplicates
    @PossibleValues( values = { "red", "orange", "yellow", "green", "blue", "violet" }, invalidValueMessage = "{0} is not a valid color." )
    @Services( { @Service( impl = ColorValueLabelService.class ), @Service( impl = ColorValueImageService.class ) } )

    ValueProperty PROP_ITEM = new ValueProperty( TYPE, "Item" );
    
    Value<String> getItem();
    void setItem( String value );
    
}
