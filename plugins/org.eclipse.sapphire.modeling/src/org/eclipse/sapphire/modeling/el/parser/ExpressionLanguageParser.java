/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.modeling.el.parser;

import java.io.StringReader;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.modeling.el.Function;
import org.eclipse.sapphire.modeling.el.FunctionException;
import org.eclipse.sapphire.modeling.el.parser.internal.ExpressionLanguageParserImpl;
import org.eclipse.sapphire.modeling.el.parser.internal.TokenMgrError;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class ExpressionLanguageParser
{
    @Text( "Failed while parsing an expression: {0}" )
    private static LocalizableText parseFailedMessage;
    
    static
    {
        LocalizableText.init( ExpressionLanguageParser.class );
    }

    public static Function parse( final String expression )
    {
        final ExpressionLanguageParserImpl parser = new ExpressionLanguageParserImpl( new StringReader( expression ) );
        
        try
        {
            return parser.Start();
        }
        catch( TokenMgrError e )
        {
            throw new FunctionException( Status.createErrorStatus( parseFailedMessage.format( expression ), e ) );
        }
        catch( Exception e )
        {
            throw new FunctionException( Status.createErrorStatus( parseFailedMessage.format( expression ), e ) );
        }
    }
    
}

