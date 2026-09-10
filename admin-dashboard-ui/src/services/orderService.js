const API_BASE_URL = 'http://localhost:8080/api/orders';

export const fetchOrders = async () => {
  try {
    const response = await fetch(API_BASE_URL);
    if (!response.ok) {
      throw new Error('Network response was not ok');
    }
    return await response.json();
  } catch (error) {
    console.error('API Error fetching orders:', error);
    throw error;
  }
};