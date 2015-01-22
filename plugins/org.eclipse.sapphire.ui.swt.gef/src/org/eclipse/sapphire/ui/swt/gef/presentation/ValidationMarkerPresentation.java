/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Shenxue Zhou - initial implementation and ongoing maintenance
 *    Ling Hao - [383924]  Flexible diagram node shapes
 ******************************************************************************/

package org.eclipse.sapphire.ui.swt.gef.presentation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.MouseMotionListener;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.sapphire.Event;
import org.eclipse.sapphire.FilteredListener;
import org.eclipse.sapphire.Listener;
import org.eclipse.sapphire.LoggingService;
import org.eclipse.sapphire.Sapphire;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.SapphirePart;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContext;
import org.eclipse.sapphire.ui.assist.PropertyEditorAssistContributor;
import org.eclipse.sapphire.ui.assist.internal.ActionsSectionAssistContributor;
import org.eclipse.sapphire.ui.assist.internal.FactsAssistContributor;
import org.eclipse.sapphire.ui.assist.internal.InfoSectionAssistContributor;
import org.eclipse.sapphire.ui.assist.internal.ProblemsAssistContributor;
import org.eclipse.sapphire.ui.assist.internal.ProblemsSectionAssistContributor;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDialog;
import org.eclipse.sapphire.ui.assist.internal.ResetActionsAssistContributor;
import org.eclipse.sapphire.ui.assist.internal.RestoreInitialValueActionsAssistContributor;
import org.eclipse.sapphire.ui.assist.internal.ShowInSourceActionAssistContributor;
import org.eclipse.sapphire.ui.diagram.editor.DiagramNodePart;
import org.eclipse.sapphire.ui.diagram.editor.ValidationMarkerContentEvent;
import org.eclipse.sapphire.ui.diagram.editor.ValidationMarkerPart;
import org.eclipse.sapphire.ui.diagram.shape.def.ValidationMarkerSize;
import org.eclipse.sapphire.ui.forms.swt.SwtResourceCache;
import org.eclipse.sapphire.ui.forms.swt.SwtUtil;
import org.eclipse.sapphire.ui.swt.gef.figures.SmoothImageFigure;
import org.eclipse.sapphire.ui.swt.gef.model.DiagramResourceCache;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

/**
 * @author <a href="mailto:shenxue.zhou@oracle.com">Shenxue Zhou</a>
 * @author <a href="mailto:ling.hao@oracle.com">Ling Hao</a>
 */

@SuppressWarnings("restriction")
public class ValidationMarkerPresentation extends ShapePresentation 
{
    private static final ImageDescriptor IMG_ERROR_SMALL
			= SwtUtil.createImageDescriptor( SmoothImageFigure.class, "error_small.png" );
    private static final ImageDescriptor IMG_ERROR
			= SwtUtil.createImageDescriptor( SmoothImageFigure.class, "error.gif" );
    private static final ImageDescriptor IMG_WARNING_SMALL
			= SwtUtil.createImageDescriptor( SmoothImageFigure.class, "warning_small.png" );
    private static final ImageDescriptor IMG_WARNING
			= SwtUtil.createImageDescriptor( SmoothImageFigure.class, "warning.gif" );
    
    private static final List<Class<? extends PropertyEditorAssistContributor>> SYSTEM_CONTRIBUTORS
    		= new ArrayList<Class<? extends PropertyEditorAssistContributor>>();

	static
	{
	    SYSTEM_CONTRIBUTORS.add( InfoSectionAssistContributor.class );
	    SYSTEM_CONTRIBUTORS.add( FactsAssistContributor.class );
	    SYSTEM_CONTRIBUTORS.add( ProblemsSectionAssistContributor.class );
	    SYSTEM_CONTRIBUTORS.add( ActionsSectionAssistContributor.class );
	    SYSTEM_CONTRIBUTORS.add( ResetActionsAssistContributor.class );
	    SYSTEM_CONTRIBUTORS.add( RestoreInitialValueActionsAssistContributor.class );
	    SYSTEM_CONTRIBUTORS.add( ShowInSourceActionAssistContributor.class );
	    SYSTEM_CONTRIBUTORS.add( ProblemsAssistContributor.class );
	}
	
    private SwtResourceCache imageCache;
	private PropertyEditorAssistContext assistContext;
	private IFigure validationMarkerFigure;
	private Status problem;
	private final List<PropertyEditorAssistContributor> contributors;
	private Listener validationListener;
	
