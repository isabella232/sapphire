/******************************************************************************
 * Copyright (c) 2011 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329114] rewrite context help binding feature
 ******************************************************************************/

package org.eclipse.sapphire.ui;

import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelProperty;
import org.eclipse.sapphire.ui.util.SapphireHelpSystem;
import org.eclipse.swt.events.HelpEvent;
import org.eclipse.swt.events.HelpListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.SharedScrolledComposite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class SapphireRenderingContext 
{
    private final ISapphirePart part;
    private final SapphireRenderingContext parent;
    protected Shell shell;
    protected Composite composite;
    
    public SapphireRenderingContext( final ISapphirePart part,
                                     final Composite composite )
    {
        this( part, null, composite );
    }
    
    public SapphireRenderingContext( final ISapphirePart part,
                                     final SapphireRenderingContext parent,
                                     final Composite composite )
    {
        this.part = part;
        this.parent = parent;
        this.shell = composite.getShell();
        this.composite = composite;
    }
    
    public SapphireRenderingContext( final ISapphirePart part,
                                     final Shell shell )
    {
        this.part = part;
        this.parent = null;
        this.shell = shell;
        this.composite = null;
    }
    
    public ISapphirePart getPart()
    {
        return this.part;
    }
    
    public final SapphireImageCache getImageCache()
    {
        return getPart().getImageCache();
    }
    
    public Shell getShell()
    {
        return this.shell;
    }
    
    public Display getDisplay()
    {
        return this.shell.getDisplay();
    }
    
    public Composite getComposite()
    {
        return this.composite;
    }
    
    public void layout()
    {
        if( this.parent == null )
        {
            Composite composite = this.composite;
            
            while( composite != null )
            {
                if( composite instanceof SharedScrolledComposite )
                {
                    composite.getShell().layout( true, true );
                    ( (SharedScrolledComposite) composite ).reflow( true );
                    return;
                }
                else if( composite instanceof Shell )
                {
                    composite.layout( true, true );
                    return;
                }
                else
                {
                    composite = composite.getParent();
                }
            }
        }
        else
        {
            this.parent.layout();
        }
    }
    
    public void adapt( final Control control )
    {
        if( this.parent != null )
        {
            this.parent.adapt( control );
        }
    }
    
    public final void setHelp( final Control control,
                               final IModelElement modelElement,
                               final ModelProperty property )
    {
        final SapphireHelpContext context = new SapphireHelpContext(modelElement, property);
        if (context.getText() != null || (context.getRelatedTopics() != null && context.getRelatedTopics().length > 0)) 
        {
            control.addHelpListener(new HelpListener() 
            {
                public void helpRequested(HelpEvent event) 
                {
                    // determine a location in the upper right corner of the widget
                    Point point = SapphireHelpSystem.computePopUpLocation(event.widget.getDisplay());
                    // display the help
                    PlatformUI.getWorkbench().getHelpSystem().displayContext(context, point.x, point.y);
                }
            });
        }
    }
    
}
