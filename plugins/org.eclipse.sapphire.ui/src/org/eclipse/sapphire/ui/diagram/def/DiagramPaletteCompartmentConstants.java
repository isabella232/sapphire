/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.def;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 */

public class DiagramPaletteCompartmentConstants 
{
    @Text( "connections" )
    private static LocalizableText connectionsCompartmentLabel;
    
    @Text( "objects" )
    private static LocalizableText nodesCompartmentLabel;
    
    static
    {
        LocalizableText.init( DiagramPaletteCompartmentConstants.class );
    }

    public static final String CONNECTIONS_COMPARTMENT_ID = "Sapphire.Diagram.Palette.Connections";
	public static final String NODES_COMPARTMENT_ID = "Sapphire.Diagram.Palette.Nodes";
	public static final String CONNECTIONS_COMPARTMENT_LABEL = connectionsCompartmentLabel.text();
	public static final String NODES_COMPARTMENT_LABEL = nodesCompartmentLabel.text();
	
}
