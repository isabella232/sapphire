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

package org.eclipse.sapphire.ui;

import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SapphireCondition
{
    protected SapphirePartContext context;
    protected String parameter;
    
    public void init( final SapphirePartContext context,
                      final String parameter )
    {
        this.context = context;
        this.parameter = parameter;
    }
    
    public abstract boolean evaluate();
    
    public List<String> getDependencies()
    {
        return Collections.emptyList();
    }
}
