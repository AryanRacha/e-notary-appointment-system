import React, { useState } from 'react';
import { api } from '../services/api';

export default function Login({ onLoginSuccess, onNavigate }) {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        setLoading(true);
        try {
            const data = await api.login(email, password);
            if (data.success && data.user) {
                onLoginSuccess(data.user);
            }
        } catch (err) {
            setError(err.message || 'Invalid email or password');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="container my-auto py-5">
            <div className="row justify-content-center">
                <div className="col-lg-5 col-md-7">
                    <div className="card shadow-sm border-0">
                        <div className="card-body p-4 p-md-5">
                            <div className="text-center mb-4">
                                <i className="bi bi-shield-lock-fill text-primary display-4"></i>
                                <h2 className="fw-bold mt-2">Sign In</h2>
                                <p className="text-muted">Enter your credentials to access your appointments</p>
                            </div>

                            {error && (
                                <div className="alert alert-danger alert-dismissible fade show" role="alert" id="login-error-banner">
                                    <i className="bi bi-exclamation-triangle-fill me-2"></i>
                                    <span>{error}</span>
                                    <button type="button" className="btn-close" onClick={() => setError('')}></button>
                                </div>
                            )}

                            <form onSubmit={handleSubmit} id="form-login">
                                <div className="mb-3">
                                    <label htmlFor="input-login-email" className="form-label">Email Address</label>
                                    <input
                                        type="email"
                                        className="form-control"
                                        id="input-login-email"
                                        placeholder="name@example.com"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        required
                                    />
                                </div>

                                <div className="mb-4">
                                    <label htmlFor="input-login-password" className="form-label">Password</label>
                                    <input
                                        type="password"
                                        className="form-control"
                                        id="input-login-password"
                                        placeholder="Your password"
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        required
                                    />
                                </div>

                                <button type="submit" className="btn btn-primary w-100 py-2 fw-semibold" id="btn-submit-login" disabled={loading}>
                                    {loading ? (
                                        <span><span className="spinner-border spinner-border-sm me-2"></span>Signing In...</span>
                                    ) : (
                                        <span><i className="bi bi-box-arrow-in-right me-2"></i>Sign In</span>
                                    )}
                                </button>
                            </form>

                            <div className="text-center mt-4">
                                <span className="text-muted small">Don't have an account? </span>
                                <button className="btn btn-link p-0 small fw-bold" onClick={() => onNavigate('register')} id="link-goto-register">
                                    Register Now
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
