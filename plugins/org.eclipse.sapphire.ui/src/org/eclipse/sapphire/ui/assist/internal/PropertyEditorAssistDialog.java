/******************************************************************************
 * Copyright (c) 2014 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 *    Ling Hao - [bugzilla 329103] modernize the look of property editor assist popup
 ******************************************************************************/

package org.eclipse.sapphire.ui.assist.internal;

import java.util.Map;

import org.eclipse.sapphire.ImageData;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.EditFailedException;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContribution;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistSection;
import org.eclipse.sapphire.ui.forms.swt.Popup;
import org.eclipse.sapphire.ui.forms.swt.internal.text.SapphireFormText;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public class PropertyEditorAssistDialog extends Popup
{
    private final PropertyEditorAssistContext context;
    private final FormToolkit toolkit;
    private Composite composite;
    
    public PropertyEditorAssistDialog( final Shell shell,
                                       final Point point,
                                       final PropertyEditorAssistContext context )
    {
        super( shell, point);
        
        this.toolkit = new FormToolkit( Display.getDefault() );
        this.context = context;
    }
    
    protected void configureShell( Shell shell )
    {
        super.configureShell( shell );
    }
    
    @Override
    protected Control createContentArea( final Composite parent )
    {
        this.composite = (Composite) super.createContentArea( parent );
        
        final ScrolledForm form = this.toolkit.createScrolledForm( this.composite );
        
        TableWrapLayout layout = new TableWrapLayout();
        layout.leftMargin = 5;
        layout.rightMargin = 5;
        layout.topMargin = 5;
        layout.bottomMargin = 0;
        layout.verticalSpacing = 10;
        form.getBody().setLayout( layout );
        
        for( PropertyEditorAssistSection secdef : this.context.getSections().values() )
        {
            if( secdef.getContributions().isEmpty() )
            {
                continue;
            }
            
            final Section section 
                = this.toolkit.createSection( form.getBody(), ExpandableComposite.EXPANDED );
            
            this.toolkit.createCompositeSeparator( section );
            section.clientVerticalSpacing = 9;
            section.setText( secdef.getLabel() );
            
            TableWrapData td = new TableWrapData();
            td.align = TableWrapData.FILL;
            td.grabHorizontal = true;
            section.setLayoutData( td );
            
            final Composite composite = this.toolkit.createComposite( section );

            layout = new TableWrapLayout();
            layout.leftMargin = 0;
            layout.rightMargin = 0;
            layout.topMargin = 0;
            layout.bottomMargin = 0;
            layout.verticalSpacing = 0;
            composite.setLayout( layout );
            
            section.setClient( composite );
            
            for( final PropertyEditorAssistContribution contribution : secdef.getContributions() )
            {
                final SapphireFormText text = new SapphireFormText( composite, SWT.WRAP );
                
                td = new TableWrapData();
                td.align = TableWrapData.FILL;
                td.grabHorizontal = true;
                td.maxWidth = 600;
                text.setLayoutData( td );
                
                for( Map.Entry<String,ImageData> image : contribution.images().entrySet() )
                {
                    text.setImage( image.getKey(), image.getValue() );
                }
                
                final StringBuffer buffer = new StringBuffer();
                buffer.append( "<form>" );
                buffer.append( contribution.text() );
                buffer.append( "</form>" );
                text.setText( buffer.toString(), true, false );
                
                text.addHyperlinkListener
                (
                    new HyperlinkAdapter()
                    {
                        @Override
                        public void linkActivated( final HyperlinkEvent event )
                        {
                            try
                            {
                                final Runnable operation = contribution.link( (String) event.getHref() );
                                
                                if( operation != null )
                                {
                                    operation.run();
                                }
                            }
                            catch( Exception e )
                            {
                                // The EditFailedException happen here only as the result of the user explicitly deciding
                                // not not go forward with an action. They serve the purpose of an abort signal so we
                                // don't log them. Everything else gets logged.
                                
                                final EditFailedException editFailedException = EditFailedException.findAsCause( e );
                                
                                if( editFailedException == null )
                                {
                                    Sapphire.service( LoggingService.class ).log( e );
                                }
                            }
                            finally
                            {
                                close();
                            }
                        }
                     }
                );
            }
        }
        
        parent.pack();
        
        return this.composite;
    }
    
    public boolean close()
    {
        if( getShell() == null || getShell().isDisposed() )
        {
            return true;
        }
        
        this.toolkit.dispose();
        return super.close();
    }
    
    protected Control getFocusControl()
    {
        return this.composite;
    }
    
}
