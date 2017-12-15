/******************************************************************************
 * Copyright (c) 2013 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.form.editors.masterdetails.def.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.services.DefaultValueService;
import org.eclipse.sapphire.services.DefaultValueServiceData;
import org.eclipse.sapphire.ui.form.editors.masterdetails.def.IMasterDetailsContentNodeDef;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class MasterDetailsSectionDefLabelDefaultValueProvider extends DefaultValueService
{
    @Override
    protected DefaultValueServiceData data()
    {
        refresh();
        return super.data();
    }

    @Override
    protected DefaultValueServiceData compute()
    {
        final IMasterDetailsContentNodeDef node = (IMasterDetailsContentNodeDef) context( IModelElement.class ).parent().parent();
        return new DefaultValueServiceData( node.getLabel().getText() );
    }
    
}
