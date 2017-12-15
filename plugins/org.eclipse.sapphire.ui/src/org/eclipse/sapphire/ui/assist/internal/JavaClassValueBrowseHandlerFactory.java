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

package org.eclipse.sapphire.ui.assist.internal;

import org.eclipse.sapphire.modeling.ValueProperty;
import org.eclipse.sapphire.modeling.java.JavaTypeName;
import org.eclipse.sapphire.ui.assist.BrowseHandler;
import org.eclipse.sapphire.ui.assist.BrowseHandlerFactory;
import org.eclipse.sapphire.ui.java.JavaTypeBrowseHandler;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class JavaClassValueBrowseHandlerFactory 

    extends BrowseHandlerFactory
    
{
    @Override
    public boolean isApplicable( final ValueProperty property )
    {
        return property.isOfType( JavaTypeName.class );
    }

    @Override
    public BrowseHandler create()
    {
        return new JavaTypeBrowseHandler();
    }

}