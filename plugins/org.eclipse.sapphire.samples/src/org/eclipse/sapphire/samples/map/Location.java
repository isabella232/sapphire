/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - miscellaneous improvements
 ******************************************************************************/

package org.eclipse.sapphire.samples.map;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.modeling.ModelElementList;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.annotations.GenerateImpl;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Required;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;
import org.eclipse.sapphire.modeling.xml.annotations.XmlListBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@GenerateImpl

public interface Location extends IModelElement 
{
    ModelElementType TYPE = new ModelElementType( Location.class );
    
    // *** Name ***
    
    @XmlBinding( path = "name" )
    @Label( standard = "name" )
    @Required

    ValueProperty PROP_NAME = new ValueProperty( TYPE, "Name" );

    Value<String> getName();
    void setName( String name );
    
    // *** RailwayRoutes ***
    
    @Type( base = RailwayRoute.class )
    @XmlListBinding( path = "railway", mappings = @XmlListBinding.Mapping( element = "route", type = RailwayRoute.class ) )
    
    ListProperty PROP_RAILWAY_ROUTES = new ListProperty( TYPE, "RailwayRoutes" );
    
    ModelElementList<RailwayRoute> getRailwayRoutes();
    
    
}
