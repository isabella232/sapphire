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

package org.eclipse.sapphire.ui;

import org.eclipse.help.HelpSystem;
import org.eclipse.help.IContext;
import org.eclipse.sapphire.modeling.IModelElement;
import org.eclipse.sapphire.modeling.ModelElementType;
import org.eclipse.sapphire.modeling.ModelProperty;
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
    private final SapphirePart part;
    private final SapphireRenderingContext parent;
    protected Composite composite;
    private String helpContextIdPrefix;
    
    public SapphireRenderingContext( final SapphirePart part,
                                     final Composite composite )
    {
        this( part, null, composite );
    }
    
    public SapphireRenderingContext( final SapphirePart part,
                                     final SapphireRenderingContext parent,
                                     final Composite composite )
    {
        this.part = part;
        this.parent = parent;
        this.composite = composite;
        this.helpContextIdPrefix = null;
    }
    
    public SapphirePart getPart()
    {
        return this.part;
    }
    
    public final SapphireImageCache getImageCache()
    {
        return getPart().getImageCache();
    }
    
    public Shell getShell()
    {
        return this.composite.getShell();
    }
    
    public Display getDisplay()
    {
        return this.composite.getDisplay();
    }
    
    public Composite getComposite()
    {
        return this.composite;
    }
    
    public void layout()
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
        final String prefix = getHelpContextIdPrefix();

        if( prefix != null )
        {
            String helpContextId = null;
            ModelProperty p = property;
            
            while( p != null ) 
            {
                final StringBuilder buf = new StringBuilder();
                
                buf.append( prefix );
                buf.append( getUnqualifiedTypeName( p.getModelElementType() ) );
                buf.append( '-' );
                buf.append( property.getName() );
                
                IContext context = HelpSystem.getContext( buf.toString() );
                
                if( context != null ) 
                {
                    helpContextId = buf.toString();
                    break;
                }
                
                p = p.getBase();
            }
            
            if( helpContextId == null )
            {
                final ModelElementType type = ModelElementType.getModelElementType( modelElement.getClass() );
                final StringBuilder buf = new StringBuilder();
                
                buf.append( prefix );
                buf.append( getUnqualifiedTypeName( type ) );
                
                IContext context = HelpSystem.getContext( buf.toString() );
                
                if( context != null ) 
                {
                    helpContextId = buf.toString();
                }
            }
            
            if( helpContextId != null )
            {
                PlatformUI.getWorkbench().getHelpSystem().setHelp( control, helpContextId );
            }
        }
    }
    
    protected String getHelpContextIdPrefix()
    {
        if( this.helpContextIdPrefix != null )
        {
            return this.helpContextIdPrefix;
        }
        else if( this.parent != null )
        {
            return this.parent.getHelpContextIdPrefix( );
        }

        return null;
    }
    
    public void setHelpContextIdPrefix( final String helpContextIdPrefix )
    {
        this.helpContextIdPrefix = helpContextIdPrefix;
    }
    
    private String getUnqualifiedTypeName( final ModelElementType type )
    {
        final String className = type.getModelElementClass().getName();
        final int lastDot = className.lastIndexOf( '.' );
        
        return ( lastDot == -1 ? className : className.substring( lastDot + 1 ) );
    }
    
}
