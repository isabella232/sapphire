/******************************************************************************
 * Copyright (c) 2013 Liferay and Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Gregory Amerson - initial implementation
 *    Konstantin Komissarchik - initial implementation review and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.internal;

import org.eclipse.sapphire.modeling.ListProperty;
import org.eclipse.sapphire.services.Service;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.services.ServiceFactory;
import org.eclipse.sapphire.ui.ListSelectionService;
import org.eclipse.sapphire.ui.PropertyEditorPart;

/**
 * @author <a href="mailto:gregory.amerson@liferay.com">Gregory Amerson</a>
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ListSelectionServiceFactory extends ServiceFactory
{
    @Override
    public boolean applicable( final ServiceContext context, 
                               final Class<? extends Service> service )
    {
        final PropertyEditorPart propertyEditorPart = context.find( PropertyEditorPart.class );

        return ( propertyEditorPart != null && propertyEditorPart.getProperty() instanceof ListProperty );
    }

    @Override
    public Service create( final ServiceContext context, final Class<? extends Service> service )
    {
        return new ListSelectionService();
    }

}