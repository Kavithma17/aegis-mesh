import React from 'react';
import '../styles/Dashboard.css';

const navItems = [
  { label: 'Dashboard', icon: '🏠', active: true },
  { label: 'Analytics', icon: '📊' },
  { label: 'Products', icon: '📦' },
  { label: 'Offers', icon: '🏷️' },
  { label: 'Inventory', icon: '📋' },
  { label: 'Orders', icon: '🛒' },
  { label: 'Sales', icon: '📈' },
  { label: 'Customer', icon: '👤' },
  { label: 'Newsletter', icon: '✉️' },
  { label: 'Settings', icon: '⚙️' },
];

export default function Sidebar() {
  return (
    <aside className="sidebar">
      <div className="sidebar-brand">
        <h2>🛡️ Aegis-mesh</h2>
      </div>
      <nav className="sidebar-nav">
        {navItems.map((item, idx) => (
          <a key={idx} href="#_" className={`sidebar-item ${item.active ? 'active' : ''}`}>
            <span className="sidebar-icon">{item.icon}</span>
            <span>{item.label}</span>
          </a>
        ))}
      </nav>
    </aside>
  );
}