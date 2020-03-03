/*
 * ------------------------------------------------------------------------
 *
 * Copyright (C) 2014 Eli Lilly and Company Limited
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * ------------------------------------------------------------------------
*/
package org.erlwood.knime.utils.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;
import javax.swing.text.JTextComponent;


@SuppressWarnings("serial")
public class HintTextField extends JTextField implements FocusListener
{

    private String hint;

    public HintTextField ()
    {
        this("");
    }

    public HintTextField(final String hint)
    {
        setHint(hint);
        super.addFocusListener(this);
    }

    public void setHint(String hint)
    {
        this.hint = hint;
        setUI(new HintTextFieldUI(hint, true));
        //setText(this.hint);
    }


    public void focusGained(FocusEvent e)
    {
        if(this.getText().length() == 0)
        {
            super.setText("");
        }
    }

    public void focusLost(FocusEvent e)
    {
        if(this.getText().length() == 0)
        {
            setHint(hint);
        }
    }

    public String getText()
    {
        String typed = super.getText();
        return typed.equals(hint)?"":typed;
    }
}

class HintTextFieldUI extends javax.swing.plaf.basic.BasicTextFieldUI implements FocusListener
{

    private String hint;
    private boolean hideOnFocus;
    private Color color;

    public Color getColor()
    {
        return color;
    }

    public void setColor(Color color)
    {
        this.color = color;
        repaint();
    }

    private void repaint()
    {
        if(getComponent() != null)
        {
            getComponent().repaint();
        }
    }

    public boolean isHideOnFocus()
    {
        return hideOnFocus;
    }

    public void setHideOnFocus(boolean hideOnFocus)
    {
        this.hideOnFocus = hideOnFocus;
        repaint();
    }

    public String getHint()
    {
        return hint;
    }

    public void setHint(String hint)
    {
        this.hint = hint;
        repaint();
    }

    public HintTextFieldUI(String hint)
    {
        this(hint, false);
    }

    public HintTextFieldUI(String hint, boolean hideOnFocus)
    {
        this(hint, hideOnFocus, null);
    }

    public HintTextFieldUI(String hint, boolean hideOnFocus, Color color)
    {
        this.hint = hint;
        this.hideOnFocus = hideOnFocus;
        this.color = color;
    }


    protected void paintSafely(Graphics g)
    {
        super.paintSafely(g);
        JTextComponent comp = getComponent();
        if(hint != null && comp.getText().length() == 0 && (!(hideOnFocus && comp.hasFocus())))
        {
            if(color != null)
            {
                g.setColor(color);
            }
            else
            {
                g.setColor(Color.gray);
            }
            int padding = (comp.getHeight() - comp.getFont().getSize()) / 2;
            g.drawString(hint, 5, comp.getHeight() - padding - 1);
        }
    }


    public void focusGained(FocusEvent e)
    {
        if(hideOnFocus) repaint();

    }


    public void focusLost(FocusEvent e)
    {
        if(hideOnFocus) repaint();
    }

    protected void installListeners()
    {
        super.installListeners();
        getComponent().addFocusListener(this);
    }

    protected void uninstallListeners()
    {
        super.uninstallListeners();
        getComponent().removeFocusListener(this);
    }
}
