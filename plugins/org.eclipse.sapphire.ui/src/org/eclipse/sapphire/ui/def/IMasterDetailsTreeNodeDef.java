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
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.BooleanPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.GenerateXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.ListPropertyXmlBindingMapping;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.ui.def.internal.ClassReferenceResolver;
import org.eclipse.sapphire.ui.def.internal.ImageReferenceResolver;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateXmlBinding

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
    
    @Label( standard = "label" )
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<String> getLabel();
    void setLabel( String label );
    
    // *** DynamicLabelProperty ***
    
    @Label( standard = "dynamic label property" )
    @XmlBinding( path = "dynamic-label/property" )
    
    ValueProperty PROP_DYNAMIC_LABEL_PROPERTY = new ValueProperty( TYPE, "DynamicLabelProperty" );
    
    Value<String> getDynamicLabelProperty();
    void setDynamicLabelProperty( String dynamicLabelProperty );
    
    // *** DynamicLabelNullValueText ***
    
    @Label( standard = "dynamic label property" )
    @XmlBinding( path = "dynamic-label/null-value-label" )
    
    ValueProperty PROP_DYNAMIC_LABEL_NULL_VALUE_TEXT = new ValueProperty( TYPE, "DynamicLabelNullValueText" );
    
    Value<String> getDynamicLabelNullValueText();
    void setDynamicLabelNullValueText( String dynamicLabelNullValueText );
    
    // *** UseModelElementImage ***
    
    @Type( base = Boolean.class )
    @Label( standard = "use model element image" )
    @DefaultValue( "false" )
    @BooleanPropertyXmlBinding( path = "use-model-element-image", treatExistenceAsValue = true, valueWhenPresent = true )
    
    ValueProperty PROP_USE_MODEL_ELEMENT_IMAGE = new ValueProperty( TYPE, "UseModelElementImage" );
    
    Value<Boolean> getUseModelElementImage();
    void setUseModelElementImage( String useModelElementImage );
    void setUseModelElementImage( Boolean useModelElementImage );
    
    // *** ImagePath ***
    
    @Reference( target = ImageDescriptor.class, resolver = ImageReferenceResolver.class )
    @Label( standard = "image path" )
    @XmlBinding( path = "image" )
    
    ValueProperty PROP_IMAGE_PATH = new ValueProperty( TYPE, "ImagePath" );
    
    ReferenceValue<ImageDescriptor> getImagePath();
    void setImagePath( String imagePath );
    
    // *** Sections ***
    
    @Label( standard = "sections" )
    @Type( base = IMasterDetailsPageSectionDef.class )
    @ListPropertyXmlBinding( mappings = { @ListPropertyXmlBindingMapping( element = "section", type = IMasterDetailsPageSectionDef.class ) } )
    
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
    
    @ListPropertyXmlBinding
    ( 
        mappings = 
        { 
            @ListPropertyXmlBindingMapping( element = "node", type = IMasterDetailsTreeNodeDef.class ),
            @ListPropertyXmlBindingMapping( element = "node-ref", type = IMasterDetailsTreeNodeRef.class ),
            @ListPropertyXmlBindingMapping( element = "node-list", type = IMasterDetailsTreeNodeFactoryDef.class ),
            @ListPropertyXmlBindingMapping( element = "node-list-ref", type = IMasterDetailsTreeNodeFactoryRef.class )
        }
    )
    
    ListProperty PROP_CHILD_NODES = new ListProperty( TYPE, "ChildNodes" );
    
    ModelElementList<IMasterDetailsTreeNodeListEntry> getChildNodes();

    // *** ActionSetDef ***
    
    @Type( base = IActionSetDef.class )
    @Label( standard = "actions" )
    @XmlBinding( path = "actions" )
    
    ElementProperty PROP_ACTION_SET_DEF = new ElementProperty( TYPE, "ActionSetDef" );
    
    IActionSetDef getActionSetDef();
    IActionSetDef getActionSetDef( boolean createIfNecessary );
    
    // *** VisibleWhenConditionClass ***
    
    @Reference( target = Class.class, resolver = ClassReferenceResolver.class )
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
