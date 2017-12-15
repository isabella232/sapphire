/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [408293] RuntimeException when selecting from multi-select list in sample
 ******************************************************************************/

package org.eclipse.sapphire.samples.gallery;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Services;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.samples.gallery.internal.ColorPossibleValuesService;
import org.eclipse.sapphire.samples.gallery.internal.ColorValueImageService;
import org.eclipse.sapphire.samples.gallery.internal.ColorValueLabelService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

@GenerateImpl

public interface ListPropertiesGallery

    extends IModelElement

{
    ModelElementType TYPE = new ModelElementType( ListPropertiesGallery.class );
    
    // *** Enabled ***

    @Type( base = Boolean.class )
    @DefaultValue( text = "true" )
    @XmlBinding( path = "enabled" )
    
    ValueProperty PROP_ENABLED = new ValueProperty( TYPE, "Enabled" );
    
    Value<Boolean> getEnabled();
    void setEnabled( String value );
    void setEnabled( Boolean value );
    
    // *** Homogeneous ***
    
    @Type( base = IChildElement.class )
    @Enablement( expr = "${ Enabled }" )
    @XmlListBinding( path = "homogeneous", mappings = @XmlListBinding.Mapping( element = "child", type = IChildElement.class ) )
    
    ListProperty PROP_HOMOGENEOUS = new ListProperty( TYPE, "Homogeneous" );
    
    ModelElementList<IChildElement> getHomogeneous();
    
    // *** HomogeneousWithJavaType ***
    
    @Type( base = IListItemWithJavaType.class )
    @Label( standard = "homogeneous list of java types")
    @Enablement( expr = "${ Enabled }" )
    @XmlListBinding( path = "homogeneous-java-types", mappings = @XmlListBinding.Mapping( element = "child", type = IListItemWithJavaType.class ) )
    
    ListProperty PROP_HOMOGENEOUS_WITH_JAVA_TYPE = new ListProperty( TYPE, "HomogeneousWithJavaType" );
    
    ModelElementList<IListItemWithJavaType> getHomogeneousWithJavaType();

    // *** Heterogeneous ***
    
    @Type( base = IChildElement.class, possible = { IChildElement.class, IChildElementWithInteger.class, IChildElementWithEnum.class } )
    @Enablement( expr = "${ Enabled }" )
    
    @XmlListBinding
    (
        path = "heterogeneous", 
        mappings = 
        {
            @XmlListBinding.Mapping( element = "child", type = IChildElement.class ),
            @XmlListBinding.Mapping( element = "child-with-integer", type = IChildElementWithInteger.class ),
            @XmlListBinding.Mapping( element = "child-with-enum", type = IChildElementWithEnum.class )
        }
    )
    
    ListProperty PROP_HETEROGENEOUS = new ListProperty( TYPE, "Heterogeneous" );
    
    ModelElementList<IChildElement> getHeterogeneous();
    
    // *** CustomPossibleTypes ***
    
    @Type( base = ListPropertyCustomGallery.class )
    @XmlBinding( path = "custom-possible-types" )
    
    ImpliedElementProperty PROP_CUSTOM_POSSIBLE_TYPES = new ImpliedElementProperty( TYPE, "CustomPossibleTypes" );
    
    ListPropertyCustomGallery getCustomPossibleTypes();
    
    // *** MultiSelectString ***
    
    @GenerateImpl

    interface MultiSelectStringItem extends IModelElement
    {
        ModelElementType TYPE = new ModelElementType( MultiSelectStringItem.class );
        
        // *** Item ***
        
        @Label( standard = "color" )
        @NoDuplicates
        @Services( { @Service( impl = ColorValueLabelService.class ), @Service( impl = ColorValueImageService.class ) } )

        ValueProperty PROP_ITEM = new ValueProperty( TYPE, "Item" );
        
        Value<String> getItem();
        void setItem( String value );
    }
    
    @Type( base = MultiSelectStringItem.class )
    @Label( standard = "multi-select string" )
    @Enablement( expr = "${ Enabled }" )
    @Service( impl = ColorPossibleValuesService.class )
    
    ListProperty PROP_MULTI_SELECT_STRING = new ListProperty( TYPE, "MultiSelectString" );
    
    ModelElementList<MultiSelectStringItem> getMultiSelectString();
    
    // *** MultiSelectEnum ***
    
    @GenerateImpl

    interface MultiSelectEnumItem extends IModelElement
    {
        ModelElementType TYPE = new ModelElementType( MultiSelectEnumItem.class );
        
        // *** Item ***
        
        @Type( base = Color.class )
        @Label( standard = "color" )
        @NoDuplicates
    
        ValueProperty PROP_ITEM = new ValueProperty( TYPE, "Item" );
        
        Value<Color> getItem();
        void setItem( String value );
        void setItem( Color value );
    }
    
    @Type( base = MultiSelectEnumItem.class )
    @Label( standard = "multi-select enumeration" )
    @Enablement( expr = "${ Enabled }" )
    
    ListProperty PROP_MULTI_SELECT_ENUM = new ListProperty( TYPE, "MultiSelectEnum" );
    
    ModelElementList<MultiSelectEnumItem> getMultiSelectEnum();

}
