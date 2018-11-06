package io.fabric8.docker.sample.demo;

import java.io.*;
import java.sql.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.flywaydb.core.Flyway;

/**
 * Popup Tomcat, migrate DB and start LogService
 *
 * @author roland
 * @since 08.08.14
 */
public class LogService extends HttpServlet {

    // Fireup tomcat and register this servlet
    public static void main(String[] args) throws LifecycleException, SQLException {
        Tomcat tomcat = new Tomcat();
        tomcat.setPort(8080);
        File base = new File(System.getProperty("java.io.tmpdir"));
        Context rootCtx = tomcat.addContext("/", base.getAbsolutePath());
        Tomcat.addServlet(rootCtx, "log", new LogService());
        rootCtx.addServletMapping("/*", "log");
        tomcat.start();
        tomcat.getServer().await();
    }

    // Log into DB and print out all logs.
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        try (Connection connection = DriverManager.getConnection(getConnectionUrl(),
                                                                 "postgres",
                                                                 null)) {
            // Insert current request in DB ...
            insertLog(req, connection);

            // ... and then return all logs stored so far
            resp.setContentType("text/plain");
            PrintWriter out = resp.getWriter();
            printOutLogs(connection, out);

        } catch (SQLException e) {
            throw new ServletException("Cannot update DB: " + e,e);
        }
    }

    // Init DB and create table if required
    public LogService() throws SQLException {
        Flyway flyway = new Flyway();
        flyway.setDataSource(getConnectionUrl(), "postgres", null);
        flyway.migrate();
    }


    // ===================================================================================
    // Helper methods

    // Extract connection URL from environment variable as setup by docker (or manually)
    private String getConnectionUrl() {
        String dbPort = System.getenv("DB_PORT");

        // Fallback for alexec Plugin which does not support configuration of link aliases
        if (dbPort == null) {
            dbPort = System.getenv("SHOOTOUT_DOCKER_MAVEN_DB_PORT");
        }
        if (dbPort == null) {
            throw new IllegalArgumentException("No DB_PORT or SHOOTOUT_DOCKER_MAVEN_DB_PORT environment variable set. Please check you docker link parameters.");
        }
        Pattern pattern = Pattern.compile("^[^/]*//(.*)");
        Matcher matcher = pattern.matcher(dbPort);
        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid format of DB_PORT variable: Expected tcp://host:port and not " + dbPort);
        }
        String hostAndPort = matcher.group(1);
        return "jdbc:postgresql://" + hostAndPort + "/postgres";
    }

    private void printOutLogs(Connection connection, PrintWriter out) throws SQLException {
        Statement select = connection.createStatement();
        ResultSet result = select.executeQuery("SELECT * FROM LOGGING ORDER BY DATE ASC");
        while (result.next()) {
            Timestamp date = result.getTimestamp("DATE");
            String ip = result.getString("IP");
            String url = result.getString("URL");
            out.println(date + "\t\t" + ip + "\t\t" + url);
        }
    }

    private void insertLog(HttpServletRequest req, Connection connection) throws SQLException {
        try (PreparedStatement stmt =
                     connection.prepareStatement("INSERT INTO LOGGING (date,ip,url) VALUES (?,?,?)")) {
            stmt.setTimestamp(1, new Timestamp((new java.util.Date()).getTime()));
            stmt.setString(2, req.getRemoteAddr());
            stmt.setString(3, req.getRequestURI());
            stmt.executeUpdate();
        }
    }


}
