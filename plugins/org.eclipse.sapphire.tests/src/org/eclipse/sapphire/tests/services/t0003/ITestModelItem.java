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

package org.eclipse.sapphire.tests.services.t0003;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.NoDuplicates;
import org.eclipse.sapphire.modeling.annotations.Service;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface ITestModelItem extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( ITestModelItem.class );
    
    // *** Name ***
    
    @NoDuplicates
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String value );
    
    // *** Id ***
    
    @NoDuplicates
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
