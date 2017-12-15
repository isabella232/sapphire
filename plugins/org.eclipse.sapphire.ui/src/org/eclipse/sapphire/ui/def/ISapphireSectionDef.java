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

import org.eclipse.sapphire.java.JavaType;
import org.eclipse.sapphire.java.JavaTypeName;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ReferenceValue;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.Enablement;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Reference;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.localization.Localizable;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "section" )
@GenerateImpl

public interface ISapphireSectionDef

    extends ISapphireCompositeDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireSectionDef.class );
    
    // *** Label ***
    
    @Type( base = Function.class )
    @Label( standard = "label" )
    @Localizable
    @XmlBinding( path = "label" )
    
    ValueProperty PROP_LABEL = new ValueProperty( TYPE, "Label" );
    
    Value<Function> getLabel();
    void setLabel( String value );
    void setLabel( Function value );
    
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
