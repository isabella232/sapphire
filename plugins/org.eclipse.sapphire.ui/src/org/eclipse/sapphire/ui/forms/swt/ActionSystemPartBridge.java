/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt;

import org.eclipse.jface.action.Action;
import org.eclipse.sapphire.Disposable;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.modeling.CapitalizationType;
import org.eclipse.sapphire.modeling.localization.LabelTransformer;
import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionSystemPart;
import org.eclipse.sapphire.ui.def.SapphireActionType;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class ActionSystemPartBridge extends Action implements Disposable
{
	private SapphireActionSystemPart sapphireActionSystemPart;
	private Listener listener;
	
	public ActionSystemPartBridge( final SapphireActionSystemPart sapphireActionSystemPart )
	{
		this.sapphireActionSystemPart = sapphireActionSystemPart;
		
		this.listener = new Listener()
		{
            @Override
            public void handle( final Event event )
            {
                if( event instanceof SapphireActionSystemPart.EnablementChangedEvent )
                {
                    refreshEnablement();
                }
                else if( event instanceof SapphireActionSystemPart.CheckedStateChangedEvent )
                {
                    refreshCheckedState();
                }
                else if( event instanceof SapphireActionSystemPart.LabelChangedEvent )
                {
                    refreshText();
                }
                else if( event instanceof SapphireActionSystemPart.ImagesChangedEvent )
                {
                    refreshImage();
                }
            }
		};
		
		this.sapphireActionSystemPart.attach( this.listener );
		
		refreshEnablement();
		refreshText();
		refreshImage();
		
		if( ( this.sapphireActionSystemPart instanceof SapphireAction && ( (SapphireAction) this.sapphireActionSystemPart ).getType() == SapphireActionType.TOGGLE ) || 
		    ( this.sapphireActionSystemPart instanceof SapphireActionHandler && ( (SapphireActionHandler) this.sapphireActionSystemPart ).getAction().getType() == SapphireActionType.TOGGLE ) )
		{
		    refreshCheckedState();
		}
	}
	
	private void refreshEnablement()
	{
	    setEnabled( this.sapphireActionSystemPart.isEnabled() );
	}
	
	private void refreshCheckedState()
	{
	    setChecked( this.sapphireActionSystemPart.isChecked() );
	}
	
	private void refreshText()
	{
	    setText( LabelTransformer.transform( this.sapphireActionSystemPart.getLabel(), CapitalizationType.TITLE_STYLE, true ) );
	}
	
	private void refreshImage()
	{
	    setImageDescriptor( SwtUtil.toImageDescriptor( this.sapphireActionSystemPart.getImage( 16 ) ) );
	}

	@Override
	public void dispose()
	{
	    this.sapphireActionSystemPart.detach( this.listener );
	}

}
