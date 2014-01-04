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

import static org.eclipse.sapphire.ui.forms.PropertyEditorPart.RELATED_CONTROLS;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;

import java.util.List;

import org.eclipse.sapphire.ui.SapphireAction;
import org.eclipse.sapphire.ui.SapphireActionGroup;
import org.eclipse.sapphire.ui.SapphireActionHandler;
import org.eclipse.sapphire.ui.SapphireActionHandlerFilter;
import org.eclipse.sapphire.ui.SapphireActionSystem;
import org.eclipse.sapphire.ui.assist.internal.PropertyEditorAssistDecorator;
import org.eclipse.sapphire.ui.forms.FormComponentPart;
import org.eclipse.sapphire.ui.forms.PropertyEditorPart;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.ToolBar;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public abstract class AbstractSlushBucketPropertyEditorPresentation extends TablePropertyEditorPresentation
{
    public AbstractSlushBucketPropertyEditorPresentation( final FormComponentPart part, final SwtPresentation parent, final Composite composite )
    {
        super( part, parent, composite );
    }

    @Override
    protected void createContents( final Composite parent )
    {
        final PropertyEditorPart part = part();

        final Composite mainComposite = createMainComposite( parent );
        mainComposite.setLayout( new FillLayout( SWT.HORIZONTAL ) );

        final Composite sourceTableComposite = new Composite( mainComposite, SWT.NONE );
        sourceTableComposite.setLayout( glspacing( glayout( 3, 0, 0 ), 0 ) );
        
        final Composite decoratorComposite = new Composite( sourceTableComposite, SWT.NONE );
        decoratorComposite.setLayoutData( gdvalign( gd(), SWT.TOP ) );
        decoratorComposite.setLayout( glayout( 1, 0, 2, 0, 0 ) );
        
        final PropertyEditorAssistDecorator decorator = new PropertyEditorAssistDecorator( part, decoratorComposite );
        
        decorator.control().setLayoutData( gd() );
        setDecorator( decorator );
        
        final Control sourceTable = createSourceControl( sourceTableComposite );
        sourceTable.setLayoutData( gdfill() );
        
        final Composite toolbarComposite = new Composite( sourceTableComposite, SWT.NONE );
        toolbarComposite.setLayoutData( gd() );
        toolbarComposite.setLayout( glayout( 1, 5, 5, 0, 0 ) );
        
        final ToolBar toolbar = new ToolBar( toolbarComposite, SWT.FLAT | SWT.VERTICAL );
        toolbar.setLayoutData( gd() );
        addControl( toolbar );
        
        final Composite tableComposite = new Composite( mainComposite, SWT.NONE );
        tableComposite.setLayout( glayout( 2, 0, 0 ) );
        
        decorator.addEditorControl( mainComposite );
        decorator.addEditorControl( sourceTableComposite );
        decorator.addEditorControl( decoratorComposite );
        decorator.addEditorControl( sourceTable );
        decorator.addEditorControl( toolbarComposite );
        decorator.addEditorControl( toolbar );
        
        final Table mainTable = (Table) super.createContents( tableComposite, true );
        
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
