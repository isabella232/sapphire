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

import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.IModelForXml;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBindingModelImpl;
import org.eclipse.sapphire.modeling.xml.annotations.RootXmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBindingModelImpl
@RootXmlBinding( elementName = "gallery" )

public interface IGallery

    extends IModelForXml
    
{
    ModelElementType TYPE = new ModelElementType( IGallery.class );
    
    // *** IntegerValueGallery ***
    
    @Type( base = IIntegerValueGallery.class )

    ElementProperty PROP_INTEGER_VALUE_GALLERY = new ElementProperty( TYPE, "IntegerValueGallery" );
    
    IIntegerValueGallery getIntegerValueGallery();
    
    // *** LongIntegerValueGallery ***
    
    @Type( base = ILongIntegerValueGallery.class )

    ElementProperty PROP_LONG_INTEGER_VALUE_GALLERY = new ElementProperty( TYPE, "LongIntegerValueGallery" );
    
    ILongIntegerValueGallery getLongIntegerValueGallery();
    
    // *** BigIntegerValueGallery ***

    @Type( base = IBigIntegerValueGallery.class )
    
    ElementProperty PROP_BIG_INTEGER_VALUE_GALLERY = new ElementProperty( TYPE, "BigIntegerValueGallery" );
    
    IBigIntegerValueGallery getBigIntegerValueGallery();
    
    // *** FloatValueGallery ***

    @Type( base = IFloatValueGallery.class )
    
    ElementProperty PROP_FLOAT_VALUE_GALLERY = new ElementProperty( TYPE, "FloatValueGallery" );
    
    IFloatValueGallery getFloatValueGallery();
    
    // *** DoubleValueGallery ***

    @Type( base = IDoubleValueGallery.class )
    
    ElementProperty PROP_DOUBLE_VALUE_GALLERY = new ElementProperty( TYPE, "DoubleValueGallery" );
    
    IDoubleValueGallery getDoubleValueGallery();
    
    // *** BigDecimalValueGallery ***

    @Type( base = IBigDecimalValueGallery.class )
    
    ElementProperty PROP_BIG_DECIMAL_VALUE_GALLERY = new ElementProperty( TYPE, "BigDecimalValueGallery" );
    
    IBigDecimalValueGallery getBigDecimalValueGallery();
    
    // *** EnumValueGallery ***

    @Type( base = IEnumValueGallery.class )
    
    ElementProperty PROP_ENUM_VALUE_GALLERY = new ElementProperty( TYPE, "EnumValueGallery" );
    
    IEnumValueGallery getEnumValueGallery();
    
    // *** JavaTypeNameValueGallery ***

    @Type( base = IJavaTypeNameValueGallery.class )
    
    ElementProperty PROP_JAVA_TYPE_NAME_VALUE_GALLERY = new ElementProperty( TYPE, "JavaTypeNameValueGallery" );
    
    IJavaTypeNameValueGallery getJavaTypeNameValueGallery();
    
    // *** BrowseSupportGallery ***
    
    @Type( base = IBrowseSupportGallery.class )
    @Label( standard = "browse support gallery" )
    
    ElementProperty PROP_BROWSE_SUPPORT_GALLERY = new ElementProperty( TYPE, "BrowseSupportGallery" );
    
    IBrowseSupportGallery getBrowseSupportGallery();
    
    // *** MultiSelectListGallery ***
    
    @Type( base = IMultiSelectListGallery.class )
    @Label( standard = "multi select list gallery" )
    
    ElementProperty PROP_MULTI_SELECT_LIST_GALLERY = new ElementProperty( TYPE, "MultiSelectListGallery" );
    
    IMultiSelectListGallery getMultiSelectListGallery();
    
}
