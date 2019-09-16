package com.example.ordermanagement;

public interface IMethodCaller {
    void openEditOrderDialog(int pos);
    void deleteOrder(int pos);
    void opemGoogleMaps(Order order);
}
