package org.marketsuite.component.util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.LayoutManager;
import java.awt.Rectangle;
import java.util.Hashtable;
import java.util.StringTokenizer;

/*
  Layout to emulate David Flanagan's XmtLayout as described
  in his book Motif Tools
*/
public class XmtLayout implements LayoutManager {
    enum Just {
        FILLED,
        FLUSHLEFT,
        FLUSHTOP,
        FLUSHRIGHT,
        FLUSHBOTTOM,
        CENTERED,
    }

    enum Space {
        NONE,
        EVEN,
        LREVEN,
        INTERVAL,
        LCR,
        LTABBED,
        CTABBED,
        RTABBED,
    }

    static class PanelConstraints extends Object {
        public boolean fullSize = false;
        public int Iwidth = 0;
        public int Iheight = 0;
        public int x = 0;
        public int y = 0;
        public int width = 0;
        public int height = 0;
        public int stretch = 1;
        public int frame_type = -1;
        public Just just = Just.FILLED;
        public boolean useIwidth = false;
        public boolean useIheight = false;
        public int frame_where = 0;
        //public int      padX = 2;
        //public int      padY = 2;
        public int padTop = 2;
        public int padLeft = 2;
        public int padRight = 2;
        public int padBottom = 2;

        PanelConstraints(XmtLayout nl) {
            padLeft = nl.default_padLeft;
            padRight = nl.default_padRight;
            padTop = nl.default_padTop;
            padBottom = nl.default_padBottom;
        }

    }

    private boolean debug;
    public static final int FrameNone = 0;
    public static final int FrameBox = 1;
    public static final int FrameLeft = 2;
    public static final int FrameRight = 3;
    public static final int FrameTop = 4;
    public static final int FrameBottom = 5;
    public static final int FrameShadowIn = 0;
    public static final int FrameShadowOut = 1;
    public static final int FrameEtchedIn = 2;
    public static final int FrameEtchedOut = 3;
    public static final int FrameSingleLine = 4;
    public static final int FrameDoubleLine = 5;
    public static final int FrameInside = 0;
    public static final int FrameThrough = 1;
    public static final int FrameOutside = 2;
    private Dimension pref_dimension;
    //private boolean laid_out = false;
    public static final int margin_height_init = 8;
    public static final int margin_width_init = 8;
    public static final int space_init = 4;
    public int default_padX;
    public int default_padY;
    public int default_padLeft;
    public int default_padRight;
    public int default_padBottom;
    public int default_padTop;
    public static final int Horizontal = 0;
    public static final int Vertical = 1;
    public int maxHeight;
    public int maxWidth;
    public int num_visible;
    public int total_stretch;
    public boolean equalx = false;
    public boolean equaly = false;
    public int orient;
    public Space space_type = Space.NONE;
    public int item_stretch = 1;
    public int space_stretch = 1;
    public int space = space_init;
    public int marginHeight = margin_height_init;
    public int marginWidth = margin_width_init;
    public PanelConstraints pc;
    private Hashtable<Component, PanelConstraints> ht;

    public Hashtable getHashtable() {
        return ht;
    }

    //   Constructors
    public XmtLayout() {
        this("vertical");
    }

    public XmtLayout(String s) {
        default_padLeft = default_padRight = 2;
        default_padTop = default_padBottom = 2;
        //default_padX = 2;
        //default_padY = 2;
        parseLayout(s);
        ht = new Hashtable<Component, PanelConstraints>();
    }

    public void setDebug(boolean d) {
        debug = d;
    }

    private final int get_int(String s) {
        int val = 0;
        try {
            val = Integer.parseInt(s);
        }
        catch (NumberFormatException e) {
            val = 0;
        }
        return val;
    }

    private final boolean check_just(String s, PanelConstraints pc1) {
        boolean ret = true;
        try {
            pc1.just = Just.valueOf(s.toUpperCase());
        } catch (Exception ex) {
            ret = false;
        }
        return ret;
    }

