package app.fmt;

import entities.fmt.CSVParser;
import entities.fmt.Person;
import entities.fmt.Item;
import entities.fmt.Store;
import entities.fmt.Invoice;
import entities.fmt.Equipment;
import entities.fmt.Product;
import entities.fmt.Purchase;
import entities.fmt.Service;
import entities.fmt.Lease;
import entities.fmt.Purchase;
import entities.fmt.Address;
import entities.fmt.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.ResultSet;
import java.util.Map;

/**
 * One-time utility to load CSV data into MySQL database
 */
public class DatabasePopulator {
    public static void main(String[] args) {
        // CSVParser loads all data from CSV files
        Map<String, Person> persons = CSVParser.personMap;
        Map<String, Item> items = CSVParser.itemMap;
        Map<String, Store> stores = CSVParser.storeMap;
        Map<String, Invoice> invoices = CSVParser.invoiceMap;

        try (Connection conn = DatabaseConnection.getConnection()) {
            insertAddressesAndPersons(conn, persons);
            insertItems(conn, items);
            insertStores(conn, stores, persons);
            insertInvoices(conn, invoices, stores, persons);
            insertInvoiceItems(conn, invoices);
            System.out.println("Database populated successfully!");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private static int insertAddress(Connection conn, entities.fmt.Address address) throws SQLException {
        String sql = "INSERT INTO Address (street, city, state, zip, country) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
        ps.setString(1, address.getStreet());
        ps.setString(2, address.getCity());
        ps.setString(3, address.getState());
        ps.setString(4, address.getZip());
        ps.setString(5, address.getCountry());
        ps.executeUpdate();
        ResultSet keys = ps.getGeneratedKeys();
        keys.next();
        return keys.getInt(1);
    }

    private static void insertAddressesAndPersons(Connection conn, Map<String, Person> persons) throws SQLException {
        for (Map.Entry<String, Person> entry : persons.entrySet()) {
            Person p = entry.getValue();
            int addressId = insertAddress(conn, p.getAddress());

            String sql = "INSERT INTO Person (personCode, firstName, lastName, addressId) VALUES (?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, p.getPersonCode());
            ps.setString(2, p.getFirstName());
            ps.setString(3, p.getLastName());
            ps.setInt(4, addressId);
            ps.executeUpdate();

            ResultSet keys = ps.getGeneratedKeys();
            keys.next();
            int personId = keys.getInt(1);

            for (String email : p.getEmails()) {
                String emailSql = "INSERT INTO Email (emailAddress, personId) VALUES (?, ?)";
                PreparedStatement eps = conn.prepareStatement(emailSql);
                eps.setString(1, email);
                eps.setInt(2, personId);
                eps.executeUpdate();
            }
        }
    }

    private static void insertItems(Connection conn, Map<String, Item> items) throws SQLException {
        for (Map.Entry<String, Item> entry : items.entrySet()) {
            Item item = entry.getValue();
            String type = "";
            double hourlyRate = 0, unitPrice = 0;
            String unit = null, model = null;

            if (item instanceof Equipment) {
                type = "Equipment";
                model = ((Equipment) item).getModel();
            } else if (item instanceof Product) {
                type = "Product";
                unit = ((Product) item).getUnit();
                unitPrice = ((Product) item).getUnitPrice();
            } else if (item instanceof Service) {
                type = "Service";
                hourlyRate = ((Service) item).getHourlyRate();
            }

            String sql = "INSERT INTO Item (itemCode, type, itemName, hourlyRate, unit, unitPrice, model) VALUES (?, ?, ?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, item.getItemCode());
            ps.setString(2, type);
            ps.setString(3, item.getItemName());
            ps.setDouble(4, hourlyRate);
            ps.setString(5, unit);
            ps.setDouble(6, unitPrice);
            ps.setString(7, model);
            ps.executeUpdate();
        }
    }

    private static void insertStores(Connection conn, Map<String, Store> stores, Map<String, Person> persons)
            throws SQLException {
        for (Map.Entry<String, Store> entry : stores.entrySet()) {
            Store store = entry.getValue();
            int addressId = insertAddress(conn, store.getStoreAddress());

            // Look up manager's personId from DB
            String managerCode = store.getManager().getPersonCode();
            PreparedStatement findManager = conn.prepareStatement("SELECT personId FROM Person WHERE personCode = ?");
            findManager.setString(1, managerCode);
            ResultSet rs = findManager.executeQuery();
            rs.next();
            int managerId = rs.getInt(1);

            String sql = "INSERT INTO Store (storeCode, managerId, addressId) VALUES (?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, store.getStoreCode());
            ps.setInt(2, managerId);
            ps.setInt(3, addressId);
            ps.executeUpdate();
        }
    }

    private static void insertInvoices(Connection conn, Map<String, Invoice> invoices, Map<String, Store> stores,
            Map<String, Person> persons) throws SQLException {
        for (Map.Entry<String, Invoice> entry : invoices.entrySet()) {
            Invoice invoice = entry.getValue();

            // Look up IDs from DB
            PreparedStatement findStore = conn.prepareStatement("SELECT storeId FROM Store WHERE storeCode = ?");
            findStore.setString(1, invoice.getStoreCode().getStoreCode());
            ResultSet storeRs = findStore.executeQuery();
            storeRs.next();
            int storeId = storeRs.getInt(1);

            PreparedStatement findCustomer = conn.prepareStatement("SELECT personId FROM Person WHERE personCode = ?");
            findCustomer.setString(1, invoice.getCustomer().getPersonCode());
            ResultSet customerRs = findCustomer.executeQuery();
            customerRs.next();
            int customerId = customerRs.getInt(1);

            PreparedStatement findSalesperson = conn
                    .prepareStatement("SELECT personId FROM Person WHERE personCode = ?");
            findSalesperson.setString(1, invoice.getSalesperson().getPersonCode());
            ResultSet salespersonRs = findSalesperson.executeQuery();
            salespersonRs.next();
            int salespersonId = salespersonRs.getInt(1);

            String sql = "INSERT INTO Invoice (invoiceCode, date, customerId, salespersonId, storeId) VALUES (?, ?, ?, ?, ?)";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, invoice.getInvoiceCode());
            ps.setString(2, invoice.getDate().toString());
            ps.setInt(3, customerId);
            ps.setInt(4, salespersonId);
            ps.setInt(5, storeId);
            ps.executeUpdate();
        }
    }

    private static void insertInvoiceItems(Connection conn, Map<String, Invoice> invoices) throws SQLException {
        for (Map.Entry<String, Invoice> entry : invoices.entrySet()) {
            Invoice invoice = entry.getValue();

            // Look up invoiceId from DB
            PreparedStatement findInvoice = conn
                    .prepareStatement("SELECT invoiceId FROM Invoice WHERE invoiceCode = ?");
            findInvoice.setString(1, invoice.getInvoiceCode());
            ResultSet invoiceRs = findInvoice.executeQuery();
            invoiceRs.next();
            int invoiceId = invoiceRs.getInt(1);

            for (Item item : invoice.getInvoiceItems()) {
                // Look up itemId from DB
                PreparedStatement findItem = conn.prepareStatement("SELECT itemId FROM Item WHERE itemCode = ?");
                findItem.setString(1, item.getItemCode());
                ResultSet itemRs = findItem.executeQuery();
                itemRs.next();
                int itemId = itemRs.getInt(1);

                String eStatus = null;
                String startDate = null;
                String endDate = null;
                Integer quantity = null;
                Double hours = null;
                Double fee = null;
                Double purchasePrice = null;

                if (item instanceof Lease) {
                    Lease lease = (Lease) item;
                    eStatus = "Leased";
                    startDate = lease.getStartDate().toString();
                    endDate = lease.getEndDate().toString();
                    fee = lease.getFee();
                } else if (item instanceof Purchase) {
                    Purchase purchase = (Purchase) item;
                    eStatus = "Purchased";
                    purchasePrice = purchase.getPurchasePrice();
                } else if (item instanceof Product) {
                    Product product = (Product) item;
                    quantity = product.getQuantity();
                } else if (item instanceof Service) {
                    Service service = (Service) item;
                    hours = service.getHours();
                }

                String sql = "INSERT INTO InvoiceItem (invoiceId, itemId, eStatus, startDate, endDate, quantity, hours, fee, purchasePrice) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, invoiceId);
                ps.setInt(2, itemId);
                ps.setString(3, eStatus);
                ps.setString(4, startDate);
                ps.setString(5, endDate);
                if (quantity != null)
                    ps.setInt(6, quantity);
                else
                    ps.setNull(6, java.sql.Types.INTEGER);
                if (hours != null)
                    ps.setDouble(7, hours);
                else
                    ps.setNull(7, java.sql.Types.DOUBLE);
                if (fee != null)
                    ps.setDouble(8, fee);
                else
                    ps.setNull(8, java.sql.Types.DOUBLE);
                if (purchasePrice != null)
                    ps.setDouble(9, purchasePrice);
                else
                    ps.setNull(9, java.sql.Types.DOUBLE);
                ps.executeUpdate();
            }
        }
    }
}