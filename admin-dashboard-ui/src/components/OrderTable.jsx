import React from 'react';

export default function OrderTable({ orders, loading }) {
  if (loading) {
    return <div className="center-message">Loading live system streams...</div>;
  }

  if (orders.length === 0) {
    return <div className="center-message">No matching records found.</div>;
  }

  return (
    <div className="table-wrapper">
      <table className="dashboard-table">
        <thead>
          <tr className="table-head">
            <th className="th-cell">Order ID</th>
            <th className="th-cell">Customer</th>
            <th className="th-cell">Product</th>
            <th className="th-cell">Quantity</th>
            <th className="th-cell">Amount</th>
            <th className="th-cell">Status</th>
          </tr>
        </thead>
        <tbody>
          {orders.map((order, idx) => {
            const statusClass = 
              order.status === 'COMPLETED' ? 'badge-completed' : 
              order.status === 'FAILED' ? 'badge-failed' : 'badge-pending';

            return (
              <tr key={order.id} className={idx % 2 === 0 ? 'row-even' : 'row-odd'}>
                <td className="td-cell">#{order.id}</td>
                <td className="td-cell" style={{ fontWeight: '600' }}>{order.customerName}</td>
                <td className="td-cell">{order.product}</td>
                <td className="td-cell">{order.quantity}</td>
                <td className="td-cell">${order.totalAmount?.toFixed(2)}</td>
                <td className="td-cell">
                  <span className={`badge ${statusClass}`}>{order.status}</span>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}