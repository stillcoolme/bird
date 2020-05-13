package com.stillcoolme.designpattern.behavior.callback.jdbc;

import com.stillcoolme.designpattern.model.Client;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author: stillcoolme
 * @date: 2020/5/10 17:33
 * Function:
 */
public class JdbcTemplateDemo {

    private JdbcTemplate jdbcTemplate;

    public Client queryClient(long id) throws SQLException {
        String sql = "select * from user where id="+id;
        return (Client) jdbcTemplate.query(sql, new ClientRowMapper());
    }

    class ClientRowMapper implements RowMapper<Client> {
        public Client mapRow(ResultSet rs, int rowNum) throws SQLException {
            Client user = new Client(rs.getString("name"));
            return user;
        }
    }

}
