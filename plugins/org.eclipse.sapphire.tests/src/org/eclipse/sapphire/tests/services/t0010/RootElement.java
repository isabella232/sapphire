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

package org.eclipse.sapphire.tests.services.t0010;

import org.eclipse.sapphire.Since;
import org.eclipse.sapphire.Version;
import org.eclipse.sapphire.VersionCompatibility;
import org.eclipse.sapphire.VersionCompatibilityTarget;
import org.eclipse.sapphire.modeling.ElementProperty;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ImpliedElementProperty;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementHandle;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DefaultValue;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.annotations.Type;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@VersionCompatibilityTarget( version = "${ Version }", versioned = "Test Versioned System" )
@GenerateImpl

public interface RootElement extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( RootElement.class );
    
    // *** Version ***
    
    @Type( base = Version.class )

    ValueProperty PROP_VERSION = new ValueProperty( TYPE, "Version" );
    
    Value<Version> getVersion();
    void setVersion( String value );
    void setVersion( Version value );
    
    // *** Switch ***
    
    @Type( base = Boolean.class )
    @DefaultValue( text = "false" )

    ValueProperty PROP_SWITCH = new ValueProperty( TYPE, "Switch" );
    
    Value<Boolean> getSwitch();
    void setSwitch( String value );
    void setSwitch( Boolean value );
    
    // *** ValueUnconstrained ***
    
    ValueProperty PROP_VALUE_UNCONSTRAINED = new ValueProperty( TYPE, "ValueUnconstrained" );
    
    Value<String> getValueUnconstrained();
    void setValueUnconstrained( String value );
    
    // *** ValueSince ***
    
    @Since( "1.2" )
    
    ValueProperty PROP_VALUE_SINCE = new ValueProperty( TYPE, "ValueSince" );
    
    Value<String> getValueSince();
    void setValueSince( String value );
    
    // *** ValueSinceDynamic ***
    
    @Since( "${ Switch ? '2.0' : '1.2' }" )
    
    ValueProperty PROP_VALUE_SINCE_DYNAMIC = new ValueProperty( TYPE, "ValueSinceDynamic" );
    
    Value<String> getValueSinceDynamic();
    void setValueSinceDynamic( String value );
    
    // *** ValueVersionCompatibility ***
    
    @VersionCompatibility( "[1.2.3-1.3)" )
    
    ValueProperty PROP_VALUE_VERSION_COMPATIBILITY = new ValueProperty( TYPE, "ValueVersionCompatibility" );
    
    Value<String> getValueVersionCompatibility();
    void setValueVersionCompatibility( String value );
    
    // *** ValueVersionCompatibilityDynamic ***
    
    @VersionCompatibility( "${ Switch ? '[2.0' : '[1.2.3-1.3)' }" )
    
    ValueProperty PROP_VALUE_VERSION_COMPATIBILITY_DYNAMIC = new ValueProperty( TYPE, "ValueVersionCompatibilityDynamic" );
    
    Value<String> getValueVersionCompatibilityDynamic();
    void setValueVersionCompatibilityDynamic( String value );
    
    // *** ValueVersionCompatibilityService ***
    
    @Service( impl = TestVersionCompatibilityService.class )
    
    ValueProperty PROP_VALUE_VERSION_COMPATIBILITY_SERVICE = new ValueProperty( TYPE, "ValueVersionCompatibilityService" );
    
    Value<String> getValueVersionCompatibilityService();
    void setValueVersionCompatibilityService( String value );
    
    // *** Child ***
    
    @Type( base = ChildElement.class )
    @Since( "2.0" )

    ElementProperty PROP_CHILD = new ElementProperty( TYPE, "Child" );
    
    ModelElementHandle<ChildElement> getChild();
    
    // *** ChildImplied ***
    
    @Type( base = ChildElement.class )
    @Since( "2.0" )
    
    ImpliedElementProperty PROP_CHILD_IMPLIED = new ImpliedElementProperty( TYPE, "ChildImplied" );
    
    ChildElement getChildImplied();
    
    // *** Children ***
    
    @Type( base = ChildElement.class )
    @Since( "2.0" )
    
    ListProperty PROP_CHILDREN = new ListProperty( TYPE, "Children" );
    
    ModelElementList<ChildElement> getChildren();
    
}
