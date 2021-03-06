package com.sismics.music.core.dao.dbi.mapper;

import com.sismics.music.core.model.dbi.UserAlbum;
import com.sismics.util.dbi.BaseResultSetMapper;
import org.skife.jdbi.v2.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * User / album result set mapper.
 *
 * @author jtremeaux
 */
public class UserAlbumMapper extends BaseResultSetMapper<UserAlbum> {
    public String[] getColumns() {
        return new String[] {
            "USA_ID_C",
            "USA_IDUSER_C",
            "USA_IDALBUM_C",
            "USA_CREATEDATE_D",
            "USA_DELETEDATE_D"};
    }

    @Override
    public UserAlbum map(int index, ResultSet r, StatementContext ctx) throws SQLException {
        final String[] columns = getColumns();
        int column = 0;
        return new UserAlbum(
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getString(columns[column++]),
                r.getInt(columns[column++]),
                r.getDate(columns[column++]),
                r.getDate(columns[column++]));
    }
}
