package com.revature.dao;
import com.revature.util.ConnectionUtil;
import com.revature.util.Page;
import com.revature.util.PageOptions;
import com.revature.model.Chef;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;
import java.sql.SQLException;


/**
 * The ChefDAO class abstracts the CRUD operations for Chef objects.
 * It provides functionality to interact with the database for performing 
 * operations such as creating, retrieving, updating, and deleting Chef records. 
 * 
 * The class primarily uses a ConnectionUtil object to connect to the database and includes methods for searching, paginating, and mapping results from database queries.
 */

public class ChefDAO {

    /** A utility class for establishing connections to the database. */
    @SuppressWarnings("unused")
    private ConnectionUtil connectionUtil;

    /** 
     * Constructs a ChefDAO with the specified ConnectionUtil for database connectivity.
     * 
     * TODO: Finish the implementation so that this class's instance variables are initialized accordingly.
     * 
     * @param connectionUtil the utility used to connect to the database
     */
    public ChefDAO(ConnectionUtil connectionUtil) {
        this.connectionUtil = connectionUtil;
    }

    /**
     * TODO: Retrieves all chefs from the database.
     * 
     * @return a list of all Chef objects
     */
    public List<Chef> getAllChefs() {
        List<Chef> chefs = new ArrayList<>();
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM CHEF ORDER BY id")) {
            while (rs.next()) {
                chefs.add(mapSingleRow(rs));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch all chefs", e);
        }
        return chefs;
    }

    /**
     * TODO: Retrieves a paginated list of all chefs from the database.
     * 
     * @param pageOptions options for pagination, including page size and page number
     * @return a paginated list of Chef objects
     */
    public Page<Chef> getAllChefs(PageOptions pageOptions) {
        try (var conn = connectionUtil.getConnection();
             var stmt = conn.createStatement();
             var rs = stmt.executeQuery("SELECT * FROM CHEF ORDER BY " + pageOptions.getSortBy() + " " + pageOptions.getSortDirection())) {
            return pageResults(rs, pageOptions);
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch paginated chefs", e);
        }
        return new Page<>();
    }

    /**
     * TODO: Retrieves a Chef record by its unique identifier.
     *
     * @param id the unique identifier of the Chef to retrieve.
     * @return the Chef object, if found.
     */
    public Chef getChefById(int id) {
        try (var conn = connectionUtil.getConnection();
             var ps = conn.prepareStatement("SELECT * FROM CHEF WHERE id = ?")) {
            ps.setInt(1, id);
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapSingleRow(rs);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch chef by id: " + id, e);
        }
        return null;
    }

