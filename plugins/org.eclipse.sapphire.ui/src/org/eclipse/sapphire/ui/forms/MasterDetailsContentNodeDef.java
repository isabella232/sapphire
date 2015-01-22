/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms;

import org.eclipse.sapphire.ElementList;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.ListProperty;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "master details node" )
@XmlBinding( path = "node" )
@Image( path = "MasterDetailsContentNodeDef.png" )

public interface MasterDetailsContentNodeDef extends MasterDetailsContentNodeChildDef, PropertiesViewContributorDef
{
    ElementType TYPE = new ElementType( MasterDetailsContentNodeDef.class );
    
    // *** Property ***
    
    @Label( standard = "property" )
    @XmlBinding( path = "property" )
    
    ValueProperty PROP_PROPERTY = new ValueProperty( TYPE, "Property" );
    
    Value<String> getProperty();
    void setProperty( String property );
    
    // *** Label ***
    
    @Type( base = Function.class )
    @Label( standard = "label" )
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
    
    // *** Decorations ***
    
    @Type( base = TextDecorationDef.class )
    @Label( standard = "decorations" )
    @XmlListBinding( path = "" )
    
    ListProperty PROP_DECORATIONS = new ListProperty( TYPE, "Decorations" );
    
    ElementList<TextDecorationDef> getDecorations();
    
    // *** Sections ***
    
    @Label( standard = "sections" )
    @Type( base = FormComponentDef.class, possible = { SectionDef.class, SectionRef.class } )
    @XmlListBinding( path = "" )
    
    ListProperty PROP_SECTIONS = new ListProperty( TYPE, "Sections" );
    
    ElementList<FormComponentDef> getSections();
    
    // *** ChildNodes ***
    
    @Label( standard = "child nodes" )
    @XmlListBinding( path = "" )
    
    @Type
    ( 
        base = MasterDetailsContentNodeChildDef.class, 
        possible = 
        { 
            MasterDetailsContentNodeDef.class,
            MasterDetailsContentNodeFactoryDef.class,
            MasterDetailsContentNodeRef.class
        } 
    )
    
    ListProperty PROP_CHILD_NODES = new ListProperty( TYPE, "ChildNodes" );
    
    ElementList<MasterDetailsContentNodeChildDef> getChildNodes();
    
}
