import api from '../api/axios'

export const loginUser = async (email, password) => {
    try {
        const response = await api.post('/auth/login', {email, password})
        const { jwt, requiresPasswordReset, username, role } = response.data;
  
        // Save token
        localStorage.setItem('token', jwt);
        localStorage.setItem('username', username);
        localStorage.setItem('role', role);
        
        if (requiresPasswordReset) {
            localStorage.setItem('requiresPasswordReset', 'true');
        }
        return response.data;
    } catch (error) {
        const errorMessage = error.response?.data?.message || 'Login failed. Please try again.';
        const newError = new Error(errorMessage);
        newError.status = error.response?.status;
        throw newError;
    }
};

export const registerUser = async (userData) => {
    try {
        const response = await api.post('/auth/register', userData, {
            headers: {
                'Content-Type': 'multipart/form-data',
            },
        });
        return response.data;
    } catch (error) {
        throw error;
    }
};

export const logout = () => {
    localStorage.removeItem('token');
};
