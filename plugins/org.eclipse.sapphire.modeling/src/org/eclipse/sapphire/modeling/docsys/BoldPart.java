/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.docsys;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class BoldPart

    extends DocumentationPart
    
{
    private final boolean open;
    
    public BoldPart( final boolean open )
    {
        this.open = open;
    }
    
    public boolean isOpen()
    {
        return this.open;
    }
}
