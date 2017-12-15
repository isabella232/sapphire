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

package org.eclipse.sapphire.modeling.scripting.internal.ast;

import org.eclipse.sapphire.modeling.scripting.VariableResolver;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class SyntaxTreeNode
{
    public abstract Object execute( final VariableResolver variableResolver );
    
    protected static boolean toBoolean( final Object obj )
    {
        if( obj == null )
        {
            return false;
        }
        else if( obj instanceof Boolean )
        {
            return (Boolean) obj;
        }
        else
        {
            return Boolean.valueOf( obj.toString() );
        }
    }
}
