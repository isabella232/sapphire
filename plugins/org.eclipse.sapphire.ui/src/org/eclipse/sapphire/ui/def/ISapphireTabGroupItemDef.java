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

package org.eclipse.sapphire.ui.def;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NonNullValue;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "tab group item" )
@GenerateImpl

public interface ISapphireTabGroupItemDef

    extends ISapphireCompositeDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireTabGroupItemDef.class );
    
    // *** Label ***
    
    @Label( standard = "label" )
    @NonNullValue
    @Localizable
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String label );
    
    // *** ImagePath ***
    
    @Reference( target = ImageDescriptor.class )
    @Label( standard = "image path" )
    @XmlBinding( path = "image" )
    
    ValueProperty PROP_IMAGE_PATH = new ValueProperty( TYPE, "ImagePath" );
    
    ReferenceValue<ImageDescriptor> getImagePath();
    void setImagePath( String imagePath );
    
}