    private final boolean check_space(String s) {
        boolean ret = true;
        try {
            space_type = Space.valueOf(s.toUpperCase());
        } catch (Exception ex) {
            ret = false;
        }
        return ret;
    }

    public final void
    parseLayout(String s) {
        StringTokenizer st = new StringTokenizer(s);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (check_space(tok)) continue;
            else if (tok.equalsIgnoreCase("equalx")) equalx = true;
            else if (tok.equalsIgnoreCase("equaly")) equaly = true;
            else if (tok.equalsIgnoreCase("space")) space = get_int(st.nextToken());
            else if (tok.startsWith("+"))
                space_stretch = get_int(tok.substring(1));
            else if (tok.startsWith("/"))
                item_stretch = get_int(tok.substring(1));
            else if (tok.equalsIgnoreCase("margin")) {
                int temp = get_int(st.nextToken());
                if (temp >= 0) marginWidth = temp;
                temp = get_int(st.nextToken());
                if (temp >= 0) marginHeight = temp;
            }
            else if (tok.equalsIgnoreCase("marginWidth"))
                marginWidth = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("marginHeight"))
                marginHeight = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("vertical")) orient = Vertical;
            else if (tok.equalsIgnoreCase("Col")) orient = Vertical;
            else if (tok.equalsIgnoreCase("horizontal")) orient = Horizontal;
            else if (tok.equalsIgnoreCase("Row")) orient = Horizontal;
            else if (tok.equalsIgnoreCase("padX")) {
                //default_padX = get_int(st.nextToken());
                default_padRight = default_padLeft = get_int(st.nextToken());
            }
            else if (tok.equalsIgnoreCase("padY")) {
                //default_padY = get_int(st.nextToken());
                default_padTop = default_padBottom = get_int(st.nextToken());
            }
            else if (tok.equalsIgnoreCase("padTop")) default_padTop = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("padBottom")) default_padBottom = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("padLeft")) default_padLeft = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("padRight")) default_padRight = get_int(st.nextToken());
            else System.out.println("Unknown layout token: " + this + "  " + tok + " :: " + s);
        }
    }

    public final void
    addLayoutComponent(String name, Component comp) {
        PanelConstraints pc1 = new PanelConstraints(this);
        StringTokenizer st = new StringTokenizer(name);
        while (st.hasMoreTokens()) {
            String tok = st.nextToken();
            if (check_just(tok, pc1)) continue;
            if (tok.equalsIgnoreCase("stretch"))
                pc1.stretch = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("fullSize"))
                pc1.fullSize = true;
            else if (tok.equalsIgnoreCase("Fixed"))
                pc1.stretch = 0;
            else if (tok.equalsIgnoreCase("Iwidth")) {
                pc1.Iwidth = get_int(st.nextToken());
                pc1.useIwidth = true;
            }
            else if (tok.equalsIgnoreCase("Iheight")) {
                pc1.Iheight = get_int(st.nextToken());
                pc1.useIheight = true;
            }
            else if (tok.equalsIgnoreCase("padTop"))
                pc1.padTop = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("padBottom"))
                pc1.padBottom = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("padRight"))
                pc1.padRight = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("padLeft"))
                pc1.padLeft = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("padX"))
                pc1.padLeft = pc1.padRight = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("padY"))
                pc1.padTop = pc1.padBottom = get_int(st.nextToken());
            else if (tok.equalsIgnoreCase("Etched"))
                pc1.frame_type = FrameEtchedIn;
            else if (tok.equalsIgnoreCase("EtchedOut"))
                pc1.frame_type = FrameEtchedOut;
            else if (tok.equalsIgnoreCase("Shadow"))
                pc1.frame_type = FrameShadowIn;
            else if (tok.equalsIgnoreCase("ShadowOut"))
                pc1.frame_type = FrameShadowOut;
        }
        ht.put(comp, pc1);
    }

    public final void
    addLayoutComponent(Component comp, Object constraints) {
        addLayoutComponent((String) constraints, comp);
    }

    public final void removeLayoutComponent(Component comp) {/**/}

    public final Dimension
    preferredLayoutSize(Container target) {
        Insets margins = target.getInsets();
        int width, height, margin_w, margin_h;
        int num_visible1, total_stretch1;
        int kwidth, kheight;
        int total_padX = 0, total_padY = 0;
        int box_width = 0;
        int box_height = 0;
        this.maxWidth = 0;
        this.maxHeight = 0;
        int inset_w = 0;
        int inset_h = 0;
        width = 0;
        height = 0;
        margin_w = this.marginWidth;
        margin_h = this.marginHeight;
        inset_w = 2 * margin_w + margins.left + margins.right;
        inset_h = 2 * margin_h + margins.top + margins.bottom;
        num_visible1 = 0;
        total_stretch1 = 0;
        for (int i = 0, max = target.getComponentCount(); i < max; i++) {
            Component c = target.getComponent(i);
            if (c.isVisible()) {
                PanelConstraints kid = ht.get(c);
                if (kid == null) {
                    kid = new PanelConstraints(this);
                    ht.put(c, kid);
                }
                if (kid.fullSize) continue;

                total_stretch1 = total_stretch1 + kid.stretch;
                num_visible1 = num_visible1 + 1;
                Dimension d = c.getPreferredSize();
                kwidth = d.width;
                kheight = d.height;
                if (kid.useIwidth) kwidth = kid.Iwidth;
                if (kid.useIheight) kheight = kid.Iheight;
                total_padX += kid.padLeft + kid.padRight;
                total_padY += kid.padTop + kid.padBottom;
                kwidth += kid.padLeft + kid.padRight;
                kheight += kid.padTop + kid.padBottom;
                if (kwidth > this.maxWidth) this.maxWidth = kwidth;
                if (kheight > this.maxHeight) this.maxHeight = kheight;
                height += kheight;
                width += kwidth;
            }
        }

        if (this.orient == Vertical) {   // Column
            if (this.equaly)
                height = this.maxHeight * num_visible1 + total_padY;
            box_width = this.maxWidth + inset_w;
            box_height = height + inset_h;
            switch (this.space_type) {
                case NONE:
                    break;
                case EVEN:
                    box_height = box_height + this.space * (num_visible1 + 1);
                    break;
                case LREVEN:
                    box_height = box_height + this.space * (num_visible1 - 1);
                    break;
                default:
                    box_height = box_height + this.space * num_visible1;
                    break;
            }
        }
        else {   // Row
            if (this.equalx)
                width = this.maxWidth * num_visible1 + total_padX;
            box_width = width + inset_w;
            box_height = this.maxHeight + inset_h;
            switch (this.space_type) {
                case NONE:
                    break;
                case EVEN:
                    box_width = box_width + this.space * (num_visible1 + 1);
                    break;
                case LREVEN:
                    box_width = box_width + this.space * (num_visible1 - 1);
                    break;
                default:
                    box_width = box_width + this.space * num_visible1;
                    break;
            }
        }
        this.num_visible = num_visible1;
        this.total_stretch = total_stretch1;
        pref_dimension = new Dimension(box_width, box_height);
        return pref_dimension;
    }

    public final Dimension minimumLayoutSize(Container target) {
        return new Dimension(0, 0);
    }

    public final Dimension maximumLayoutSize(Container target) {
        return new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE);
    }

    public float getLayoutAlignmentX(Container target) {
        return (float) 0.5;
    }

    public float getLayoutAlignmentY(Container target) {
        return (float) 0.5;
    }

    public void invalidateLayout(Container target) {
        //laid_out = false;
    }

    public final void layoutContainer(Container target) {
        Insets margins = target.getInsets();
        Rectangle current_bounds, new_bounds;
        int box_width, box_height;
        int pwidth, pheight;
        double kwidth, kheight;
        int numkids;
        int total_stretch1;
        double item_stretch1 = 0.0, space_stretch1 = 0.0;
        double space_delta = 0.0, item_delta = 0.0;
        double tab_delta;
        int x, y, delta;
        double init_x, init_y;
        Dimension target_size = target.getSize();
        pwidth = target_size.width;
        pheight = target_size.height;
        Dimension pref = preferredLayoutSize(target);
        box_width = pref.width;
        box_height = pref.height;
        if (debug)
            System.out.println("size= " + target_size.width + "    pref= " + pref.width);
        numkids = this.num_visible;
        if (numkids <= 0) return;
        total_stretch1 = this.space_stretch;
        if (this.total_stretch > 0)
            total_stretch1 += this.item_stretch;
        if (total_stretch1 > 0) {
            item_stretch1 = (double) this.item_stretch / total_stretch1;
            space_stretch1 = (double) this.space_stretch / total_stretch1;
        }
        pwidth -= 2 * marginWidth;
        pheight -= 2 * marginHeight;
        box_height -= 2 * marginHeight;
        box_width -= 2 * marginWidth;
        if (this.orient == Vertical) {
            delta = pheight - box_height;
            tab_delta = (double) pheight / (double) numkids;
        }
        else {
            delta = pwidth - box_width;
            tab_delta = (double) pwidth / (double) numkids;
        }
        init_x = marginWidth + margins.left;
        init_y = marginHeight + margins.top;
        switch (this.space_type) {
            case NONE:
                item_delta = 0;
                space_delta = 0;
                break;
            case EVEN:
                item_delta = item_stretch1 * delta;
                space_delta = space_stretch1 * delta / (numkids + 1);
                if (this.orient == Vertical)
                    init_y = (int) (init_y + this.space + space_delta);
                else
                    init_x = (int) (init_x + this.space + space_delta);
                break;
            case LREVEN:
                item_delta = item_stretch1 * delta;
                if (numkids > 1)
                    space_delta = space_stretch1 * delta / (numkids - 1);
                break;
            default:
                space_delta = delta / (double) numkids;
                item_delta = item_stretch1 * delta;
                break;
        }
        x = (int) init_x;
        y = (int) init_y;
        int num_vis = 0;
        for (int i = 0, max = target.getComponentCount(); i < max; i++) {
            Component c = target.getComponent(i);
            if (!c.isVisible()) continue;
            PanelConstraints kid = ht.get(c);
            if (kid == null) {
                kid = new PanelConstraints(this);
                ht.put(c, kid);
            }
            if (kid.fullSize) {
                c.setBounds(0, 0, target_size.width, target_size.height);
                continue;
            }
            pref = c.getPreferredSize();
            kwidth = pref.width;
            kheight = pref.height;
            if (kid.useIwidth) kwidth = kid.Iwidth;
            if (kid.useIheight) kheight = kid.Iheight;
            if (this.orient == Vertical) {    // Column
                if (this.equalx) kwidth = this.maxWidth;
                if (this.equaly) kheight = this.maxHeight;
                switch (kid.just) {
                    case FILLED:
                        //kwidth=pwidth - 2*kid.padX - margins.left - margins.right;
                        kwidth = pwidth - (kid.padLeft + kid.padRight) - margins.left - margins.right;
                        //x = marginWidth + margins.left + kid.padX;
                        x = marginWidth + margins.left + kid.padLeft;
                        break;
                    case FLUSHLEFT:
                    case FLUSHTOP:
                        //x = marginWidth + margins.left + kid.padX;
                        x = marginWidth + margins.left + kid.padLeft;
                        break;
                    case FLUSHRIGHT:
                    case FLUSHBOTTOM:
                        x = (int) (pwidth - margins.right - kwidth);
                        break;
                    case CENTERED:
                        x = (int) (marginWidth + (pwidth - kwidth) / 2);
                        break;
                }
                if (this.total_stretch > 0)
                    kheight = (int) (kheight +
                        item_delta *
                            kid.stretch /
                            this.total_stretch);
                switch (this.space_type) {
                    case LTABBED:
                        y = (int) init_y;
                        init_y += tab_delta;
                        break;
                    case CTABBED:
                        y = (int) (init_y + (tab_delta - kheight) / 2);
                        init_y += tab_delta;
                        break;
                    case RTABBED:
                        y = (int) (init_y + tab_delta - kheight);
                        init_y += tab_delta;
                        break;
                    default:
                        y = (int) (init_y + num_vis * space_delta);
                        //init_y += kheight + 2*kid.padY + this.space;
                        init_y += kheight + kid.padTop + kid.padBottom + this.space;
                        break;
                }
                current_bounds = c.getBounds();
                new_bounds = new Rectangle(x, y + kid.padTop, (int) kwidth, (int) kheight);
                if (!current_bounds.equals(new_bounds)) {
                    try {
                        c.setBounds(new_bounds);
                    }
                    catch (Exception ex1) {
                        System.out.println("component= " + c);
                    }
                    kid.x = x;
                    //kid.y = y+kid.padY;
                    kid.y = y + kid.padTop;
                    kid.width = (int) kwidth;
                    kid.height = (int) kheight;
                    if (current_bounds.width != kwidth || current_bounds.height != kheight)
                        c.doLayout();
                }
            }
            else {   // Row
                if (this.equaly) kheight = this.maxHeight;
                if (this.equalx) kwidth = this.maxWidth;
                switch (kid.just) {
                    case FILLED:
                        //y = marginHeight + margins.top + kid.padY;
                        y = marginHeight + margins.top + kid.padTop;
                        //kheight = pheight - margins.top - margins.bottom - 2*kid.padY;
                        kheight = pheight - margins.top - margins.bottom - (kid.padTop + kid.padBottom);
                        break;
                    case FLUSHTOP:
                    case FLUSHLEFT:
                        //y = marginHeight + margins.top + kid.padY;
                        y = marginHeight + margins.top + kid.padTop;
                        break;
                    case FLUSHBOTTOM:
                    case FLUSHRIGHT:
                        y = (int) (pheight - kheight - margins.bottom);
                        break;
                    case CENTERED:
                        y = (int) (marginHeight + (pheight - kheight) / 2);
                        break;
                }
                if (this.total_stretch > 0)
                    kwidth = kwidth +
                        item_delta *
                            kid.stretch /
                            this.total_stretch;
                switch (this.space_type) {
                    case LTABBED:
                        x = (int) init_x;
                        init_x += tab_delta;
                        break;
                    case CTABBED:
                        x = (int) (init_x + (tab_delta - kwidth) / 2);
                        init_x += tab_delta;
                        break;
                    case RTABBED:
                        x = (int) (init_x + tab_delta - kwidth);
                        init_x += tab_delta;
                        break;
                    default:
                        x = (int) (init_x + num_vis * space_delta);
                        //init_x += kwidth + 2*kid.padX + this.space ;
                        init_x += kwidth + kid.padLeft + kid.padRight + this.space;
                        break;
                }
                current_bounds = c.getBounds();
                new_bounds = new Rectangle(x + kid.padLeft, y, (int) kwidth, (int) kheight);
                if (!current_bounds.equals(new_bounds)) {
                    try {
                        c.setBounds(new_bounds);
                    }
                    catch (Exception ex2) {
                        System.out.println("component= " + c);
                    }
                    //kid.x = x+kid.padX;
                    kid.x = x + kid.padLeft;
                    kid.y = y;
                    kid.width = (int) kwidth;
                    kid.height = (int) kheight;

                    if (current_bounds.width != (int) kwidth || current_bounds.height != (int) kheight)
                        c.doLayout();
                }
            }
            num_vis++;
        }
    }
}