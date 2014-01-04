/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram;

import java.util.List;

import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;

/**
 * Responsible for listing and establishing connections in a diagram.
 * 
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ConnectionService extends Service 
{
    /**
     * Determines whether establishing a connection between the specified nodes is valid.
     * 
     * @param node1 the first node
     * @param node2 the second node
     * @param connectionType the connection type
     * @return true if connection is valid and false otherwise
     */
    
    public abstract boolean valid( DiagramNodePart node1, DiagramNodePart node2, String connectionType );
    
    /**
     * Creates a connection between the specified nodes.
     * 
     * @param node1 the first node
     * @param node2 the second node
     * @param connectionType the connection type
     * @return the created connection part
     */
    
    public abstract DiagramConnectionPart connect( DiagramNodePart node1, DiagramNodePart node2, String connectionType );

    /**
     * Lists the existing connections.
     * 
     * @return the list of existing connections
     */
    
    public abstract List<DiagramConnectionPart> list();

}
