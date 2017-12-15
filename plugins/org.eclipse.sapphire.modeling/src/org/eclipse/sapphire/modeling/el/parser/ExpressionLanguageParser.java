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

package org.eclipse.sapphire.modeling.el.parser;

import java.io.Reader;
import java.io.StringReader;

import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionException;
import org.eclipse.sapphire.modeling.el.parser.internal.ExpressionLanguageParserImpl;
import org.eclipse.sapphire.modeling.el.parser.internal.TokenMgrError;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ExpressionLanguageParser
{
    public static Function parse( final String expression )
    {
        return parse( new StringReader( expression ) );
    }
    
    public static Function parse( final Reader expression )
    {
        final ExpressionLanguageParserImpl parser = new ExpressionLanguageParserImpl( expression );
        
        try
        {
            return parser.Start();
        }
        catch( TokenMgrError e )
        {
            throw new FunctionException( e );
        }
        catch( Exception e )
        {
            throw new FunctionException( e );
        }
    }
    
}

