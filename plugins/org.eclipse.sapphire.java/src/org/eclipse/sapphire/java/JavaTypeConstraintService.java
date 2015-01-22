/******************************************************************************
 * Copyright (c) 2015 Oracle and Liferay
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Gregory Amerson - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.java;

import java.util.SortedSet;

import org.eclipse.sapphire.services.DataService;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 */

public abstract class JavaTypeConstraintService extends DataService<JavaTypeConstraintServiceData>
{
    @Override
    protected final void initDataService()
    {
        initJavaTypeConstraintService();
    }

    protected void initJavaTypeConstraintService()
    {
    }
    
    public final SortedSet<JavaTypeKind> kinds()
    {
        return data().kinds();
    }
    
    public final SortedSet<String> types()
    {
        return data().types();
    }
    
    public final JavaTypeConstraintBehavior behavior()
    {
        return data().behavior();
    }

}
