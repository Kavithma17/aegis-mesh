import React from 'react';

export default function MetricCard({ label, value, valueStyle }) {
  return (
    <div className="metric-card">
      <p className="metric-label">{label}</p>
      <h3 className="metric-value" style={valueStyle}>{value}</h3>
    </div>
  );
}