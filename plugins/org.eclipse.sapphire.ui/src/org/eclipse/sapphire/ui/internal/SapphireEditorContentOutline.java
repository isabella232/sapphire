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

package org.eclipse.sapphire.ui.internal;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IStatusLineManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.sapphire.ui.SapphireEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.SubActionBars;
import org.eclipse.ui.internal.services.INestable;
import org.eclipse.ui.internal.services.IServiceLocatorCreator;
import org.eclipse.ui.internal.services.ServiceLocator;
import org.eclipse.ui.part.IPageBookViewPage;
import org.eclipse.ui.part.IPageSite;
import org.eclipse.ui.part.Page;
import org.eclipse.ui.part.PageBook;
import org.eclipse.ui.services.IDisposable;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;

/**
 * @author <a href="mailto:konstantin.komissarchik@oracle.com">Konstantin Komissarchik</a>
 */

@SuppressWarnings( { "restriction", "unqualified-field-access" } )

public final class SapphireEditorContentOutline
    
    extends Page
    implements IContentOutlinePage, ISelectionProvider, ISelectionChangedListener

{
    private SapphireEditor editor;
    private ISelection selection;
    private List<ISelectionChangedListener> listeners;
    private PageBook pagebook;
    private IContentOutlinePage currentPage;
    private IContentOutlinePage emptyPage;
    private Map<IContentOutlinePage,SubPageSite> pageToPageSite;
    
    public SapphireEditorContentOutline( final SapphireEditor editor )
    {
        this.editor = editor;
        this.listeners = new ArrayList<ISelectionChangedListener>();
        this.pageToPageSite = new HashMap<IContentOutlinePage,SubPageSite>();
    }
    
    public void addFocusListener( FocusListener listener )
    {
    }
    
    public void addSelectionChangedListener( ISelectionChangedListener listener )
    {
        this.listeners.add( listener );
    }
    
    public void createControl( Composite parent )
    {
        this.pagebook = new PageBook( parent, SWT.NONE );
    }
    
    public void setActionBars( IActionBars actionBars )
    {
        // It is more natural to think of the initial refresh operation happening in the 
        // createControl method, but refresh requires action bars to be available and the
        // setActionBars method is called after the createControl method.
        
        refresh();
    }
    
    public void dispose()
    {
        if( this.pagebook != null && ! this.pagebook.isDisposed() )
        {
            this.pagebook.dispose();
        }
        
        if( this.emptyPage != null )
        {
            this.emptyPage.dispose();
            this.emptyPage = null;
        }
        
        for( IContentOutlinePage page : this.pageToPageSite.keySet() )
        {
            page.dispose();
        }
        
        this.pageToPageSite.clear();
        this.pagebook = null;
        this.listeners = null;
        
        getSite().getActionBars().updateActionBars();
    }
    
    public boolean isDisposed()
    {
        return this.listeners == null;
    }
    
    public Control getControl()
    {
        return this.pagebook;
    }
    
    public PageBook getPagebook()
    {
        return this.pagebook;
    }
    
    public ISelection getSelection()
    {
        return this.selection;
    }
    
    public void makeContributions( IMenuManager menuManager,
                                   IToolBarManager toolBarManager,
                                   IStatusLineManager statusLineManager )
    {
    }
    
    public void removeFocusListener( FocusListener listener )
    {
    }
    
    public void removeSelectionChangedListener( ISelectionChangedListener listener )
    {
        this.listeners.remove( listener );
    }
    
    public void selectionChanged( SelectionChangedEvent event )
    {
        setSelection( event.getSelection() );
    }
    
    public void setFocus()
    {
        if( this.currentPage != null ) this.currentPage.setFocus();
    }
    
    private IContentOutlinePage getEmptyPage()
    {
        if( this.emptyPage == null ) this.emptyPage = new EmptyOutlinePage();
        return this.emptyPage;
    }
    
    public void refresh()
    {
        IContentOutlinePage page = this.editor.getContentOutlineForActivePage();
        
        if( page == null )
        {
            page = getEmptyPage();
        }
        
        if( this.currentPage != null )
        {
            this.currentPage.removeSelectionChangedListener( this );
            this.pageToPageSite.get( this.currentPage ).deactivate();
        }
        
        page.addSelectionChangedListener( this );
        
        this.currentPage = page;
        
        if( this.pagebook == null )
        {
            return;
        }
        
        Control control = page.getControl();
        
        final SubPageSite site;
        
        if( control == null || control.isDisposed() )
        {
            site = new SubPageSite( getSite() );
            this.pageToPageSite.put( page, site );
            
            if( page instanceof IPageBookViewPage )
            {
                try
                {
                    ( (IPageBookViewPage) page ).init( site );
                }
                catch( PartInitException e )
                {
                    throw new RuntimeException( e );
                }
            }
            
            page.createControl( this.pagebook );
            
            control = page.getControl();
        }
        else
        {
            site = this.pageToPageSite.get( this.currentPage );
        }
        
        site.activate();
        
        this.pagebook.showPage( control );
        this.currentPage = page;
        
        getSite().getActionBars().updateActionBars();
    }
    
    /**
     * Set the selection.
     */
    public void setSelection( ISelection selection )
    {
        this.selection = selection;
        if( this.listeners == null ) return;
        SelectionChangedEvent e = new SelectionChangedEvent( this, selection );
        for( int i = 0; i < this.listeners.size(); i++ )
        {
            this.listeners.get( i ).selectionChanged( e );
        }
    }
    
    private static final class EmptyOutlinePage implements IContentOutlinePage
    {
        private Composite control;
        
        /**
         * 
         */
        public EmptyOutlinePage()
        {
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.ui.part.IPage#createControl(org.eclipse.swt.widgets.Composite
         * )
         */
        public void createControl( Composite parent )
        {
            this.control = new Composite( parent, SWT.NULL );
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.part.IPage#dispose()
         */
        public void dispose()
        {
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.part.IPage#getControl()
         */
        public Control getControl()
        {
            return this.control;
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.ui.part.IPage#setActionBars(org.eclipse.ui.IActionBars)
         */
        public void setActionBars( IActionBars actionBars )
        {
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.part.IPage#setFocus()
         */
        public void setFocus()
        {
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ISelectionProvider#addSelectionChangedListener
         * (org.eclipse.jface.viewers.ISelectionChangedListener)
         */
        public void addSelectionChangedListener( ISelectionChangedListener listener )
        {
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.viewers.ISelectionProvider#getSelection()
         */
        public ISelection getSelection()
        {
            return new ISelection()
            {
                public boolean isEmpty()
                {
                    return true;
                }
            };
        }
        
        /*
         * (non-Javadoc)
         * 
         * @seeorg.eclipse.jface.viewers.ISelectionProvider#
         * removeSelectionChangedListener
         * (org.eclipse.jface.viewers.ISelectionChangedListener)
         */
        public void removeSelectionChangedListener( ISelectionChangedListener listener )
        {
        }
        
        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.viewers.ISelectionProvider#setSelection(org.eclipse
         * .jface.viewers.ISelection)
         */
        public void setSelection( ISelection selection )
        {
        }
    }
    
    private static final class SubPageSite implements IPageSite, INestable {

        /**
         * The "parent" view site
         */
        private IPageSite parentSite;

        /**
         * A selection provider set by the page. Value is <code>null</code> until
         * set.
         */
        private ISelectionProvider selectionProvider;

        /**
         * The localized service locator for this page site. This locator is never
         * <code>null</code>.
         */
        private final ServiceLocator serviceLocator;

        /**
         * The action bars for this site
         */
        private SubActionBars subActionBars;

        /**
         * Creates a new sub view site of the given parent view site.
         * 
         * @param parentViewSite
         *            the parent view site
         */
        public SubPageSite(final IPageSite parentViewSite) {
            Assert.isNotNull(parentViewSite);
            parentSite = parentViewSite;
            subActionBars = new SubActionBars(parentViewSite.getActionBars(), this);

            // Initialize the service locator.
            IServiceLocatorCreator slc = (IServiceLocatorCreator) parentSite
                    .getService(IServiceLocatorCreator.class);
            this.serviceLocator = (ServiceLocator) slc.createServiceLocator(
                    parentViewSite, null, new IDisposable(){
                        public void dispose() {
                            // TODO: Commented out due to Eclipse 4.2 incompatibility
                            /*final Control control = ((PartSite)parentViewSite).getPane().getControl();
                            if (control != null && !control.isDisposed()) {
                                ((PartSite)parentViewSite).getPane().doHide();
                            }*/
                        }
                    });
            initializeDefaultServices();
        }

        private void initializeDefaultServices() {
        }

        protected void dispose() {
            subActionBars.dispose();
            serviceLocator.dispose();
        }

        public IActionBars getActionBars() {
            return subActionBars;
        }

        @SuppressWarnings( "rawtypes" )
        public Object getAdapter(Class adapter) {
            return Platform.getAdapterManager().getAdapter(this, adapter);
        }

        public IWorkbenchPage getPage() {
            return parentSite.getPage();
        }

        public ISelectionProvider getSelectionProvider() {
            return selectionProvider;
        }

        @SuppressWarnings( "rawtypes" )
        public final Object getService(final Class key) {
            return serviceLocator.getService(key);
        }

        public Shell getShell() {
            return parentSite.getShell();
        }

        public IWorkbenchWindow getWorkbenchWindow() {
            return parentSite.getWorkbenchWindow();
        }
        
        @SuppressWarnings( "rawtypes" )
        public final boolean hasService(final Class key) {
            return serviceLocator.hasService(key);
        }

        public void registerContextMenu(String menuID, MenuManager menuMgr,
                ISelectionProvider selProvider) {
        }

        public void setSelectionProvider(ISelectionProvider provider) {
            selectionProvider = provider;
        }

        public void activate() {
            serviceLocator.activate();
            this.subActionBars.activate();
        }

        public void deactivate() {
            serviceLocator.deactivate();
            this.subActionBars.deactivate();
        }
    }
    
}
