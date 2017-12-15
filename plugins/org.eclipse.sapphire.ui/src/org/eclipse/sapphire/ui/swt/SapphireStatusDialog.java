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

package org.eclipse.sapphire.ui.swt;

import static org.eclipse.sapphire.ui.util.SwtUtil.gd;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdfill;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdhhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.gdwhint;
import static org.eclipse.sapphire.ui.util.SwtUtil.glayout;
import static org.eclipse.sapphire.ui.util.SwtUtil.glspacing;
import static org.eclipse.sapphire.ui.util.SwtUtil.valign;

import java.util.Iterator;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

/**
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

public final class SapphireStatusDialog

    extends Dialog
    
{
    private final IStatus status;
    private TreeViewer treeViewer;
    private Tree tree;
    
    private SapphireStatusDialog( final Shell shell,
                                  final IStatus status )
    {
        super( shell );
        this.status = status;
    }
    
    public static void open( final Shell shell,
                             final IStatus status )
    {
        if( status.isMultiStatus() )
        {
            ( new SapphireStatusDialog( shell, status ) ).open();
        }
        else
        {
            if( status.getSeverity() == IStatus.ERROR )
            {
                MessageDialog.openError( shell, Resources.errorDialogTitle, status.getMessage() );
            }
            else
            {
                MessageDialog.openWarning( shell, Resources.warningDialogTitle, status.getMessage() );
            }
        }
    }
    
    @Override
    
    protected Control createDialogArea( final Composite parent )
    {
        getShell().setText( Resources.problemsDialogTitle );
        
        final Composite composite = (Composite) super.createDialogArea( parent );
        composite.setLayout( glspacing( glayout( 2, 10, 10 ), 10 ) );
        
        final Label imageLabel = new Label( composite, SWT.NONE );
        imageLabel.setLayoutData( valign( gd(), SWT.TOP ) );
        
        if( this.status.getSeverity() == IStatus.ERROR )
        {
            imageLabel.setImage( getShell().getDisplay().getSystemImage( SWT.ICON_ERROR ) );
        }
        else
        {
            imageLabel.setImage( getShell().getDisplay().getSystemImage( SWT.ICON_WARNING ) );
        }
        
        this.treeViewer = new TreeViewer( composite, SWT.BORDER | SWT.MULTI );
        
        this.tree = this.treeViewer.getTree();
        this.tree.setLayoutData( gdwhint( gdhhint( gdfill(), 100 ), 400 ) );
        
        final ITreeContentProvider contentProvider = new ITreeContentProvider()
        {
            public Object[] getElements( final Object input )
            {
                return SapphireStatusDialog.this.status.getChildren();
            }

            public Object[] getChildren( final Object element )
            {
                return ( (IStatus) element ).getChildren();
            }

            public boolean hasChildren( final Object element )
            {
                return ( ( (IStatus) element ).getChildren().length > 0 );
            }

            public Object getParent( final Object element )
            {
                return findParent( null, SapphireStatusDialog.this.status.getChildren(), (IStatus) element );
            }
            
            private IStatus findParent( final IStatus parent,
                                        final IStatus[] children,
                                        final IStatus element )
            {
                for( IStatus child : children )
                {
                    if( child == element )
                    {
                        return parent;
                    }
                    else
                    {
                        final IStatus result = findParent( child, child.getChildren(), element );
                        
                        if( result != null )
                        {
                            return result;
                        }
                    }
                }
                
                return null;
            }

            public void inputChanged( final Viewer viewer,
                                      final Object oldInput,
                                      final Object newInput )
            {
            }
            
            public void dispose()
            {
            }
        };
        
        final ISharedImages sharedImages = PlatformUI.getWorkbench().getSharedImages();
        
        final ILabelProvider labelProvider = new ILabelProvider()
        {
            public String getText( final Object element )
            {
                return ( (IStatus) element ).getMessage();
            }

            public Image getImage( final Object element )
            {
                if( ( (IStatus) element ).getSeverity() == IStatus.ERROR )
                {
                    return sharedImages.getImage( ISharedImages.IMG_OBJS_ERROR_TSK );
                }
                else
                {
                    return sharedImages.getImage( ISharedImages.IMG_OBJS_WARN_TSK );
                }
            }

            public void addListener( final ILabelProviderListener listener )
            {
            }

            public void removeListener( final ILabelProviderListener listener )
            {
            }
            
            public boolean isLabelProperty( final Object element,
                                            final String property )
            {
                return false;
            }

            public void dispose()
            {
            }
        };
        
        this.treeViewer.setContentProvider( contentProvider );
        this.treeViewer.setLabelProvider( labelProvider );
        this.treeViewer.setInput( this );
        
        final Menu menu = new Menu( this.tree );
        this.tree.setMenu( menu );
        
        final MenuItem copyMenuItem = new MenuItem( menu, SWT.PUSH );
        copyMenuItem.setText( Resources.copyMenuItem );
        copyMenuItem.setImage( sharedImages.getImage( ISharedImages.IMG_TOOL_COPY ) );
        
        copyMenuItem.addSelectionListener
        (
            new SelectionAdapter()
            {
                @Override
                public void widgetSelected( final SelectionEvent event )
                {
                    copySelectionToClipboard();
                }
            }
        );
        
        return composite;
    }
    
    @Override
    
    protected void createButtonsForButtonBar( final Composite parent ) 
    {
        createButton( parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true );
    }
    
    @Override
    
    protected boolean isResizable()
    {
        return true;
    }
    
    private void copySelectionToClipboard()
    {
        final StringBuilder buf = new StringBuilder();
        final String nl = System.getProperty( "line.separator" );
        final IStructuredSelection selection = (IStructuredSelection) this.treeViewer.getSelection();
        
        for( Iterator<?> itr = selection.iterator(); itr.hasNext(); )
        {
            final IStatus st = (IStatus) itr.next();
            
            if( buf.length() > 0 )
            {
                buf.append( nl );
            }
            
            buf.append( st.getSeverity() == IStatus.ERROR ? Resources.errorMessagePrefix : Resources.warningMessagePrefix );
            buf.append( ' ' );
            buf.append( st.getMessage() );
        }
        
        final String text = buf.toString();
        
        if( text.length() > 0 )
        {
            final Clipboard clipboard = new Clipboard( this.tree.getDisplay() );
            final TextTransfer textTransfer = TextTransfer.getInstance();
            clipboard.setContents( new Object[] { text }, new Transfer[] { textTransfer } );
        }
    }
    
    private static final class Resources
    
        extends NLS
        
    {
        public static String errorDialogTitle;
        public static String warningDialogTitle;
        public static String problemsDialogTitle;
        public static String errorMessagePrefix;
        public static String warningMessagePrefix;
        public static String copyMenuItem;
        
        static
        {
            initializeMessages( SapphireStatusDialog.class.getName(), Resources.class );
        }
    }
    
}
