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

package org.eclipse.sapphire.tests.services.t0003;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.Service;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestModelItem extends Element
{
    ElementType TYPE = new ElementType( TestModelItem.class );
    
    // *** Name ***
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String value );
    
    // *** Id ***
    
    @DependsOn( "Name" )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String value );
    
    // *** Custom1 ***
    
    @Service( impl = CustomDependenciesService.class )
    
    ValueProperty PROP_CUSTOM_1 = new ValueProperty( TYPE, "Custom1" );
    
    Value<String> getCustom1();
    void setCustom1( String value );
    
    // *** Custom2 ***
    
    @DependsOn( "Custom1" )
    @Service( impl = CustomDependenciesService.class )
    
    ValueProperty PROP_CUSTOM_2 = new ValueProperty( TYPE, "Custom2" );
    
    Value<String> getCustom2();
    void setCustom2( String value );
    
}
