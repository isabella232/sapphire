/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class SwtPresentation extends Presentation
{
    private Shell shell;
    private SwtResourceCache resources;
    
    public SwtPresentation( final SapphirePart part, final SwtPresentation parent, final Shell shell )
    {
        super( part, parent );

        this.shell = shell;
    }

    @Override
    public SwtPresentation parent()
    {
        return (SwtPresentation) super.parent();
    }
    
    public final Shell shell()
    {
        return this.shell;
    }
    
    public final Display display()
    {
        return this.shell.getDisplay();
    }
    
    public final SwtResourceCache resources()
    {
        if( parent() == null )
        {
            if( this.resources == null )
            {
                this.resources = new SwtResourceCache();
            }
            
            return this.resources;
        }
        else
        {
            return ( (SwtPresentation) parent() ).resources();
        }
    }
    
    public void dispose()
    {
        super.dispose();

        if( this.resources != null )
        {
            this.resources.dispose();
            this.resources = null;
        }
        
        this.shell = null;
    }

}
