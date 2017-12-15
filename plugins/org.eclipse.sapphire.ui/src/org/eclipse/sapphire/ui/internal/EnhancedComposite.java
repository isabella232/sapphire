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

package org.eclipse.sapphire.ui.internal;

import static org.eclipse.sapphire.ui.swt.renderer.SwtUtil.setEnabledOnChildren;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public class EnhancedComposite

    extends Composite
    
{
    public EnhancedComposite( final Composite parent )
    {
        this( parent, SWT.NONE );
    }
    
    public EnhancedComposite( final Composite parent,
                              final int style )
    {
        super( parent, style );
    }
    
    @Override
    public void setEnabled( boolean enabled )
    {
        super.setEnabled( enabled );
        setEnabledOnChildren( this, enabled );
    }
    
}
