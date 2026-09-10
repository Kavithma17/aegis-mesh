import { useState, useEffect } from 'react';
import { fetchOrders } from '../services/orderService';
import Sidebar from '../components/Sidebar';
import MetricCard from '../components/MetricCard';
import OrderTable from '../components/OrderTable';
import '../styles/Dashboard.css';

export default function DashboardPage() {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');

  const loadOrders = async () => {
    setLoading(true);
    try {
      const data = await fetchOrders();
      setOrders(data);
    } catch (error) {
      console.error('Failed to load dashboard orders:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadOrders();
  }, []);

  const filteredOrders = orders.filter((order) => {
    const matchesSearch = 
      order.customerName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      order.product?.toLowerCase().includes(searchTerm.toLowerCase()) ||
      String(order.id).includes(searchTerm);
    
    const matchesStatus = statusFilter === 'ALL' || order.status === statusFilter;
    return matchesSearch && matchesStatus;
  });

  const totalRevenue = orders.reduce((acc, curr) => acc + (curr.totalAmount || 0), 0);

  return (
    <div className="app-layout">
      <Sidebar />
      <main className="main-content">
        <header className="admin-header">
          <div>
            <h1 className="admin-title">Overview</h1>
            <p className="admin-subtitle">Microservices Order Management & Analytics</p>
          </div>
          <button onClick={loadOrders} className="refresh-btn">
            🔄 Refresh Data
          </button>
        </header>

        <div className="metrics-grid">
          <MetricCard label="Total Revenue (Last 30 days)" value={`$${totalRevenue.toFixed(2)}`} />
          <MetricCard label="Total Orders (Last 30 days)" value={orders.length} />
          <MetricCard label="System Status" value="● Operational" valueStyle={{ color: '#10b981' }} />
        </div>

        <div className="controls-bar">
          <input
            type="text"
            placeholder="Search orders by ID, customer, or product..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="search-input"
          />
          <select
            value={statusFilter}
            onChange={(e) => setStatusFilter(e.target.value)}
            className="status-select"
          >
            <option value="ALL">All Statuses</option>
            <option value="PENDING">Pending</option>
            <option value="COMPLETED">Completed</option>
            <option value="FAILED">Failed</option>
          </select>
        </div>

        <OrderTable orders={filteredOrders} loading={loading} />
      </main>
    </div>
  );
}