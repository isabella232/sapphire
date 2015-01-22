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
 *    Mariot Chauvin <mariot.chauvin@obeo.fr> - bug 259553
 *    Konstantin Komissarchik - customization for Sapphire
 *******************************************************************************/

package org.eclipse.sapphire.ui.forms.swt.internal;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.ACC;
import org.eclipse.swt.accessibility.Accessible;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleControlAdapter;
import org.eclipse.swt.accessibility.AccessibleControlEvent;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseMoveListener;
import org.eclipse.swt.events.MouseTrackAdapter;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.TraverseEvent;
import org.eclipse.swt.events.TraverseListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.forms.FormColors;

/**
 * Shows the list of tabs in the tabbed property sheet page.
 * 
 * @author Anthony Hunter
 */

public class TabbedPropertyList
    extends Composite {

    private static final ListElement[] ELEMENTS_EMPTY = new ListElement[0];

    protected static final int NONE = -1;

    protected static final int INDENT = 7;

    private boolean focus = false;

    private ListElement[] elements;

    private int selectedElementIndex = NONE;

    private int topVisibleIndex = NONE;

    private int bottomVisibleIndex = NONE;

    private TopNavigationElement topNavigationElement;

    private BottomNavigationElement bottomNavigationElement;

    private int widestLabelIndex = NONE;

    private int tabsThatFitInComposite = NONE;

    private Color widgetForeground;

    private Color widgetBackground;

    private Color widgetNormalShadow;

    private Color widgetDarkShadow;

    private Color listBackground;

    private Color hoverGradientStart;

    private Color hoverGradientEnd;

    private Color defaultGradientStart;

    private Color defaultGradientEnd;

    private Color indentedDefaultBackground;

    private Color indentedHoverBackground;

    private Color navigationElementShadowStroke;

    private Color bottomNavigationElementShadowStroke1;

    private Color bottomNavigationElementShadowStroke2;

    public interface Item {

        /**
         * Get the icon image for the tab.
         * 
         * @return the icon image for the tab.
         */
        public Image getImage();

        /**
         * Get the text label for the tab.
         * 
         * @return the text label for the tab.
         */
        public String getText();

        /**
         * Determine if this tab is indented.
         * 
         * @return <code>true</code> if this tab is indented.
         */
        public boolean isIndented();
    }

    /**
     * One of the tabs in the tabbed property list.
     */
    public class ListElement extends Canvas {

        private Item tab;

        private int index;

        private boolean selected;

        private boolean hover;

        /**
         * Constructor for ListElement.
         * 
         * @param parent
         *            the parent Composite.
         * @param tab
         *            the tab item for the element.
         * @param index
         *            the index in the list.
         */
        public ListElement(Composite parent, final Item tab, int index) {
            super(parent, SWT.NO_FOCUS);
            this.tab = tab;
            this.hover = false;
            this.selected = false;
            this.index = index;

            addPaintListener(new PaintListener() {

                public void paintControl(PaintEvent e) {
                    paint(e);
                }
            });
            addMouseListener(new MouseAdapter() {

                public void mouseUp(MouseEvent e) {
                    if (!ListElement.this.selected) {
                        select(getIndex(ListElement.this));
                        /*
                         * We set focus to the tabbed property composite so that
                         * focus is moved to the appropriate widget in the
                         * section.
                         */
                        Composite tabbedPropertyComposite = getParent();
                        while (!(tabbedPropertyComposite instanceof TabbedPropertyComposite)) {
                            tabbedPropertyComposite = tabbedPropertyComposite
                                .getParent();
                        }
                        tabbedPropertyComposite.setFocus();
                    }
                }
            });
            addMouseMoveListener(new MouseMoveListener() {

                public void mouseMove(MouseEvent e) {
                    if (!ListElement.this.hover) {
                        ListElement.this.hover = true;
                        redraw();
                    }
                }
            });
            addMouseTrackListener(new MouseTrackAdapter() {

                public void mouseExit(MouseEvent e) {
                    ListElement.this.hover = false;
                    redraw();
                }
            });
        }

        /**
         * Set selected value for this element.
         * 
         * @param selected
         *            the selected value.
         */
        public void setSelected(boolean selected) {
            this.selected = selected;
            redraw();
        }

        /**
         * Paint the element.
         * 
         * @param e
         *            the paint event.
         */
        private void paint(PaintEvent e) {
            /*
             * draw the top two lines of the tab, same for selected, hover and
             * default
             */
            Rectangle bounds = getBounds();
            e.gc.setForeground(TabbedPropertyList.this.widgetNormalShadow);
            e.gc.drawLine(0, 0, bounds.width - 1, 0);
            e.gc.setForeground(TabbedPropertyList.this.listBackground);
            e.gc.drawLine(0, 1, bounds.width - 1, 1);

            /* draw the fill in the tab */
            if (this.selected) {
                e.gc.setBackground(TabbedPropertyList.this.listBackground);
                e.gc.fillRectangle(0, 2, bounds.width, bounds.height - 1);
            } else if (this.hover && this.tab.isIndented()) {
                e.gc.setBackground(TabbedPropertyList.this.indentedHoverBackground);
                e.gc.fillRectangle(0, 2, bounds.width - 1, bounds.height - 1);
            } else if (this.hover) {
                e.gc.setForeground(TabbedPropertyList.this.hoverGradientStart);
                e.gc.setBackground(TabbedPropertyList.this.hoverGradientEnd);
                e.gc.fillGradientRectangle(0, 2, bounds.width - 1,
                        bounds.height - 1, true);
            } else if (this.tab.isIndented()) {
                e.gc.setBackground(TabbedPropertyList.this.indentedDefaultBackground);
                e.gc.fillRectangle(0, 2, bounds.width - 1, bounds.height - 1);
            } else {
                e.gc.setForeground(TabbedPropertyList.this.defaultGradientStart);
                e.gc.setBackground(TabbedPropertyList.this.defaultGradientEnd);
                e.gc.fillGradientRectangle(0, 2, bounds.width - 1,
                        bounds.height - 1, true);
            }

            if (!this.selected) {
                e.gc.setForeground(TabbedPropertyList.this.widgetNormalShadow);
                e.gc.drawLine(bounds.width - 1, 1, bounds.width - 1,
                        bounds.height + 1);
            }

            int textIndent = INDENT;
            FontMetrics fm = e.gc.getFontMetrics();
            int height = fm.getHeight();
            int textMiddle = (bounds.height - height) / 2;

            if (this.tab.getImage() != null
                && !this.tab.getImage().isDisposed()) {
                /* draw the icon for the selected tab */
                if (this.tab.isIndented()) {
                    textIndent = textIndent + INDENT;
                } else {
                    textIndent = textIndent - 3;
                }
                e.gc.drawImage( this.tab.getImage(), textIndent, textMiddle );
                textIndent = textIndent + 16 + 5;
            } else if (this.tab.isIndented()) {
                textIndent = textIndent + INDENT;
            }

            /* draw the text */
            e.gc.setForeground(TabbedPropertyList.this.widgetForeground);
            if (this.selected) {
                /* selected tab is bold font */
                e.gc.setFont(JFaceResources.getFontRegistry().getBold(
                        JFaceResources.DEFAULT_FONT));
            }
            e.gc.drawText(this.tab.getText(), textIndent, textMiddle, true);
            if (((TabbedPropertyList) getParent()).focus && this.selected) {
                /* draw a line if the tab has focus */
                Point point = e.gc.textExtent(this.tab.getText());
                e.gc.drawLine(textIndent, bounds.height - 4, textIndent
                    + point.x, bounds.height - 4);
            }

            /* draw the bottom line on the tab for selected and default */
            if (!this.hover) {
                e.gc.setForeground(TabbedPropertyList.this.listBackground);
                e.gc.drawLine(0, bounds.height - 1, bounds.width - 2,
                        bounds.height - 1);
            }
        }

        /**
         * Get the tab item.
         * 
         * @return the tab item.
         */
        public Item getTabItem() {
            return this.tab;
        }

        public String toString() {
            return this.tab.getText();
        }
    }

    /**
     * The top navigation element in the tabbed property list. It looks like a
     * scroll button when scrolling is needed or is just a spacer when no
     * scrolling is required.
     */
    public class TopNavigationElement extends Canvas {

        /**
         * Constructor for TopNavigationElement.
         * 
         * @param parent
         *            the parent Composite.
         */
        public TopNavigationElement(Composite parent) {
            super(parent, SWT.NO_FOCUS);
            addPaintListener(new PaintListener() {

                public void paintControl(PaintEvent e) {
                    paint(e);
                }
            });
            addMouseListener(new MouseAdapter() {

                public void mouseUp(MouseEvent e) {
                    if (isUpScrollRequired()) {
                        TabbedPropertyList.this.bottomVisibleIndex--;
                        if (TabbedPropertyList.this.topVisibleIndex != 0) {
                            TabbedPropertyList.this.topVisibleIndex--;
                        }
                        layoutTabs();
                        TabbedPropertyList.this.topNavigationElement.redraw();
                        TabbedPropertyList.this.bottomNavigationElement.redraw();
                    }
                }
            });
        }

        /**
         * Paint the element.
         * 
         * @param e
         *            the paint event.
         */
        private void paint(PaintEvent e) {
            e.gc.setBackground(TabbedPropertyList.this.widgetBackground);
            e.gc.setForeground(TabbedPropertyList.this.widgetForeground);
            Rectangle bounds = getBounds();

            if (TabbedPropertyList.this.elements.length != 0) {
                e.gc.fillRectangle(0, 0, bounds.width, bounds.height);
                e.gc.setForeground(TabbedPropertyList.this.widgetNormalShadow);
                e.gc.drawLine(bounds.width - 1, 0, bounds.width - 1,
                    bounds.height - 1);
            } else {
                throw new IllegalStateException();
            }

            if (isUpScrollRequired()) {
                e.gc.setForeground(TabbedPropertyList.this.widgetDarkShadow);
                int middle = bounds.width / 2;
                e.gc.drawLine(middle + 1, 3, middle + 5, 7);
                e.gc.drawLine(middle, 3, middle - 4, 7);
                e.gc.drawLine(middle - 3, 7, middle + 4, 7);

                e.gc.setForeground(TabbedPropertyList.this.listBackground);
                e.gc.drawLine(middle, 4, middle + 1, 4);
                e.gc.drawLine(middle - 1, 5, middle + 2, 5);
                e.gc.drawLine(middle - 2, 6, middle + 3, 6);

                e.gc.setForeground(TabbedPropertyList.this.widgetNormalShadow);
                e.gc.drawLine(0, 0, bounds.width - 2, 0);
                e.gc.setForeground(TabbedPropertyList.this.navigationElementShadowStroke);
                e.gc.drawLine(0, 1, bounds.width - 2, 1);
                e.gc.drawLine(0, bounds.height - 1, bounds.width - 2,
                        bounds.height - 1);
            }
        }
    }

    /**
     * The top navigation element in the tabbed property list. It looks like a
     * scroll button when scrolling is needed or is just a spacer when no
     * scrolling is required.
     */
    public class BottomNavigationElement extends Canvas {

        /**
         * Constructor for BottomNavigationElement.
         * 
         * @param parent
         *            the parent Composite.
         */
        public BottomNavigationElement(Composite parent) {
            super(parent, SWT.NO_FOCUS);
            addPaintListener(new PaintListener() {

                public void paintControl(PaintEvent e) {
                    paint(e);
                }
            });
            addMouseListener(new MouseAdapter() {

                public void mouseUp(MouseEvent e) {
                    if (isDownScrollRequired()) {
                        TabbedPropertyList.this.topVisibleIndex++;
                        if (TabbedPropertyList.this.bottomVisibleIndex != TabbedPropertyList.this.elements.length - 1) {
                            TabbedPropertyList.this.bottomVisibleIndex++;
                        }
                        layoutTabs();
                        TabbedPropertyList.this.topNavigationElement.redraw();
                        TabbedPropertyList.this.bottomNavigationElement.redraw();
                    }
                }
            });
        }

        /**
         * Paint the element.
         * 
         * @param e
         *            the paint event.
         */
        private void paint(PaintEvent e) {
            e.gc.setBackground(TabbedPropertyList.this.widgetBackground);
            e.gc.setForeground(TabbedPropertyList.this.widgetForeground);
            Rectangle bounds = getBounds();

            if (TabbedPropertyList.this.elements.length != 0) {
                e.gc.fillRectangle(0, 0, bounds.width, bounds.height);
                e.gc.setForeground(TabbedPropertyList.this.widgetNormalShadow);
                e.gc.drawLine(bounds.width - 1, 0, bounds.width - 1,
                        bounds.height - 1);
                e.gc.drawLine(0, 0, bounds.width - 1, 0);

                e.gc.setForeground(TabbedPropertyList.this.bottomNavigationElementShadowStroke1);
                e.gc.drawLine(0, 1, bounds.width - 2, 1);
                e.gc.setForeground(TabbedPropertyList.this.bottomNavigationElementShadowStroke2);
                e.gc.drawLine(0, 2, bounds.width - 2, 2);
            } else {
                e.gc.setBackground(TabbedPropertyList.this.listBackground);
                e.gc.fillRectangle(0, 0, bounds.width, bounds.height);
            }

            if (isDownScrollRequired()) {
                e.gc.setForeground(TabbedPropertyList.this.widgetDarkShadow);
                int middle = bounds.width / 2;
                int bottom = bounds.height - 3;
                e.gc.drawLine(middle + 1, bottom, middle + 5, bottom - 4);
                e.gc.drawLine(middle, bottom, middle - 4, bottom - 4);
                e.gc.drawLine(middle - 3, bottom - 4, middle + 4, bottom - 4);

                e.gc.setForeground(TabbedPropertyList.this.listBackground);
                e.gc.drawLine(middle, bottom - 1, middle + 1, bottom - 1);
                e.gc.drawLine(middle - 1, bottom - 2, middle + 2, bottom - 2);
                e.gc.drawLine(middle - 2, bottom - 3, middle + 3, bottom - 3);

                e.gc.setForeground(TabbedPropertyList.this.widgetNormalShadow);
                e.gc.drawLine(0, bottom - 7, bounds.width - 2, bottom - 7);
                e.gc.setForeground(TabbedPropertyList.this.navigationElementShadowStroke);
                e.gc.drawLine(0, bottom + 2, bounds.width - 2, bottom + 2);
                e.gc.drawLine(0, bottom - 6, bounds.width - 2, bottom - 6);
            }
        }
    }

    /**
     * Constructor for TabbedPropertyList.
     * 
     * @param parent
     *            the parent widget.
     */
    public TabbedPropertyList(Composite parent) {
        super(parent, SWT.NO_FOCUS);
        removeAll();
        setLayout(new FormLayout());
        initColours();
        initAccessible();
        this.topNavigationElement = new TopNavigationElement(this);
        this.bottomNavigationElement = new BottomNavigationElement(this);

        this.addFocusListener(new FocusListener() {

            public void focusGained(FocusEvent e) {
                TabbedPropertyList.this.focus = true;
                int i = getSelectionIndex();
                if (i >= 0) {
                    TabbedPropertyList.this.elements[i].redraw();
                }
            }

            public void focusLost(FocusEvent e) {
                TabbedPropertyList.this.focus = false;
                int i = getSelectionIndex();
                if (i >= 0) {
                    TabbedPropertyList.this.elements[i].redraw();
                }
            }
        });
        this.addControlListener(new ControlAdapter() {

            public void controlResized(ControlEvent e) {
                computeTopAndBottomTab();
            }
        });
        this.addTraverseListener(new TraverseListener() {

            public void keyTraversed(TraverseEvent e) {
                if (e.detail == SWT.TRAVERSE_ARROW_PREVIOUS
                    || e.detail == SWT.TRAVERSE_ARROW_NEXT) {
                    int nMax = TabbedPropertyList.this.elements.length - 1;
                    int nCurrent = getSelectionIndex();
                    if (e.detail == SWT.TRAVERSE_ARROW_PREVIOUS) {
                        nCurrent -= 1;
                        nCurrent = Math.max(0, nCurrent);
                    } else if (e.detail == SWT.TRAVERSE_ARROW_NEXT) {
                        nCurrent += 1;
                        nCurrent = Math.min(nCurrent, nMax);
                    }
                    select(nCurrent);
                    redraw();
                } else {
                    e.doit = true;
                }
            }
        });
    }

    /**
     * Calculate the number of tabs that will fit in the tab list composite.
     */
    protected void computeTabsThatFitInComposite() {
        this.tabsThatFitInComposite = Math.round( ( (float) ( getSize().y - 22 ) ) / getTabHeight() );
        if (this.tabsThatFitInComposite <= 0) {
            this.tabsThatFitInComposite = 1;
        }
    }

    /**
     * Returns the element with the given index from this list viewer. Returns
     * <code>null</code> if the index is out of range.
     * 
     * @param index
     *            the zero-based index
     * @return the element at the given index, or <code>null</code> if the
     *         index is out of range
     */
    public Object getElementAt(int index) {
        if (index >= 0 && index < this.elements.length) {
            return this.elements[index];
        }
        return null;
    }

    /**
     * Returns the zero-relative index of the item which is currently selected
     * in the receiver, or -1 if no item is selected.
     * 
     * @return the index of the selected item
     */
    public int getSelectionIndex() {
        return this.selectedElementIndex;
    }

    /**
     * Removes all elements from this list.
     */
    public void removeAll() {
        if (this.elements != null) {
            for (int i = 0; i < this.elements.length; i++) {
                this.elements[i].dispose();
            }
        }
        this.elements = ELEMENTS_EMPTY;
        this.selectedElementIndex = NONE;
        this.widestLabelIndex = NONE;
        this.topVisibleIndex = NONE;
        this.bottomVisibleIndex = NONE;
    }

    /**
     * Sets the new list elements.
     * 
     * @param children
     */
    public void setElements(Object[] children) {
        if (this.elements != ELEMENTS_EMPTY) {
            removeAll();
        }
        this.elements = new ListElement[children.length];
        if (children.length == 0) {
            this.widestLabelIndex = NONE;
        } else {
            this.widestLabelIndex = 0;
            for (int i = 0; i < children.length; i++) {
                this.elements[i] = new ListElement(this, (Item) children[i], i);
                this.elements[i].setVisible(false);
                this.elements[i].setLayoutData(null);

                if (i != this.widestLabelIndex) {
                    String label = ((Item) children[i]).getText();
                    int width = getTextDimension(label).x;
                    if (((Item) children[i]).isIndented()) {
                        width = width + INDENT;
                    }
                    if (width > getTextDimension(((Item) children[this.widestLabelIndex])
                            .getText()).x) {
                        this.widestLabelIndex = i;
                    }
                }
            }
        }

        computeTopAndBottomTab();
    }

    /**
     * Selects one of the elements in the list.
     * 
     * @param index
     *            the index of the element to select.
     */
    public void select(int index) {
        if (getSelectionIndex() == index) {
            /*
             * this index is already selected.
             */
            return;
        }
        if (index >= 0 && index < this.elements.length) {
            int lastSelected = getSelectionIndex();
            this.elements[index].setSelected(true);
            this.selectedElementIndex = index;
            if (lastSelected != NONE) {
                this.elements[lastSelected].setSelected(false);
                if (getSelectionIndex() != this.elements.length - 1) {
                    /*
                     * redraw the next tab to fix the border by calling
                     * setSelected()
                     */
                    this.elements[getSelectionIndex() + 1].setSelected(false);
                }
            }
            this.topNavigationElement.redraw();
            this.bottomNavigationElement.redraw();

            if (this.selectedElementIndex < this.topVisibleIndex
                || this.selectedElementIndex > this.bottomVisibleIndex) {
                computeTopAndBottomTab();
            }
        }
        notifyListeners(SWT.Selection, new Event());
    }
    
    public void select( final Item item )
    {
        for( int i = 0; i < this.elements.length; i++ )
        {
            if( this.elements[ i ].getTabItem() == item )
            {
                select( i );
                break;
            }
        }
    }

    /**
     * Deselects all the elements in the list.
     */
    public void deselectAll() {
        if (getSelectionIndex() != NONE) {
            this.elements[getSelectionIndex()].setSelected(false);
            this.selectedElementIndex = NONE;
        }
    }
    
    /**
     * Redraws the tab at the specified index to account for label and image changes.
     */
    
    public void update( final int index )
    {
        this.elements[ index ].redraw();
    }

    private int getIndex(ListElement element) {
        return element.index;
    }

    public Point computeSize(int wHint, int hHint, boolean changed) {
        Point result = super.computeSize(hHint, wHint, changed);
        if (this.widestLabelIndex == -1) {
            throw new IllegalStateException();
        } else {
            Item widestTab = this.elements[this.widestLabelIndex].getTabItem();
            int width = getTextDimension(widestTab.getText()).x + INDENT;
            /*
             * To anticipate for the icon placement we should always keep the
             * space available after the label. So when the active tab includes
             * an icon the width of the tab doesn't change.
             */
            if (widestTab.getImage() != null) {
                width = width + 16 + 4;
            }
            if (widestTab.isIndented()) {
                width = width + 10;
            }
            /*
             * Add 10 pixels to the right of the longest string as a margin.
             */
            result.x = width + 10;
        }
        return result;
    }

    /**
     * Get the dimensions of the provided string.
     * 
     * @param text
     *            the string.
     * @return the dimensions of the provided string.
     */
    private Point getTextDimension(String text) {
        GC gc = new GC(this);
        gc.setFont(JFaceResources.getFontRegistry().getBold(
                JFaceResources.DEFAULT_FONT));
        Point point = gc.textExtent(text);
        point.x++;
        gc.dispose();
        return point;
    }

    /**
     * Initialize the colours used in the list.
     */
    private void initColours() {
        
        final Display display = Display.getCurrent();
        
        /*
         * Colour 3 COLOR_LIST_BACKGROUND
         */
        this.listBackground = display.getSystemColor(SWT.COLOR_LIST_BACKGROUND);

        /*
         * Colour 13 COLOR_WIDGET_BACKGROUND
         */
        this.widgetBackground = display.getSystemColor(SWT.COLOR_WIDGET_BACKGROUND);

        /*
         * Colour 15 COLOR_WIDGET_DARK_SHADOW
         */
        this.widgetDarkShadow = display.getSystemColor(SWT.COLOR_WIDGET_DARK_SHADOW);

        /*
         * Colour 16 COLOR_WIDGET_FOREGROUND
         */
        this.widgetForeground = display.getSystemColor(SWT.COLOR_WIDGET_FOREGROUND);

        /*
         * Colour 19 COLOR_WIDGET_NORMAL_SHADOW
         */
        this.widgetNormalShadow = display.getSystemColor(SWT.COLOR_WIDGET_NORMAL_SHADOW);

        RGB infoBackground = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND).getRGB();
        RGB white = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE).getRGB();
        RGB black = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK).getRGB();

        /*
         * gradient in the default tab: start colour WIDGET_NORMAL_SHADOW 100% +
         * white 20% + INFO_BACKGROUND 60% end colour WIDGET_NORMAL_SHADOW 100% +
         * INFO_BACKGROUND 40%
         */
        this.defaultGradientStart = new Color( display, FormColors.blend(infoBackground, FormColors.blend(white, this.widgetNormalShadow.getRGB(), 20), 60));
        this.defaultGradientEnd = new Color( display, FormColors.blend(infoBackground, this.widgetNormalShadow.getRGB(), 40));

        this.navigationElementShadowStroke = new Color( display, FormColors.blend(white, this.widgetNormalShadow.getRGB(), 55));
        this.bottomNavigationElementShadowStroke1 = new Color( display, FormColors.blend(black, this.widgetBackground.getRGB(), 10));
        this.bottomNavigationElementShadowStroke2 = new Color( display, FormColors.blend(black, this.widgetBackground.getRGB(), 5));

        /*
         * gradient in the hover tab: start colour WIDGET_BACKGROUND 100% +
         * white 20% end colour WIDGET_BACKGROUND 100% + WIDGET_NORMAL_SHADOW
         * 10%
         */
        this.hoverGradientStart = new Color( display, FormColors.blend(white, this.widgetBackground.getRGB(), 20));
        this.hoverGradientEnd = new Color( display, FormColors.blend(this.widgetNormalShadow.getRGB(), this.widgetBackground.getRGB(), 10));

        this.indentedDefaultBackground = new Color( display, FormColors.blend(white, this.widgetBackground.getRGB(), 10));
        this.indentedHoverBackground = new Color( display, FormColors.blend(white, this.widgetBackground.getRGB(), 75));
        
        addDisposeListener
        (
            new DisposeListener()
            {
                public void widgetDisposed( final DisposeEvent event )
                {
                    TabbedPropertyList.this.defaultGradientStart.dispose();
                    TabbedPropertyList.this.defaultGradientEnd.dispose();
                    TabbedPropertyList.this.navigationElementShadowStroke.dispose();
                    TabbedPropertyList.this.bottomNavigationElementShadowStroke1.dispose();
                    TabbedPropertyList.this.bottomNavigationElementShadowStroke2.dispose();
                    TabbedPropertyList.this.hoverGradientStart.dispose();
                    TabbedPropertyList.this.hoverGradientEnd.dispose();
                    TabbedPropertyList.this.indentedDefaultBackground.dispose();
                    TabbedPropertyList.this.indentedHoverBackground.dispose();
                }
            }
        );
    }

    /**
     * Get the height of a tab. The height of the tab is the height of the text
     * plus buffer.
     * 
     * @return the height of a tab.
     */
    private int getTabHeight() {
        int tabHeight = getTextDimension("").y + INDENT; //$NON-NLS-1$ 
        if (this.tabsThatFitInComposite == 1) {
            /*
             * if only one tab will fix, reduce the size of the tab height so
             * that the navigation elements fit.
             */
            int ret = getBounds().height - 20;
            return (ret > tabHeight) ? tabHeight
                : (ret < 5) ? 5
                    : ret;
        }
        return tabHeight;
    }

    /**
     * Determine if a downward scrolling is required.
     * 
     * @return true if downward scrolling is required.
     */
    private boolean isDownScrollRequired() {
        return this.elements.length > this.tabsThatFitInComposite
            && this.bottomVisibleIndex != this.elements.length - 1;
    }

    /**
     * Determine if an upward scrolling is required.
     * 
     * @return true if upward scrolling is required.
     */
    private boolean isUpScrollRequired() {
        return this.elements.length > this.tabsThatFitInComposite && this.topVisibleIndex != 0;
    }

    /**
     * Based on available space, figure out the top and bottom tabs in the list.
     */
    private void computeTopAndBottomTab() {
        computeTabsThatFitInComposite();
        if (this.elements.length == 0) {
            /*
             * no tabs to display.
             */
            this.topVisibleIndex = 0;
            this.bottomVisibleIndex = 0;
        } else if (this.tabsThatFitInComposite >= this.elements.length) {
            /*
             * all the tabs fit.
             */
            this.topVisibleIndex = 0;
            this.bottomVisibleIndex = this.elements.length - 1;
        } else if (getSelectionIndex() == NONE) {
            /*
             * there is no selected tab yet, assume that tab one would
             * be selected for now.
             */
            this.topVisibleIndex = 0;
            this.bottomVisibleIndex = this.tabsThatFitInComposite - 1;
        } else if (getSelectionIndex() + this.tabsThatFitInComposite > this.elements.length) {
            /*
             * the selected tab is near the bottom.
             */
            this.bottomVisibleIndex = this.elements.length - 1;
            this.topVisibleIndex = this.bottomVisibleIndex - this.tabsThatFitInComposite + 1;
        } else {
            /*
             * the selected tab is near the top.
             */
            this.topVisibleIndex = this.selectedElementIndex;
            this.bottomVisibleIndex = this.selectedElementIndex + this.tabsThatFitInComposite
                - 1;
        }
        layoutTabs();
    }

    /**
     * Layout the tabs.
     */
    private void layoutTabs() {
        //System.out.println("TabFit " + tabsThatFitInComposite + " length "
        //  + elements.length + " top " + topVisibleIndex + " bottom "
        //  + bottomVisibleIndex);
        if (this.tabsThatFitInComposite == NONE || this.elements.length == 0) {
            FormData formData = new FormData();
            formData.left = new FormAttachment(0, 0);
            formData.right = new FormAttachment(100, 0);
            formData.top = new FormAttachment(0, 0);
            formData.height = getTabHeight();
            this.topNavigationElement.setLayoutData(formData);

            formData = new FormData();
            formData.left = new FormAttachment(0, 0);
            formData.right = new FormAttachment(100, 0);
            formData.top = new FormAttachment(this.topNavigationElement, 0);
            formData.bottom = new FormAttachment(100, 0);
            this.bottomNavigationElement.setLayoutData(formData);
        } else {

            FormData formData = new FormData();
            formData.left = new FormAttachment(0, 0);
            formData.right = new FormAttachment(100, 0);
            formData.top = new FormAttachment(0, 0);
            formData.height = 10;
            this.topNavigationElement.setLayoutData(formData);

            /*
             * use nextElement to attach the layout to the previous canvas
             * widget in the list.
             */
            Canvas nextElement = this.topNavigationElement;

            for (int i = 0; i < this.elements.length; i++) {
                //System.out.print(i + " [" + elements[i].getText() + "]");
                if (i < this.topVisibleIndex || i > this.bottomVisibleIndex) {
                    /*
                     * this tab is not visible
                     */
                    this.elements[i].setLayoutData(null);
                    this.elements[i].setVisible(false);
                } else {
                    /*
                     * this tab is visible.
                     */
                    //System.out.print(" visible");
                    formData = new FormData();
                    formData.height = getTabHeight();
                    formData.left = new FormAttachment(0, 0);
                    formData.right = new FormAttachment(100, 0);
                    formData.top = new FormAttachment(nextElement, 0);
                    nextElement = this.elements[i];
                    this.elements[i].setLayoutData(formData);
                    this.elements[i].setVisible(true);
                }

                //if (i == selectedElementIndex) {
                //  System.out.print(" selected");
                //}
                //System.out.println("");
            }
            formData = new FormData();
            formData.left = new FormAttachment(0, 0);
            formData.right = new FormAttachment(100, 0);
            formData.top = new FormAttachment(nextElement, 0);
            formData.bottom = new FormAttachment(100, 0);
            formData.height = 10;
            this.bottomNavigationElement.setLayoutData(formData);
        }
        //System.out.println("");

        // layout so that we have enough space for the new labels
        Composite grandparent = getParent().getParent();
        grandparent.layout(true);
        layout(true);
    }

    /**
     * Initialize the accessibility adapter.
     */
    private void initAccessible() {
        final Accessible accessible = getAccessible();
        accessible.addAccessibleListener(new AccessibleAdapter() {

            public void getName(AccessibleEvent e) {
                if (getSelectionIndex() != NONE) {
                    e.result = TabbedPropertyList.this.elements[getSelectionIndex()].getTabItem()
                            .getText();
                }
            }

            public void getHelp(AccessibleEvent e) {
                if (getSelectionIndex() != NONE) {
                    e.result = TabbedPropertyList.this.elements[getSelectionIndex()].getTabItem()
                            .getText();
                }
            }
        });

        accessible.addAccessibleControlListener(new AccessibleControlAdapter() {

            public void getChildAtPoint(AccessibleControlEvent e) {
                Point pt = toControl(new Point(e.x, e.y));
                e.childID = (getBounds().contains(pt)) ? ACC.CHILDID_SELF
                    : ACC.CHILDID_NONE;
            }

            public void getLocation(AccessibleControlEvent e) {
                if (getSelectionIndex() != NONE) {
                    Rectangle location = TabbedPropertyList.this.elements[getSelectionIndex()]
                        .getBounds();
                    Point pt = toDisplay(new Point(location.x, location.y));
                    e.x = pt.x;
                    e.y = pt.y;
                    e.width = location.width;
                    e.height = location.height;
                }
            }

            public void getChildCount(AccessibleControlEvent e) {
                e.detail = 0;
            }

            public void getRole(AccessibleControlEvent e) {
                e.detail = ACC.ROLE_TABITEM;
            }

            public void getState(AccessibleControlEvent e) {
                e.detail = ACC.STATE_NORMAL | ACC.STATE_SELECTABLE
                    | ACC.STATE_SELECTED | ACC.STATE_FOCUSED
                    | ACC.STATE_FOCUSABLE;
            }
        });

        addListener(SWT.Selection, new Listener() {

            public void handleEvent(Event event) {
                if (isFocusControl()) {
                    accessible.setFocus(ACC.CHILDID_SELF);
                }
            }
        });

        addListener(SWT.FocusIn, new Listener() {

            public void handleEvent(Event event) {
                accessible.setFocus(ACC.CHILDID_SELF);
            }
        });
    }
}
