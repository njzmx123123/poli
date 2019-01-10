package com.shzlw.poli.dao;

import com.shzlw.poli.dao.mapper.FilterRowMapper;
import com.shzlw.poli.model.Filter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;

@Repository
public class FilterDao {

    @Autowired
    JdbcTemplate jt;

    public List<Filter> fetchAllByDashboardId(long dashboardId) {
        String sql = "SELECT id, data FROM p_filter WHERE dashboard_id=?";
        return jt.query(sql, new Object[] { dashboardId }, new FilterRowMapper());
    }

    public Filter fetchById(long id) {
        String sql = "SELECT id, data FROM p_filter WHERE id=?";
        try {
            return jt.queryForObject(sql, new Object[]{ id }, new FilterRowMapper());
        } catch (EmptyResultDataAccessException e) {
            return null;
        }
    }

    public long add(Filter f) {
        String sql = "INSERT INTO p_filter(dashboard_id, data) VALUES(?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jt.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setLong(1, f.getDashboardId());
            ps.setString(2, f.getData());
            return ps;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public int delete(long id) {
        String sql = "DELETE FROM p_filter WHERE id=?";
        return jt.update(sql, new Object[]{ id });
    }
}
