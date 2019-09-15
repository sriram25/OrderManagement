package com.example.ordermanagement;

public class Order {

    private String orderNum;
    private String orderDueDate;
    private String customerName;
    private String customerPhoneNumber;
    private String customerAddrs;
    private String orderTotal;
    private String lat;
    private String lng;

    public Order(String orderNum, String orderDueDate, String customerName, String customerPhoneNumber, String customerAddrs, String orderTotal, String lat, String lng) {
        this.orderNum = orderNum;
        this.orderDueDate = orderDueDate;
        this.customerName = customerName;
        this.customerPhoneNumber = customerPhoneNumber;
        this.customerAddrs = customerAddrs;
        this.orderTotal = orderTotal;
        this.lat = lat;
        this.lng = lng;
    }

    public String getOrderNum() {
        return orderNum;
    }

    public String getOrderDueDate() {
        return orderDueDate;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhoneNumber() {
        return customerPhoneNumber;
    }

    public String getCustomerAddrs() {
        return customerAddrs;
    }

    public String getOrderTotal() {
        return orderTotal;
    }

    public String getLat() {
        return lat;
    }

    public String getLng() {
        return lng;
    }
}
