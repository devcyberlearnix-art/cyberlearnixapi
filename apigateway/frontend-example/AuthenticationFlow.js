// AuthenticationFlow.js - Complete React Authentication Component

import React, { useState, useEffect } from 'react';
import axios from 'axios';

const AuthenticationFlow = () => {
    const [step, setStep] = useState('initial'); // 'initial', 'login', 'register'
    const [userExists, setUserExists] = useState(false);
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');

    // Form states
    const [loginData, setLoginData] = useState({
        username: '',
        password: '',
        deviceId: generateDeviceId(),
        deviceName: getDeviceName()
    });

    const [registerData, setRegisterData] = useState({
        username: '',
        email: '',
        password: '',
        confirmPassword: '',
        firstName: '',
        lastName: '',
        phoneNumber: '',
        deviceId: generateDeviceId(),
        deviceName: getDeviceName(),
        acceptTerms: false,
        acceptPrivacyPolicy: false,
        subscribeToNewsletter: false
    });

    // Utility functions
    function generateDeviceId() {
        return localStorage.getItem('deviceId') || 
               (Math.random().toString(36).substring(2, 15) + 
                Math.random().toString(36).substring(2, 15));
    }

    function getDeviceName() {
        return navigator.userAgent.includes('Mobile') ? 'Mobile Device' : 'Desktop';
    }

    // API calls
    const checkUserExists = async (username, email) => {
        try {
            const params = new URLSearchParams();
            if (username) params.append('username', username);
            if (email) params.append('email', email);
            
            const response = await axios.get(`/api/auth/check-user?${params}`);
            return response.data.exists;
        } catch (error) {
            console.error('Error checking user existence:', error);
            return false;
        }
    };

    const login = async (credentials) => {
        setLoading(true);
        setError('');
        
        try {
            const response = await axios.post('/api/auth/login', credentials);
            
            if (response.data.success) {
                // Store tokens and user data
                localStorage.setItem('accessToken', response.data.accessToken);
                localStorage.setItem('refreshToken', response.data.refreshToken);
                localStorage.setItem('user', JSON.stringify(response.data.user));
                localStorage.setItem('deviceId', credentials.deviceId);
                
                setSuccess('Login successful! Redirecting...');
                setTimeout(() => {
                    window.location.href = '/dashboard';
                }, 1000);
            }
        } catch (error) {
            if (error.response?.status === 404) {
                setError('User not found. Would you like to register?');
                setStep('register');
            } else if (error.response?.status === 401) {
                setError('Invalid username or password');
            } else {
                setError('Login failed. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    const register = async (registrationData) => {
        setLoading(true);
        setError('');
        
        try {
            const response = await axios.post('/api/auth/register', registrationData);
            
            if (response.data.success) {
                // Store tokens and user data
                localStorage.setItem('accessToken', response.data.accessToken);
                localStorage.setItem('refreshToken', response.data.refreshToken);
                localStorage.setItem('user', JSON.stringify(response.data.user));
                localStorage.setItem('deviceId', registrationData.deviceId);
                
                setSuccess('Registration successful! Welcome aboard!');
                setTimeout(() => {
                    window.location.href = '/welcome';
                }, 1000);
            }
        } catch (error) {
            if (error.response?.status === 409) {
                setError('User already exists. Please login instead.');
                setStep('login');
            } else {
                setError(error.response?.data?.message || 'Registration failed. Please try again.');
            }
        } finally {
            setLoading(false);
        }
    };

    // Form handlers
    const handleUsernameCheck = async (username, email) => {
        if (username.length > 2 || email.length > 5) {
            const exists = await checkUserExists(username, email);
            setUserExists(exists);
            setStep(exists ? 'login' : 'register');
        }
    };

    const handleLoginSubmit = (e) => {
        e.preventDefault();
        
        // Validation
        if (!loginData.username || !loginData.password) {
            setError('Please fill in all fields');
            return;
        }
        
        login(loginData);
    };

    const handleRegisterSubmit = (e) => {
        e.preventDefault();
        
        // Validation
        const { username, email, password, confirmPassword, acceptTerms, acceptPrivacyPolicy } = registerData;
        
        if (!username || !email || !password || !confirmPassword) {
            setError('Please fill in all required fields');
            return;
        }
        
        if (password !== confirmPassword) {
            setError('Passwords do not match');
            return;
        }
        
        if (!acceptTerms || !acceptPrivacyPolicy) {
            setError('Please accept the terms and privacy policy');
            return;
        }
        
        register(registerData);
    };

    // Render components
    const renderInitialForm = () => (
        <div className="auth-container">
            <h2>Welcome to SwachVega</h2>
            <p>Enter your username or email to get started</p>
            
            <form onSubmit={(e) => e.preventDefault()}>
                <div className="form-group">
                    <input
                        type="text"
                        placeholder="Username or Email"
                        value={loginData.username}
                        onChange={(e) => {
                            setLoginData({...loginData, username: e.target.value});
                            setRegisterData({...registerData, username: e.target.value});
                            handleUsernameCheck(e.target.value, '');
                        }}
                        className="form-control"
                    />
                </div>
                
                <div className="form-group">
                    <input
                        type="email"
                        placeholder="Email Address"
                        value={registerData.email}
                        onChange={(e) => {
                            setRegisterData({...registerData, email: e.target.value});
                            handleUsernameCheck(loginData.username, e.target.value);
                        }}
                        className="form-control"
                    />
                </div>
                
                <div className="form-actions">
                    <button
                        type="button"
                        onClick={() => setStep('login')}
                        className="btn btn-primary"
                    >
                        Login
                    </button>
                    <button
                        type="button"
                        onClick={() => setStep('register')}
                        className="btn btn-secondary"
                    >
                        Register
                    </button>
                </div>
            </form>
        </div>
    );

    const renderLoginForm = () => (
        <div className="auth-container">
            <h2>Login to Your Account</h2>
            
            <form onSubmit={handleLoginSubmit}>
                <div className="form-group">
                    <input
                        type="text"
                        placeholder="Username or Email"
                        value={loginData.username}
                        onChange={(e) => setLoginData({...loginData, username: e.target.value})}
                        className="form-control"
                        required
                    />
                </div>
                
                <div className="form-group">
                    <input
                        type="password"
                        placeholder="Password"
                        value={loginData.password}
                        onChange={(e) => setLoginData({...loginData, password: e.target.value})}
                        className="form-control"
                        required
                    />
                </div>
                
                <div className="form-actions">
                    <button
                        type="submit"
                        className="btn btn-primary"
                        disabled={loading}
                    >
                        {loading ? 'Logging in...' : 'Login'}
                    </button>
                    <button
                        type="button"
                        onClick={() => setStep('register')}
                        className="btn btn-link"
                    >
                        Don't have an account? Register
                    </button>
                </div>
            </form>
        </div>
    );

    const renderRegisterForm = () => (
        <div className="auth-container">
            <h2>Create Your Account</h2>
            
            <form onSubmit={handleRegisterSubmit}>
                <div className="form-row">
                    <div className="form-group">
                        <input
                            type="text"
                            placeholder="Username"
                            value={registerData.username}
                            onChange={(e) => setRegisterData({...registerData, username: e.target.value})}
                            className="form-control"
                            required
                        />
                    </div>
                    <div className="form-group">
                        <input
                            type="email"
                            placeholder="Email Address"
                            value={registerData.email}
                            onChange={(e) => setRegisterData({...registerData, email: e.target.value})}
                            className="form-control"
                            required
                        />
                    </div>
                </div>
                
                <div className="form-row">
                    <div className="form-group">
                        <input
                            type="text"
                            placeholder="First Name"
                            value={registerData.firstName}
                            onChange={(e) => setRegisterData({...registerData, firstName: e.target.value})}
                            className="form-control"
                        />
                    </div>
                    <div className="form-group">
                        <input
                            type="text"
                            placeholder="Last Name"
                            value={registerData.lastName}
                            onChange={(e) => setRegisterData({...registerData, lastName: e.target.value})}
                            className="form-control"
                        />
                    </div>
                </div>
                
                <div className="form-group">
                    <input
                        type="tel"
                        placeholder="Phone Number"
                        value={registerData.phoneNumber}
                        onChange={(e) => setRegisterData({...registerData, phoneNumber: e.target.value})}
                        className="form-control"
                    />
                </div>
                
                <div className="form-row">
                    <div className="form-group">
                        <input
                            type="password"
                            placeholder="Password"
                            value={registerData.password}
                            onChange={(e) => setRegisterData({...registerData, password: e.target.value})}
                            className="form-control"
                            required
                        />
                    </div>
                    <div className="form-group">
                        <input
                            type="password"
                            placeholder="Confirm Password"
                            value={registerData.confirmPassword}
                            onChange={(e) => setRegisterData({...registerData, confirmPassword: e.target.value})}
                            className="form-control"
                            required
                        />
                    </div>
                </div>
                
                <div className="form-group">
                    <label className="checkbox-label">
                        <input
                            type="checkbox"
                            checked={registerData.acceptTerms}
                            onChange={(e) => setRegisterData({...registerData, acceptTerms: e.target.checked})}
                        />
                        I accept the <a href="/terms" target="_blank">Terms and Conditions</a>
                    </label>
                </div>
                
                <div className="form-group">
                    <label className="checkbox-label">
                        <input
                            type="checkbox"
                            checked={registerData.acceptPrivacyPolicy}
                            onChange={(e) => setRegisterData({...registerData, acceptPrivacyPolicy: e.target.checked})}
                        />
                        I accept the <a href="/privacy" target="_blank">Privacy Policy</a>
                    </label>
                </div>
                
                <div className="form-group">
                    <label className="checkbox-label">
                        <input
                            type="checkbox"
                            checked={registerData.subscribeToNewsletter}
                            onChange={(e) => setRegisterData({...registerData, subscribeToNewsletter: e.target.checked})}
                        />
                        Subscribe to our newsletter
                    </label>
                </div>
                
                <div className="form-actions">
                    <button
                        type="submit"
                        className="btn btn-primary"
                        disabled={loading}
                    >
                        {loading ? 'Creating Account...' : 'Create Account'}
                    </button>
                    <button
                        type="button"
                        onClick={() => setStep('login')}
                        className="btn btn-link"
                    >
                        Already have an account? Login
                    </button>
                </div>
            </form>
        </div>
    );

    return (
        <div className="authentication-flow">
            {error && (
                <div className="alert alert-danger">
                    {error}
                    <button onClick={() => setError('')} className="alert-close">×</button>
                </div>
            )}
            
            {success && (
                <div className="alert alert-success">
                    {success}
                </div>
            )}
            
            {step === 'initial' && renderInitialForm()}
            {step === 'login' && renderLoginForm()}
            {step === 'register' && renderRegisterForm()}
        </div>
    );
};

export default AuthenticationFlow;
