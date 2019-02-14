import java.util.Date;
import java.text.DateFormat;
import java.util.TimeZone;
import java.io.*;

public class WebHelper {

    private int errorCode;
    private String mimeType;
    private String dateToString;
    private File file;

    
    /**
    * Method to return the given type of file
    **/
    public String getMimeType() {
        return this.mimeType;
    }
    
    /**
    * Method to set mimeType of file
    * @param String - The type of file being served
    **/
    public void setMimeType(String type) {
        this.mimeType = type;
    }
    
    /**
    * Method to return errorCode
    **/
    public int getErrorCode() {
        return this.errorCode;
    }
    
    /**
    * Method to set the ErrroCode
    * @param integer to set errorcode to 
    **/
    public void setErrorCode(int num) {
        this.errorCode = num;
    }
    
    /**
    * Method to return the date and time from given timezone
    **/
    public String getDate() {
        return this.dateToString;
    }
    
    /**
    * Method to set the date according to timezone
    * @param String - TimeZone code
    **/
    public void setDate(String timezone) {
        Date date = new Date();
        DateFormat dateF = DateFormat.getDateTimeInstance();
        dateF.setTimeZone(TimeZone.getTimeZone(timezone));
        
        this.dateToString = dateF.format(date);
    }
    
    /**
    * Method to create and check if the given file exists
    * @param String - name of the file wanting to check
    **/
    public boolean checkFileExists(String fileName) {
        File file = new File(fileName);
        return file.exists() && !file.isDirectory() && file.isFile();
    }
    
}
