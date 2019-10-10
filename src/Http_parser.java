import java.net.*; 
import java.io.*; 
import java.text.*;
import java.util.*;

public class Http_parser
{ 
	private static final String[][] HTTP_REPLY = {{"200", "OK"},{"400", "Bad Request"}, {"404", "Not Found"}};
  private BufferedReader reader;
  private String method, url;
  private Hashtable headers, params;
  private String ver;
  private static final boolean DEBUG = true;
	// initialize socket and input output streams 
	// constructor to put ip address and port 
	public Http_parser(InputStreamReader is) 
	{ 
		this.reader = new BufferedReader(is);
		this.method ="";
		this.url = "";
		this.ver = "";
		this.headers = new Hashtable();
		this.params = new Hashtable();
	}

	public int parseRequest() throws IOException {
    String prms[], cmd[], temp[];
    int ret, idx, i;

    ret = 200; // default is OK now
    String l1 = reader.readLine();
    if (l1 == null || l1.length() == 0) return 400;

    cmd = l1.split("\\s");
    if (cmd.length != 3) {
      return 400;
    }

    if (cmd[2].indexOf("HTTP/1.1") == 0){
			// Get version num
			this.ver = cmd[2].substring(5);
			if (DEBUG) {
				System.out.println("HTTP Version Number:" + this.ver);
			}
	}
    else ret = 400;

		
    if (cmd[0].equals("GET")) {
      method = cmd[0];

      idx = cmd[1].indexOf('?');
      if (idx < 0) url = cmd[1];
      else {
        url = URLDecoder.decode(cmd[1].substring(0, idx), "ISO-8859-1");
        prms = cmd[1].substring(idx+1).split("&");

        params = new Hashtable();
        for (i=0; i<prms.length; i++) {
          temp = prms[i].split("=");
          if (temp.length == 2) {
            // we use ISO-8859-1 as temporary charset and then
            // String.getBytes("ISO-8859-1") to get the data
            params.put(URLDecoder.decode(temp[0], "ISO-8859-1"),
                       URLDecoder.decode(temp[1], "ISO-8859-1"));
          }
          else if(temp.length == 1 && prms[i].indexOf('=') == prms[i].length()-1) {
            // handle empty string separatedly
            params.put(URLDecoder.decode(temp[0], "ISO-8859-1"), "");
          }
        }
      }
      parseHeaders();
      if (headers == null) ret = 400;
    }
    else if (cmd[0].equals("POST")) {
      // Not implemented
    }
    else {
      // Bad request
      ret = 400;
    }

    if (this.ver.equals("1.1") && getHeader("Host") == null) {
      ret = 400;
    }

    return ret;
  }

  private void parseHeaders() throws IOException {
    String line;
    int idx;

    line = reader.readLine();
    while (!line.equals("")) {
      idx = line.indexOf(':');
      if (idx < 0) {
        headers = null;
        break;
      }
      else {
        headers.put(line.substring(0, idx).toLowerCase(), line.substring(idx+1).trim());
      }
      line = reader.readLine();
    }
  }
	
  public String getMethod() {
    return method;
  }

  public String getHeader(String key) {
    if (headers != null)
      return (String) headers.get(key.toLowerCase());
    else return null;
  }

  public Hashtable getHeaders() {
    return headers;
  }

  public String getRequestURL() {
    return url;
  }

  public String getParam(String key) {
    return (String) params.get(key);
  }

  public Hashtable getParams() {
    return params;
  }

  public String getVersion() {
    return this.ver;
  }


  public static String getHttpReply(int codevalue) {
    String key, ret;
    int i;

    ret = null;
    key = "" + codevalue;
    for (i=0; i<HTTP_REPLY.length; i++) {
      if (HTTP_REPLY[i][0].equals(key)) {
        ret = codevalue + " " + HTTP_REPLY[i][1];
        break;
      }
    }

    return ret;
  }

  public static String getDateHeader() {
    SimpleDateFormat format;
    String ret;

    format = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss", Locale.US);
    format.setTimeZone(TimeZone.getTimeZone("GMT"));
    ret = "Date: " + format.format(new Date()) + " GMT";

    return ret;
  }
}
