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

package org.eclipse.sapphire.samples.calendar.ui;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.samples.internal.SapphireSamplesPlugin;
import org.eclipse.sapphire.ui.ISapphirePart;
import org.eclipse.sapphire.ui.actions.Action;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class ShowContactDetailsAction

    extends Action
    
{
    public ShowContactDetailsAction()
    {
        setImageDescriptor( SapphireSamplesPlugin.getImageDescriptor( "images/person.png" ) );
    }
    
    @Override
    protected Object run( final Shell shell )
    {
        final ISapphirePart part = getPart();
        final CalendarEditor editor = part.getNearestPart( CalendarEditor.class );
        final IModelElement modelElement = part.getModelElement();
        
        ContactDetailsJumpHandler.jump( editor, modelElement );
        
        return null;
    }
    
}
