import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCart } from '../../context/CartContext';
import orderService from '../../services/orderService';
import './CheckoutPage.css';
import SecureImage from '../../components/SecureImage';
import { ToastContainer, toast } from 'react-toastify';
import 'react-toastify/dist/ReactToastify.css';

const CheckoutPage = () => {
  const { cart, removeFromCart, clearCart, getCartTotal } = useCart();
  const navigate = useNavigate();

  const [companies, setCompanies] = useState([]);
  const [selectedCompanyId, setSelectedCompanyId] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [pastOrders, setPastOrders] = useState([]);
  const [ordersLoading, setOrdersLoading] = useState(false);

  useEffect(() => {
    const fetchCompanies = async () => {
      try {
        const response = await orderService.getCustomerCompanies();
        setCompanies(response.data);
        if (response.data.length > 0) {
          setSelectedCompanyId(response.data[0].id);
        }
      } catch (err) {
        console.error("Couldn't fetch companies:", err);
      }
    };
    fetchCompanies();
    fetchPastOrders();
  }, []);

  const fetchPastOrders = async () => {
      setOrdersLoading(true);
      try {
          const response = await orderService.getMyOrders(); 
          setPastOrders(response);
      } catch (err) {
          console.error("Couldn't fetch order history", err);
      } finally {
          setOrdersLoading(false);
      }
  };

  const handleCheckout = async () => {
    if (!selectedCompanyId) {
      toast.warning("Please select a company to deliver to.", { position: "top-center" });
      return;
    }

    if (cart.length === 0) {
      toast.warning("Your cart is empty.", { position: "top-center" });
      return;
    }

    setLoading(true);

    const orderPayload = {
      companyId: parseInt(selectedCompanyId),
      items: cart.map(item => ({
        productId: item.id,
        quantity: item.quantity
      }))
    };

    try {
      await orderService.createOrder(orderPayload);
      clearCart();
      
      fetchPastOrders();
      toast.success("Order placed successfully!");
      
    } catch (err) {
      const responseData = err.response?.data;
      let errorMessage = "Error while placing order.";

      if (err.response && err.response.status === 409) {
          errorMessage = "It seems someone bought the last items while you were checking out! Please review your cart.";
      } else if (responseData) {
        if (typeof responseData === 'string') {
          errorMessage = responseData;
        } else if (responseData.message) {
          errorMessage = responseData.message;
        }
      }

      toast.error(errorMessage, {
          position: "top-center",
          autoClose: 5000,
          theme: "colored"
      });
    } finally {
      setLoading(false);
    }
  };

  const formatDate = (dateString) => {
      if (!dateString) return 'N/A';
      return new Date(dateString).toLocaleDateString('en-GB', {
          year: 'numeric', month: 'short', day: 'numeric',
          hour: '2-digit', minute: '2-digit'
      });
  };

  return (
    <div className="checkout-page">
      {cart.length > 0 ? (
          <div className="checkout-container">
            <div className="cart-summary">
              <h2>Your cart</h2>
              <div className="cart-items">
                {cart.map(item => (
                  <div key={item.id} className="cart-item">
                       <SecureImage imageUrl={item.productImage || '/static/product.jpg'} altText={item.name} className="cart-item-img" />
                    <div className="cart-item-info">
                      <h4>{item.name}</h4>
                      <p>Quantity: {item.quantity}</p>
                      <p>Price: ${Number(item.price).toFixed(2)}</p>
                    </div>
                    <div className="cart-item-total">
                      <p className="item-total-price">${(item.price * item.quantity).toFixed(2)}</p>
                      <button className="btn-remove" onClick={() => removeFromCart(item.id)}>Remove</button>
                    </div>
                  </div>
                ))}
              </div>
            </div>

            <div className="checkout-details">
              <h2>Delivery Details</h2>
              
              <div className="form-group">
                <label>Select company for delivery:</label>
                <select 
                  value={selectedCompanyId} 
                  onChange={(e) => setSelectedCompanyId(e.target.value)}
                  className="checkout-select"
                >
                  <option value="" disabled>-- Select company --</option>
                  {companies.map(comp => (
                    <option key={comp.id} value={comp.id}>{comp.name}</option>
                  ))}
                </select>
              </div>

              <div className="order-total">
                <h3>Total to pay:</h3>
                <span className="total-amount">${getCartTotal().toFixed(2)}</span>
              </div>

              {error && <div className="error-message">{error}</div>}

              <button 
                className="btn-confirm-order" 
                onClick={handleCheckout} 
                disabled={loading}
              >
                {loading ? "Processing..." : "Confirm Order"}
              </button>
            </div>
          </div>
      ) : (
          <div className="checkout-empty">
            <h2>Your cart is empty</h2>
            <button className="btn-primary" onClick={() => navigate('/shop')}>Return to shop</button>
          </div>
      )}
      <div className="order-history-section">
          <h2>Your Order History</h2>
          
          {ordersLoading ? (
              <p>Loading your past orders...</p>
          ) : pastOrders.length === 0 ? (
              <p className="no-orders-text">You haven't made any orders yet.</p>
          ) : (
              <div className="order-table-wrapper">
                  <table className="order-history-table">
                      <thead>
                          <tr>
                              <th>Order ID</th>
                              <th>Date</th>
                              <th>Delivery Company</th>
                              <th>Items</th>
                              <th>Total Amount</th>
                          </tr>
                      </thead>
                      <tbody>
                          {pastOrders.map(order => (
                              <tr key={order.id}>
                                  <td>#{order.id}</td>
                                  <td>{formatDate(order.orderDate)}</td>
                                  <td>{order.deliveryCompanyName || 'N/A'}</td>
                                  <td>
                                      <ul className="order-items-list">
                                          {order.items?.map((item, idx) => (
                                              <li key={idx}>
                                                  {item.quantity}x {item.productName} 
                                              </li>
                                          ))}
                                      </ul>
                                  </td>
                                  <td className="font-bold text-teal">
                                      ${Number(order.totalPrice).toFixed(2)}
                                  </td>
                              </tr>
                          ))}
                      </tbody>
                  </table>
              </div>
          )}
      </div>

      <ToastContainer />
    </div>
  );
};

export default CheckoutPage;
