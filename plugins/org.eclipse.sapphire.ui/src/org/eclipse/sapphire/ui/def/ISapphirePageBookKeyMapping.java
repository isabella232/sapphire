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

import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@Label( standard = "page" )
@GenerateImpl

public interface ISapphirePageBookKeyMapping

    extends ISapphireCompositeDef
    
{
    ModelElementType TYPE = new ModelElementType( ISapphirePageBookKeyMapping.class );
    
    // *** Key ***
    
    @Label( standard = "key" )
    @Required
    @XmlBinding( path = "key" )
    
    ValueProperty PROP_KEY = new ValueProperty( TYPE, "Key" );
    
    Value<String> getKey();
    void setKey( String key );

}
