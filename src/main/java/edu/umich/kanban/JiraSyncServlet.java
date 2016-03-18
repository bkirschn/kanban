package edu.umich.kanban;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.util.Properties;
import java.util.logging.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Servlet implementation class JiraSyncServlet
 */
public class JiraSyncServlet extends HttpServlet {
   private static final long serialVersionUID = 1L;
   private static Log M_log = LogFactory.getLog(JiraSyncServlet.class);
   private  Properties props = new Properties();
   String envHome = System.getenv("OPENSHIFT_DATA_DIR");
   
   public JiraSyncServlet() {
      try
      {
         envHome = System.getenv("OPENSHIFT_DATA_DIR");
         if ( null == envHome )
            envHome = System.getenv("CATALINA_HOME");
         
         FileInputStream fis = new FileInputStream(envHome+"/jira.properties");
         props.load( fis );
      }
      catch (Exception e)
      {
         e.printStackTrace();
      }
   }
   
   /**
    * @see HttpServlet#doGet(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doGet(HttpServletRequest request, HttpServletResponse response) throws  ServletException, IOException {
      response.setContentType("text/html");
      PrintWriter out =response.getWriter();
      String queryString = request.getQueryString();
      
      XmlMerge xmlMerge = new XmlMerge(props);
      
      // If queryString starts with ".", then it's a mergeFiles suffix
      if (queryString.startsWith(".")) {
         String mergeFiles="";
         try {
            String xmlPath = props.getProperty("xml.path");
            String xsltPath = props.getProperty("xslt.path");
            mergeFiles=xmlMerge.mergeFiles(queryString, 
                                           envHome+"/"+xsltPath,
                                           envHome+"/"+xmlPath);
         } catch (Exception e) {
            e.printStackTrace();
         }
         dataOutput(out, mergeFiles);
      }
      
      // Otherwise if the queryString equals wip, it's a "WIP" request
      else if(queryString.equals("wip")) {
         StringBuilder jsonWip=new StringBuilder("[ {");
         jsonWip.append("\"todo\" : \"")
            .append(props.getProperty("wip.todo"))
            .append("\"");
         jsonWip.append(", \"inprogress\" : \"");
         jsonWip.append(props.getProperty("wip.inprogress")).append("\"");
         jsonWip.append(", \"review\" : \"");
         jsonWip.append(props.getProperty("wip.review")).append("\"");
         jsonWip.append("} ]");
         dataOutput(out, jsonWip.toString());
      }
   }
   
   private void dataOutput(PrintWriter out, String mergeFiles) {
      out.println(mergeFiles);
      M_log.debug("merged json: "+mergeFiles);
      out.close();
   }
   
   /**
    * @see HttpServlet#doPost(HttpServletRequest request, HttpServletResponse response)
    */
   protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
      // TODO Auto-generated method stub
   }
   
}
