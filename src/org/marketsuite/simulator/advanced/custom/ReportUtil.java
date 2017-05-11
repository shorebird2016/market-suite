package org.marketsuite.simulator.advanced.custom;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

class ReportUtil {
    //read report template into memory
    static ArrayList<String> openTemplate(String file_path) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file_path));
        ArrayList<String> file_list = new ArrayList<String>();
        String line;
        //skip all comment lines
        while ( (line = br.readLine()) != null ) {
            file_list.add(line);
        }
        return file_list;
    }
}
