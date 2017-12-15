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

package org.eclipse.sapphire.tests.modeling.misc.t0011;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.DependsOn;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Service;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface TestElementChild extends IModelElement
{
    ModelElementType TYPE = new ModelElementType( TestElementChild.class );
    
    // *** Id ***
    
    @Required
    @XmlBinding( path = "id" )
    
    ValueProperty PROP_ID = new ValueProperty( TYPE, "Id" );
    
    Value<String> getId();
    void setId( String value );
    
    // *** Reference ***
    
    @XmlBinding( path = "reference" )
    
    ValueProperty PROP_REFERENCE = new ValueProperty( TYPE, "Reference" );
    
    Value<String> getReference();
    void setReference( String value );
    
    // *** Content ***
    
    @DependsOn( { "Reference", "#/Id", "#/Content" } )
    @Service( impl = ContentDefaultValueService.class )
    @XmlBinding( path = "content" )
    
    ValueProperty PROP_CONTENT = new ValueProperty( TYPE, "Content" );
    
    Value<String> getContent();
    void setContent( String value );

}