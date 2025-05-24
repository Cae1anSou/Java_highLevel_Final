package cxz.Final_Project.dao;

import cxz.Final_Project.utill.DBUtill;

import java.sql.Connection;
import java.sql.SQLException;

public abstract class BaseDAO {
    protected Connection getConnection() throws SQLException {
        return DBUtill.getConnection();
    }
}
