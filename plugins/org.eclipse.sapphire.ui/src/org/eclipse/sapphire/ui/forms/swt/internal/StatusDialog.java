/******************************************************************************
 * Copyright (c) 2015 Oracle
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Konstantin Komissarchik - initial implementation
 ******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gd;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdfill;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdhhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdvalign;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.gdwhint;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glayout;
import static org.eclipse.sapphire.ui.forms.swt.GridLayoutUtil.glspacing;

import java.util.Iterator;
import java.util.SortedSet;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.sapphire.LocalizableText;
import org.eclipse.sapphire.Text;
import org.eclipse.sapphire.modeling.Status;
import org.eclipse.sapphire.ui.forms.swt.SwtResourceCache;
import org.eclipse.sapphire.ui.forms.swt.SwtUtil;
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
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

public final class StatusDialog extends Dialog
{
    @Text( "Error" )
    private static LocalizableText errorDialogTitle;
    
    @Text( "Warning" )
    private static LocalizableText warningDialogTitle;
    
    @Text( "Problems" )
    private static LocalizableText problemsDialogTitle;
    
    @Text( "[error]" )
    private static LocalizableText errorMessagePrefix;
    
    @Text( "[warning]" )
    private static LocalizableText warningMessagePrefix;
    
    @Text( "Copy" )
    private static LocalizableText copyMenuItemLabel;
    
    static
    {
        LocalizableText.init( StatusDialog.class );
    }

    private final Status status;
    private TreeViewer treeViewer;
    private Tree tree;
    
    private StatusDialog( final Shell shell, final Status status )
    {
        super( shell );
        this.status = status;
    }
    
    public static void open( final Shell shell,
                             final Status status )
    {
        if( status.children().isEmpty() )
        {
            if( status.severity() == Status.Severity.ERROR )
            {
                MessageDialog.openError( shell, errorDialogTitle.text(), status.message() );
            }
            else
            {
                MessageDialog.openWarning( shell, warningDialogTitle.text(), status.message() );
            }
        }
        else
        {
            ( new StatusDialog( shell, status ) ).open();
        }
    }
    
    @Override
    
    protected Control createDialogArea( final Composite parent )
    {
        getShell().setText( problemsDialogTitle.text() );
        
        final Composite composite = (Composite) super.createDialogArea( parent );
        composite.setLayout( glspacing( glayout( 2, 10, 10 ), 10 ) );
        
        final Label imageLabel = new Label( composite, SWT.NONE );
        imageLabel.setLayoutData( gdvalign( gd(), SWT.TOP ) );
        
        if( this.status.severity() == Status.Severity.ERROR )
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
                return StatusDialog.this.status.children().toArray();
            }

            public Object[] getChildren( final Object element )
            {
                return ( (Status) element ).children().toArray();
            }

            public boolean hasChildren( final Object element )
            {
                return ( ! ( (Status) element ).children().isEmpty() );
            }

            public Object getParent( final Object element )
            {
                return findParent( null, StatusDialog.this.status.children(), (Status) element );
            }
            
            private Status findParent( final Status parent,
                                       final SortedSet<Status> children,
                                       final Status element )
            {
                for( Status child : children )
                {
                    if( child == element )
                    {
                        return parent;
                    }
                    else
                    {
                        final Status result = findParent( child, child.children(), element );
                        
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
            private Image errorImage;
            private Image warningImage;
            
            public String getText( final Object element )
            {
                return ( (Status) element ).message();
            }

            public Image getImage( final Object element )
            {
                if( ( (Status) element ).severity() == Status.Severity.ERROR )
                {
                    return getErrorImage();
                }
                else
                {
                    return getWarningImage();
                }
            }
            
            private Image getErrorImage()
            {
                if( this.errorImage == null )
                {
                    this.errorImage = SwtUtil.createImageDescriptor( SwtResourceCache.class, "Error.png" ).createImage();
                }
                
                return this.errorImage;
            }
            
            private Image getWarningImage()
            {
                if( this.warningImage == null )
                {
                    this.warningImage = SwtUtil.createImageDescriptor( SwtResourceCache.class, "Warning.png" ).createImage();
                }
                
                return this.warningImage;
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
                if( this.errorImage != null )
                {
                    this.errorImage.dispose();
                }
                
                if( this.warningImage != null )
                {
                    this.warningImage.dispose();
                }
            }
        };
        
        this.treeViewer.setContentProvider( contentProvider );
        this.treeViewer.setLabelProvider( labelProvider );
        this.treeViewer.setInput( this );
        
        final Menu menu = new Menu( this.tree );
        this.tree.setMenu( menu );
        
        final MenuItem copyMenuItem = new MenuItem( menu, SWT.PUSH );
        copyMenuItem.setText( copyMenuItemLabel.text() );
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
            final Status st = (Status) itr.next();
            
            if( buf.length() > 0 )
            {
                buf.append( nl );
            }
            
            buf.append( st.severity() == Status.Severity.ERROR ? errorMessagePrefix : warningMessagePrefix );
            buf.append( ' ' );
            buf.append( st.message() );
        }
        
        final String text = buf.toString();
        
        if( text.length() > 0 )
        {
            final Clipboard clipboard = new Clipboard( this.tree.getDisplay() );
            final TextTransfer textTransfer = TextTransfer.getInstance();
            clipboard.setContents( new Object[] { text }, new Transfer[] { textTransfer } );
        }
    }
    
}
