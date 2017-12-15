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

public final class VariableOrLiteral extends SyntaxTreeNode
{
    private final String name;
    
    public VariableOrLiteral( final String name )
    {
        this.name = name;
    }
    
    @Override
    public Object execute( final VariableResolver variableResolver )
    {
        final Object val = variableResolver.resolve( this.name );
        return ( val == null ? this.name : val );
    }
}

