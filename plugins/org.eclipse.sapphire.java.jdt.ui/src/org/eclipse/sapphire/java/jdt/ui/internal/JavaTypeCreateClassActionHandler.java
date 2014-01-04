/******************************************************************************
 * Copyright (c) 2014 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amereson - [363551] JavaTypeConstraintService
 ******************************************************************************/

package org.eclipse.sapphire.java.jdt.ui.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.java.JavaTypeConstraintService;
import org.eclipse.sapphire.java.JavaTypeKind;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public final class JavaTypeCreateClassActionHandler extends JavaTypeCreateActionHandler
{
    public JavaTypeCreateClassActionHandler()
    {
        super( JavaTypeKind.CLASS );
    }
    
    public static final class Condition extends JavaTypeCreateActionHandler.Condition
    {
        @Override
        protected boolean evaluate( final JavaTypeConstraintService javaTypeConstraintService )
        {
            if( javaTypeConstraintService == null )
            {
                return true;
            }
            else
            {
                final SortedSet<JavaTypeKind> kinds = javaTypeConstraintService.kinds();
                return kinds.contains( JavaTypeKind.CLASS ) || kinds.contains( JavaTypeKind.ABSTRACT_CLASS );
            }
        }
    }    

}