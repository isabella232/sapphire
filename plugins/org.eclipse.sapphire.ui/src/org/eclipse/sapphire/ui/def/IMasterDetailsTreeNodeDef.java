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
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface IMasterDetailsTreeNodeDef

    extends IMasterDetailsTreeNodeListEntry, ISapphirePartDef
    
{
    ModelElementType TYPE = new ModelElementType( IMasterDetailsTreeNodeDef.class );
    
    // *** Id ***
    
    @Label( standard = "ID" )
    @XmlBinding( path = "id" )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String id );
    
    // *** Property ***
    
    @Label( standard = "property" )
    @XmlBinding( path = "property" )
    
    ValueProperty PROP_PROPERTY = new ValueProperty( TYPE, "Property" );
    
    Value<String> getProperty();
    void setProperty( String property );
    
    // *** Label ***
    
    @Type( base = Function.class )
    @Label( standard = "label" )
    @Localizable
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<Function> getLabel();
    void setLabel( String value );
    void setLabel( Function value );
    
    // *** UseModelElementImage ***
    
    @Type( base = Boolean.class )
    @Label( standard = "use model element image" )
    @DefaultValue( text = "false" )
    @XmlValueBinding( path = "use-model-element-image", mapExistanceToValue = "true;false" )
    
    ValueProperty PROP_USE_MODEL_ELEMENT_IMAGE = new ValueProperty( TYPE, "UseModelElementImage" );
    
    Value<Boolean> getUseModelElementImage();
    void setUseModelElementImage( String useModelElementImage );
    void setUseModelElementImage( Boolean useModelElementImage );
    
    // *** ImagePath ***
    
    @Reference( target = ImageDescriptor.class )
    @Label( standard = "image path" )
    @XmlBinding( path = "image" )
    
    ValueProperty PROP_IMAGE_PATH = new ValueProperty( TYPE, "ImagePath" );
    
    ReferenceValue<ImageDescriptor> getImagePath();
    void setImagePath( String imagePath );
    
    // *** Sections ***
    
    @Label( standard = "sections" )
    @Type( base = IMasterDetailsPageSectionDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "section", type = IMasterDetailsPageSectionDef.class ) )
    
    ListProperty PROP_SECTIONS = new ListProperty( TYPE, "Sections" ); //$NON-NLS-1$
    
    ModelElementList<IMasterDetailsPageSectionDef> getSections();
    
    // *** ChildNodes ***
    
    @Label( standard = "child nodes" )
    
    @Type
    ( 
        base = IMasterDetailsTreeNodeListEntry.class, 
        possible = 
        { 
            IMasterDetailsTreeNodeDef.class,
            IMasterDetailsTreeNodeRef.class,
            IMasterDetailsTreeNodeFactoryDef.class,
            IMasterDetailsTreeNodeFactoryRef.class
        } 
    )
    
    @XmlListBinding
    ( 
        mappings = 
        { 
            @XmlListBinding.Mapping( element = "node", type = IMasterDetailsTreeNodeDef.class ),
            @XmlListBinding.Mapping( element = "node-ref", type = IMasterDetailsTreeNodeRef.class ),
            @XmlListBinding.Mapping( element = "node-list", type = IMasterDetailsTreeNodeFactoryDef.class ),
            @XmlListBinding.Mapping( element = "node-list-ref", type = IMasterDetailsTreeNodeFactoryRef.class )
        }
    )
    
    ListProperty PROP_CHILD_NODES = new ListProperty( TYPE, "ChildNodes" );
    
    ModelElementList<IMasterDetailsTreeNodeListEntry> getChildNodes();

    // *** VisibleWhenConditionClass ***
    
    @Reference( target = Class.class )
    @Label( standard = "visible when condition class" )
    @XmlBinding( path = "visible-when/condition/class" )
    
    ValueProperty PROP_VISIBLE_WHEN_CONDITION_CLASS = new ValueProperty( TYPE, "VisibleWhenConditionClass" );
    
    ReferenceValue<Class<?>> getVisibleWhenConditionClass();
    void setVisibleWhenConditionClass( String visibleWhenConditionClass );
    
    // *** VisibleWhenConditionParameter ***
    
    @Label( standard = "visible when condition parameter" )
    @XmlBinding( path = "visible-when/condition/parameter" )
    
    ValueProperty PROP_VISIBLE_WHEN_CONDITION_PARAMETER = new ValueProperty( TYPE, "VisibleWhenConditionParameter" );
    
    Value<String> getVisibleWhenConditionParameter();
    void setVisibleWhenConditionParameter( String visibleWhenConditionParameter );

}
