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

package org.eclipse.sapphire.ui.forms.swt.internal;

import java.util.Calendar;
import java.util.Date;

import org.eclipse.sapphire.MasterConversionService;
import org.eclipse.sapphire.ui.Presentation;
import org.eclipse.sapphire.ui.forms.BrowseActionHandler;
import org.eclipse.sapphire.ui.forms.PropertyEditorCondition;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.sapphire.ui.forms.swt.FormComponentPresentation;
import org.eclipse.sapphire.ui.forms.swt.Popup;
import org.eclipse.sapphire.ui.forms.swt.PropertyEditorPresentation2;
import org.eclipse.sapphire.util.MutableReference;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DateTime;

/**
 * Opens a calendar to allow date selection. Activates if the property is a value property of type Date.
 * 
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class CalendarBrowseActionHandler extends BrowseActionHandler
{
    public static final String ID = "Sapphire.Browse.Calendar";
    
    public CalendarBrowseActionHandler()
    {
        setId( ID );
    }
    
    @Override
    protected String browse( final Presentation context )
    {
        final MutableReference<String> result = new MutableReference<String>();
        
        final Popup dialog = new Popup( ( (FormComponentPresentation) context ).shell(), null )
        {
            private DateTime calendar;
            
            @Override
            protected Point getInitialLocation( final Point size )
            {
                return ( (PropertyEditorPresentation2) context ).getActionPopupPosition( size.x, size.y );
            }

            @Override
            protected Control createContentArea( final Composite parent )
            {
                this.calendar = new DateTime( parent, SWT.CALENDAR );
                
                final Date existing = (Date) property().content();
                
                if( existing != null )
                {
                    final Calendar cal = Calendar.getInstance();
                    cal.setTime( existing );
                    
                    this.calendar.setYear( cal.get( Calendar.YEAR ) );
                    this.calendar.setMonth( cal.get( Calendar.MONTH ) );
                    this.calendar.setDay( cal.get( Calendar.DATE ) );
                }
                
                this.calendar.addMouseListener
                (
                    new MouseAdapter()
                    {
                        @Override
                        public void mouseDoubleClick( final MouseEvent event )
                        {
                            registerSelectionAndClose();
                        }
                    }
                );
                
                this.calendar.addKeyListener
                (
                    new KeyAdapter()
                    {
                        @Override
                        public void keyPressed( final KeyEvent event )
                        {
                            if( event.character == SWT.CR )
                            {
                                registerSelectionAndClose();
                            }
                        }
                    }
                );
                
                return calendar;
            }
            
            private void registerSelectionAndClose()
            {
                final Calendar cal = Calendar.getInstance();
                cal.set( this.calendar.getYear(), this.calendar.getMonth(), this.calendar.getDay() );
                final Date date = cal.getTime();
                
                result.set( property().service( MasterConversionService.class ).convert( date, String.class ) );
                
                close();
            }
        };
        
        dialog.setBlockOnOpen( true );
        
        dialog.open();
        
        return result.get();
    }
    
    public static final class Condition extends PropertyEditorCondition
    {
        @Override
        protected boolean evaluate( final PropertyEditorPart part )
        {
            return ( part.property().definition().getTypeClass() == Date.class );
        }
    }
    
}