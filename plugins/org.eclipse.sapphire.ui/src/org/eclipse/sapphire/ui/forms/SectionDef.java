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

import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.LongString;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.annotations.Whitespace;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlValueBinding;
import org.eclipse.sapphire.ui.def.internal.SectionDefLabelDefaultValueProvider;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "section" )
@Image( path = "SectionDef.png" )
@XmlBinding( path = "section" )

public interface SectionDef extends CompositeDef
{
    ElementType TYPE = new ElementType( SectionDef.class );
    
    // *** Label ***
    
    @Type( base = Function.class )
    @Label( standard = "label" )
    @Service( impl = SectionDefLabelDefaultValueProvider.class )
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<Function> getLabel();
    void setLabel( String value );
    void setLabel( Function value );
    
    // *** Description ***
    
    @Type( base = Function.class )
    @Label( standard = "description" )
    @LongString
    @Whitespace( collapse = true )
    @XmlValueBinding( path = "description" )
    
    ValueProperty PROP_DESCRIPTION = new ValueProperty( TYPE, "Description" );
    
    Value<Function> getDescription();
    void setDescription( String value );
    void setDescription( Function value );
    
    // *** Collapsible ***
    
    @Type( base = Boolean.class )
    @Label( standard = "collapsible" )
    @DefaultValue( text = "false" )
    @XmlBinding( path = "collapsible" )
    
    ValueProperty PROP_COLLAPSIBLE = new ValueProperty( TYPE, "Collapsible" );
    
    Value<Boolean> getCollapsible();
    void setCollapsible( String value );
    void setCollapsible( Boolean value );
    
    // *** CollapsedInitially ***
    
    @Type( base = Boolean.class )
    @Label( standard = "collapsed initially" )
    @DefaultValue( text = "false" )
    @Enablement( expr = "${ Collapsible }" )
    @XmlBinding( path = "collapsed-initially" )
    
    ValueProperty PROP_COLLAPSED_INITIALLY = new ValueProperty( TYPE, "CollapsedInitially" );
    
    Value<Boolean> getCollapsedInitially();
    void setCollapsedInitially( String value );
    void setCollapsedInitially( Boolean value );
    
}
