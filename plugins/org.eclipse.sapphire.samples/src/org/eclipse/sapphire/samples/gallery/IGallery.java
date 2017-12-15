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
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlRootBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl
@XmlRootBinding( elementName = "gallery" )

public interface IGallery

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( IGallery.class );
    
    // *** IntegerValueGallery ***
    
    @Type( base = IIntegerValueGallery.class )
    @XmlBinding( path = "integer" )

    ImpliedElementProperty PROP_INTEGER_VALUE_GALLERY = new ImpliedElementProperty( TYPE, "IntegerValueGallery" );
    
    IIntegerValueGallery getIntegerValueGallery();
    
    // *** LongIntegerValueGallery ***
    
    @Type( base = ILongIntegerValueGallery.class )
    @XmlBinding( path = "long-integer" )

    ImpliedElementProperty PROP_LONG_INTEGER_VALUE_GALLERY = new ImpliedElementProperty( TYPE, "LongIntegerValueGallery" );
    
    ILongIntegerValueGallery getLongIntegerValueGallery();
    
    // *** BigIntegerValueGallery ***

    @Type( base = IBigIntegerValueGallery.class )
    @XmlBinding( path = "big-integer" )
    
    ImpliedElementProperty PROP_BIG_INTEGER_VALUE_GALLERY = new ImpliedElementProperty( TYPE, "BigIntegerValueGallery" );
    
    IBigIntegerValueGallery getBigIntegerValueGallery();
    
    // *** FloatValueGallery ***

    @Type( base = IFloatValueGallery.class )
    @XmlBinding( path = "float" )
    
    ImpliedElementProperty PROP_FLOAT_VALUE_GALLERY = new ImpliedElementProperty( TYPE, "FloatValueGallery" );
    
    IFloatValueGallery getFloatValueGallery();
    
    // *** DoubleValueGallery ***

    @Type( base = IDoubleValueGallery.class )
    @XmlBinding( path = "double" )
    
    ImpliedElementProperty PROP_DOUBLE_VALUE_GALLERY = new ImpliedElementProperty( TYPE, "DoubleValueGallery" );
    
    IDoubleValueGallery getDoubleValueGallery();
    
    // *** BigDecimalValueGallery ***

    @Type( base = IBigDecimalValueGallery.class )
    @XmlBinding( path = "big-decimal" )
    
    ImpliedElementProperty PROP_BIG_DECIMAL_VALUE_GALLERY = new ImpliedElementProperty( TYPE, "BigDecimalValueGallery" );
    
    IBigDecimalValueGallery getBigDecimalValueGallery();
    
    // *** EnumValueGallery ***

    @Type( base = IEnumValueGallery.class )
    @XmlBinding( path = "enum" )
    
    ImpliedElementProperty PROP_ENUM_VALUE_GALLERY = new ImpliedElementProperty( TYPE, "EnumValueGallery" );
    
    IEnumValueGallery getEnumValueGallery();
    
    // *** JavaTypeNameValueGallery ***

    @Type( base = IJavaTypeNameValueGallery.class )
    @XmlBinding( path = "java-type-name" )
    
    ImpliedElementProperty PROP_JAVA_TYPE_NAME_VALUE_GALLERY = new ImpliedElementProperty( TYPE, "JavaTypeNameValueGallery" );
    
    IJavaTypeNameValueGallery getJavaTypeNameValueGallery();
    
    // *** CustomValueGallery ***
    
    @Type( base = ICustomValueGallery.class )
    @XmlBinding( path = "custom-value" )
    
    ImpliedElementProperty PROP_CUSTOM_VALUE_GALLERY = new ImpliedElementProperty( TYPE, "CustomValueGallery" );
    
    ICustomValueGallery getCustomValueGallery();
    
    // *** BrowseSupportGallery ***
    
    @Type( base = IBrowseSupportGallery.class )
    @Label( standard = "browse support gallery" )
    @XmlBinding( path = "browse-support" )
    
    ImpliedElementProperty PROP_BROWSE_SUPPORT_GALLERY = new ImpliedElementProperty( TYPE, "BrowseSupportGallery" );
    
    IBrowseSupportGallery getBrowseSupportGallery();
    
    // *** HomogeneousList ***
    
    @Type( base = IListItem.class )
    @Label( standard = "homogeneous list")
    @XmlListBinding( path = "homogeneous-list", mappings = @XmlListBinding.Mapping( element = "item", type = IListItem.class ) )
    
    ListProperty PROP_HOMOGENEOUS_LIST = new ListProperty( TYPE, "HomogeneousList" );
    
    ModelElementList<IListItem> getHomogeneousList();
    
    // *** HeterogeneousList ***
    
    @Type( base = IListItem.class, possible = { IListItem.class, IListItemWithInteger.class, IListItemWithEnum.class } )
    @Label( standard = "heterogeneous list" )
    
    @XmlListBinding
    (
        path = "heterogeneous-list", 
        mappings = 
        {
            @XmlListBinding.Mapping( element = "item", type = IListItem.class ),
            @XmlListBinding.Mapping( element = "item-with-integer", type = IListItemWithInteger.class ),
            @XmlListBinding.Mapping( element = "item-with-enum", type = IListItemWithEnum.class )
        }
    )
    
    ListProperty PROP_HETEROGENEOUS_LIST = new ListProperty( TYPE, "HeterogeneousList" );
    
    ModelElementList<IListItem> getHeterogeneousList();

    // *** MultiSelectListGallery ***
    
    @Type( base = IMultiSelectListGallery.class )
    @Label( standard = "multi select list gallery" )
    @XmlBinding( path = "multi-select-list" )
    
    ImpliedElementProperty PROP_MULTI_SELECT_LIST_GALLERY = new ImpliedElementProperty( TYPE, "MultiSelectListGallery" );
    
    IMultiSelectListGallery getMultiSelectListGallery();
    
    // *** ValuePropertyActionsGallery ***
    
    @Type( base = IValuePropertyActionsGallery.class )
    @Label( standard = "value property actions gallery" )
    @XmlBinding( path = "value-property-actions-gallery" )
    
    ImpliedElementProperty PROP_VALUE_PROPERTY_ACTIONS_GALLERY = new ImpliedElementProperty( TYPE, "ValuePropertyActionsGallery" );
    
    IValuePropertyActionsGallery getValuePropertyActionsGallery();

    // *** IHelpGallery ***
    
    @Type( base = IHelpGallery.class )
    @XmlBinding( path = "help" )

    ImpliedElementProperty PROP_HELP_GALLERY = new ImpliedElementProperty( TYPE, "HelpGallery" );
    
    IHelpGallery getHelpGallery();

    // *** IExtendedHelpGallery ***
    
    @Type( base = IExtendedHelpGallery.class )
    @XmlBinding( path = "extended-help" )

    ImpliedElementProperty PROP_EXTENDED_HELP_GALLERY = new ImpliedElementProperty( TYPE, "ExtendedHelpGallery" );
    
    IExtendedHelpGallery getExtendedHelpGallery();

    // *** IEnablementGallery ***
    
    @Type( base = IEnablementGallery.class )
    @XmlBinding( path = "enablement" )

    ImpliedElementProperty PROP_ENABLEMENT_GALLERY = new ImpliedElementProperty( TYPE, "EnablementGallery" );
    
    IEnablementGallery getEnablementGallery();
    
}
