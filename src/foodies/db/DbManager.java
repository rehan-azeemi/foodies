/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package foodies.db;

import foodies.model.Item;
import foodies.model.ItemType;
import foodies.model.Order;
import foodies.model.OrderStats;
import foodies.model.User;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Rehan Ali Azeemi
 */
public class DbManager {

    public static User authenticateUser(User user) {
        User authenticateUser = null;
        try {
            PreparedStatement ps = DbConnection.getInstance().prepareStatement("Select * from Users where username = ? AND password = ?");
            ps.setString(1, user.getUsername());
            ps.setString(2, user.getPassword());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                authenticateUser = new User();
                authenticateUser.setUserId(rs.getInt(1));
                authenticateUser.setUsername(rs.getString(2));
                authenticateUser.setPassword(rs.getString(3));
                authenticateUser.setTableNo(rs.getString(4));
                authenticateUser.setUserType(rs.getString(5));
            }
            return authenticateUser;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return authenticateUser;
    }

    public static List<Item> getItems() {
        List<Item> itemList = new ArrayList<Item>();
        try {
            PreparedStatement ps = DbConnection.getInstance().prepareStatement("SELECT * FROM Item i \n"
                    + "INNER JOIN Item_type t \n"
                    + "ON i.item_type_id = t.item_type_id ");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Item item = new Item();
                ItemType itemType = new ItemType();

                item.setItemId(rs.getInt(1));
                item.setName(rs.getString(2));
                item.setDescription(rs.getString(3));
                item.setPrice(rs.getInt(4));
                itemType.setItemTypeId(rs.getInt(5));
                itemType.setItemType(rs.getString(7));
                item.setItemType(itemType);

                itemList.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemList;
    }

    public static void placeOrder(List<Item> listItems, User user) {
        try {
            PreparedStatement ps = DbConnection.getInstance().prepareStatement("Insert Into `Order`(table_id,order_date,order_time,order_status) values(?,?,?,?)");
            ps.setInt(1, user.getUserId());
            ps.setString(2, LocalDate.now().toString());
            ps.setString(3, LocalTime.now().toString());
            ps.setInt(4, 0);
            if (ps.executeUpdate() == 1) {
                ps = DbConnection.getInstance().prepareStatement("Select max(order_id) from `order`");
                ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    int orderId = rs.getInt(1);
                    for (Item item : listItems) {
                        if (item.getQuantity() > 0) {
                            ps = DbConnection.getInstance().prepareStatement("Insert into order_detail(order_id,item_id,quantity,price) values(?,?,?,?)");
                            ps.setInt(1, orderId);
                            ps.setInt(2, item.getItemId());
                            ps.setInt(3, item.getQuantity());
                            ps.setInt(4, item.getPrice());
                            ps.executeUpdate();
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Order> getAllOrders(int status) {
        List<Order> listOrders = new ArrayList<Order>();
        try {
            PreparedStatement ps = DbConnection.getInstance().prepareStatement("Select * from `Order` where order_status = ?");
            ps.setInt(1, status);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getInt(1));
                order.setTableId(rs.getInt(2));
                order.setOrderDate(rs.getString(3));
                order.setOrderTime(rs.getString(4));
                order.setFinishOrder(rs.getString(5));
                listOrders.add(order);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return listOrders;
    }

    public static List<Item> getItemsByOrderId(int orderId) {
        List<Item> itemList = new ArrayList<Item>();
        try {
            PreparedStatement ps = DbConnection.getInstance().prepareStatement("SELECT `name`,od.price,quantity FROM item i \n"
                    + "INNER JOIN order_detail od ON i.item_id = od.item_id \n"
                    + "WHERE order_id = ?");
            ps.setInt(1, orderId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Item item = new Item();
                item.setName(rs.getString(1));
                item.setPrice(rs.getInt(2));
                item.setQuantity(rs.getInt(3));
                itemList.add(item);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemList;
    }

    public static void finishOrder(int orderId) {
        try {
            PreparedStatement ps = DbConnection.getInstance().prepareStatement("Update `order` set order_status = 1,order_finished = ? where order_id = ?");
            ps.setString(1, LocalTime.now().toString());
            ps.setInt(2, orderId);

            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static List<OrderStats> getOrderStats() {
        List<OrderStats> itemList = new ArrayList<OrderStats>();
        try {
            PreparedStatement ps = DbConnection.getInstance().prepareStatement("SELECT it.`item_type`,i.`name`,COUNT(*) FROM Item i\n" +
            "INNER JOIN item_type it \n" +
            "ON i.item_type_id = it.item_type_id \n" +
            "INNER JOIN order_detail od \n" +
            "ON od.`item_id` = i.`item_id` \n" +
            "GROUP BY it.item_type,i.name ");

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                OrderStats orderStats = new OrderStats();
                orderStats.setItemType(rs.getString(1));
                orderStats.setName(rs.getString(2));
                orderStats.setQuantity(rs.getInt(3));
                itemList.add(orderStats);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return itemList;
    }
    
    public static void insertItem(Item item) {
        try {
            PreparedStatement ps = DbConnection.getInstance().prepareStatement("Insert Into Item(name,price,description,item_type_id) Select ?,?,?,item_type_id from item_type where item_type = ?");
            ps.setString(1, item.getName());
            ps.setInt(2, item.getPrice());
            ps.setString(3, item.getDescription());
            ps.setString(4, item.getItemType().getItemType());
            ps.executeUpdate();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
