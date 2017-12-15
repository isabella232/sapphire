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

package org.eclipse.sapphire.modeling.java.internal;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.osgi.util.NLS;
import org.eclipse.sapphire.modeling.Value;
import org.eclipse.sapphire.modeling.annotations.ModelPropertyValidator;
import org.eclipse.sapphire.modeling.java.JavaPackageName;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class QualifiedJavaIdentifierValueValidator

    extends ModelPropertyValidator<Value<JavaPackageName>>

{
    @Override
    public IStatus validate( final Value<JavaPackageName> value )
    {
        final String val = value.getText( false );
        boolean valid = true;
        
        if( val != null )
        {
            final int STATE_EXPECTING_FIRST = 1;
            final int STATE_EXPECTING_NEXT = 2;
            
            int state = STATE_EXPECTING_FIRST;
            
            for( int i = 0, n = val.length(); i < n; i++ )
            {
                final char ch = val.charAt( i );
                
                if( state == STATE_EXPECTING_FIRST )
                {
                    if( Character.isJavaIdentifierStart( ch ) )
                    {
                        state = STATE_EXPECTING_NEXT;
                    }
                    else
                    {
                        valid = false;
                        break;
                    }
                }
                else
                {
                    if( ch == '.' )
                    {
                        state = STATE_EXPECTING_FIRST;
                    }
                    else if( Character.isJavaIdentifierPart( ch ) )
                    {
                        // Keep state as STATE_EXPECTING_NEXT.
                    }
                    else
                    {
                        valid = false;
                        break;
                    }
                }
            }
            
            if( state == STATE_EXPECTING_FIRST )
            {
                valid = false;
            }
        }
        
        if( valid )
        {
            return Status.OK_STATUS;
        }
        else
        {
            final String msg = NLS.bind( Resources.invalidQualifiedJavaIdentifierMessage, val );
            return createErrorStatus( msg );
        }
    }
    
    private static final class Resources
        
        extends NLS

    {
        public static String invalidQualifiedJavaIdentifierMessage;
        
        static
        {
            initializeMessages( QualifiedJavaIdentifierValueValidator.class.getName(), Resources.class );
        }
    }

}
