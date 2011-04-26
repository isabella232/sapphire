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

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.Documentation;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "param" )
@GenerateImpl

public interface ISapphireParam

    extends IModelElement
    
{
    ModelElementType TYPE = new ModelElementType( ISapphireParam.class );
    
    // *** Name ***
    
    @Label( standard = "name" )
    @Required
    @XmlBinding( path = "name" )
    @Documentation( content = "The name of the parameter." )
    
    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );
    
    Value<String> getName();
    void setName( String name );
    
    // *** Value ***
    
    @Label( standard = "value" )
    @Required
    @XmlBinding( path = "value" )
    @Documentation( content = "The value of the parameter." )
    
    ValueProperty PROP_VALUE = new ValueProperty( TYPE, "Value" );
    
    Value<String> getValue();
    void setValue( String value );
}
