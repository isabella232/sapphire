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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.eclipse.ui.internal.PartSite;
import org.eclipse.ui.internal.PopupMenuExtender;
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
 * @author <a href="mailto:konstantin@komissarchik.net">Konstantin Komissarchik</a>
 */

@SuppressWarnings( { "restriction", "unqualified-field-access", "unchecked" } )

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
    
    public class EmptyOutlinePage
        implements IContentOutlinePage
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
    
    public class SubPageSite implements IPageSite, INestable {

        /**
         * The list of menu extender for each registered menu.
         */
        private ArrayList menuExtenders;

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
                            final Control control = ((PartSite)parentViewSite).getPane().getControl();
                            if (control != null && !control.isDisposed()) {
                                ((PartSite)parentViewSite).getPane().doHide();
                            }
                        }
                    });
            initializeDefaultServices();
        }

        /**
         * Initialize the slave services for this site.
         */
        private void initializeDefaultServices() {
        }

        /**
         * Disposes of the menu extender contributions.
         */
        protected void dispose() {
            if (menuExtenders != null) {
                HashSet managers = new HashSet(menuExtenders.size());
                for (int i = 0; i < menuExtenders.size(); i++) {
                    PopupMenuExtender ext = (PopupMenuExtender) menuExtenders.get(i);
                    managers.add(ext.getManager());
                    ext.dispose();
                }
                if (managers.size()>0) {
                    for (Iterator iterator = managers.iterator(); iterator
                            .hasNext();) {
                        MenuManager mgr = (MenuManager) iterator.next();
                        mgr.dispose();
                    }
                }
                menuExtenders = null;
            }
            subActionBars.dispose();
            serviceLocator.dispose();
        }

        /**
         * The PageSite implementation of this <code>IPageSite</code> method
         * returns the <code>SubActionBars</code> for this site.
         * 
         * @return the subactionbars for this site
         */
        public IActionBars getActionBars() {
            return subActionBars;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
         */
        public Object getAdapter(Class adapter) {
            return Platform.getAdapterManager().getAdapter(this, adapter);
        }

        /*
         * (non-Javadoc) Method declared on IPageSite.
         */
        public IWorkbenchPage getPage() {
            return parentSite.getPage();
        }

        /*
         * (non-Javadoc) Method declared on IPageSite.
         */
        public ISelectionProvider getSelectionProvider() {
            return selectionProvider;
        }

        public final Object getService(final Class key) {
            return serviceLocator.getService(key);
        }

        /*
         * (non-Javadoc) Method declared on IPageSite.
         */
        public Shell getShell() {
            return parentSite.getShell();
        }

        /*
         * (non-Javadoc) Method declared on IPageSite.
         */
        public IWorkbenchWindow getWorkbenchWindow() {
            return parentSite.getWorkbenchWindow();
        }

        public final boolean hasService(final Class key) {
            return serviceLocator.hasService(key);
        }

        /*
         * (non-Javadoc) Method declared on IPageSite.
         */
        public void registerContextMenu(String menuID, MenuManager menuMgr,
                ISelectionProvider selProvider) {
            if (menuExtenders == null) {
                menuExtenders = new ArrayList(1);
            }
            /*PartSite.registerContextMenu(menuID, menuMgr, selProvider, false,
                    parentSite.getPart(), menuExtenders);*/
        }

        /*
         * (non-Javadoc) Method declared on IPageSite.
         */
        public void setSelectionProvider(ISelectionProvider provider) {
            selectionProvider = provider;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.internal.services.INestable#activate()
         * 
         * @since 3.2
         */
        public void activate() {
            serviceLocator.activate();
            this.subActionBars.activate();
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.ui.internal.services.INestable#deactivate()
         * 
         * @since 3.2
         */
        public void deactivate() {
            serviceLocator.deactivate();
            this.subActionBars.deactivate();
        }
    }
    
}
