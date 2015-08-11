/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation review and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.internal;

import org.eclipse.sapphire.ValueProperty;
import org.eclipse.sapphire.services.ServiceCondition;
import org.eclipse.sapphire.services.ServiceContext;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class TextSelectionServiceCondition extends ServiceCondition
{
    @Override
    public boolean applicable( final ServiceContext context )
    {
        final PropertyEditorPart propertyEditorPart = context.find( PropertyEditorPart.class );

        return ( propertyEditorPart != null && propertyEditorPart.property().definition() instanceof ValueProperty );
    }

}