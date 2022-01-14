/*
 * Copyright 2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */
package gchisto2;

import gchisto2.gui.MainFrame;
import gchisto2.utils.errorchecking.ErrorReporting;

/**
 * 应用程序入口类
 *
 * @author Tony Printezis
 */
public class GcHisto2Main {
    
    /**
     * 阻止此类被实例化
     */
    private GcHisto2Main() {
    }
    
    /**
     * 这是一个基于Java的GUI应用程序
     *
     * @param args 命令行参数
     */
    public static void main(String[] args) {
        ErrorReporting.setShowWarnings(true);
        MainFrame frame = new MainFrame();
        frame.setSize(900, 560);
        // 设置窗口位于屏幕中央
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.loadGcTraces(args);
    }
    
}
