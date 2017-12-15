/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.diagram.actions.internal;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.LoggingService;
import org.eclipse.sapphire.ui.SapphireCondition;
import org.eclipse.sapphire.ui.SourceEditorService;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class DiagramShowInSourceActionHandlerCondition 

    extends SapphireCondition
    
{
    @Override
    protected boolean evaluate()
    {
        final IModelElement element = getPart().getModelElement();
        final SourceEditorService service = element.adapt( SourceEditorService.class );
        
        if( service != null )
        {
            try
            {
                return service.find( element, null );
            }
            catch( Exception e )
            {
                LoggingService.log( e );
            }
        }
        
        return false;
    }

}