/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Ling Hao - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.def.internal;

import org.eclipse.sapphire.ui.def.ISapphireDocumentationDef;
import org.eclipse.sapphire.ui.def.ISapphireDocumentationRef;
import org.eclipse.sapphire.ui.def.ISapphireUiDef;

/**
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

public final class DocumentationRefMethods
{
    public static ISapphireDocumentationDef resolve( final ISapphireDocumentationRef ref )
    {
        final ISapphireUiDef rootdef = ref.nearest( ISapphireUiDef.class );
        return rootdef.getDocumentationDef( ref.getId().text(), true );
    }
    
}
