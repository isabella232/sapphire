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

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.modeling.DefaultValueService;
import org.eclipse.sapphire.ui.def.ISapphireCompositeDef;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class CompositeMarginTopBottomDefaultValueProvider

    extends DefaultValueService
    
{
    @Override
    public String getDefaultValue()
    {
        final ISapphireCompositeDef def = (ISapphireCompositeDef) element();
        return def.getMarginHeight().getText();
    }
    
}
