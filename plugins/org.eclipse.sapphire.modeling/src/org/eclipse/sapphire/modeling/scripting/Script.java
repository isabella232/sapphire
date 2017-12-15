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

package org.eclipse.sapphire.modeling.scripting;

import java.io.StringReader;

import org.eclipse.sapphire.modeling.scripting.internal.ast.SyntaxTreeNode;
import org.eclipse.sapphire.modeling.scripting.internal.parser.ScriptParser;
import org.eclipse.sapphire.modeling.scripting.internal.parser.TokenMgrError;

/**
 * A very simple script parsing and executing engine. Used in places where simple expressions must be
 * processed. May get replaced with integration to a real scripting engine when OEPE minimum requires get
 * set at Java 6.
 * 
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class Script
{
    private final SyntaxTreeNode root;
    
    public Script( final String text )
    {
        this.root = parse( text );
    }
    
    public Object execute( final VariableResolver variableResolver )
    {
        return this.root.execute( variableResolver );
    }
    
    private SyntaxTreeNode parse( final String script )
    {
        final ScriptParser parser = new ScriptParser( new StringReader( script ) );
        
        try
        {
            return parser.Start();
        }
        catch( TokenMgrError e )
        {
            throw new RuntimeException( e );
        }
        catch( Exception e )
        {
            throw new RuntimeException( e );
        }
    }
    
}

