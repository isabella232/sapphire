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
import org.eclipse.sapphire.modeling.annotations.DelegateImplementation;
import org.eclipse.sapphire.modeling.annotations.Image;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.FoldingXmlValueBindingImpl;
import org.eclipse.sapphire.modeling.xml.annotations.CustomXmlValueBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;
import org.eclipse.sapphire.ui.def.ISapphireParam;
import org.eclipse.sapphire.ui.forms.internal.MasterDetailsContentNodeRefMethods;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@Label( standard = "master details node reference" )
@XmlBinding( path = "node-include" )
@Image( path = "MasterDetailsContentNodeRef.png" )

public interface MasterDetailsContentNodeRef extends MasterDetailsContentNodeChildDef
{
    ElementType TYPE = new ElementType( MasterDetailsContentNodeRef.class );
    
    // *** Part ***
    
    @Label( standard = "part" )
    @CustomXmlValueBinding( impl = FoldingXmlValueBindingImpl.class, params = "part" )
    
    ValueProperty PROP_PART = new ValueProperty( TYPE, "Part" );
    
    Value<String> getPart();
    void setPart( String value );

    // *** Params ***
    
    @Label( standard = "params" )
    @Type( base = ISapphireParam.class )
    @XmlListBinding( mappings = @XmlListBinding.Mapping( element = "param", type = ISapphireParam.class ) )
    
    ListProperty PROP_PARAMS = new ListProperty( TYPE, "Params" );
    
    ElementList<ISapphireParam> getParams();

    // *** Method : resolve ***
    
    @DelegateImplementation( MasterDetailsContentNodeRefMethods.class )
    
    MasterDetailsContentNodeChildDef resolve();

}
