import React, { useState } from 'react';
import { api } from '../services/api';

export default function Register({ onNavigate }) {
    const [role, setRole] = useState('CLIENT');
    const [fullName, setFullName] = useState('');
    const [email, setEmail] = useState('');
    const [phone, setPhone] = useState('');
    const [password, setPassword] = useState('');
    const [licenseNumber, setLicenseNumber] = useState('');
    const [officeAddress, setOfficeAddress] = useState('');
    const [specialization, setSpecialization] = useState('');

    const [error, setError] = useState('');
    const [success, setSuccess] = useState('');
    const [loading, setLoading] = useState(false);

    async function handleSubmit(e) {
        e.preventDefault();
        setError('');
        setSuccess('');

        if (role === 'NOTARY' && !licenseNumber.trim()) {
            setError('License number is mandatory for notaries.');
            return;
        }

        setLoading(true);
        try {
            await api.register({
                fullName,
                email,
                phone,
                password,
                role,
                licenseNumber: role === 'NOTARY' ? licenseNumber : null,
                officeAddress: role === 'NOTARY' ? officeAddress : null,
                specialization: role === 'NOTARY' ? specialization : null
            });
            setSuccess('Account created successfully! Redirecting to sign in...');
            setTimeout(() => {
                onNavigate('login');
            }, 1500);
        } catch (err) {
            setError(err.message || 'Registration failed');
        } finally {
            setLoading(false);
        }
    }

    return (
        <div className="container my-auto py-5">
            <div className="row justify-content-center">
                <div className="col-lg-6 col-md-8">
                    <div className="card shadow-sm border-0">
                        <div className="card-body p-4 p-md-5">
                            <div className="text-center mb-4">
                                <i className="bi bi-person-plus-fill text-primary display-4"></i>
                                <h2 className="fw-bold mt-2">Create an Account</h2>
                                <p className="text-muted">Register as a Client seeking notarization or a Certified Notary Public</p>
                            </div>

                            {error && (
                                <div className="alert alert-danger alert-dismissible fade show" role="alert" id="register-error-banner">
                                    <i className="bi bi-exclamation-triangle-fill me-2"></i>
                                    <span>{error}</span>
                                    <button type="button" className="btn-close" onClick={() => setError('')}></button>
                                </div>
                            )}

                            {success && (
                                <div className="alert alert-success alert-dismissible fade show" role="alert" id="register-success-banner">
                                    <i className="bi bi-check-circle-fill me-2"></i>
                                    <span>{success}</span>
                                </div>
                            )}

                            <form onSubmit={handleSubmit} id="form-register">
                                {/* Role Selection */}
                                <div className="mb-3">
                                    <label className="form-label fw-bold">I want to register as:</label>
                                    <div className="d-flex gap-3">
                                        <div className="form-check">
                                            <input
                                                className="form-check-input"
                                                type="radio"
                                                name="role"
                                                id="role-client"
                                                value="CLIENT"
                                                checked={role === 'CLIENT'}
                                                onChange={() => setRole('CLIENT')}
                                            />
                                            <label className="form-check-label fw-semibold" htmlFor="role-client">
                                                <i className="bi bi-person me-1"></i>Client (Citizen)
                                            </label>
                                        </div>
                                        <div className="form-check">
                                            <input
                                                className="form-check-input"
                                                type="radio"
                                                name="role"
                                                id="role-notary"
                                                value="NOTARY"
                                                checked={role === 'NOTARY'}
                                                onChange={() => setRole('NOTARY')}
                                            />
                                            <label className="form-check-label fw-semibold" htmlFor="role-notary">
                                                <i className="bi bi-award me-1"></i>Certified Notary Public
                                            </label>
                                        </div>
                                    </div>
                                </div>

                                {/* Full Name */}
                                <div className="mb-3">
                                    <label htmlFor="input-fullName" className="form-label">Full Legal Name</label>
                                    <input
                                        type="text"
                                        className="form-control"
                                        id="input-fullName"
                                        placeholder="e.g. John Doe"
                                        value={fullName}
                                        onChange={(e) => setFullName(e.target.value)}
                                        required
                                    />
                                </div>

                                {/* Email */}
                                <div className="mb-3">
                                    <label htmlFor="input-email" className="form-label">Email Address</label>
                                    <input
                                        type="email"
                                        className="form-control"
                                        id="input-email"
                                        placeholder="name@example.com"
                                        value={email}
                                        onChange={(e) => setEmail(e.target.value)}
                                        required
                                    />
                                </div>

                                {/* Phone */}
                                <div className="mb-3">
                                    <label htmlFor="input-phone" className="form-label">Phone Number</label>
                                    <input
                                        type="tel"
                                        className="form-control"
                                        id="input-phone"
                                        placeholder="+1 (555) 000-0000"
                                        value={phone}
                                        onChange={(e) => setPhone(e.target.value)}
                                    />
                                </div>

                                {/* Password */}
                                <div className="mb-3">
                                    <label htmlFor="input-password" className="form-label">Password</label>
                                    <input
                                        type="password"
                                        className="form-control"
                                        id="input-password"
                                        placeholder="Minimum 6 characters"
                                        minLength={6}
                                        value={password}
                                        onChange={(e) => setPassword(e.target.value)}
                                        required
                                    />
                                </div>

                                {/* Notary Details (conditionally rendered) */}
                                {role === 'NOTARY' && (
                                    <div id="notary-profile-section" className="border rounded p-3 bg-light mb-3">
                                        <h6 className="fw-bold text-primary mb-3">
                                            <i className="bi bi-file-earmark-lock me-1"></i>Notary Credentials
                                        </h6>
                                        <div className="mb-3">
                                            <label htmlFor="input-licenseNumber" className="form-label">State / Commission License #</label>
                                            <input
                                                type="text"
                                                className="form-control"
                                                id="input-licenseNumber"
                                                placeholder="e.g. NOT-98765-STATE"
                                                value={licenseNumber}
                                                onChange={(e) => setLicenseNumber(e.target.value)}
                                                required={role === 'NOTARY'}
                                            />
                                        </div>
                                        <div className="mb-3">
                                            <label htmlFor="input-officeAddress" className="form-label">Office Address</label>
                                            <input
                                                type="text"
                                                className="form-control"
                                                id="input-officeAddress"
                                                placeholder="123 Legal Way, Suite 400"
                                                value={officeAddress}
                                                onChange={(e) => setOfficeAddress(e.target.value)}
                                            />
                                        </div>
                                        <div className="mb-3">
                                            <label htmlFor="input-specialization" className="form-label">Specialization</label>
                                            <input
                                                type="text"
                                                className="form-control"
                                                id="input-specialization"
                                                placeholder="e.g. Real Estate Deeds, Affidavits, Wills"
                                                value={specialization}
                                                onChange={(e) => setSpecialization(e.target.value)}
                                            />
                                        </div>
                                    </div>
                                )}

                                <button type="submit" className="btn btn-primary w-100 py-2 fw-semibold" id="btn-submit-register" disabled={loading}>
                                    {loading ? (
                                        <span><span className="spinner-border spinner-border-sm me-2"></span>Creating Account...</span>
                                    ) : (
                                        <span><i className="bi bi-check-circle-fill me-2"></i>Create Account</span>
                                    )}
                                </button>
                            </form>

                            <div className="text-center mt-3">
                                <span className="text-muted small">Already registered? </span>
                                <button className="btn btn-link p-0 small fw-bold" onClick={() => onNavigate('login')} id="link-goto-login">
                                    Sign In
                                </button>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
