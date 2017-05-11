package org.marketsuite.component.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * To deep clone an object
 */
public class ObjectCloner {
    // returns a deep copy of an object
    static public Object copy(Object oldObj) {
        ObjectOutputStream oos = null;
        ObjectInputStream ois = null;
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(bos);

            // serialize and pass the object
            oos.writeObject(oldObj);
            oos.flush();
            ByteArrayInputStream bin = new ByteArrayInputStream(bos.toByteArray());
            ois = new ObjectInputStream(bin);

            // return the new object
            return ois.readObject();
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
        finally {
            try {
            	if(oos != null){
            		oos.close();
            	}
            	if(ois != null){
            		ois.close();
            	}
            }
            catch (IOException ioe) {
            	ioe.printStackTrace();
            }
        }

        return null;
    }

}