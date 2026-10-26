import React from 'react';

export default function Navbar({ currentUser, onNavigate, onLogout, currentPage }) {
    return (
        <nav class="navbar navbar-expand-lg navbar-dark bg-primary shadow-sm">
            <div class="container">
                <a class="navbar-brand fw-bold" href="#/" onClick={(e) => { e.preventDefault(); onNavigate('home'); }} id="nav-brand">
                    <i class="bi bi-pen-fill me-2"></i>E-Notary System
                </a>
                <button class="navbar-toggler" type="button" data-bs-toggle="collapse" data-bs-target="#navbarNav">
                    <span class="navbar-toggler-icon"></span>
                </button>
                <div class="collapse navbar-collapse" id="navbarNav">
                    <ul class="navbar-nav ms-auto align-items-center">
                        <li class="nav-item">
                            <a class={`nav-link ${currentPage === 'home' ? 'active' : ''}`} href="#/" onClick={(e) => { e.preventDefault(); onNavigate('home'); }} id="nav-home">
                                Home
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class={`nav-link ${currentPage === 'slots' ? 'active' : ''}`} href="#/slots" onClick={(e) => { e.preventDefault(); onNavigate('slots'); }} id="nav-slots">
                                Find Slots
                            </a>
                        </li>
                        <li class="nav-item">
                            <a class={`nav-link ${currentPage === 'track' ? 'active' : ''}`} href="#/track" onClick={(e) => { e.preventDefault(); onNavigate('track'); }} id="nav-track">
                                Track Status
                            </a>
                        </li>
                        {currentUser && currentUser.role === 'CLIENT' && (
                            <li class="nav-item">
                                <a class={`nav-link ${currentPage === 'clientDashboard' ? 'active' : ''}`} href="#/dashboard" onClick={(e) => { e.preventDefault(); onNavigate('clientDashboard'); }} id="nav-dashboard">
                                    My Bookings
                                </a>
                            </li>
                        )}
                        {currentUser && currentUser.role === 'NOTARY' && (
                            <li class="nav-item">
                                <a class={`nav-link ${currentPage === 'notaryDashboard' ? 'active' : ''}`} href="#/dashboard" onClick={(e) => { e.preventDefault(); onNavigate('notaryDashboard'); }} id="nav-notary-dashboard">
                                    Notary Portal
                                </a>
                            </li>
                        )}
                        <li class="nav-item">
                            <a class="nav-link" href="/actuator/health" target="_blank" rel="noreferrer" id="nav-health">
                                <span class="badge bg-success"><i class="bi bi-heart-pulse-fill me-1"></i>Health</span>
                            </a>
                        </li>

                        {currentUser ? (
                            <li class="nav-item ms-lg-3">
                                <span class="text-white small me-3" id="user-greeting">
                                    <i class="bi bi-person-circle me-1"></i>
                                    <strong>{currentUser.fullName}</strong>
                                    <span class="badge bg-light text-primary ms-1">{currentUser.role}</span>
                                </span>
                                <button class="btn btn-outline-light btn-sm px-3" onClick={onLogout} id="btn-logout">
                                    Logout
                                </button>
                            </li>
                        ) : (
                            <>
                                <li class="nav-item ms-lg-3">
                                    <button class="btn btn-outline-light btn-sm px-3" onClick={() => onNavigate('login')} id="btn-login">
                                        Login
                                    </button>
                                </li>
                                <li class="nav-item ms-2">
                                    <button class="btn btn-light btn-sm px-3 text-primary fw-bold" onClick={() => onNavigate('register')} id="btn-register">
                                        Register
                                    </button>
                                </li>
                            </>
                        )}
                    </ul>
                </div>
            </div>
        </nav>
    );
}
