/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import org.eclipse.help.IContext;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class HelpActionHandler extends SapphireActionHandler 
{
    public static final String ID = "Sapphire.Help";
    
    public HelpActionHandler()
    {
        setId( ID );
    }
    
    @Override
    protected Object run( final Presentation context )
    {
        final IContext documentationContext = getPart().getDocumentationContext();
        
        if ( documentationContext != null )
        {
            PlatformUI.getWorkbench().getHelpSystem().displayHelp( documentationContext );
        }
        
        return null;
    }
    
}

