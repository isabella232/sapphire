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

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
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

public interface MasterDetailsContentNodeDef

    extends MasterDetailsContentNodeChildDef, IPropertiesViewContributorDef
    
{
    ModelElementType TYPE = new ModelElementType( MasterDetailsContentNodeDef.class );
    
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
    @Type( base = MasterDetailsSectionDef.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "section", type = MasterDetailsSectionDef.class ) )
    
    ListProperty PROP_SECTIONS = new ListProperty( TYPE, "Sections" );
    
    ModelElementList<MasterDetailsSectionDef> getSections();
    
    // *** ChildNodes ***
    
    @Label( standard = "child nodes" )
    
    @Type
    ( 
        base = MasterDetailsContentNodeChildDef.class, 
        possible = 
        { 
            MasterDetailsContentNodeDef.class,
            MasterDetailsContentNodeFactoryDef.class,
            MasterDetailsContentNodeInclude.class
        } 
    )
    
    @XmlListBinding
    (
        mappings = 
        { 
            @XmlListBinding.Mapping( element = "node", type = MasterDetailsContentNodeDef.class ),
            @XmlListBinding.Mapping( element = "node-factory", type = MasterDetailsContentNodeFactoryDef.class ),
            @XmlListBinding.Mapping( element = "node-include", type = MasterDetailsContentNodeInclude.class )
        }
    )
    
    ListProperty PROP_CHILD_NODES = new ListProperty( TYPE, "ChildNodes" );
    
    ModelElementList<MasterDetailsContentNodeChildDef> getChildNodes();
    
}
