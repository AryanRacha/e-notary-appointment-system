import React from 'react';

export default function Home({ onNavigate, currentUser }) {
    return (
        <div>
            {/* Hero Section */}
            <header className="py-5 bg-white border-bottom shadow-sm">
                <div className="container py-4 text-center">
                    <span className="badge bg-primary-subtle text-primary border border-primary-subtle px-3 py-2 rounded-pill mb-3">
                        DevOps Coursework Project
                    </span>
                    <h1 className="display-5 fw-bold text-dark mb-3">Containerized E-Notary Appointment System</h1>
                    <p className="lead text-secondary mx-auto mb-4" style={{ maxWidth: '720px' }}>
                        A reliable, containerized web platform for scheduling, confirming, and managing legal notarial appointments with continuous automated CI/CD and deployment monitoring.
                    </p>
                    <div className="d-flex justify-content-center gap-3">
                        <button className="btn btn-primary btn-lg px-4" onClick={() => onNavigate('slots')} id="hero-btn-book">
                            <i className="bi bi-calendar-check me-2"></i>Browse Available Slots
                        </button>
                        <button className="btn btn-outline-secondary btn-lg px-4" onClick={() => onNavigate('track')} id="hero-btn-track">
                            <i className="bi bi-search me-2"></i>Track Appointment
                        </button>
                    </div>
                </div>
            </header>

            {/* Feature Cards Grid */}
            <main className="container py-5">
                <div className="row g-4">
                    <div className="col-md-4">
                        <div className="card h-100 shadow-sm border-0">
                            <div className="card-body p-4 text-center">
                                <div className="text-primary mb-3">
                                    <i className="bi bi-calendar3 fs-1"></i>
                                </div>
                                <h5 className="card-title fw-bold">1. Live Availability</h5>
                                <p className="card-text text-muted">
                                    Explore verified notaries and inspect available 30-minute booking slots in real time.
                                </p>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4">
                        <div className="card h-100 shadow-sm border-0">
                            <div className="card-body p-4 text-center">
                                <div className="text-success mb-3">
                                    <i className="bi bi-file-earmark-lock2 fs-1"></i>
                                </div>
                                <h5 className="card-title fw-bold">2. Instant Booking</h5>
                                <p className="card-text text-muted">
                                    Reserve slots for Affidavits, Deeds, and Powers of Attorney with an immediate reference code.
                                </p>
                            </div>
                        </div>
                    </div>
                    <div className="col-md-4">
                        <div className="card h-100 shadow-sm border-0">
                            <div className="card-body p-4 text-center">
                                <div className="text-info mb-3">
                                    <i className="bi bi-shield-check fs-1"></i>
                                </div>
                                <h5 className="card-title fw-bold">3. Transparent Status</h5>
                                <p className="card-text text-muted">
                                    Track appointment progress from confirmation to completion or self-service cancellation.
                                </p>
                            </div>
                        </div>
                    </div>
                </div>
            </main>
        </div>
    );
}
