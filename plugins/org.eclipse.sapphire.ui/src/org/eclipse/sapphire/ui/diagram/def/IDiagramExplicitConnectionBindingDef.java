/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.ElementHandle;
import org.eclipse.sapphire.ElementProperty;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.modeling.annotations.Label;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.modeling.xml.annotations.XmlBinding;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

@Label( standard = "diagram connection binding" )

public interface IDiagramExplicitConnectionBindingDef extends IDiagramConnectionBindingDef 
{
    ElementType TYPE = new ElementType( IDiagramExplicitConnectionBindingDef.class );
    
    // *** Endpoint1 ***
    
    @Type( base = IDiagramConnectionEndpointBindingDef.class )
    @XmlBinding( path = "endpoint1" )

    ElementProperty PROP_ENDPOINT_1 = new ElementProperty( TYPE, "Endpoint1" );
    
    ElementHandle<IDiagramConnectionEndpointBindingDef> getEndpoint1();

    // *** Endpoint2 ***
    
    @Type( base = IDiagramConnectionEndpointBindingDef.class )
    @XmlBinding( path = "endpoint2" )

    ElementProperty PROP_ENDPOINT_2 = new ElementProperty( TYPE, "Endpoint2" );
    
    ElementHandle<IDiagramConnectionEndpointBindingDef> getEndpoint2();
    

}
