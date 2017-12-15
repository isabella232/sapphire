/******************************************************************************
 * Copyright (c) 2012 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation and ongoing maintenance
 ******************************************************************************/

package org.eclipse.sapphire.ui.renderers.swt;

import static org.eclipse.sapphire.ui.SapphirePropertyEditor.DATA_BINDING;
import static org.eclipse.sapphire.ui.SapphirePropertyEditor.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.swt.renderer.GridLayoutUtil.glspacing;

import java.util.List;

import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFilter;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.SapphirePropertyEditor;
import org.eclipse.sapphire.ui.SapphireRenderingContext;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.internal.binding.AbstractBinding;
import org.eclipse.sapphire.ui.swt.renderer.SapphireToolBarActionPresentation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public abstract class AbstractSlushBucketPropertyEditorRenderer extends DefaultListPropertyEditorRenderer
{
    public AbstractSlushBucketPropertyEditorRenderer( final SapphireRenderingContext context,
                                                      final SapphirePropertyEditor part )
    {
        super( context, part );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final SapphirePropertyEditor part = getPart();

        final Composite mainComposite = createMainComposite( parent );
        mainComposite.setLayout( new FillLayout( SWT.HORIZONTAL ) );

        final Composite sourceTableComposite = new Composite( mainComposite, SWT.NONE );
        sourceTableComposite.setLayout( glspacing( glayout( 3, 0, 0 ), 0 ) );
        this.context.adapt( sourceTableComposite );
        
        final Composite decoratorComposite = new Composite( sourceTableComposite, SWT.NONE );
        decoratorComposite.setLayoutData( gdvalign( gd(), SWT.TOP ) );
        decoratorComposite.setLayout( glayout( 1, 0, 2, 0, 0 ) );
        this.context.adapt( decoratorComposite );
        
        final PropertyEditorAssistDecorator decorator 
            = new PropertyEditorAssistDecorator( part, this.context, decoratorComposite );
        
        decorator.control().setLayoutData( gd() );
        setDecorator( decorator );
        
        final Control sourceTable = createSourceControl( sourceTableComposite );
        sourceTable.setLayoutData( gdfill() );
        this.context.adapt( sourceTable );
        
        final Composite toolbarComposite = new Composite( sourceTableComposite, SWT.NONE );
        toolbarComposite.setLayoutData( gd() );
        toolbarComposite.setLayout( glayout( 1, 5, 5, 0, 0 ) );
        this.context.adapt( toolbarComposite );
        
        final ToolBar toolbar = new ToolBar( toolbarComposite, SWT.FLAT | SWT.VERTICAL );
        toolbar.setLayoutData( gd() );
        this.context.adapt( toolbar );
        
        final Composite tableComposite = new Composite( mainComposite, SWT.NONE );
        tableComposite.setLayout( glayout( 2, 0, 0 ) );
        this.context.adapt( tableComposite );
        
        decorator.addEditorControl( mainComposite );
        decorator.addEditorControl( sourceTableComposite );
        decorator.addEditorControl( decoratorComposite );
        decorator.addEditorControl( sourceTable );
        decorator.addEditorControl( toolbarComposite );
        decorator.addEditorControl( toolbar );
        
        final Table mainTable = (Table) super.createContents( tableComposite, true, true );
        
        final SapphireActionGroup actions = getActions();
        final SapphireAction moveRightAction = actions.getAction( SapphireActionSystem.ACTION_MOVE_RIGHT );
        final SapphireActionHandler moveRightActionHandler = createMoveRightActionHandler();
        moveRightActionHandler.init( moveRightAction, null );
        moveRightAction.addHandler( moveRightActionHandler );
        
        final SapphireToolBarActionPresentation actionsPresentation = new SapphireToolBarActionPresentation( getActionPresentationManager() );
        actionsPresentation.setToolBar( toolbar );
        
        actionsPresentation.addFilter
        (
            new SapphireActionHandlerFilter()
            {
                @Override
                public boolean check( final SapphireActionHandler handler )
                {
                    return ( handler == moveRightActionHandler );
                }
            }
        );
        
        actionsPresentation.render();
        
        addOnDisposeOperation
        (
            new Runnable()
            {
                public void run()
                {
                    moveRightAction.removeHandler( moveRightActionHandler );
                }
            }
        );

        final List<Control> relatedControls = getRelatedControls( mainTable );
        
        relatedControls.add( sourceTable );
        relatedControls.add( toolbar );
        
        ( (AbstractBinding) mainTable.getData( DATA_BINDING ) ).updateTargetAttributes();
        
        addControl( sourceTable );
    }
    
    protected abstract Control createSourceControl( Composite parent );

    protected abstract SapphireActionHandler createMoveRightActionHandler();
    
    @SuppressWarnings( "unchecked" )
    private static List<Control> getRelatedControls( final Control control )
    {
        return (List<Control>) control.getData( RELATED_CONTROLS );
    }
    
}
