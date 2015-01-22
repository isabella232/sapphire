/*******************************************************************************
 * Copyright (c) 2015 IBM Corporation and Other Contributors.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * This class was original copied from org.eclipse.ui.views.properties.tabbed 
 * then refactored and customized for Sapphire scenarios.
 * 
 * Contributors:
 *    Anthony Hunter - initial API and implementation
 *    Konstantin Komissarchik - customization for Sapphire
 *******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ScrollBar;

/**
 * Composite responsible for drawing the tabbed property sheet page.
 * 
 * @author Anthony Hunter
 */

public class TabbedPropertyComposite
    extends Composite {

    private Composite mainComposite;

    private Composite leftComposite;

    private ScrolledComposite scrolledComposite;

    private Composite tabComposite;

    private TabbedPropertyList listComposite;
    
    private Color colorWhite;
    
    /**
     * Constructor for a TabbedPropertyComposite
     * 
     * @param parent
     *            the parent widget.
     */
    public TabbedPropertyComposite(Composite parent) {
        super(parent, SWT.NO_FOCUS);

        this.colorWhite = parent.getDisplay().getSystemColor( SWT.COLOR_WHITE );

        createMainComposite();
    }

    /**
     * Create the main composite.
     */
    protected void createMainComposite() {
        this.mainComposite = new Composite( this, SWT.NO_FOCUS );
        this.mainComposite.setBackground( this.colorWhite );
        this.mainComposite.setBackgroundMode( SWT.INHERIT_DEFAULT );
        this.mainComposite.setLayout(new FormLayout());
        FormData formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        formData.top = new FormAttachment(0, 0);
        formData.bottom = new FormAttachment(100, 0);
        this.mainComposite.setLayoutData(formData);

        createMainContents();
    }

    /**
     * Create the contents in the main composite.
     */
    protected void createMainContents() {
        this.leftComposite = new Composite( this.mainComposite, SWT.NO_FOCUS );
        this.leftComposite.setLayout(new FormLayout());

        this.scrolledComposite = new ScrolledComposite(this.mainComposite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.NO_FOCUS);
        this.scrolledComposite.setBackground( this.colorWhite );
        this.scrolledComposite.setBackgroundMode( SWT.INHERIT_DEFAULT );

        FormData formData = new FormData();
        formData.left = new FormAttachment(this.leftComposite, 0);
        formData.right = new FormAttachment(100, 0);
        formData.top = new FormAttachment(0, 0);
        formData.bottom = new FormAttachment(100, 0);
        this.scrolledComposite.setLayoutData(formData);

        formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(this.scrolledComposite, 0);
        formData.top = new FormAttachment(0, 0);
        formData.bottom = new FormAttachment(100, 0);
        this.leftComposite.setLayoutData(formData);

        this.tabComposite = new Composite( this.scrolledComposite, SWT.NO_FOCUS );
        this.tabComposite.setLayout(new FormLayout());

        this.scrolledComposite.setContent(this.tabComposite);
        this.scrolledComposite.setAlwaysShowScrollBars(false);
        this.scrolledComposite.setExpandVertical(true);
        this.scrolledComposite.setExpandHorizontal(true);

        this.listComposite = new TabbedPropertyList(this.leftComposite);
        formData = new FormData();
        formData.left = new FormAttachment(0, 0);
        formData.right = new FormAttachment(100, 0);
        formData.top = new FormAttachment(0, 0);
        formData.bottom = new FormAttachment(100, 0);
        this.listComposite.setLayoutData(formData);
    }

    /**
     * Get the tabbed property list, which is the list of tabs on the left hand
     * side of this composite.
     * 
     * @return the tabbed property list.
     */
    public TabbedPropertyList getList() {
        return this.listComposite;
    }

    /**
     * Get the tab composite where sections display their property contents.
     * 
     * @return the tab composite.
     */
    public Composite getTabComposite() {
        return this.tabComposite;
    }

    /**
     * Get the scrolled composite which surrounds the title bar and tab
     * composite.
     * 
     * @return the scrolled composite.
     */
    public ScrolledComposite getScrolledComposite() {
        return this.scrolledComposite;
    }

    public void resizeScrolledComposite( final Composite currentTabComposite ) {
        Point currentTabSize = new Point(0, 0);
        if (currentTabComposite != null) {
            currentTabSize = currentTabComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT); 
        }
        getScrolledComposite().setMinSize(currentTabSize);

        ScrollBar verticalScrollBar = getScrolledComposite().getVerticalBar();
        if (verticalScrollBar != null) {
            Rectangle clientArea = getScrolledComposite().getClientArea();
            int increment = clientArea.height - 5;
            verticalScrollBar.setPageIncrement(increment);
        }

        ScrollBar horizontalScrollBar = getScrolledComposite().getHorizontalBar();
        if (horizontalScrollBar != null) {
            Rectangle clientArea = getScrolledComposite().getClientArea();
            int increment = clientArea.width - 5;
            horizontalScrollBar.setPageIncrement(increment);
        }
    }

    /**
     * @see org.eclipse.swt.widgets.Widget#dispose()
     */
    public void dispose() {
        this.listComposite.dispose();
        super.dispose();
    }
}
