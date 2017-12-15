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
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.serialization.ValueSerialization;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.samples.gallery.internal.RectangleSerializationService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface ICustomValueGallery

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ICustomValueGallery.class );
    
    // *** Rectangle ***
    
    @Type( base = Rectangle.class )
    @Label( standard = "rectangle" )
    @ValueSerialization( service = RectangleSerializationService.class )
    @XmlBinding( path = "rectangle" )
    
    ValueProperty PROP_RECTANGLE = new ValueProperty( TYPE, "Rectangle" );
    
    Value<Rectangle> getRectangle();
    void setRectangle( String value );
    void setRectangle( Rectangle value );
    
    // *** Circle ***
    
    @Type( base = Circle.class )
    @Label( standard = "circle" )
    @XmlBinding( path = "circle" )
    
    ValueProperty PROP_CIRCLE = new ValueProperty( TYPE, "Circle" );
    
    Value<Circle> getCircle();
    void setCircle( String value );
    void setCircle( Circle value );
    
}
