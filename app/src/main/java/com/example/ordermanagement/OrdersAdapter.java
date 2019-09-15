package com.example.ordermanagement;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.MyViewHolder> {

    private List<Order> orderList;
    private IMethodCaller mContext;

    public OrdersAdapter(List<Order> orderList, Context mContext) {
        this.orderList = orderList;
        this.mContext = (IMethodCaller) mContext;
    }

    @NonNull
    @Override
    public OrdersAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View itemView = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.item_order, viewGroup, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersAdapter.MyViewHolder myViewHolder, int i) {

        final int index = i;
        final Order order = orderList.get(index);

        myViewHolder.orderNum.setText("Order Number: " + order.getOrderNum());
        myViewHolder.orderDueDate.setText("Due Date: " + order.getOrderDueDate());
        myViewHolder.customerName.setText(new StringBuilder().append(order.getCustomerName()).append(", ").append(order.getCustomerPhoneNumber()).toString());
        myViewHolder.customerAddrs.setText(order.getCustomerAddrs());
        myViewHolder.orderTotal.setText("Rs. " + order.getOrderTotal());

        myViewHolder.editImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext != null) {
                    ((IMethodCaller) mContext).openEditOrderDialog(order);
                }
            }
        });

        myViewHolder.deleteImgView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext != null) {
                    mContext.deleteOrder(index);
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        TextView orderNum, orderDueDate, customerName, customerAddrs, orderTotal;
        ImageView editImgView, deleteImgView;

        MyViewHolder(@NonNull View itemView) {
            super(itemView);

            orderNum = itemView.findViewById(R.id.orderNumber);
            orderDueDate = itemView.findViewById(R.id.orderDueDate);
            customerName = itemView.findViewById(R.id.customerDetails);
            customerAddrs = itemView.findViewById(R.id.address);
            orderTotal = itemView.findViewById(R.id.orderTotal);
            editImgView = itemView.findViewById(R.id.edit);
            deleteImgView = itemView.findViewById(R.id.delete);

        }
    }
}