	public ValidationMarkerPresentation(DiagramPresentation parent, ValidationMarkerPart validationMarkerPart, 
			DiagramResourceCache resourceCache)
	{
		super(parent, validationMarkerPart, resourceCache);
		DiagramNodePart nodePart = validationMarkerPart.nearest(DiagramNodePart.class);
		this.imageCache = nodePart.getSwtResourceCache();
		
		this.contributors = new ArrayList<PropertyEditorAssistContributor>();
		
        final List<Class<?>> contributorClasses = new ArrayList<Class<?>>();        
        contributorClasses.addAll( SYSTEM_CONTRIBUTORS );
        for( Class<?> cl : contributorClasses )
        {
            try
            {
                this.contributors.add( (PropertyEditorAssistContributor) cl.newInstance() );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
        }
        final Listener contributorListener = new Listener()
        {
            @Override
            public void handle( final Event event )
            {
                Display.getDefault().asyncExec
                (
                    new Runnable()
                    {
                        public void run()
                        {
                            refreshAssistContext();
                        }
                    }
                );
            }
        };
        
        for( PropertyEditorAssistContributor contributor : this.contributors )
        {
            try
            {
                contributor.init( getContainerPart() );
            }
            catch( Exception e )
            {
                Sapphire.service( LoggingService.class ).log( e );
            }
            
            contributor.attach( contributorListener );
        }
        
        Collections.sort
        ( 
            this.contributors, 
            new Comparator<PropertyEditorAssistContributor>()
            {
                public int compare( final PropertyEditorAssistContributor c1,
                                    final PropertyEditorAssistContributor c2 )
                {
                    return ( c1.getPriority() - c2.getPriority() ); 
                }
            }
        );
        
        this.validationListener = new FilteredListener<ValidationMarkerContentEvent>()
        {
            @Override
            protected void handleTypedEvent( final ValidationMarkerContentEvent event )
            {
            	refresh();
            }
        };
        part().attach(this.validationListener);
        				
		refresh();
	}

	@Override
	public ValidationMarkerPart part()
	{
		return (ValidationMarkerPart) super.part();
	}
	
	public ValidationMarkerSize getSize()
	{
		return part().getSize();
	}
	
	@Override
	public void dispose()
	{
		part().detach(this.validationListener);
	}
	
	@Override
    public void render()
    {
		refresh();
		setFigure(this.validationMarkerFigure);
    }
	
	private SapphirePart getContainerPart()
	{
		return part().getContainerParent();
	}
	
	private void refresh()
	{
		refreshAssistContext();
		
		Image image = null;
		
		Status status = part().content();
		ValidationMarkerSize size = getSize();
		if (part().visible()) 
		{
			if (status.severity() == Status.Severity.WARNING) 
			{
				if (size == ValidationMarkerSize.SMALL) 
				{
					image = imageCache.image(IMG_WARNING_SMALL);
				} 
				else 
				{
					image = imageCache.image(IMG_WARNING);					
				}
			} 
			else if (status.severity() == Status.Severity.ERROR) 
			{
				if (size == ValidationMarkerSize.SMALL) 
				{
					image = imageCache.image(IMG_ERROR_SMALL);
				} 
				else 
				{
					image = imageCache.image(IMG_ERROR);
				}
			}
		}
		if (image != null)
		{
			this.validationMarkerFigure = new SmoothImageFigure(image);
			addMouseListener();
		}
		else 
		{
			this.validationMarkerFigure = null;
		}
	}
	
	public IFigure getValidationMarkerFigure()
	{
		return this.validationMarkerFigure;
	}
	
	private void addMouseListener()
	{
		this.validationMarkerFigure.addMouseMotionListener(new MouseMotionListener.Stub() 
		{
			@Override
			public void mouseEntered(MouseEvent me) 
			{
				validationMarkerFigure.setCursor( Display.getCurrent().getSystemCursor( SWT.CURSOR_HAND ) );
			}

		});
		
		this.validationMarkerFigure.addMouseListener(new MouseListener.Stub()
		{
			@Override
			public void mousePressed(MouseEvent me)
			{
				me.consume();
                Display.getDefault().asyncExec
                (
                    new Runnable()
                    {
                        public void run()
                        {
            				openAssistDialog();
                        }
                    }
                );
			}

		});		
	}
	
	private void openAssistDialog()
	{
		PropertyEditorAssistContext assistContext = getAssistContext();
        if( assistContext != null && ! assistContext.isEmpty() )
        {
        	// hide the context menu pad
        	getConfigurationManager().getDiagramEditor().getContextButtonManager().hideContextButtonsInstantly();
        	        	
            final org.eclipse.draw2d.geometry.Rectangle decoratorControlBounds = getFigure().getBounds().getCopy();
            org.eclipse.draw2d.geometry.Point draw2dPosition = new org.eclipse.draw2d.geometry.Point( decoratorControlBounds.x + decoratorControlBounds.width + 2, decoratorControlBounds.y + 2 );
            FigureCanvas canvas = getConfigurationManager().getDiagramEditor().getFigureCanvas();
            getFigure().translateToAbsolute(draw2dPosition);
            Point swtPosition = new Point(draw2dPosition.x, draw2dPosition.y);
            swtPosition = canvas.getDisplay().map(canvas, null, swtPosition);
            
            final PropertyEditorAssistDialog dialog = new PropertyEditorAssistDialog( getConfigurationManager().getDiagramEditor().getEditorSite().getShell(), 
            		swtPosition, this.assistContext );

            dialog.open();
        }
	}
	
	private PropertyEditorAssistContext getAssistContext()
	{
		if (this.assistContext == null)
		{
    		SapphirePart parentPart = getContainerPart();
			
	        this.assistContext = new PropertyEditorAssistContext( parentPart, page().getSite().getShell() );
	        this.problem = part().content();
            for( PropertyEditorAssistContributor c : this.contributors )
            {
                c.contribute( this.assistContext );
            }	            
	        
	        if( this.assistContext.isEmpty() )
	        {    
	            this.assistContext = null;
	        }
	        else
	        {
	            final Status.Severity valResultSeverity = this.problem.severity();
	            
	            if( valResultSeverity != Status.Severity.ERROR && valResultSeverity != Status.Severity.WARNING )
	            {
	                this.problem = null;
	            }
	        }					
		}
		return this.assistContext;
	}
	
	private void refreshAssistContext()
	{
		this.assistContext = null;
	}
}
