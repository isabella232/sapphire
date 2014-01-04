/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.workspace.internal;

import java.util.SortedSet;

import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.FactsService;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.workspace.WorkspaceRelativePath;

/**
 * Creates fact statements about property's relative to the workspace path requirement by using semantical 
 * information specified by @WorkspaceRelativePath annotation.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class WorkspaceRelativePathFactsService extends FactsService
{
    @Text( "Must be a workspace relative path" )
    private static LocalizableText statement;
    
    static
    {
        LocalizableText.init( WorkspaceRelativePathFactsService.class );
    }

    @Override
    protected void facts( final SortedSet<String> facts )
    {
        facts.add( statement.text() );
    }
    
    public static final class Condition extends ServiceCondition
    {
        @Override
        public boolean applicable( final ServiceContext context )
        {
            final ValueProperty property = context.find( ValueProperty.class );
            return ( property != null && property.hasAnnotation( WorkspaceRelativePath.class ) );
        }
    }
    
}
