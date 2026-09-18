package com.campusevent.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Formats strings into readable ASCII tables for console rendering.
 */
public class TableFormatter {

    public static void printTable(String title, String[] headers, List<String[]> rows) {
        if (title != null && !title.isEmpty()) {
            System.out.println("\n=== " + title.toUpperCase() + " ===");
        }
        if (headers == null || headers.length == 0) return;

        int numCols = headers.length;
        int[] colWidths = new int[numCols];

        for (int i = 0; i < numCols; i++) {
            colWidths[i] = headers[i].length();
        }

        if (rows != null) {
            for (String[] row : rows) {
                for (int i = 0; i < Math.min(row.length, numCols); i++) {
                    if (row[i] != null) {
                        colWidths[i] = Math.max(colWidths[i], row[i].length());
                    }
                }
            }
        }

        // Build horizontal divider border
        StringBuilder border = new StringBuilder("+");
        for (int w : colWidths) {
            for (int i = 0; i < w + 2; i++) {
                border.append("-");
            }
            border.append("+");
        }
        String borderStr = border.toString();

        System.out.println(borderStr);

        // Build header
        StringBuilder headerLine = new StringBuilder("|");
        for (int i = 0; i < numCols; i++) {
            headerLine.append(String.format(" %-" + colWidths[i] + "s |", headers[i]));
        }
        System.out.println(headerLine);
        System.out.println(borderStr);

        // Build rows
        if (rows == null || rows.isEmpty()) {
            StringBuilder emptyLine = new StringBuilder("|");
            int totalLen = borderStr.length() - 2;
            emptyLine.append(String.format(" %-" + (totalLen - 1) + "s|", "No records found."));
            System.out.println(emptyLine);
        } else {
            for (String[] row : rows) {
                StringBuilder rowLine = new StringBuilder("|");
                for (int i = 0; i < numCols; i++) {
                    String cell = (i < row.length && row[i] != null) ? row[i] : "";
                    rowLine.append(String.format(" %-" + colWidths[i] + "s |", cell));
                }
                System.out.println(rowLine);
            }
        }
        System.out.println(borderStr);
    }
}
