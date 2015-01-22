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

package org.eclipse.sapphire.tests.workspace.t0001;

import org.eclipse.sapphire.Element;
import org.eclipse.sapphire.ElementType;
import org.eclipse.sapphire.Value;
import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.modeling.Path;
import org.eclipse.sapphire.modeling.annotations.MustExist;
import org.eclipse.sapphire.modeling.annotations.Type;
import org.eclipse.sapphire.workspace.ProjectRelativePath;
import org.eclipse.sapphire.workspace.WorkspaceRelativePath;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public interface TestElement extends Element
{
    ElementType TYPE = new ElementType( TestElement.class );
    
    // *** WorkspaceRelativePath ***
    
    @Type( base = Path.class )
    @WorkspaceRelativePath
    @MustExist
    
    ValueProperty PROP_WORKSPACE_RELATIVE_PATH = new ValueProperty( TYPE, "WorkspaceRelativePath" );
    
    Value<Path> getWorkspaceRelativePath();
    void setWorkspaceRelativePath( String value );
    void setWorkspaceRelativePath( Path value );
    
    // *** ProjectRelativePath ***
    
    @Type( base = Path.class )
    @ProjectRelativePath
    @MustExist

    ValueProperty PROP_PROJECT_RELATIVE_PATH = new ValueProperty( TYPE, "ProjectRelativePath" );
    
    Value<Path> getProjectRelativePath();
    void setProjectRelativePath( String value );
    void setProjectRelativePath( Path value );
    
}