    /**
     * TODO: Creates a new Chef record in the database.
     *
     * @param chef the Chef object to be created.
     * @return the unique identifier of the created Chef.
     */
    public int createChef(Chef chef) {
        try (var conn = connectionUtil.getConnection();
             var ps = conn.prepareStatement(
                     "INSERT INTO CHEF (username, email, password, is_admin) VALUES (?, ?, ?, ?)",
                     java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, chef.getUsername());
            ps.setString(2, chef.getEmail());
            ps.setString(3, chef.getPassword());
            ps.setBoolean(4, chef.isAdmin());
            ps.executeUpdate();
            try (var keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    int newId = keys.getInt(1);
                    chef.setId(newId);
                    return newId;
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create chef", e);
        }
        return 0;
    }

    /**
     * TODO: Updates an existing Chef record in the database.
     *
     * @param chef the Chef object containing updated information.
     */
    public void updateChef(Chef chef) {
        try (var conn = connectionUtil.getConnection();
             var ps = conn.prepareStatement(
                     "UPDATE CHEF SET username = ?, email = ?, password = ?, is_admin = ? WHERE id = ?")) {
            ps.setString(1, chef.getUsername());
            ps.setString(2, chef.getEmail());
            ps.setString(3, chef.getPassword());
            ps.setBoolean(4, chef.isAdmin());
            ps.setInt(5, chef.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update chef with id: " + chef.getId(), e);
        }
    }

    /**
     * TODO: Deletes a Chef record from the database.
     *
     * @param chef the Chef object to be deleted.
     */
    public void deleteChef(Chef chef) {
        try (var conn = connectionUtil.getConnection();
             var ps = conn.prepareStatement("DELETE FROM CHEF WHERE id = ?")) {
            ps.setInt(1, chef.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete chef with id: " + chef.getId(), e);
        }
    }

    /**
     * TODO: Searches for Chef records by a search term in the username.
     *
     * @param term the search term to filter Chef usernames.
     * @return a list of Chef objects that match the search term.
     */
    public List<Chef> searchChefsByTerm(String term) {
        List<Chef> chefs = new ArrayList<>();
        try (var conn = connectionUtil.getConnection();
             var ps = conn.prepareStatement("SELECT * FROM CHEF WHERE username LIKE ? ORDER BY id")) {
            ps.setString(1, "%" + term + "%");
            try (var rs = ps.executeQuery()) {
                while (rs.next()) {
                    chefs.add(mapSingleRow(rs));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search chefs by term: " + term, e);
        }
        return chefs;
    }

    /**
     * TODO: Searches for chefs based on a specified term and returns a paginated result.
     * 
     * @param term the search term to filter chefs by
     * @param pageOptions options for pagination, including page size and page number
     * @return a paginated list of Chef objects that match the search term
     */
    public Page<Chef> searchChefsByTerm(String term, PageOptions pageOptions) {
        try (var conn = connectionUtil.getConnection();
             var ps = conn.prepareStatement(
                     "SELECT * FROM CHEF WHERE username LIKE ? ORDER BY " + pageOptions.getSortBy() + " " + pageOptions.getSortDirection())) {
            ps.setString(1, "%" + term + "%");
            try (var rs = ps.executeQuery()) {
                return pageResults(rs, pageOptions);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to search paginated chefs by term: " + term, e);
        }
        return new Page<>();
    }

    
    // below are helper methods that are included for your convenience

    /**
     * Maps a single row from the ResultSet to a Chef object.
     *
     * @param set the ResultSet containing Chef data.
     * @return a Chef object representing the row.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
    private Chef mapSingleRow(ResultSet set) throws SQLException {
        int id = set.getInt("id");
        String username = set.getString("username");
        String email = set.getString("email");
        String password = set.getString("password");
        boolean isAdmin = set.getBoolean("is_admin");
        return new Chef(id, username, email, password, isAdmin);
    }

    /**
     * Maps multiple rows from the ResultSet to a list of Chef objects.
     *
     * @param set the ResultSet containing Chef data.
     * @return a list of Chef objects.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
    private List<Chef> mapRows(ResultSet set) throws SQLException {
        List<Chef> chefs = new ArrayList<>();
        while (set.next()) {
            chefs.add(mapSingleRow(set));
        }
        return chefs;
    }

    /**
     * Paginates the results of a ResultSet into a Page of Chef objects.
     *
     * @param set the ResultSet containing Chef data.
     * @param pageOptions options for pagination and sorting.
     * @return a Page of Chef objects containing the paginated results.
     * @throws SQLException if an error occurs while accessing the ResultSet.
     */
    private Page<Chef> pageResults(ResultSet set, PageOptions pageOptions) throws SQLException {
        List<Chef> chefs = mapRows(set);
        int offset = (pageOptions.getPageNumber() - 1) * pageOptions.getPageSize();
        int limit = offset + pageOptions.getPageSize();
        List<Chef> slicedList = sliceList(chefs, offset, limit);
        return new Page<>(pageOptions.getPageNumber(), pageOptions.getPageSize(),
                chefs.size() / pageOptions.getPageSize(), chefs.size(), slicedList);
    }

    /**
     * Slices a list of Chef objects from a starting index to an ending index.
     *
     * @param list the list of Chef objects to slice.
     * @param start the starting index.
     * @param end the ending index.
     * @return a sliced list of Chef objects.
     */
    private List<Chef> sliceList(List<Chef> list, int start, int end) {
        List<Chef> sliced = new ArrayList<>();
        for (int i = start; i < end; i++) {
            sliced.add(list.get(i));
        }
        return sliced;
    }
}

