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

package org.eclipse.sapphire.java.jdt.ui.internal;

import org.eclipse.sapphire.java.JavaTypeConstraint;
import org.eclipse.sapphire.java.JavaTypeKind;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class JavaTypeCreateInterfaceActionHandler extends JavaTypeCreateActionHandler
{
    public JavaTypeCreateInterfaceActionHandler()
    {
        super( JavaTypeKind.INTERFACE );
    }
    
    public static final class Condition extends JavaTypeCreateActionHandler.Condition
    {
        @Override
        protected boolean evaluate( final JavaTypeConstraint javaTypeConstraint )
        {
            if( javaTypeConstraint == null )
            {
                return true;
            }
            else
            {
                for( JavaTypeKind kind : javaTypeConstraint.kind() )
                {
                    if( kind == JavaTypeKind.INTERFACE )
                    {
                        return true;
                    }
                }
                
                return false;
            }
        }
    }    

}