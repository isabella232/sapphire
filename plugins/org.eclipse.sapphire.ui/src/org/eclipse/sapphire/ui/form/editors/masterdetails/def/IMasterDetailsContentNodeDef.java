/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.form.editors.masterdetails.def;

import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.IPropertiesViewContributorDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "content outline node" )
@GenerateImpl

public interface IMasterDetailsContentNodeDef

    extends IMasterDetailsContentNodeChildDef, IPropertiesViewContributorDef
    
{
    ModelElementType TYPE = new ModelElementType( IMasterDetailsContentNodeDef.class );
    
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
    
    // *** Image ***
    
    @Type( base = Function.class )
    @Label( standard = "image" )
    @XmlBinding( path = "image" )
    
    ValueProperty PROP_IMAGE = new ValueProperty( TYPE, "Image" );
    
    Value<Function> getImage();
    void setImage( String value );
    void setImage( Function value );
    
    // *** Sections ***
    
    @Label( standard = "sections" )
    @Type( base = IMasterDetailsSectionDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "section", type = IMasterDetailsSectionDef.class ) )
    
    ListProperty PROP_SECTIONS = new ListProperty( TYPE, "Sections" );
    
    ModelElementList<IMasterDetailsSectionDef> getSections();
    
    // *** ChildNodes ***
    
    @Label( standard = "child nodes" )
    
    @Type
    ( 
        base = IMasterDetailsContentNodeChildDef.class, 
        possible = 
        { 
            IMasterDetailsContentNodeDef.class,
            IMasterDetailsContentNodeFactoryDef.class,
            IMasterDetailsContentNodeInclude.class
        } 
    )
    
    @XmlListBinding
    (
        mappings = 
        { 
            @XmlListBinding.Mapping( element = "node", type = IMasterDetailsContentNodeDef.class ),
            @XmlListBinding.Mapping( element = "node-factory", type = IMasterDetailsContentNodeFactoryDef.class ),
            @XmlListBinding.Mapping( element = "node-include", type = IMasterDetailsContentNodeInclude.class )
        }
    )
    
    ListProperty PROP_CHILD_NODES = new ListProperty( TYPE, "ChildNodes" );
    
    ModelElementList<IMasterDetailsContentNodeChildDef> getChildNodes();
    
    // *** VisibleWhenConditionClass ***
    
    @Type( base = JavaTypeName.class )
    @Reference( target = JavaType.class )
    @Label( standard = "visible when condition class" )
    @XmlBinding( path = "visible-when/condition/class" )
    
    ValueProperty PROP_VISIBLE_WHEN_CONDITION_CLASS = new ValueProperty( TYPE, "VisibleWhenConditionClass" );
    
    ReferenceValue<JavaTypeName,JavaType> getVisibleWhenConditionClass();
    void setVisibleWhenConditionClass( String value );
    void setVisibleWhenConditionClass( JavaTypeName value );
    
    // *** VisibleWhenConditionParameter ***
    
    @Label( standard = "visible when condition parameter" )
    @XmlBinding( path = "visible-when/condition/parameter" )
    
    ValueProperty PROP_VISIBLE_WHEN_CONDITION_PARAMETER = new ValueProperty( TYPE, "VisibleWhenConditionParameter" );
    
    Value<String> getVisibleWhenConditionParameter();
    void setVisibleWhenConditionParameter( String visibleWhenConditionParameter );

}
