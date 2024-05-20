package org.bytebound;

public class DatabaseData {
    private String jdbcUrl = "jdbc:mysql://localhost:3306/shkafdatabase";
    private String dbUsername = "root";
    private String dbPassword = "";

    //setters
    public void setDbPassword(String dbPassword) {
        this.dbPassword = dbPassword;
    }

    public void setDbUsername(String dbUsername) {
        this.dbUsername = dbUsername;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    //getters
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }
    public String getDbPassword() {
        return dbPassword;
    }
}
